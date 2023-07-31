/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.databind.io;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.json.JsonFactoryFactory;
import gov.nist.secauto.metaschema.databind.io.json.JsonUtil;
import gov.nist.secauto.metaschema.databind.io.yaml.YamlFactoryFactory;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A default implementation of an {@link IBoundLoader}.
 */
public class DefaultBoundLoader implements IBoundLoader {
  public static final int LOOK_AHEAD_BYTES = 32_768;
  // @NonNull
  // private static final JsonFactory JSON_FACTORY = new JsonFactory();
  // @NonNull
  // private static final XmlFactory XML_FACTORY = new XmlFactory();
  // @NonNull
  // private static final YAMLFactory YAML_FACTORY = new YAMLFactory();

  private JsonFactory[] detectorFactory;

  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final IMutableConfiguration<DeserializationFeature<?>> configuration;

  /**
   * An {@link EntityResolver} is not provided by default.
   */
  @Nullable
  private EntityResolver entityResolver;

  /**
   * Construct a new OSCAL loader instance, using the provided {@link IBindingContext}.
   *
   * @param bindingContext
   *          the Metaschema binding context to use to load Java types
   */
  public DefaultBoundLoader(@NonNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
    this.configuration = new DefaultConfiguration<>();
  }

  @Override
  public IBoundLoader enableFeature(DeserializationFeature<?> feature) {
    return set(feature, true);
  }

  @Override
  public IBoundLoader disableFeature(DeserializationFeature<?> feature) {
    return set(feature, false);
  }

  @Override
  public boolean isFeatureEnabled(DeserializationFeature<?> feature) {
    return getConfiguration().isFeatureEnabled(feature);
  }

  @Override
  public Map<DeserializationFeature<?>, Object> getFeatureValues() {
    return getConfiguration().getFeatureValues();
  }

  @Override
  public IBoundLoader applyConfiguration(@NonNull IConfiguration<DeserializationFeature<?>> other) {
    getConfiguration().applyConfiguration(other);
    resetDetector();
    return this;
  }

  private void resetDetector() {
    // reset the detector
    detectorFactory = null;
  }

  @NonNull
  protected IMutableConfiguration<DeserializationFeature<?>> getConfiguration() {
    return configuration;
  }

  @Override
  public IBoundLoader set(DeserializationFeature<?> feature, Object value) {
    getConfiguration().set(feature, value);
    resetDetector();
    return this;
  }

  @Override
  public <V> V get(DeserializationFeature<?> feature) {
    return getConfiguration().get(feature);
  }

  @Override
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  @Override
  public void setEntityResolver(@NonNull EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  @Override
  public Format detectFormat(InputSource source) throws IOException {
    Format retval;
    if (source.getCharacterStream() != null) {
      throw new UnsupportedOperationException("Character streams are not supported");
    } else if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      retval = detectFormatInternal(ObjectUtils.notNull(source.getByteStream()));
    } else {
      // fall back to a URL-based connection
      URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        retval = detectFormatInternal(ObjectUtils.notNull(is));
      }
    }
    return retval;
  }

  @NonNull
  protected Format detectFormatInternal(@NonNull InputStream is) throws IOException {
    return detectFormatInternal(is, LOOK_AHEAD_BYTES - 1);
  }

  @NonNull
  protected Format detectFormatInternal(@NonNull InputStream is, int lookAheadBytes) throws IOException {
    DataFormatMatcher matcher = matchFormat(is, lookAheadBytes);
    return formatFromMatcher(matcher);
  }

  private JsonFactory[] getDetectorFactory() {
    if (detectorFactory == null) {
      detectorFactory = new JsonFactory[3];
      detectorFactory[0] = YamlFactoryFactory.newParserFactoryInstance(getConfiguration());
      detectorFactory[1] = JsonFactoryFactory.instance();
      detectorFactory[2] = new XmlFactory();
    }
    return detectorFactory;
  }

  @NonNull
  protected DataFormatMatcher matchFormat(@NonNull InputStream is, int lookAheadBytes) throws IOException {
    DataFormatDetector det = new DataFormatDetector(getDetectorFactory());
    det = det.withMinimalMatch(MatchStrength.INCONCLUSIVE).withOptimalMatch(MatchStrength.SOLID_MATCH)
        .withMaxInputLookahead(lookAheadBytes);

    DataFormatMatcher matcher = det.findFormat(is);
    switch (matcher.getMatchStrength()) {
    case FULL_MATCH:
    case SOLID_MATCH:
    case WEAK_MATCH:
    case INCONCLUSIVE:
      return matcher;
    case NO_MATCH:
    default:
      throw new UnsupportedOperationException("Unable to identify format");
    }
  }

  @NonNull
  protected Format formatFromMatcher(@NonNull DataFormatMatcher matcher) {
    Format retval;
    String formatName = matcher.getMatchedFormatName();
    if (YAMLFactory.FORMAT_NAME_YAML.equals(formatName)) {
      retval = Format.YAML;
    } else if (JsonFactory.FORMAT_NAME_JSON.equals(formatName)) {
      retval = Format.JSON;
    } else if (XmlFactory.FORMAT_NAME_XML.equals(formatName)) {
      retval = Format.XML;
    } else {
      throw new UnsupportedOperationException(String.format("The detected format '%s' is not supported", formatName));
    }
    return retval;
  }

  @NonNull
  private static BufferedInputStream toBufferedInputStream(@NonNull InputStream is) {
    BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES); // NOPMD - stream not owned
    bis.mark(LOOK_AHEAD_BYTES);
    return bis;
  }

  @Override
  public <CLASS> CLASS load(@NonNull URL url) throws IOException, URISyntaxException {
    // TODO: avoid node item
    return INodeItem.toValue(loadAsNodeItem(url));
  }

  @Override
  @NonNull
  public <CLASS> CLASS load(@NonNull Path path) throws IOException {
    // TODO: avoid node item
    return INodeItem.toValue(loadAsNodeItem(path));
  }

  @Override
  @NonNull
  public <CLASS> CLASS load(@NonNull File file) throws IOException {
    // TODO: avoid node item
    return INodeItem.toValue(loadAsNodeItem(file));
  }

  @Override
  @NonNull
  public <CLASS> CLASS load(@NonNull InputStream is, @NonNull URI documentUri) throws IOException {
    // TODO: avoid node item
    return INodeItem.toValue(loadAsNodeItem(is, documentUri));
  }

  @Override
  @NonNull
  public <CLASS> CLASS load(@NonNull InputSource source) throws IOException {
    return INodeItem.toValue(loadAsNodeItem(source));
  }

  @Override
  public <CLASS> CLASS load(Class<CLASS> clazz, InputSource source) throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    CLASS retval;
    if (source.getCharacterStream() != null) {
      throw new UnsupportedOperationException("Character streams are not supported");
    } else if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      try (BufferedInputStream bis = new BufferedInputStream(source.getByteStream(), LOOK_AHEAD_BYTES)) {
        retval = loadInternal(clazz, bis, uri);
      }
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        try (BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES)) {
          retval = loadInternal(clazz, bis, uri);
        }
      }
    }
    return retval;
  }

  @NonNull
  protected <CLASS> CLASS loadInternal(@NonNull Class<CLASS> clazz, @NonNull BufferedInputStream bis,
      @NonNull URI documentUri) throws IOException {
    // we cannot close this stream, since it will cause the underlying stream to be closed
    bis.mark(LOOK_AHEAD_BYTES);

    Format format = detectFormatInternal(bis);
    bis.reset();

    IDeserializer<CLASS> deserializer = getDeserializer(clazz, format, getConfiguration());
    return deserializer.deserialize(bis, documentUri);
  }

  @Override
  public IDocumentNodeItem loadAsNodeItem(InputSource source) throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    IDocumentNodeItem retval;
    if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      try (BufferedInputStream bis = toBufferedInputStream(ObjectUtils.notNull(source.getByteStream()))) {
        retval = loadAsNodeItemInternal(bis, uri);
      }
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        try (BufferedInputStream bis = toBufferedInputStream(ObjectUtils.notNull(is))) {
          retval = loadAsNodeItemInternal(bis, uri);
        }
      }
    }
    return retval;
  }

  @Override
  public IDocumentNodeItem loadAsNodeItem(@NonNull Format format, @NonNull InputSource source)
      throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    IDocumentNodeItem retval;
    if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      try (@SuppressWarnings("resource") BufferedInputStream bis = toBufferedInputStream(
          ObjectUtils.requireNonNull(source.getByteStream()))) {
        Class<?> clazz = detectModel(bis, format); // NOPMD - must be called before reset
        retval = deserializeToNodeItem(clazz, format, bis, uri);
      }
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        try (BufferedInputStream bis = toBufferedInputStream(ObjectUtils.notNull(is))) {
          Class<?> clazz = detectModel(bis, format); // NOPMD - must be called before reset
          retval = deserializeToNodeItem(clazz, format, bis, uri);
        }
      }
    }
    return retval;
  }

  @NonNull
  protected IDocumentNodeItem loadAsNodeItemInternal(@NonNull BufferedInputStream bis, @NonNull URI documentUri)
      throws IOException {
    DataFormatMatcher matcher = matchFormat(bis, LOOK_AHEAD_BYTES - 1);
    Format format = formatFromMatcher(matcher);
    Class<?> clazz = detectModel(matcher, format); // NOPMD - must be called before reset
    return deserializeToNodeItem(clazz, format, bis, documentUri);
  }

  @NonNull
  protected IDocumentNodeItem deserializeToNodeItem(@NonNull Class<?> clazz, @NonNull Format format,
      @NonNull BufferedInputStream bis, @NonNull URI documentUri) throws IOException {
    try {
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }

    IDeserializer<?> deserializer = getDeserializer(clazz, format, getConfiguration());
    return (IDocumentNodeItem) deserializer.deserializeToNodeItem(bis, documentUri);
  }

  @NonNull
  protected Class<?> detectModel(@NonNull BufferedInputStream bis, @NonNull Format format)
      throws IOException {
    Class<?> clazz;
    switch (format) {
    case JSON:
      clazz = detectModelJsonClass(ObjectUtils.notNull(JsonFactoryFactory.instance().createParser(bis)));
      if (clazz == null) {
        throw new IllegalStateException(
            String.format("Detected format '%s', but unable to detect the bound data type", format.name()));
      }
      break;
    case YAML:
      YAMLFactory factory = YamlFactoryFactory.newParserFactoryInstance(getConfiguration());
      clazz = detectModelJsonClass(ObjectUtils.notNull(factory.createParser(bis)));
      if (clazz == null) {
        throw new IllegalStateException(
            String.format("Detected format '%s', but unable to detect the bound data type", format.name()));
      }
      break;
    case XML:
      clazz = detectModelXmlClass(ObjectUtils.notNull(bis));
      break;
    default:
      throw new UnsupportedOperationException(
          String.format("The format '%s' is not supported", format));
    }

    try {
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }
    return clazz;
  }

  @NonNull
  protected Class<?> detectModel(@NonNull DataFormatMatcher matcher, @NonNull Format format)
      throws IOException {
    Class<?> clazz;
    switch (format) {
    case JSON:
    case YAML:
      clazz = detectModelJsonClass(ObjectUtils.notNull(matcher.createParserWithMatch()));
      if (clazz == null) {
        throw new IllegalStateException(
            String.format("Detected format '%s', but unable to detect the bound data type", format.name()));
      }
      break;
    case XML:
      clazz = detectModelXmlClass(ObjectUtils.notNull(matcher.getDataStream()));
      break;
    default:
      throw new UnsupportedOperationException(
          String.format("The detected format '%s' is not supported", matcher.getMatchedFormatName()));
    }
    return clazz;
  }

  @NonNull
  protected Class<?> detectModelXmlClass(@NonNull InputStream is) throws IOException {

    QName startElementQName;
    try {
      XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) XMLInputFactory.newInstance();
      assert xmlInputFactory instanceof WstxInputFactory;
      xmlInputFactory.configureForXmlConformance();
      xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

      Reader reader = new InputStreamReader(is, Charset.forName("UTF8"));
      XMLEventReader2 eventReader = (XMLEventReader2) xmlInputFactory.createXMLEventReader(reader);
      if (eventReader.peek().isStartDocument()) {
        while (eventReader.hasNext() && !eventReader.peek().isStartElement()) {
          eventReader.nextEvent();
        }
      }

      if (!eventReader.peek().isStartElement()) {
        throw new UnsupportedOperationException("Unable to detect a start element");
      }

      StartElement start = eventReader.nextEvent().asStartElement();
      startElementQName = ObjectUtils.notNull(start.getName());
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }

    Class<?> clazz = getBoundClassForXmlQName(startElementQName);

    if (clazz == null) {
      throw new UnsupportedOperationException("Unrecognized element name: " + startElementQName.toString());
    }
    return clazz;
  }

  protected Class<?> getBoundClassForXmlQName(@NonNull QName rootQName) {
    return getBindingContext().getBoundClassForXmlQName(rootQName);
  }

  @Nullable
  protected Class<?> detectModelJsonClass(@NonNull JsonParser parser) throws IOException {
    Class<?> retval = null;
    JsonUtil.advanceAndAssert(parser, JsonToken.START_OBJECT);
    outer: while (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
      String name = ObjectUtils.notNull(parser.getCurrentName());
      if ("$schema".equals(name)) {
        // do nothing
        parser.nextToken();
        // JsonUtil.skipNextValue(parser);
      } else {
        retval = getBoundClassForJsonName(name);
        break outer;
      }
    }
    return retval;
  }

  protected Class<?> getBoundClassForJsonName(@NonNull String rootName) {
    return getBindingContext().getBoundClassForJsonName(rootName);
  }

  @NonNull
  protected <CLASS> IDeserializer<CLASS> getDeserializer(
      @NonNull Class<CLASS> clazz,
      @NonNull Format format,
      @NonNull IConfiguration<DeserializationFeature<?>> config) {
    IDeserializer<CLASS> retval = getBindingContext().newDeserializer(format, clazz);
    retval.applyConfiguration(config);
    return retval;
  }
}
