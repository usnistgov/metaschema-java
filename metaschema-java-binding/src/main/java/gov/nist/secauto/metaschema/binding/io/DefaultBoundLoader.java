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

package gov.nist.secauto.metaschema.binding.io;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.model.common.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

/**
 * A default implementation of an {@link IBoundLoader}.
 */
public class DefaultBoundLoader implements IBoundLoader {
  public static final int LOOK_AHEAD_BYTES = 32_768;
  @NotNull
  private static final JsonFactory JSON_FACTORY = new JsonFactory();
  @NotNull
  private static final XmlFactory XML_FACTORY = new XmlFactory();
  @NotNull
  private static final YAMLFactory YAML_FACTORY = new YAMLFactory();

  @NotNull
  private final IBindingContext bindingContext;
  @NotNull
  private final IMutableConfiguration<DeserializationFeature> configuration;

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
  @SuppressWarnings("null")
  public DefaultBoundLoader(@NotNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
    this.configuration = new DefaultConfiguration<>(DeserializationFeature.class);
  }

  @Override
  public IMutableConfiguration<DeserializationFeature> enableFeature(DeserializationFeature feature) {
    return configuration.enableFeature(feature);
  }

  @Override
  public IMutableConfiguration<DeserializationFeature> disableFeature(DeserializationFeature feature) {
    return configuration.disableFeature(feature);
  }

  @Override
  public boolean isFeatureEnabled(DeserializationFeature feature) {
    return configuration.isFeatureEnabled(feature);
  }

  @Override
  public Set<@NotNull DeserializationFeature> getFeatureSet() {
    return configuration.getFeatureSet();
  }

  @Override
  public IMutableConfiguration<DeserializationFeature>
      applyConfiguration(@NotNull IConfiguration<DeserializationFeature> other) {
    return configuration.applyConfiguration(other);
  }

  @NotNull
  protected IMutableConfiguration<DeserializationFeature> getConfiguration() {
    return configuration;
  }

  /**
   * Get the configured Metaschema binding context to use to load Java types.
   * 
   * @return the binding context
   */
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  @Override
  public @Nullable EntityResolver setEntityResolver(@NotNull EntityResolver resolver) {
    EntityResolver retval = this.entityResolver;
    this.entityResolver = resolver;
    return retval;
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

  @NotNull
  protected Format detectFormatInternal(@NotNull InputStream is) throws IOException {
    return detectFormatInternal(is, LOOK_AHEAD_BYTES - 1);
  }

  @NotNull
  protected Format detectFormatInternal(@NotNull InputStream is, int lookAheadBytes) throws IOException {
    DataFormatMatcher matcher = matchFormat(is, lookAheadBytes);
    return formatFromMatcher(matcher);
  }

  @NotNull
  protected DataFormatMatcher matchFormat(@NotNull InputStream is, int lookAheadBytes) throws IOException {
    DataFormatDetector det = new DataFormatDetector(new JsonFactory[] { YAML_FACTORY, JSON_FACTORY, XML_FACTORY });
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

  @NotNull
  protected Format formatFromMatcher(@NotNull DataFormatMatcher matcher) {
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

  @Override
  public IDocumentNodeItem loadAsNodeItem(InputSource source) throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    IDocumentNodeItem retval;
    if (source.getCharacterStream() != null) {
      throw new UnsupportedOperationException("Character streams are not supported");
    } else if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      BufferedInputStream bis = new BufferedInputStream(source.getByteStream(), LOOK_AHEAD_BYTES);
      bis.mark(LOOK_AHEAD_BYTES);
      retval = loadAsNodeItemInternal(bis, uri);
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);
        bis.mark(LOOK_AHEAD_BYTES);
        retval = loadAsNodeItemInternal(bis, uri);
      }
    }
    return retval;
  }

  @NotNull
  protected IDocumentNodeItem loadAsNodeItemInternal(@NotNull BufferedInputStream bis, @NotNull URI documentUri)
      throws IOException {
    DataFormatMatcher matcher = matchFormat(bis, LOOK_AHEAD_BYTES - 1);
    Format format = formatFromMatcher(matcher);

    Class<?> clazz = detectModel(matcher, format);

    IDeserializer<?> deserializer = getDeserializer(clazz, format, getConfiguration());

    try {
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }

    return  (IDocumentNodeItem) deserializer.deserializeToNodeItem(bis, documentUri);
  }

  @NotNull
  protected Class<?> detectModel(@NotNull DataFormatMatcher matcher, @NotNull Format format)
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

  @NotNull
  protected Class<?> detectModelXmlClass(@NotNull InputStream is) throws IOException {

    QName startElementQName;
    try {
      XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
      xmlInputFactory.configureForXmlConformance();
      xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);

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

  protected Class<?> getBoundClassForXmlQName(@NotNull QName rootQName) {
    return getBindingContext().getBoundClassForXmlQName(rootQName);
  }

  @Nullable
  protected Class<?> detectModelJsonClass(@NotNull JsonParser parser) throws IOException {
    Class<?> retval = null;
    JsonUtil.advanceAndAssert(parser, JsonToken.START_OBJECT);
    outer: while (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
      String name = parser.getCurrentName();
      switch (name) {
      case "$schema":
        // do nothing
        parser.nextToken();
        // JsonUtil.skipNextValue(parser);
        break;
      default:
        retval = getBoundClassForJsonName(name);
        break outer;
      }
    }
    return retval;
  }

  protected Class<?> getBoundClassForJsonName(@NotNull String rootName) {
    return getBindingContext().getBoundClassForJsonName(rootName);
  }

  @Override
  public <CLASS> CLASS load(Class<CLASS> clazz, InputSource source) throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    CLASS retval;
    if (source.getCharacterStream() != null) {
      throw new UnsupportedOperationException("Character streams are not supported");
    } else if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      BufferedInputStream bis = new BufferedInputStream(source.getByteStream(), LOOK_AHEAD_BYTES);
      retval = loadInternal(clazz, bis, uri);
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);
        retval = loadInternal(clazz, bis, uri);
      }
    }
    return retval;
  }

  @NotNull
  protected <CLASS> CLASS loadInternal(@NotNull Class<CLASS> clazz, @NotNull BufferedInputStream bis,
      @NotNull URI documentUri) throws IOException {
    // we cannot close this stream, since it will cause the underlying stream to be closed
    bis.mark(LOOK_AHEAD_BYTES);

    Format format = detectFormatInternal(bis);
    bis.reset();

    IDeserializer<CLASS> deserializer = getDeserializer(clazz, format, getConfiguration());
    INodeItem nodeItem = deserializer.deserializeToNodeItem(bis, documentUri);
    return nodeItem.toBoundObject();
  }

  @NotNull
  protected <CLASS> IDeserializer<CLASS> getDeserializer(@NotNull Class<CLASS> clazz, @NotNull Format format,
      @NotNull IConfiguration<DeserializationFeature> config) {
    IDeserializer<CLASS> retval = getBindingContext().newDeserializer(format, clazz);
    retval.applyConfiguration(config);
    return retval;
  }
}
