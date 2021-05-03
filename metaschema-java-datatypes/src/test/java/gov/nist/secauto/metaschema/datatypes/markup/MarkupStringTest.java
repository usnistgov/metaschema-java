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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.html2md.converter.internal.HtmlConverterCoreNodeRenderer;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.datatypes.markup.flexmark.AstCollectingVisitor;
import gov.nist.secauto.metaschema.datatypes.markup.flexmark.insertanchor.InsertAnchorNode;
import gov.nist.secauto.metaschema.datatypes.util.IteratorUtil;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.evt.MergedNsContext;
import org.codehaus.stax2.ri.evt.NamespaceEventImpl;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

class MarkupStringTest {
  private static final String MARKUP_HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
  private static final String MARKUP_HTML_PREFIX = "";

  MarkupXmlStreamWriter newMarkupXmlStreamWriter(boolean handleBlockElements) {
    return new MarkupXmlStreamWriter(MARKUP_HTML_NAMESPACE, handleBlockElements);
  }

  XMLStreamWriter2 newXmlStreamWriter(StringWriter stringWriter) throws XMLStreamException {
    XMLOutputFactory2 factory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
    factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
    XMLStreamWriter2 xmlStreamWriter = (XMLStreamWriter2) factory.createXMLStreamWriter(stringWriter);
    NamespaceContext nsContext = MergedNsContext.construct(xmlStreamWriter.getNamespaceContext(),
        List.of(NamespaceEventImpl.constructNamespace(null, MARKUP_HTML_PREFIX, MARKUP_HTML_NAMESPACE)));
    xmlStreamWriter.setNamespaceContext(nsContext);
    return xmlStreamWriter;
  }

  @Test
  void markupLineFromMarkdownTest() {
    String markdown = "Some \\**more* **text** and a param: {{ insert: param, insert }}.";
    String html
        = "<p>Some *<em>more</em> <strong>text</strong> and a param: <insert type=\"param\" id-ref=\"insert\" />.</p>";

    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    Document document = ms.getDocument();

    assertNotNull(document);
    visitor.collect(document);
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

    // Document[0, 49]
    List<Node> documentChildren = IteratorUtil.toList(document.getChildren());
    {
      // Paragraph[0, 49]
      // ensure there is a single paragraph
      assertEquals(1, documentChildren.size());
      Node paragraph = documentChildren.get(0);
      assertTrue(paragraph instanceof Paragraph);

      List<Node> paragraphChildren = IteratorUtil.toList(paragraph.getChildren());
      // TextBase[0, 7] chars:[0, 7, "Some \*"]
      {
        TextBase textBase = (TextBase) paragraphChildren.get(0);
        List<Node> textBaseChildren = IteratorUtil.toList(textBase.getChildren());
        // Text[0, 5] chars:[0, 5, "Some "]
        {
          Text text = (Text) textBaseChildren.get(0);
          assertEquals("Some ", text.getChars().toString());
        }
        // EscapedCharacter[5, 7] textOpen:[5, 6, "\"] text:[6, 7, "*"]
        {
          EscapedCharacter text = (EscapedCharacter) textBaseChildren.get(1);
          assertEquals("*", text.getText().toString());
        }
      }
      // Emphasis[7, 13] textOpen:[7, 8, "*"] text:[8, 12, "more"] textClose:[12, 13, "*"]
      {
        Emphasis emphasis = (Emphasis) paragraphChildren.get(1);
        List<Node> emphasisChildren = IteratorUtil.toList(emphasis.getChildren());
        // Text[8, 12] chars:[8, 12, "more"]
        {
          Text text = (Text) emphasisChildren.get(0);
          assertEquals("more", text.getChars().toString());
        }
      }
      // Text[13, 14] chars:[13, 14, " "]
      {
        Text text = (Text) paragraphChildren.get(2);
        assertEquals(" ", text.getChars().toString());
      }
      // StrongEmphasis[14, 22] textOpen:[14, 16, "**"] text:[16, 20, "text"] textClose:[20, 22, "**"]
      {
        StrongEmphasis strongEmphasis = (StrongEmphasis) paragraphChildren.get(3);
        List<Node> strongEmphasisChildren = IteratorUtil.toList(strongEmphasis.getChildren());
        // Text[16, 20] chars:[16, 20, "text"]
        {
          Text text = (Text) strongEmphasisChildren.get(0);
          assertEquals("text", text.getChars().toString());
        }

      }
      // Text[22, 36] chars:[22, 36, " and … ram: "]
      {
        Text text = (Text) paragraphChildren.get(4);
        assertEquals(" and a param: ", text.getChars().toString());
      }
      // InsertAnchorNode[0, 0] name:[39, 45, "insert"]
      {
        @SuppressWarnings("unused")
        InsertAnchorNode insert = (InsertAnchorNode) paragraphChildren.get(5);
      }
      // Text[48, 49] chars:[48, 49, "."]
      {
        Text text = (Text) paragraphChildren.get(6);
        assertEquals(".", text.getChars().toString());
      }
    }

    assertEquals(markdown, ms.toMarkdown());
    assertEquals(html, ms.toHtml());
  }

  @Test
  void markupMultilineFromMarkdownTest() {
    String markdown = "# Example\n\nSome \"\\**more*\" **text**\n\nA param: {{ insert: param, insert }}.";
    String html = "<h1>Example</h1>\n"
        + "<p>Some <q>*<em>more</em></q> <strong>text</strong></p>\n"
        + "<p>A param: <insert type=\"param\" id-ref=\"insert\" />.</p>";
    MarkupMultiline ms = MarkupMultiline.fromMarkdown(markdown);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

    assertEquals(markdown, ms.toMarkdown());
    assertEquals(html, ms.toHtml());
  }

  /*
   * {@link HtmlConverterCoreNodeRenderer} has a bug on line 629 in the call to "wrapTextNodes", which
   * can add spaces to the HTML string
   */
  @Test
  void markupMultilineFromHtmlTest() {
    String html = "<h1>Example</h1>\n"
        + "<p><a href=\"link\">text</a><q>quote1</q></p>\n"
        + "<table>\n"
        + "<thead>\n"
        + "<tr><th>Heading 1</th></tr>\n"
        + "</thead>\n"
        + "<tbody>\n"
        + "<tr><td><q>data1</q> <insert type=\"param\" id-ref=\"insert\" /></td></tr>\n"
        + "<tr><td><q>data2</q> <insert type=\"param\" id-ref=\"insert\" /></td></tr>\n"
        + "</tbody>\n"
        + "</table>\n"
        + "<p>Some <q><em>more</em></q> <strong>text</strong> <img src=\"src\" alt=\"alt\" /></p>";
    String markdown = "# Example\n"
        + "\n"
        + "[text](link)\"quote1\"\n"
        + "\n"
        + "|              Heading 1              |\n"
        + "|-------------------------------------|\n"
        + "| \"data1\" {{ insert: param, insert }} |\n"
        + "| \"data2\" {{ insert: param, insert }} |\n"
        + "\n"
        + "Some \"*more*\" **text** ![alt](src)";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println("Source");
    System.out.println("======");
    System.out.println(html);
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

    assertEquals(markdown, ms.toMarkdown());
    assertEquals(html, ms.toHtml());
  }

  @Test
  void preMarkdown() {
    MarkupMultiline ms = MarkupMultiline.fromHtml("<pre>Example **some** *code*</pre>");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());
  }

  @Test
  void pCodeMarkdown() {
    MarkupMultiline ms = MarkupMultiline.fromHtml("<p>Example<code>**some** *code*</code></p>");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());
  }

  @Test
  void testEntityRoundTrip() throws XMLStreamException {
    String markdown = "hijacked was used (e.g., the &lt;CTRL&gt; + &lt;ALT&gt; + &lt;DEL&gt; keys).";
    String html = "<p>hijacked was used (e.g., the &lt;CTRL&gt; + &lt;ALT&gt; + &lt;DEL&gt; keys).</p>";

    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    Document document = ms.getDocument();

    assertNotNull(document);
    visitor.collect(document);
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

    assertEquals(markdown, ms.toMarkdown());
    assertEquals(html, ms.toHtml().trim());

    MarkupXmlStreamWriter writer = newMarkupXmlStreamWriter(true);
    StringWriter stringWriter = new StringWriter();
    XMLStreamWriter2 xmlStreamWriter = newXmlStreamWriter(stringWriter);

    writer.visitChildren(ms.getDocument(), xmlStreamWriter);
    xmlStreamWriter.close();

    assertEquals(html, stringWriter.toString());
  }

  @Test
  void testAposRoundTrip() throws XMLStreamException {
    String markdown = "a user’s identity";
    String html = "<p>a user’s identity</p>";

    // test from Markdown source
    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    Document document = ms.getDocument();

    assertNotNull(document);
    visitor.collect(document);
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

    assertEquals(markdown, ms.toMarkdown());
    assertEquals(html, ms.toHtml().trim());

    MarkupXmlStreamWriter writer = newMarkupXmlStreamWriter(true);
    StringWriter stringWriter = new StringWriter();
    XMLStreamWriter2 xmlStreamWriter = newXmlStreamWriter(stringWriter);

    writer.visitChildren(ms.getDocument(), xmlStreamWriter);
    xmlStreamWriter.close();

    assertEquals(html, stringWriter.toString());

    // test from HTML source
    ms = MarkupLine.fromHtml(html);
    document = ms.getDocument();

    assertNotNull(document);
    visitor.collect(document);
    System.out.println("Markup AST");
    System.out.println("==========");
    System.out.println(visitor.getAst());
    System.out.println("HTML Output");
    System.out.println("===========");
    System.out.println(ms.toHtml());
    System.out.println("Markdown Output");
    System.out.println("===============");
    System.out.println(ms.toMarkdown());

  }
}
