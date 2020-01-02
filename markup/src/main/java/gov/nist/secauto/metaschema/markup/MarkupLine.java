package gov.nist.secauto.metaschema.markup;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkupLine extends MarkupString {
	private static final Parser markdownParser;

	static {
		MutableDataSet options = new MutableDataSet();
		// disable inline HTML
		options.set(Parser.HTML_BLOCK_PARSER, false);
		// disable list processing
		options.set(Parser.LIST_BLOCK_PARSER, false);

		markdownParser = FlexmarkFactory.instance().newMarkdownParser(options);
	}


	public static MarkupLine fromHtml(String html) {
		return new MarkupLine(FlexmarkFactory.instance().fromHtml(html, null, markdownParser));
	}

	public static MarkupLine fromMarkdown(String html) {
		return new MarkupLine(FlexmarkFactory.instance().fromMarkdown(html));
	}

	public MarkupLine(Document astNode) {
		super(astNode);
		Node child = astNode.getFirstChild();
		if (child == null) {
			// empty markdown
		} else {
			if (child instanceof Block && child.getNext() != null)
			throw new IllegalStateException("multiple blocks not allowed");
		}
	}
}
