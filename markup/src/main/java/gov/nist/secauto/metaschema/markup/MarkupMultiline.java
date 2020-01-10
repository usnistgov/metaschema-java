package gov.nist.secauto.metaschema.markup;

import com.vladsch.flexmark.util.ast.Document;

public class MarkupMultiline extends MarkupString {

	public static MarkupMultiline fromHtml(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromHtml(html));
	}

	public static MarkupMultiline fromMarkdown(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromMarkdown(html));
	}

	public MarkupMultiline(Document astNode) {
		super(astNode);
	}

}
