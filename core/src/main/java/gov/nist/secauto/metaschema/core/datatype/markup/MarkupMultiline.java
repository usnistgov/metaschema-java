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

package gov.nist.secauto.metaschema.core.datatype.markup;

import com.vladsch.flexmark.util.ast.Document;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.FlexmarkFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MarkupMultiline
    extends AbstractMarkupString<MarkupMultiline> {

  @NonNull
  private static final FlexmarkFactory FLEXMARK_FACTORY = FlexmarkFactory.instance();

  /**
   * Convert the provided HTML string into markup.
   *
   * @param html
   *          the HTML
   * @return the multiline markup instance
   */
  @NonNull
  public static MarkupMultiline fromHtml(@NonNull String html) {
    return new MarkupMultiline(
        parseHtml(
            html,
            FLEXMARK_FACTORY.getFlexmarkHtmlConverter(),
            FLEXMARK_FACTORY.getMarkdownParser()));
  }

  /**
   * Convert the provided markdown string into markup.
   *
   * @param markdown
   *          the markup
   * @return the multiline markup instance
   */
  @NonNull
  public static MarkupMultiline fromMarkdown(@NonNull String markdown) {
    return new MarkupMultiline(
        parseMarkdown(markdown, FLEXMARK_FACTORY.getMarkdownParser()));
  }

  /**
   * Construct a new multiline markup instance.
   *
   * @param astNode
   *          the parsed markup AST
   */
  public MarkupMultiline(@NonNull Document astNode) {
    super(astNode);
  }

  @Override
  public FlexmarkFactory getFlexmarkFactory() {
    return FLEXMARK_FACTORY;
  }

  @Override
  public MarkupMultiline copy() {
    // TODO: find a way to do a deep copy
    // this is a shallow copy that uses the same underlying Document object
    return new MarkupMultiline(getDocument());
  }

  @Override
  public boolean isBlock() {
    return true;
  }
}
