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

package gov.nist.secauto.metaschema.model.common.datatype.markup;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.AbstractMarkupWriter.Handler;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IMarkupWriter<T, E extends Throwable> {

  void writeElement(
      @NonNull String localName,
      @NonNull Node node,
      @Nullable Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeElement(
      @NonNull String localName,
      @NonNull Node node,
      @NonNull Map<String, String> attributes,
      @Nullable Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeElement(
      @NonNull QName qname,
      @NonNull Node node,
      @NonNull Map<String, String> attributes,
      @Nullable Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeEmptyElement(
      String localName,
      Map<String, String> attributes) throws E;

  void writeEmptyElement(
      @NonNull QName qName,
      @NonNull Map<String, String> attributes) throws E;

  void writeElementStart(
      @NonNull QName qname) throws E;

  void writeElementStart(
      @NonNull QName qname,
      @NonNull Map<String, String> attributes) throws E;

  void writeElementEnd(QName qName) throws E;

  void writeText(@NonNull String text) throws E;

  void writeHtmlEntity(@NonNull String entityText) throws E;

  void writeLink(
      @NonNull LinkNode node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeText(@NonNull Text node) throws E;

  void writeText(@NonNull EscapedCharacter node) throws E;

  void writeHtmlEntity(@NonNull HtmlEntity node) throws E;

  void writeHtmlEntity(@NonNull TypographicSmarts node) throws E;

  void writeTypographicQuotes(
      @NonNull TypographicQuotes node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeInlineHtml(HtmlInline node) throws E;

  void writeBlockHtml(HtmlBlock node) throws E;

  void writeTable(
      @NonNull TableBlock node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> cellChilddHandler) throws E;

  void writeImage(
      @NonNull Image node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeInsertAnchor(@NonNull InsertAnchorNode node) throws E;

  void writeHeading(
      @NonNull Heading node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeCodeBlock(
      @NonNull IndentedCodeBlock node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;

  void writeCodeBlock(
      @NonNull FencedCodeBlock node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E;
}
