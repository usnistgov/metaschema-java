package gov.nist.secauto.metaschema.markup;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.markup.flexmark.AstCollectingVisitor;

public class MarkupString {
	private final Node node;

//
//	public MarkupString() {
//		this(null);
//	}
	
	public MarkupString(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	public String toHtml() {
		return toHTML(FlexmarkFactory.instance().getHtmlRenderer());
	}

	protected String toHTML(HtmlRenderer renderer) {
		return renderer.render(getNode());
	}

	public String toMarkdown() {
		return toMarkdown(FlexmarkFactory.instance().getFormatter());
	}

	protected String toMarkdown(Formatter formatter) {
		return formatter.render(getNode());
	}

	@Override
	public String toString() {
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(getNode());
		return visitor.getAst();
	}
}
