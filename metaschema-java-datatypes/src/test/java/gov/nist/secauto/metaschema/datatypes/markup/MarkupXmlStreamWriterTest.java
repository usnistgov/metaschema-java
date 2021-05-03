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

package gov.nist.secauto.metaschema.datatypes.markup;

import static org.junit.jupiter.api.Assertions.*;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.datatypes.markup.flexmark.AstCollectingVisitor;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.evt.MergedNsContext;
import org.codehaus.stax2.ri.evt.NamespaceEventImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

class MarkupXmlStreamWriterTest {

  @Test
  void testHTML() throws XMLStreamException {
    String html = "<h1>Example</h1>\n"
        + "<p><a href=\"link\">text</a><q>quote1</q></p>\n"
        + "<table>\n"
        + "<thead>\n"
        + "<tr><th>Heading 1</th></tr>\n"
        + "</thead>\n"
        + "<tbody>\n"
        + "<tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr>\n"
        + "<tr><td><q>data2</q> <first a=\"1\"> <insert param-id=\"insert\" /></td></tr>\n"
        + "</tbody>\n"
        + "</table>\n"
        + "<p>Some <q><em>more</em></q> <strong>text</strong> <img src=\"src\" alt=\"alt\" /></p>\n";
    String namespace = "http://www.w3.org/1999/xhtml";
    String prefix = "";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());

    MarkupXmlStreamWriter writer = new MarkupXmlStreamWriter(namespace, ms instanceof MarkupMultiline);

    XMLOutputFactory2 factory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
    factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
    XMLStreamWriter2 xmlStreamWriter = (XMLStreamWriter2) factory.createXMLStreamWriter(System.out);
    NamespaceContext nsContext = MergedNsContext.construct(xmlStreamWriter.getNamespaceContext(),
        List.of(NamespaceEventImpl.constructNamespace(null, prefix != null ? prefix : "", namespace)));
    xmlStreamWriter.setNamespaceContext(nsContext);
    writer.visitChildren(ms.getDocument(), xmlStreamWriter);
    xmlStreamWriter.close();
  }

}
