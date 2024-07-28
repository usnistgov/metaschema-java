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

package gov.nist.secauto.metaschema.core.datatype.markup;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.AstCollectingVisitor;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.evt.MergedNsContext;
import org.codehaus.stax2.ri.evt.NamespaceEventImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

class MarkupStringTest {
  private static final Logger LOGGER = LogManager.getLogger(MarkupStringTest.class);
  private static final String MARKUP_HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
  private static final String MARKUP_HTML_PREFIX = "";

  @NonNull
  XMLStreamWriter2 newXmlStreamWriter(@NonNull StringWriter stringWriter) throws XMLStreamException {
    XMLOutputFactory2 factory = (XMLOutputFactory2) XMLOutputFactory.newInstance();
    assert factory instanceof WstxOutputFactory;
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

    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    Document document = ms.getDocument();

    Assertions.assertNotNull(document);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(document));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    // Document[0, 49]
    List<Node> documentChildren = CollectionUtil.toList(document.getChildren());
    {
      // Paragraph[0, 49]
      // ensure there is a single paragraph
      Assertions.assertEquals(1, documentChildren.size());
      Node paragraph = documentChildren.get(0);
      Assertions.assertTrue(paragraph instanceof Paragraph);

      List<Node> paragraphChildren = CollectionUtil.toList(paragraph.getChildren());
      // TextBase[0, 7] chars:[0, 7, "Some \*"]
      {
        TextBase textBase = (TextBase) paragraphChildren.get(0);
        List<Node> textBaseChildren = CollectionUtil.toList(textBase.getChildren());
        // Text[0, 5] chars:[0, 5, "Some "]
        {
          Text text = (Text) textBaseChildren.get(0);
          Assertions.assertEquals("Some ", text.getChars().toString());
        }
        // EscapedCharacter[5, 7] textOpen:[5, 6, "\"] text:[6, 7, "*"]
        {
          EscapedCharacter text = (EscapedCharacter) textBaseChildren.get(1);
          Assertions.assertEquals("*", text.getText().toString());
        }
      }
      // Emphasis[7, 13] textOpen:[7, 8, "*"] text:[8, 12, "more"] textClose:[12, 13,
      // "*"]
      {
        Emphasis emphasis = (Emphasis) paragraphChildren.get(1);
        List<Node> emphasisChildren = CollectionUtil.toList(emphasis.getChildren());
        // Text[8, 12] chars:[8, 12, "more"]
        {
          Text text = (Text) emphasisChildren.get(0);
          Assertions.assertEquals("more", text.getChars().toString());
        }
      }
      // Text[13, 14] chars:[13, 14, " "]
      {
        Text text = (Text) paragraphChildren.get(2);
        Assertions.assertEquals(" ", text.getChars().toString());
      }
      // StrongEmphasis[14, 22] textOpen:[14, 16, "**"] text:[16, 20, "text"]
      // textClose:[20, 22, "**"]
      {
        StrongEmphasis strongEmphasis = (StrongEmphasis) paragraphChildren.get(3);
        List<Node> strongEmphasisChildren = CollectionUtil.toList(strongEmphasis.getChildren());
        // Text[16, 20] chars:[16, 20, "text"]
        {
          Text text = (Text) strongEmphasisChildren.get(0);
          Assertions.assertEquals("text", text.getChars().toString());
        }

      }
      // Text[22, 36] chars:[22, 36, " and … ram: "]
      {
        Text text = (Text) paragraphChildren.get(4);
        Assertions.assertEquals(" and a param: ", text.getChars().toString());
      }
      // InsertAnchorNode[0, 0] name:[39, 45, "insert"]
      {
        @SuppressWarnings("unused") InsertAnchorNode insert = (InsertAnchorNode) paragraphChildren.get(5);
      }
      // Text[48, 49] chars:[48, 49, "."]
      {
        Text text = (Text) paragraphChildren.get(6);
        Assertions.assertEquals(".", text.getChars().toString());
      }
    }

    Assertions.assertEquals(markdown, ms.toMarkdown());

    String html
        = "Some *<em>more</em> <strong>text</strong> and a param: <insert type=\"param\" id-ref=\"insert\" />.";

    Assertions.assertEquals(html, ms.toHtml());
  }

  @Test
  void markupMultilineFromMarkdownTest() throws XMLStreamException, IOException {
    final String markdown = "# Example\n\nSome \"\\**more*\" **text**\n\nA param: {{ insert: param, insert }}.";
    final String html = "<h1>Example</h1>\n"
        + "<p>Some <q>*<em>more</em></q> <strong>text</strong></p>\n"
        + "<p>A param: <insert type=\"param\" id-ref=\"insert\"/>.</p>";

    MarkupMultiline ms = MarkupMultiline.fromMarkdown(markdown);
    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toXHtml(""));
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toXHtml(""));
  }

  @Test
  void markupMultilineFromHtmlTest() throws XMLStreamException, IOException {
    final String html = "<h1>Example</h1>\n"
        + "<p><a href=\"link\">text</a><q>quote1</q></p>\n"
        + "<table>\n"
        + "<thead>\n"
        + "<tr><th>Heading 1</th></tr>\n"
        + "</thead>\n"
        + "<tbody>\n"
        + "<tr><td><q>data1</q> <insert type=\"param\" id-ref=\"insert\"/></td></tr>\n"
        + "<tr><td><q>data2</q> <insert type=\"param\" id-ref=\"insert\"/></td></tr>\n"
        + "</tbody>\n"
        + "</table>\n"
        + "<p>Some <q><em>more</em></q> <strong>text</strong> <img src=\"src\" alt=\"alt\"/></p>";
    final String markdown = "# Example\n"
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
    LOGGER.atDebug().log("HTML Source: {}", html);
    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toXHtml(""));
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());
    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toXHtml(""));
  }

  /*
   * {@link HtmlConverterCoreNodeRenderer} has a bug on line 629 in the call to "wrapTextNodes", which
   * can add spaces to the HTML string. This relates to <a
   * href="https://github.com/vsch/flexmark-java/issues/422">an existing issue</a>.
   */
  @Test
  void markupSpaceHandlingTest() throws XMLStreamException, IOException {
    final String html = "<p>a <q><em>b</em></q> <strong>c</strong></p>";
    final String markdown = "a <q>*b*</q> **c**";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);
    LOGGER.atDebug().log("HTML Source: {}", html);
    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toXHtml(""));
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());
    Assertions.assertNotEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toXHtml(""));
  }

  @Test
  void preMarkdown() {
    String htmlPreOnly = "<pre>Example **some** *code*</pre>";
    final String html = "<pre><code>Example **some** *code*\n"
        + "</code></pre>";
    final String markdown = "```\n"
        + "Example **some** *code*\n"
        + "```";

    MarkupMultiline ms = MarkupMultiline.fromHtml(htmlPreOnly);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toHtml());
  }

  @Test
  void preCodeMarkdown() {
    final String html = "<pre><code>Example **some** *code*\n"
        + "nextline\n"
        + "</code></pre>";
    final String markdown = "    Example **some** *code*\n"
        + "    nextline\n";

    MarkupMultiline ms = MarkupMultiline.fromHtml(html);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toHtml());
  }

  @Test
  void paragraphCodeMarkdown() {
    final String html = "<p>Example<code>**some** *code*</code></p>";
    final String markdown = "Example`**some** *code*`";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toHtml());
  }

  @Test
  void testEntityRoundTrip() throws XMLStreamException {
    final String markdown = "hijacked was used (e.g., the &lt;CTRL&gt; + &lt;ALT&gt; + &lt;DEL&gt; keys).";
    final String html = "hijacked was used (e.g., the &lt;CTRL&gt; + &lt;ALT&gt; + &lt;DEL&gt; keys).";
    final String xhtml = "<p>hijacked was used (e.g., the &lt;CTRL&gt; + &lt;ALT&gt; + &lt;DEL&gt; keys).</p>";

    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    Document document = ms.getDocument();

    Assertions.assertNotNull(document);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toHtml().trim());

    StringWriter stringWriter = new StringWriter();
    XMLStreamWriter2 xmlStreamWriter = newXmlStreamWriter(stringWriter);

    xmlStreamWriter.writeStartElement(MARKUP_HTML_NAMESPACE, "p");

    ms.writeXHtml(MARKUP_HTML_NAMESPACE, xmlStreamWriter);

    xmlStreamWriter.writeEndElement();

    xmlStreamWriter.close();

    Assertions.assertEquals(xhtml, stringWriter.toString());
  }

  @Test
  void testAposRoundTrip() throws XMLStreamException {
    String markdown = "a user’s identity";

    // test from Markdown source
    MarkupLine ms = MarkupLine.fromMarkdown(markdown);
    Document document = ms.getDocument();

    Assertions.assertNotNull(document);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    Assertions.assertEquals(markdown, ms.toMarkdown());

    String html = "a user’s identity";
    Assertions.assertEquals(html, ms.toHtml().trim());

    StringWriter stringWriter = new StringWriter();
    XMLStreamWriter2 xmlStreamWriter = newXmlStreamWriter(stringWriter);

    xmlStreamWriter.writeStartElement(MARKUP_HTML_NAMESPACE, "p");

    ms.writeXHtml(MARKUP_HTML_NAMESPACE, xmlStreamWriter);

    xmlStreamWriter.writeEndElement();

    xmlStreamWriter.close();

    Assertions.assertEquals("<p>" + html + "</p>", stringWriter.toString());

    // test from HTML source
    ms = MarkupLine.fromHtml(html);
    document = ms.getDocument();

    Assertions.assertNotNull(document);
    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());
    Assertions.assertEquals(markdown, ms.toMarkdown());
    Assertions.assertEquals(html, ms.toHtml().trim());
  }

  @Test
  void testHtml() {
    String html = "<p>before &lt;thing[02] text&gt; after</p>";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);

    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(ms.getDocument()));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());
  }

  @Test
  void testIntraTagNewline() {
    // addresses usnistgov/liboscal-java#5
    String html = "<h1>A custom title\n" +
        "  <em>with italic</em>\n" +
        "</h1>";
    MarkupMultiline ms = MarkupMultiline.fromHtml(html);

    Document doc = ms.getDocument();
    LOGGER.atDebug().log("AST: {}", AstCollectingVisitor.asString(doc));
    LOGGER.atDebug().log("HTML: {}", ms.toHtml());
    LOGGER.atDebug().log("Markdown: {}", ms.toMarkdown());

    List<Node> children = CollectionUtil.toList(doc.getChildren());
    // ensure there is only 1 child and that it is a heading
    Assertions.assertAll(
        () -> Assertions.assertEquals(1, children.size()),
        () -> Assertions.assertEquals(Heading.class, children.get(0).getClass()));
  }
}
