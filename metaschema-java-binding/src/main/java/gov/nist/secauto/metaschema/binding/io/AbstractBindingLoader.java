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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.model.common.metapath.DocumentLoader;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public abstract class AbstractBindingLoader implements DocumentLoader {
  public static final int LOOK_AHEAD_BYTES = 3072;
  private static final JsonFactory jsonFactory = new JsonFactory();
  private static final XmlFactory xmlFactory = new XmlFactory();
  private static final YAMLFactory yamlFactory = new YAMLFactory();

  public static DataFormatMatcher detectFormat(InputStream is) throws IOException {
    return detectFormat(is, LOOK_AHEAD_BYTES);
  }

  public static DataFormatMatcher detectFormat(InputStream is, int lookAheadBytes) throws IOException {

    DataFormatDetector det = new DataFormatDetector(new JsonFactory[] { yamlFactory, jsonFactory, xmlFactory });
    det = det.withMinimalMatch(MatchStrength.INCONCLUSIVE).withOptimalMatch(MatchStrength.SOLID_MATCH)
        .withMaxInputLookahead(lookAheadBytes);

    DataFormatMatcher matcher = det.findFormat(is);
    return matcher;
  }

  private final BindingContext bindingContext;
  private final MutableConfiguration configuration;

  /**
   * Construct a new OSCAL loader instance, using the provided {@link BindingContext}.
   * 
   * @param bindingContext
   *          the Metaschema binding context to use to load Java types
   */
  public AbstractBindingLoader(BindingContext bindingContext) {
    this.bindingContext = bindingContext;
    configuration = new MutableConfiguration().enableFeature(Feature.DESERIALIZE_ROOT);
  }

  public void enableFeature(Feature feature) {
    configuration.enableFeature(feature);
  }

  public void disableFeature(Feature feature) {
    configuration.disableFeature(feature);
  }

  public boolean isFeatureEnabled(Feature feature) {
    return configuration.isFeatureEnabled(feature, false);
  }

  protected Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Get the configured Metaschema binding context to use to load Java types.
   * 
   * @return the binding context
   */
  public BindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public INodeItem loadAsNodeItem(URL url) throws IOException {
    Object item = load(url);
    return IXdmFactory.INSTANCE.newRootAssemblyNodeItem(item, bindingContext);
  }

  @Override
  public INodeItem loadAsNodeItem(File file) throws FileNotFoundException, IOException {
    Object item = load(file);
    return IXdmFactory.INSTANCE.newRootAssemblyNodeItem(item, bindingContext);
  }

  @Override
  public INodeItem loadAsNodeItem(InputStream is) throws IOException {
    Object item = load(is);
    return IXdmFactory.INSTANCE.newRootAssemblyNodeItem(item, bindingContext);
  }

  public <CLASS> CLASS load(URL url) throws IOException {
    return load(url.openStream());
  }

  public <CLASS> CLASS load(File file) throws FileNotFoundException, IOException {
    if (!file.exists()) {
      throw new FileNotFoundException(file.getAbsolutePath());
    }
    return load(new FileInputStream(file));
  }

  public <CLASS> CLASS load(InputStream is) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);
    bis.mark(LOOK_AHEAD_BYTES);

    DataFormatMatcher matcher = matchFormat(bis);
    Format format = formatFromMatcher(matcher);

    Deserializer<?> deserializer;
    switch (format) {
    case JSON:
      deserializer = detectModelJson(matcher, Format.JSON);
      break;
    case XML:
      deserializer = detectModelXml(matcher);
      break;
    case YAML:
      // uses a JSON-based parser
      deserializer = detectModelJson(matcher, Format.YAML);
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

    try {
      @SuppressWarnings("unchecked")
      CLASS retval = (CLASS) deserializer.deserialize(bis);
      return retval;
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Load the specified data file as the specified Java class.
   * 
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param file
   *          the file to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   * @throws FileNotFoundException
   *           if the specified file does not exist
   */
  public <CLASS> CLASS load(Class<CLASS> clazz, File file) throws FileNotFoundException, IOException {
    if (!file.exists()) {
      throw new FileNotFoundException(file.getAbsolutePath());
    }
    return load(clazz, new FileInputStream(file));
  }

  public <CLASS> CLASS load(Class<CLASS> clazz, URL url) throws IOException {
    return load(clazz, url.openStream());
  }

  public <CLASS> CLASS load(Class<CLASS> clazz, InputStream is) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is, LOOK_AHEAD_BYTES);
    bis.mark(LOOK_AHEAD_BYTES);

    DataFormatMatcher matcher = matchFormat(is);
    Format format = formatFromMatcher(matcher);

    Deserializer<CLASS> deserializer = getDeserializer(clazz, format, getConfiguration());

    try {
      bis.reset();
    } catch (IOException ex) {
      throw new IOException("Unable to reset input stream before parsing", ex);
    }
    try {
      return deserializer.deserialize(bis);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  protected Format formatFromMatcher(DataFormatMatcher matcher) {
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

  protected DataFormatMatcher matchFormat(InputStream is) throws IOException {
    DataFormatMatcher matcher = detectFormat(is);
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

  protected Deserializer<?> detectModelXml(DataFormatMatcher matcher) throws IOException {
    Class<?> clazz = detectModelXmlClass(matcher.getDataStream());
    return getDeserializer(clazz, Format.XML, getConfiguration());
  }

  private Deserializer<?> detectModelJson(DataFormatMatcher matcher, Format format) throws IOException {
    Class<?> clazz = detectModelJsonClass(matcher.createParserWithMatch());
    return getDeserializer(clazz, format, getConfiguration());
  }

  protected Class<?> detectModelXmlClass(InputStream is) throws IOException {

    QName startElementQName;
    try {
      XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
      xmlInputFactory.configureForXmlConformance();
      xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);

      try (Reader reader = new InputStreamReader(is, Charset.forName("UTF8"))) {
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
      }
    } catch (IOException | XMLStreamException ex) {
      throw new IOException(ex);
    }

    Class<?> clazz = getBoundClassForXmlQName(startElementQName);

    if (clazz == null) {
      throw new UnsupportedOperationException("Unrecognized element name: " + startElementQName.toString());
    }
    return clazz;
  }

  protected abstract Class<?> getBoundClassForXmlQName(QName startElementQName);

  protected abstract Class<?> getBoundClassForJsonName(String name);

  protected Class<?> detectModelJsonClass(JsonParser parser) throws IOException {
    Class<?> retval = null;
    JsonUtil.consumeAndAssert(parser, JsonToken.START_OBJECT);
    outer: while (JsonToken.FIELD_NAME.equals(parser.nextToken())) {
      String name = parser.getCurrentName();
      switch (name) {
      case "$schema":
        JsonUtil.skipNextValue(parser);
        break;
      default:
        retval = getBoundClassForJsonName(name);
        break outer;
      }
    }
    return retval;
  }

  protected <CLASS> Deserializer<CLASS> getDeserializer(Class<CLASS> clazz, Format format, Configuration config) {
    Deserializer<CLASS> retval = getBindingContext().newDeserializer(format, clazz, config);
    return retval;
  }
}
