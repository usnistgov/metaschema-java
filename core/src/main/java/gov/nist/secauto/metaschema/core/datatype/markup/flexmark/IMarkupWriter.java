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

import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.HardLineBreak;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlCommentBlock;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.MailLink;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "THROWS_METHOD_THROWS_CLAUSE_THROWABLE",
    justification = "There is a need to support varying exceptions from multiple stream writers")
public interface IMarkupWriter<T, E extends Throwable> { // NOPMD
  @NonNull
  QName asQName(@NonNull String localName);

  default void writeElement(
      @NonNull String localName,
      @NonNull Node node,
      @Nullable ChildHandler<T, E> childHandler) throws E {
    writeElement(localName, node, CollectionUtil.emptyMap(), childHandler);
  }

  default void writeElement(
      @NonNull String localName,
      @NonNull Node node,
      @NonNull Map<String, String> attributes,
      @Nullable ChildHandler<T, E> childHandler) throws E {
    QName qname = asQName(localName);
    writeElement(qname, node, attributes, childHandler);
  }

  void writeElement(
      @NonNull QName qname,
      @NonNull Node node,
      @NonNull Map<String, String> attributes,
      @Nullable ChildHandler<T, E> childHandler) throws E;

  default void writeEmptyElement(
      @NonNull String localName,
      @NonNull Map<String, String> attributes) throws E {
    QName qname = asQName(localName);
    writeEmptyElement(qname, attributes);
  }

  void writeEmptyElement(
      @NonNull QName qname,
      @NonNull Map<String, String> attributes) throws E;

  default void writeElementStart(
      @NonNull QName qname) throws E {
    writeElementStart(qname, CollectionUtil.emptyMap());
  }

  void writeElementStart(
      @NonNull QName qname,
      @NonNull Map<String, String> attributes) throws E;

  void writeElementEnd(@NonNull QName qname) throws E;

  void writeText(@NonNull Text node) throws E;

  /**
   * Handle a combination of {@link Text} and {@link EscapedCharacter} node
   * children.
   *
   * @param node
   *          the text node to write
   * @throws E
   *           if an error occured while writing
   */
  void writeText(@NonNull TextBase node) throws E;

  void writeText(@NonNull CharSequence text) throws E;

  void writeHtmlEntity(@NonNull HtmlEntity node) throws E;

  void writeHtmlEntity(@NonNull TypographicSmarts node) throws E;

  void writeParagraph(
      @NonNull Paragraph node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeLink(
      @NonNull Link node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeLink(@NonNull MailLink node) throws E;

  void writeLink(@NonNull AutoLink node) throws E;

  void writeTypographicQuotes(
      @NonNull TypographicQuotes node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeInlineHtml(@NonNull HtmlInline node) throws E;

  void writeBlockHtml(@NonNull HtmlBlock node) throws E;

  void writeTable(
      @NonNull TableBlock node,
      @NonNull ChildHandler<T, E> cellChilddHandler) throws E;

  void writeImage(@NonNull Image node) throws E;

  void writeInsertAnchor(@NonNull InsertAnchorNode node) throws E;

  void writeHeading(
      @NonNull Heading node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeCode(
      @NonNull Code node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeCodeBlock(
      @NonNull IndentedCodeBlock node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeCodeBlock(
      @NonNull FencedCodeBlock node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeCodeBlock(
      @NonNull CodeBlock node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  void writeBlockQuote(
      @NonNull BlockQuote node,
      @NonNull ChildHandler<T, E> childHandler) throws E;

  default void writeList(
      @NonNull String localName,
      @NonNull ListBlock node,
      @NonNull ChildHandler<T, E> listItemHandler) throws E {
    QName qname = asQName(localName);
    writeList(qname, node, listItemHandler);
  }

  void writeList(
      @NonNull QName qname,
      @NonNull ListBlock node,
      @NonNull ChildHandler<T, E> listItemHandler) throws E;

  void writeListItem(
      @NonNull ListItem node,
      @NonNull ChildHandler<T, E> listItemHandler) throws E;

  void writeBreak(@NonNull HardLineBreak node) throws E;

  void writeBreak(@NonNull ThematicBreak node) throws E;

  void writeComment(@NonNull HtmlCommentBlock node) throws E;

  /**
   * Provides a callback to handle node children.
   *
   * @param <T>
   *          the type of stream to write to
   * @param <E>
   *          the type of exception that can be thrown when a writing error occurs
   */
  @FunctionalInterface
  interface ChildHandler<T, E extends Throwable> { // NOPMD
    void accept(@NonNull Node node, @NonNull IMarkupWriter<T, E> writer) throws E;
  }

}
