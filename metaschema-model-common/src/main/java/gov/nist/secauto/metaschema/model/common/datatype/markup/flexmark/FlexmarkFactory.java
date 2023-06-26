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

package gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.ListOptions;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FlexmarkFactory {
  @NonNull
  private static final FlexmarkFactory SINGLETON = new FlexmarkFactory();

  @NonNull
  private final DataHolder configuration;
  @NonNull
  private final Parser markdownParser;
  @NonNull
  private final HtmlRenderer htmlRenderer;
  @NonNull
  private final Formatter formatter;
  @NonNull
  private final FlexmarkHtmlConverter htmlConverter;
  @NonNull
  final ListOptions listOptions;

  @NonNull
  public static FlexmarkFactory instance() {
    return SINGLETON;
  }

  public FlexmarkFactory() {
    this(FlexmarkConfiguration.FLEXMARK_CONFIG);
  }

  public FlexmarkFactory(@NonNull DataHolder config) {
    this.configuration = config;
    this.markdownParser = Parser.builder(config)
        .customDelimiterProcessor(new FixedEmphasisDelimiterProcessor(Parser.STRONG_WRAPS_EMPHASIS.get(config))).build();
    this.htmlRenderer = HtmlRenderer.builder(config).build();
    this.formatter = Formatter.builder(config).build();
    this.htmlConverter = FlexmarkHtmlConverter.builder(config).build();
    this.listOptions = ListOptions.get(config);
  }

  @NonNull
  public DataHolder getConfiguration() {
    return configuration;
  }

  @NonNull
  public ListOptions getListOptions() {
    return listOptions;
  }

  @NonNull
  public Parser getMarkdownParser() {
    return markdownParser;
  }

  @NonNull
  public HtmlRenderer getHtmlRenderer() {
    return htmlRenderer;
  }

  @NonNull
  public Formatter getFormatter() {
    return formatter;
  }

  @NonNull
  public FlexmarkHtmlConverter getFlexmarkHtmlConverter() {
    return htmlConverter;
  }
}
