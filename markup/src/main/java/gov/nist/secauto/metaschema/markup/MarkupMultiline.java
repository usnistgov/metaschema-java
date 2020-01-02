package gov.nist.secauto.metaschema.markup;

import com.vladsch.flexmark.util.ast.Node;

public class MarkupMultiline extends MarkupString {

	public static MarkupMultiline fromHtml(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromHtml(html));
	}

	public static MarkupMultiline fromMarkdown(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromMarkdown(html));
	}

	public MarkupMultiline(Node astNode) {
		super(astNode);
	}

}
