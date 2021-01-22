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

package gov.nist.secauto.metaschema.binding.datatypes.adapter.types;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.datatypes.adapter.types.MarkupParser;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatypes.types.markup.flexmark.AstCollectingVisitor;
import gov.nist.secauto.metaschema.datatypes.util.XmlEventUtil;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

class MarkupParserTest {

  @Test
  void test() throws XMLStreamException {
    String html = "<node>\n";
    html = html + "  <p> some text </p>\n";
    html = html + "  <p><q>text</q></p>\n";
    html = html + "  <p>some <b>text</b> <insert param-id=\"param-id\"/>.</p>\n";
    html = html + "  <h1>Example</h1>\n";
    html = html + "  <p><a href=\"link\">text</a></p>\n";
    html = html + "  <ul>\n";
    html = html + "    <li>a <strong>list item</strong></li>\n";
    html = html + "    <li>another <i>list item</i></li>\n";
    html = html + "  </ul>\n";
    // html = html + " <table>\n";
    // html = html + " <tr><th>Heading 1</th></tr>\n";
    // html = html + " <tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr>\n";
    // html = html + " </table>\n";
    html = html + "  <p>Some <em>more</em> <strong>text</strong><img alt=\"alt\" src=\"src\"/></p>";
    html = html + "</node>\n";
    XMLInputFactory2 factory = (XMLInputFactory2) WstxInputFactory.newInstance();
    factory.configureForXmlConformance();
    factory.setProperty(XMLInputFactory2.IS_COALESCING, true);
    XMLEventReader2 reader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(html));
    System.out.println("Start: " + XmlEventUtil.toString(reader.nextEvent()));
    System.out.println("Start: " + XmlEventUtil.toString(reader.nextEvent()));
    MarkupParser parser = new MarkupParser();
    MarkupMultiline markupString = parser.parseMarkupMultiline(reader);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(markupString.getDocument());
    System.out.println(html);
    System.out.println(visitor.getAst());
    System.out.println(markupString.toMarkdown());
  }

}
