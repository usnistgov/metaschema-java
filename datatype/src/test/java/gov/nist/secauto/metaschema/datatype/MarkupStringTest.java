package gov.nist.secauto.metaschema.datatype;

import org.junit.jupiter.api.Test;

import gov.nist.secauto.metaschema.datatype.flexmark.AstCollectingVisitor;

class MarkupStringTest {

	@Test
	void fromMarkdown() {
		MarkupString ms = MarkupString.fromMarkdown("Example\n=======\n\nSome \\**more* **text**\n\nA param: {{ insert }}.");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(ms.getNode());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHTML());
		System.out.println(ms.toMarkdown());
	}

	@Test
	void fromHTML() {
		MarkupString ms = MarkupString.fromHTML(
				"<h1>Example</h1><table><tr><th>Heading 1</th></tr><tr><td>data1 <insert param-id=\"insert\" /></td></tr></table><p>Some <em>more</em> <strong>text</strong></p>");
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor = new AstCollectingVisitor();
		visitor.collect(ms.getNode());
		System.out.println(visitor.getAst());
		System.out.println(ms.toHTML());
		System.out.println(ms.toMarkdown());
	}
}
