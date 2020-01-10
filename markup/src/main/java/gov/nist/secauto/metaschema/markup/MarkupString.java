package gov.nist.secauto.metaschema.markup;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Document;

import gov.nist.secauto.metaschema.markup.flexmark.AstCollectingVisitor;

public class MarkupString {
	private final Document document;

//
//	public MarkupString() {
//		this(null);
//	}
	
	public MarkupString(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public String toHtml() {
		return toHTML(FlexmarkFactory.instance().getHtmlRenderer());
	}

	protected String toHTML(HtmlRenderer renderer) {
		return renderer.render(getDocument());
	}

	public String toMarkdown() {
		return toMarkdown(FlexmarkFactory.instance().getFormatter());
	}

	protected String toMarkdown(Formatter formatter) {
		return formatter.render(getDocument());
	}

	@Override
	public String toString() {
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(getDocument());
		return visitor.getAst();
	}
}
