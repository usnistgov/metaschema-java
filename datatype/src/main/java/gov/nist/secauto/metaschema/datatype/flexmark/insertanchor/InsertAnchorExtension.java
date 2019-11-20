package gov.nist.secauto.metaschema.datatype.flexmark.insertanchor;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.internal.InsertAnchorFormatter;
import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.internal.InsertAnchorHtmlNodeRenderer;
import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.internal.InsertAnchorInlineParser;
import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.internal.InsertAnchorNodeRenderer;

public class InsertAnchorExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension, Formatter.FormatterExtension, FlexmarkHtmlConverter.HtmlConverterExtension  {
	public static final DataKey<Boolean> ENABLE_INLINE_INSERT_ANCHORS = new DataKey<>("ENABLE_INLINE_INSERT_ANCHORS", true);
	public static final DataKey<Boolean> ENABLE_RENDERING = new DataKey<>("ENABLE_RENDERING", true);

	public static Extension create() {
		return new InsertAnchorExtension();
	}

	public InsertAnchorExtension() {
	}

	@Override
	public void parserOptions(MutableDataHolder options) {
	}

	@Override
	public void rendererOptions(MutableDataHolder options) {
	}

	@Override
	public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
		rendererBuilder.nodeRendererFactory(new InsertAnchorNodeRenderer.Factory());
	}

	@Override
	public void extend(Parser.Builder parserBuilder) {
		if (ENABLE_INLINE_INSERT_ANCHORS.getFrom(parserBuilder)) {
			parserBuilder.customInlineParserExtensionFactory(new InsertAnchorInlineParser.Factory());
		}
	}

	@Override
	public void extend(Formatter.Builder builder) {
		builder.nodeFormatterFactory(new InsertAnchorFormatter.Factory());
	}

	@Override
	public void extend(FlexmarkHtmlConverter.Builder builder) {
		builder.htmlNodeRendererFactory(new InsertAnchorHtmlNodeRenderer.Factory());
	}
}
