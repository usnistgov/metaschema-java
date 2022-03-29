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
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

/**
 * A default implementation of a {@link IBoundLoader}.
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
  private final IMutableConfiguration configuration;

  /**
   * Construct a new OSCAL loader instance, using the provided {@link IBindingContext}.
   * 
   * @param bindingContext
   *          the Metaschema binding context to use to load Java types
   */
  public DefaultBoundLoader(@NotNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
    this.configuration = new DefaultMutableConfiguration();
    this.configuration.enableFeature(Feature.DESERIALIZE_JSON_ROOT_PROPERTY);
  }

  @Override
  public IMutableConfiguration enableFeature(Feature feature) {
    return configuration.enableFeature(feature);
  }

  @Override
  public IMutableConfiguration disableFeature(Feature feature) {
    return configuration.disableFeature(feature);
  }

  @Override
  public boolean isFeatureEnabled(Feature feature) {
    return configuration.isFeatureEnabled(feature);
  }

  @Override
  public Map<@NotNull Feature, Boolean> getFeatureSettings() {
    return configuration.getFeatureSettings();
  }

  @NotNull
  protected IConfiguration getConfiguration() {
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
  public Format detectFormat(InputStream is) throws IOException {
    DataFormatMatcher matcher = matchFormat(is);
    return formatFromMatcher(matcher);
  }

  // TODO: consolidate this with the similar load class
  @Override
  public IDocumentNodeItem loadAsNodeItem(InputStream is, URI documentUri) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);
    bis.mark(LOOK_AHEAD_BYTES);

    DataFormatMatcher matcher = matchFormat(bis, LOOK_AHEAD_BYTES - 1);
    Format format = formatFromMatcher(matcher);

    IDeserializer<?> deserializer;
    switch (format) {
    case JSON:
      deserializer = detectModelJson(matcher.createParserWithMatch(), Format.JSON);
      break;
    case XML:
      deserializer = detectModelXml(matcher.getDataStream());
      break;
    case YAML:
      // uses a JSON-based parser
      deserializer = detectModelJson(matcher.createParserWithMatch(), Format.YAML);
      break;
    default:
      throw new UnsupportedOperationException(
          String.format("The detected format '%s' is not supported", matcher.getMatchedFormatName()));
    }

    try {
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }

    return loadAsNodeItem(deserializer, bis, documentUri);
  }

  @NotNull
  protected <CLASS> IDocumentNodeItem loadAsNodeItem(@NotNull IDeserializer<CLASS> deserializer,
      @NotNull InputStream is,
      @NotNull URI documentUri)
      throws IOException {
    return (IDocumentNodeItem) deserializer.deserializeToNodeItem(is, documentUri);
  }

  @Override
  public <CLASS> CLASS load(Class<CLASS> clazz, InputStream is, URI documentUri) throws IOException {
    // we cannot close this stream, since it will cause the underlying stream to be closed
    BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);

    DataFormatMatcher matcher;
    try {
      bis.mark(LOOK_AHEAD_BYTES);
      matcher = matchFormat(bis);
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }

    Format format = formatFromMatcher(matcher);
    IDeserializer<CLASS> deserializer = getDeserializer(clazz, format, getConfiguration());
    return loadAsObject(deserializer, bis, documentUri);
  }

  @NotNull
  protected <CLASS> CLASS loadAsObject(@NotNull IDeserializer<CLASS> deserializer, @NotNull InputStream is,
      @NotNull URI documentUri)
      throws IOException {
    INodeItem nodeItem = loadAsNodeItem(deserializer, is, documentUri);
    return nodeItem.toBoundObject();
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

  @NotNull
  protected DataFormatMatcher matchFormat(@NotNull InputStream is) throws IOException {
    return matchFormat(is, LOOK_AHEAD_BYTES);
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
  protected IDeserializer<?> detectModelXml(@NotNull InputStream is) throws IOException {
    Class<?> clazz = detectModelXmlClass(is);

    return getDeserializer(clazz, Format.XML, getConfiguration());
  }

  @NotNull
  protected IDeserializer<?> detectModelJson(@NotNull JsonParser parser, @NotNull Format format) throws IOException {
    Class<?> clazz = detectModelJsonClass(parser);
    return getDeserializer(clazz, format, getConfiguration());
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
      startElementQName = start.getName();
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

  protected Class<?> getBoundClassForJsonName(@NotNull String rootName) {
    return getBindingContext().getBoundClassForJsonName(rootName);
  }

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

  @NotNull
  protected <CLASS> IDeserializer<CLASS> getDeserializer(@NotNull Class<CLASS> clazz, @NotNull Format format,
      @NotNull IConfiguration config) {
    IDeserializer<CLASS> retval = getBindingContext().newDeserializer(format, clazz);
    for (Map.Entry<Feature, Boolean> entry : config.getFeatureSettings().entrySet()) {
      if (Boolean.TRUE.equals(entry.getValue())) {
        retval.enableFeature(entry.getKey());
      } else if (Boolean.FALSE.equals(entry.getValue())) {
        retval.disableFeature(entry.getKey());
      }
    }
    return retval;
  }
}
