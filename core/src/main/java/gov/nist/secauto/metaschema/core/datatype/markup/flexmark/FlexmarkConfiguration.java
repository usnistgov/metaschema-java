/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.core.datatype.markup.flexmark;

import com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.SubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.format.options.ListBulletMarker;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class FlexmarkConfiguration {

  @NonNull
  private static final ParserEmulationProfile PARSER_PROFILE = ParserEmulationProfile.COMMONMARK_0_29;

  @NonNull
  public static final DataSet FLEXMARK_CONFIG = initFlexmarkConfig();

  @SuppressWarnings("null")
  @NonNull
  private static DataSet initFlexmarkConfig() {
    MutableDataSet options = new MutableDataSet();
    options.setFrom(PARSER_PROFILE);

    List<Extension> extensions = List.of(
        // Metaschema insert
        InsertAnchorExtension.newInstance(),
        // q tag handling
        HtmlQuoteTagExtension.newInstance(),
        TypographicExtension.create(),
        TablesExtension.create(),
        // fix for code handling
        HtmlCodeRenderExtension.newInstance(),
        // to ensure that escaped characters are not lost
        EscapedCharacterExtension.create(),
        SuperscriptExtension.create(),
        SubscriptExtension.create()
    // AutolinkExtension.create()
    );
    Parser.EXTENSIONS.set(options, extensions);

    // AST processing expects this
    Parser.FENCED_CODE_CONTENT_BLOCK.set(options, true);
    // Parser.CODE_SOFT_LINE_BREAKS.set(options, true);
    // Parser.PARSE_INNER_HTML_COMMENTS.set(options, true);
    // Parser.HTML_BLOCK_COMMENT_ONLY_FULL_LINE.set(options, true);
    // Parser.HTML_COMMENT_BLOCKS_INTERRUPT_PARAGRAPH.set(options, true);

    // disable the built in processor, since we are configuring a patched one
    Parser.ASTERISK_DELIMITER_PROCESSOR.set(options, false);

    // configure GitHub-flavored tables
    TablesExtension.COLUMN_SPANS.set(options, false);
    TablesExtension.APPEND_MISSING_COLUMNS.set(options, true);
    TablesExtension.DISCARD_EXTRA_COLUMNS.set(options, true);
    TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH.set(options, true);

    // TypographicExtension.ENABLE_QUOTES.set(options, true); // default
    TypographicExtension.ENABLE_SMARTS.set(options, false);
    TypographicExtension.SINGLE_QUOTE_UNMATCHED.set(options, "'");
    TypographicExtension.DOUBLE_QUOTE_OPEN.set(options, "\"");
    TypographicExtension.DOUBLE_QUOTE_CLOSE.set(options, "\"");

    Map<String, String> typographicReplacementMap = new ConcurrentHashMap<>();
    typographicReplacementMap.put("“", "\"");
    typographicReplacementMap.put("”", "\"");
    typographicReplacementMap.put("&ldquo;", "“");
    typographicReplacementMap.put("&rdquo;", "”");
    // typographicReplacementMap.put("‘", "'");
    // typographicReplacementMap.put("’", "'");
    typographicReplacementMap.put("&lsquo;", "‘");
    typographicReplacementMap.put("&rsquo;", "’");
    typographicReplacementMap.put("&apos;", "’");
    // typographicReplacementMap.put("«", "<<");
    typographicReplacementMap.put("&laquo;", "«");
    // typographicReplacementMap.put("»", ">>");
    typographicReplacementMap.put("&raquo;", "»");
    // typographicReplacementMap.put("…", "...");
    typographicReplacementMap.put("&hellip;", "…");
    // typographicReplacementMap.put("–", "--");
    typographicReplacementMap.put("&endash;", "–");
    // typographicReplacementMap.put("—", "---");
    typographicReplacementMap.put("&emdash;", "—");

    FlexmarkHtmlConverter.TYPOGRAPHIC_REPLACEMENT_MAP.set(options, typographicReplacementMap);
    FlexmarkHtmlConverter.OUTPUT_UNKNOWN_TAGS.set(options, true);
    FlexmarkHtmlConverter.SETEXT_HEADINGS.set(options, false); // disable
    // needed to ensure extra empty paragraphs are ignored
    FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES.set(options, false);

    // FlexmarkHtmlConverter.RENDER_COMMENTS.set(options, true);
    // FlexmarkHtmlConverter.ADD_TRAILING_EOL.set(options, false); // default

    Formatter.MAX_TRAILING_BLANK_LINES.set(options, -1);
    Formatter.LIST_BULLET_MARKER.set(options, ListBulletMarker.DASH);

    HtmlRenderer.MAX_TRAILING_BLANK_LINES.set(options, -1);
    HtmlRenderer.UNESCAPE_HTML_ENTITIES.set(options, true);
    HtmlRenderer.PERCENT_ENCODE_URLS.set(options, true);
    // HtmlRenderer.ESCAPE_HTML_COMMENT_BLOCKS.set(options, false); // default
    // HtmlRenderer.SUPPRESS_HTML_COMMENT_BLOCKS.set(options, false); // default
    // HtmlRenderer.SUPPRESS_INLINE_HTML_COMMENTS.set(options, false); // default
    // HtmlRenderer.HARD_BREAK.set(options,"<br/>");

    return options.toImmutable();
  }

  /**
   * Get a new flexmark configuration.
   *
   * @param options
   *          the current set of options.
   * @return the configuration
   */
  public static DataSet newFlexmarkConfig(@Nullable DataHolder options) {
    return options == null ? FLEXMARK_CONFIG : DataSet.merge(FLEXMARK_CONFIG, options);
  }

  private FlexmarkConfiguration() {
    // disable construction
  }

}
