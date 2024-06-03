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

package gov.nist.secauto.metaschema.core.datatype.markup.flexmark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

class MarkupParserTest {
  private static final Logger LOGGER = LogManager.getLogger(MarkupParserTest.class);

  @Test
  void test() throws XMLStreamException {
    XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    factory.configureForXmlConformance();
    factory.setProperty(XMLInputFactory.IS_COALESCING, true);

    String html = new StringBuilder()
        .append("<node>\n")
        .append("  <p> some text </p>\n")
        .append("  <p><q>text</q></p>\n")
        .append("  <p>some <b>text</b> <insert param-id=\"param-id\"/>.</p>\n")
        .append("  <h1>Example</h1>\n")
        .append("  <p><a href=\"link\">text</a></p>\n")
        .append("  <ul>\n")
        .append("    <li>a <strong>list item</strong></li>\n")
        .append("    <li>another <i>list item</i></li>\n")
        .append("  </ul>\n")
        .append(" <table>\n")
        .append(" <tr><th>Heading 1</th></tr>\n")
        .append(" <tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr>\n")
        .append(" </table>\n")
        .append("  <p>Some <em>more</em> <strong>text</strong><img alt=\"alt\" src=\"src\"/></p>\n")
        .append("</node>\n")
        .toString();

    XMLEventReader2 reader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(html));

    CharSequence startDocument = XmlEventUtil.toString(reader.nextEvent());
    LOGGER.atDebug().log("StartDocument: {}", startDocument);

    CharSequence startElement = XmlEventUtil.toString(reader.nextEvent());
    LOGGER.atDebug().log("StartElement: {}", startElement);

    assertDoesNotThrow(() -> {
      MarkupMultiline markupString = XmlMarkupParser.instance().parseMarkupMultiline(reader);
      AstCollectingVisitor.asString(markupString.getDocument());
      // System.out.println(html);
      // System.out.println(visitor.getAst());
      // System.out.println(markupString.toMarkdown());

    });
  }

  @Test
  void emptyParagraphTest() throws XMLStreamException {
    final String html = new StringBuilder()
        .append("<node>\n")
        .append("  <p/>\n")
        .append("</node>\n")
        .toString();

    XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    factory.configureForXmlConformance();
    factory.setProperty(XMLInputFactory.IS_COALESCING, true);
    XMLEventReader2 reader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(html));

    CharSequence startDocument = XmlEventUtil.toString(reader.nextEvent());
    LOGGER.atDebug().log("StartDocument: {}", startDocument);

    CharSequence startElement = XmlEventUtil.toString(reader.nextEvent());
    LOGGER.atDebug().log("StartElement: {}", startElement);

    assertDoesNotThrow(() -> {
      MarkupMultiline ms = XmlMarkupParser.instance().parseMarkupMultiline(reader);
      LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
      LOGGER.atDebug().log("HTML: {}", ms.toXHtml(""));
      LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());
    });
  }
}
