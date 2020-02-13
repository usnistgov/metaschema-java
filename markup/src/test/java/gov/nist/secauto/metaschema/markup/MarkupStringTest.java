package gov.nist.secauto.metaschema.markup;

import org.junit.jupiter.api.Test;

import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatype.markup.MarkupString;
import gov.nist.secauto.metaschema.datatype.markup.flexmark.AstCollectingVisitor;

class MarkupStringTest {

	@Test
	void fromMarkdownLine() {
		MarkupString ms = MarkupLine.fromMarkdown("Some \\**more* **text** and a param: {{ insert }}.");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getDocument());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHtml());
		System.out.println(ms.toMarkdown());
	}

	@Test
	void fromMarkdown() {
		MarkupString ms = MarkupMultiline.fromMarkdown("# Example\n\nSome \\**more* **text**\n\nA param: {{ insert }}.");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getDocument());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHtml());
		System.out.println(ms.toMarkdown());
	}

	@Test
	void fromHTML() {
		MarkupString ms = MarkupMultiline.fromHtml(
				"<h1>Example</h1><p><a href=\"link\">text</a></p><table><tr><th>Heading 1</th></tr><tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr></table><p>Some <em>more</em> <strong>text</strong><img alt=\"alt\" src=\"src\"/></p>");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getDocument());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHtml());
		System.out.println(ms.toMarkdown());
	}

	@Test
	void preMarkdown() {
		MarkupString ms = MarkupMultiline.fromHtml(
				"<pre>Example **some** *code*</pre>");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getDocument());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHtml());
		System.out.println(ms.toMarkdown());
	}

	@Test
	void pCodeMarkdown() {
		MarkupString ms = MarkupMultiline.fromHtml(
				"<p>Example<code>**some** *code*</code></p>");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getDocument());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHtml());
		System.out.println(ms.toMarkdown());
	}
}
