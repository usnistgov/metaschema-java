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

package gov.nist.secauto.metaschema.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.secauto.metaschema.model.common.datatype.markup.AbstractMarkupString;
import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupString;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.codehaus.stax2.XMLStreamWriter2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlresolver.Resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import edu.umd.cs.findbugs.annotations.NonNull;

class CommonmarkSchemaTest {
  private static final String SCHEMA_CLASSPATH = "/markup-test.xsd";
  private static final Set<String> BLOCK_ELEMENTS = new HashSet<>();
  private static final Pattern INITIAL_ELEMENT_PATTERN
      = Pattern.compile("^\\s*<([^\\s/>]+)[^>]*>.*", Pattern.DOTALL);

  static {
    BLOCK_ELEMENTS.add("blockquote");
    BLOCK_ELEMENTS.add("h1");
    BLOCK_ELEMENTS.add("h2");
    BLOCK_ELEMENTS.add("h3");
    BLOCK_ELEMENTS.add("h4");
    BLOCK_ELEMENTS.add("h5");
    BLOCK_ELEMENTS.add("h6");
    BLOCK_ELEMENTS.add("ol");
    BLOCK_ELEMENTS.add("ul");
    BLOCK_ELEMENTS.add("p");
    BLOCK_ELEMENTS.add("pre");
    BLOCK_ELEMENTS.add("hr");
    BLOCK_ELEMENTS.add("table");
  }

  @Disabled
  @Test
  void test() {
    String vector = "<p><a href=\"foo  \r\n"
        + "bar\"></a></p><a href=\"foo  \r\n"
        + "bar\"> </a>";

    Matcher matcher = INITIAL_ELEMENT_PATTERN.matcher(vector);

    assertTrue(matcher.matches());
    assertTrue(BLOCK_ELEMENTS.contains(matcher.group(1)));
  }

  private static List<Entry> generateTestVectors() throws JsonParseException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    try (InputStream is = CommonmarkSchemaTest.class.getResourceAsStream("/commonmark-spec.json")) {
      try (JsonParser parser = mapper.getFactory().createParser(is)) {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
          throw new IllegalStateException();
        }

        List<Entry> entries = new LinkedList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
          Entry entry = mapper
              .readerFor(Entry.class)
              .readValue(parser);
          entries.add(entry);
        }
        return entries;
      }
    }
  }

  private static Schema loadDataTypeSchema() throws URISyntaxException, SAXException, IOException {
    URL url = MetaschemaLoader.class.getResource(SCHEMA_CLASSPATH);
    // System.out.println(url.toString());
    SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try (InputStream is = MetaschemaLoader.class.getResourceAsStream(SCHEMA_CLASSPATH)) {
      StreamSource source = new StreamSource(is, url.toURI().toString());

      schemafactory.setResourceResolver(new Resolver());
      return schemafactory.newSchema(source);
    }
  }

  public boolean isMultilineMarkdown(@NonNull String markdown) {
    return markdown.contains("\n");
  }

  public boolean isBlockElement(@NonNull String html) {
    Matcher matcher = INITIAL_ELEMENT_PATTERN.matcher(html);

    return matcher.matches() && BLOCK_ELEMENTS.contains(matcher.group(1));
  }

  @NonNull
  public String generateXmlInstance(@NonNull String html) {

    StringBuilder builder = new StringBuilder(206);
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"); // NOPMD

    String topLevelElementName;
    if (isBlockElement(html)) {
      topLevelElementName = "multiline";
    } else {
      topLevelElementName = "line";
    }

    builder
        .append(
            "<!DOCTYPE " + topLevelElementName + " [\r\n" // NOPMD
                + "<!ENTITY nbsp \"&#160;\">\r\n"
                + "]>\r\n")
        .append('<')
        .append(topLevelElementName)
        .append(" xmlns=\"http://csrc.nist.gov/ns/oscal/metaschema/1.0\"") // NOPMD
        .append(" xmlns:zoop=\"http://csrc.nist.gov/ns/oscal/metaschema-zoop/1.0\"")
        .append('>')
        .append(html)
        .append("</")
        .append(topLevelElementName)
        .append(">\r\n");

    return ObjectUtils.notNull(builder.toString());
  }

  @NonNull
  public String generateXmlInstance(@NonNull IMarkupString<?> content) throws XMLStreamException {

    WstxOutputFactory factory = new WstxOutputFactory();
    factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    StringWriter strWriter = new StringWriter();

    XMLStreamWriter2 xmlWriter = (XMLStreamWriter2) factory.createXMLStreamWriter(strWriter);
    xmlWriter.writeStartDocument();

    String topLevelElementName;
    if (content instanceof MarkupMultiline) {
      topLevelElementName = "multiline";
    } else {
      topLevelElementName = "line";
    }

    xmlWriter.writeDTD("<!DOCTYPE " + topLevelElementName + " [\r\n" // NOPMD
        + "<!ENTITY nbsp \"&#160;\">\r\n"
        + "]>\r\n");

    xmlWriter.writeStartElement("http://csrc.nist.gov/ns/oscal/metaschema/1.0", topLevelElementName);
    xmlWriter.writeNamespace("", "http://csrc.nist.gov/ns/oscal/metaschema/1.0");
    xmlWriter.writeNamespace("zoop", "http://csrc.nist.gov/ns/oscal/metaschema-zoop/1.0");

    IMarkupAdapter.writeHtml(content, "http://csrc.nist.gov/ns/oscal/metaschema/1.0", xmlWriter);

    xmlWriter.writeEndElement();
    xmlWriter.writeEndDocument();
    xmlWriter.flush();

    return ObjectUtils.notNull(strWriter.toString());
  }

  @Execution(ExecutionMode.SAME_THREAD)
  @DisplayName("Markup Tests")
  @TestFactory
  Stream<DynamicNode> generateConversionTests() // NOPMD
      throws IOException, URISyntaxException, SAXException {
    Schema schema = loadDataTypeSchema();

    List<Entry> entries = generateTestVectors();

    return entries.stream()
        .filter(Entry::isEnabled)
        .collect(Collectors.groupingBy(
            Entry::getSection,
            Collectors.toList()))
        .entrySet().stream()
        .map(entry -> {
          String sectionName = entry.getKey();
          List<Entry> testVectors = entry.getValue();
          return DynamicContainer.dynamicContainer(
              sectionName,
              testVectors.stream()
                  .map(testVector -> {
                    String testName = new StringBuilder()
                        .append(testVector.getSection())
                        .append(" (")
                        .append(testVector.getExampleNumber())
                        .append(")")
                        .toString();
                    return DynamicContainer.dynamicContainer(
                        testName,
                        Stream.of(
                            DynamicTest.dynamicTest(
                                testVector.getMarkdown(),
                                () -> { 
                                  // do nothing
                                }),
                            DynamicTest.dynamicTest(
                                "Markdown To XML/HTML Test Vector",
                                () -> {
                                  String markdown = testVector.getMarkdown();
                                  AbstractMarkupString<?> content;
                                  if (isMultilineMarkdown(markdown)) {
                                    content = MarkupDataTypeProvider.MARKUP_MULTILINE.parse(markdown);
                                  } else {
                                    content = MarkupDataTypeProvider.MARKUP_LINE.parse(markdown);
                                  }

                                  String xmlTestVector = generateXmlInstance(content);

                                  // System.out.println(xmlTestVector);
                                  StringReader reader = new StringReader(xmlTestVector);

                                  StreamSource source = new StreamSource(reader);
                                  Validator validator = schema.newValidator();
                                  validator.setErrorHandler(new Handler());
                                  try {
                                    validator.validate(source);
                                  } catch (SAXParseException ex) {
                                    fail(String.format("Invalid vector '%s'. %s",
                                        content,
                                        ex.getLocalizedMessage()));
                                  }
                                }),
                            DynamicTest.dynamicTest(
                                "Validate XML Test Vector",
                                () -> {
                                  Document data = testVector.getHtml();
                                  String html = data.body().html();
                                  String xmlTestVector = generateXmlInstance(html);

                                  // System.out.println(xmlTestVector);
                                  StringReader reader = new StringReader(xmlTestVector);

                                  StreamSource source = new StreamSource(reader);
                                  Validator validator = schema.newValidator();
                                  validator.setErrorHandler(new Handler());
                                  try {
                                    validator.validate(source);
                                  } catch (SAXParseException ex) {
                                    fail(String.format("Invalid vector '%s'. %s",
                                        html,
                                        ex.getLocalizedMessage()));
                                  }
                                })
                            ));
                  }));
        });
  }

  @JsonIgnoreProperties({ "comment" })
  private static class Entry {
    private final String markdown;
    private final Document html;
    private final int exampleNumber;
    private final String section;
    private final int startLine;
    private final int endLine;
    private final boolean enabled;

    @JsonCreator
    public Entry(
        @JsonProperty("markdown") String markdown,
        @JsonProperty("html") String html,
        @JsonProperty("example") int exampleNumber,
        @JsonProperty("section") String section,
        @JsonProperty("start_line") int startLine,
        @JsonProperty("end_line") int endLine,
        @JsonProperty("enabled") Boolean enabled) {
      this.markdown = markdown;
      Document document = Jsoup.parseBodyFragment(html);
      document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
      this.html = document;
      this.exampleNumber = exampleNumber;
      this.section = section;
      this.startLine = startLine;
      this.endLine = endLine;
      this.enabled = enabled == null ? true : enabled.booleanValue();
    }

    @NonNull
    public String getMarkdown() {
      return markdown;
    }

    @NonNull
    public Document getHtml() {
      return html;
    }

    public int getExampleNumber() {
      return exampleNumber;
    }

    @NonNull
    public String getSection() {
      return section;
    }

    public int getStartLine() {
      return startLine;
    }

    public int getEndLine() {
      return endLine;
    }

    public boolean isEnabled() {
      return enabled;
    }

  }

  static class Handler
      extends DefaultHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
      throw e;
    }

  }
}
