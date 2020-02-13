package gov.nist.secauto.metaschema.datatype.markup;

import com.vladsch.flexmark.util.ast.Document;

public class MarkupMultiline extends MarkupString<MarkupMultiline> {

	public static MarkupMultiline fromHtml(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromHtml(html));
	}

	public static MarkupMultiline fromMarkdown(String html) {
		return new MarkupMultiline(FlexmarkFactory.instance().fromMarkdown(html));
	}

	public MarkupMultiline(Document astNode) {
		super(astNode);
	}

	@Override
	public MarkupMultiline copy() {
		// TODO: find a way to do a deep copy
		// this is a shallow copy that uses the same underlying Document object
		return new MarkupMultiline(getDocument());
	}

}
