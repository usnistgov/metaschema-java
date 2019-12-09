package gov.nist.secauto.metaschema.datatype;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import gov.nist.secauto.metaschema.datatype.flexmark.AstCollectingVisitor;
import gov.nist.secauto.metaschema.datatype.flexmark.insertanchor.InsertAnchorExtension;

public class MarkupString {
	private static final Logger logger = LogManager.getLogger(MarkupString.class);

	private static final Parser parserInstance;
	private static final HtmlRenderer htmlRendererInstance;
	private static final Formatter formatterInstance;
	private static final FlexmarkHtmlConverter htmlConverterInstance;

	static {
		MutableDataSet options = new MutableDataSet()
				.set(Parser.EXTENSIONS,
						Arrays.asList(InsertAnchorExtension.create(), TablesExtension.create(),
								EscapedCharacterExtension.create(), SuperscriptExtension.create(),
								StrikethroughSubscriptExtension.create()))
				.set(TablesExtension.COLUMN_SPANS, false).set(TablesExtension.APPEND_MISSING_COLUMNS, true)
				.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);

//		Set<java.lang.Class<? extends Block>> supportedBlockTypes = new HashSet<>();
//		supportedBlockTypes.add(Heading.class);
//		supportedBlockTypes.add(Paragraph.class);
//		supportedBlockTypes.add(FencedCodeBlock.class);
//		supportedBlockTypes.add(IndentedCodeBlock.class);
//		supportedBlockTypes.add(BlockQuote.class);
//		supportedBlockTypes.add(ListBlock.class);
//		supportedBlockTypes.add(TableBlock.class);
		// TODO: subscript/superscript
		parserInstance = Parser.builder(options).build();

		htmlRendererInstance = HtmlRenderer.builder(options).build();
		formatterInstance = Formatter.builder(options).build();

		MutableDataSet htmlConverterOptions = new MutableDataSet().set(Parser.EXTENSIONS,
				Arrays.asList(InsertAnchorExtension.create()));
		htmlConverterInstance = FlexmarkHtmlConverter.builder(htmlConverterOptions).build();
	}

	private static Parser getParserInstance() {
		return parserInstance;
	}

	private static FlexmarkHtmlConverter getHtmlConverterInstance() {
		return htmlConverterInstance;
	}

	private static HtmlRenderer getHtmlRendererInstance() {
		return htmlRendererInstance;
	}

	private static Formatter getFormatterInstance() {
		return formatterInstance;
	}

	public static MarkupString fromHTML(String html) {
		return fromHTML(html, null, null);
	}

	public static MarkupString fromHTML(String html, FlexmarkHtmlConverter htmlParser, Parser markdownParser) {
		if (htmlParser == null) {
			htmlParser = getHtmlConverterInstance();
		}
		logger.trace("html: {}", html);
		String markdown = htmlParser.convert(html);
		logger.trace("markdown: {}", markdown);
		return fromMarkdown(markdown, markdownParser);
	}

	public static MarkupString fromMarkdown(String markdown) {
		return fromMarkdown(markdown, null);
	}

	public static MarkupString fromMarkdown(String markdown, Parser parser) {
		if (parser == null) {
			parser = getParserInstance();
		}
		Document node = parser.parse(markdown);
		return new MarkupString(node);
	}

	public enum Type {
		MARKUP_MULTILINE,
		MARKUP_LINE;
	}

	private final Node node;
	private final Type type;

//
//	public MarkupString() {
//		this(null);
//	}
	
	public MarkupString(Node node) {
		this(node, Type.MARKUP_MULTILINE);
	}

	public MarkupString(Node node, Type type) {
		this.node = node;
		this.type = type;
	}

	public Node getNode() {
		return node;
	}

	protected Type getType() {
		return type;
	}

	public String toHTML() {
		HtmlRenderer renderer = getHtmlRendererInstance();
		return renderer.render(getNode());
	}

	public String toMarkdown() {
		return getFormatterInstance().render(node);
	}

	@Override
	public String toString() {
		AstCollectingVisitor visitor = new AstCollectingVisitor();
		visitor.collect(getNode());
		return visitor.getAst();
	}
}
