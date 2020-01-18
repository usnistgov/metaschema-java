package gov.nist.secauto.metaschema.markup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.builder.BuilderBase;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.DataHolder;

import gov.nist.secauto.metaschema.markup.flexmark.insertanchor.InsertAnchorExtension;

public class FlexmarkFactory {
	private static final Logger logger = LogManager.getLogger(FlexmarkFactory.class);
	private static final FlexmarkFactory instance = new FlexmarkFactory();

	static Map<String, String> specialCharsMap = new HashMap<>();
	static {
//        specialCharsMap.put("“", "\"");
//        specialCharsMap.put("”", "\"");
		specialCharsMap.put("&ldquo;", "“");
		specialCharsMap.put("&rdquo;", "”");
//        specialCharsMap.put("‘", "'");
//        specialCharsMap.put("’", "'");
		specialCharsMap.put("&lsquo;", "‘");
		specialCharsMap.put("&rsquo;", "’");
		specialCharsMap.put("&apos;", "'");
//        specialCharsMap.put("«", "<<");
		specialCharsMap.put("&laquo;", "«");
//        specialCharsMap.put("»", ">>");
		specialCharsMap.put("&raquo;", "»");
//        specialCharsMap.put("…", "...");
		specialCharsMap.put("&hellip;", "…");
//        specialCharsMap.put("–", "--");
		specialCharsMap.put("&ndash;", "–");
//        specialCharsMap.put("—", "---");
		specialCharsMap.put("&mdash;", "—");
	}

	public static FlexmarkFactory instance() {
		return instance;
	}

	private Parser markdownParser;
	private HtmlRenderer htmlRenderer;
	private Formatter formatter;
	private FlexmarkHtmlConverter htmlConverter;

	public Document fromHtml(String html) {
		return fromHtml(html, null, null);
	}

	public Document fromHtml(String html, FlexmarkHtmlConverter htmlParser, Parser markdownParser) {
		Objects.requireNonNull(html, "html");
		if (htmlParser == null) {
			htmlParser = getFlexmarkHtmlConverter();
		}
		if (markdownParser == null) {
			markdownParser = getMarkdownParser();
		}

		String markdown = htmlParser.convert(html);
		logger.trace("markdown: {}", markdown);
		return fromMarkdown(markdown, markdownParser);
	}

	public Document fromMarkdown(String markdown) {
		return fromMarkdown(markdown, getMarkdownParser());
	}

	public Document fromMarkdown(String markdown, Parser parser) {
		Objects.requireNonNull(markdown, "markdown");
		Objects.requireNonNull(parser, "parser");

		return parser.parse(markdown);
	}

	protected void applyOptions(BuilderBase<?> builder) {
		Extension[] extensions = {
				// Metaschema insert
				InsertAnchorExtension.create(),
//				TypographicExtension.create(),
				TablesExtension.create(),
				// to ensure that escaped characters are not lost
				EscapedCharacterExtension.create(), SuperscriptExtension.create(), SubscriptExtension.create() };
		builder.extensions(Arrays.asList(extensions));

		builder.set(Parser.FENCED_CODE_CONTENT_BLOCK, true);
		// GitHub-flavored tables
		builder.set(TablesExtension.COLUMN_SPANS, false);
		builder.set(TablesExtension.APPEND_MISSING_COLUMNS, true);
		builder.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true);
		builder.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);
		builder.set(TypographicExtension.SINGLE_QUOTE_UNMATCHED, null);
		builder.set(TypographicExtension.ENABLE_QUOTES, false);
		builder.set(TypographicExtension.ENABLE_SMARTS, false);
		builder.set(FlexmarkHtmlConverter.TYPOGRAPHIC_REPLACEMENT_MAP, specialCharsMap);
	}

	public Parser getMarkdownParser() {
		synchronized (this) {
			if (markdownParser == null) {
				markdownParser = newMarkdownParser(null);
			}
			return markdownParser;
		}
	}

	public Parser newMarkdownParser(DataHolder options) {
		Parser.Builder builder;
		if (options != null) {
			builder = Parser.builder(options);
		} else {
			builder = Parser.builder();
		}

		applyOptions(builder);
		return builder.build();
	}

	public HtmlRenderer getHtmlRenderer() {
		synchronized (this) {
			if (htmlRenderer == null) {
				htmlRenderer = newHtmlRenderer(null);
			}
			return htmlRenderer;
		}
	}

	public HtmlRenderer newHtmlRenderer(DataHolder options) {
		HtmlRenderer.Builder builder;
		if (options != null) {
			builder = HtmlRenderer.builder(options);
		} else {
			builder = HtmlRenderer.builder();
		}

		applyOptions(builder);
		return builder.build();
	}

	public Formatter getFormatter() {
		synchronized (this) {
			if (formatter == null) {
				formatter = newFormatter(null);
			}
			return formatter;
		}
	}

	public Formatter newFormatter(DataHolder options) {
		Formatter.Builder builder;
		if (options != null) {
			builder = Formatter.builder(options);
		} else {
			builder = Formatter.builder();
		}

		applyOptions(builder);
		return builder.build();
	}

	public FlexmarkHtmlConverter getFlexmarkHtmlConverter() {
		synchronized (this) {
			if (htmlConverter == null) {
				htmlConverter = newFlexmarkHtmlConverter(null);
			}
			return htmlConverter;
		}
	}

	public FlexmarkHtmlConverter newFlexmarkHtmlConverter(DataHolder options) {
		FlexmarkHtmlConverter.Builder builder;
		if (options != null) {
			builder = FlexmarkHtmlConverter.builder(options);
		} else {
			builder = FlexmarkHtmlConverter.builder();
		}

		applyOptions(builder);
		return builder.build();
	}
}
