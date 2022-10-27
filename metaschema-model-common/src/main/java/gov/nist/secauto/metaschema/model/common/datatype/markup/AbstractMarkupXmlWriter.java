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

import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.util.ast.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractMarkupXmlWriter<WRITER> // NOPMD - acceptable
    extends AbstractMarkupXmlVisitor<WRITER, XMLStreamException> {

  public AbstractMarkupXmlWriter(@NonNull String namespace, boolean handleBlockElements) {
    super(namespace, handleBlockElements);
  }

  @Override
  protected void handleBasicElement(@NonNull String localName, @NonNull Node node, WRITER writer)
      throws XMLStreamException {
    QName name = newQName(localName);
    handleBasicElementStart(node, writer, name);
    visitChildren(node, writer);
    handleBasicElementEnd(node, writer, name);
  }

  protected abstract void handleBasicElementStart(@NonNull Node node, WRITER writer, @NonNull QName name)
      throws XMLStreamException;

  protected abstract void handleBasicElementEnd(@NonNull Node node, WRITER writer, @NonNull QName name)
      throws XMLStreamException;

  @Override
  protected void visitLink(LinkNode node, WRITER writer) throws XMLStreamException {
    QName name = newQName("a");
    handleLinkStart(node, writer, name);

    visitChildren(node, writer);

    handleLinkEnd(node, writer, name);
  }

  protected abstract void handleLinkStart(@NonNull LinkNode node, WRITER writer, @NonNull QName name)
      throws XMLStreamException;

  protected abstract void handleLinkEnd(@NonNull LinkNode node, WRITER writer, @NonNull QName name)
      throws XMLStreamException;

  @Override
  protected void visitTable(@NonNull TableBlock node, WRITER writer) throws XMLStreamException {
    QName tableQName = newQName("table");
    handleBasicElementStart(node, writer, tableQName);

    super.visitTable(node, writer);

    handleBasicElementEnd(node, writer, tableQName);
  }

  @Override
  protected void visitTableRow(@NonNull TableRow node, WRITER writer) throws XMLStreamException {
    QName trQName = newQName("tr");
    handleBasicElementStart(node, writer, trQName);

    for (Node childNode : node.getChildren()) {
      if (childNode instanceof TableCell) {
        handleTableCell((TableCell) childNode, writer);
      }
    }

    handleBasicElementEnd(node, writer, trQName);
  }

  private void handleTableCell(TableCell node, WRITER writer) throws XMLStreamException {
    QName cellQName;
    if (node.isHeader()) {
      cellQName = newQName("th");
    } else {
      cellQName = newQName("td");
    }
    handleBasicElementStart(node, writer, cellQName);
    visitChildren(node, writer);
    handleBasicElementEnd(node, writer, cellQName);
  }

  // protected boolean processInlineElements(Node node, WRITER writer) throws XMLStreamException { //
  // NOPMD - acceptable
  // boolean retval = false;
  // if (node instanceof Text) {
  // writeText(node.getChars().toString(), writer);
  // retval = true;
  // } else if (node instanceof EscapedCharacter) {
  // handleEscapedCharacter((EscapedCharacter) node, writer);
  // retval = true;
  // } else if (node instanceof HtmlEntity) {
  // handleHtmlEntity(((HtmlEntity) node).getChars().toString(), writer);
  // retval = true;
  // } else if (node instanceof TypographicSmarts) {
  // handleTypographicSmarts((TypographicSmarts) node, writer);
  // retval = true;
  // } else if (node instanceof TypographicQuotes) {
  // if (node instanceof DoubleQuoteNode) {
  // handleBasicElement(node, writer, "q");
  // } else {
  // handleTypographicSmarts((TypographicQuotes) node, writer);
  // }
  // retval = true;
  // } else if (node instanceof Code) {
  // handleBasicElement(node, writer, "code");
  // retval = true;
  // } else if (node instanceof StrongEmphasis) {
  // handleBasicElement(node, writer, "strong");
  // retval = true;
  // } else if (node instanceof Emphasis) {
  // handleBasicElement(node, writer, "em");
  // retval = true;
  // } else if (node instanceof ListItem) {
  // handleBasicElement(node, writer, "li");
  // retval = true;
  // } else if (node instanceof Link || node instanceof AutoLink) {
  // handleLink((LinkNode) node, writer);
  // retval = true;
  // } else if (node instanceof TextBase) {
  // // ignore these, but process their children
  // visitChildren(node, writer);
  // retval = true;
  // } else if (node instanceof Subscript) {
  // handleBasicElement(node, writer, "sub");
  // retval = true;
  // } else if (node instanceof Superscript) {
  // handleBasicElement(node, writer, "sup");
  // retval = true;
  // } else if (node instanceof Image) {
  // handleImage((Image) node, writer);
  // retval = true;
  // } else if (node instanceof InsertAnchorNode) {
  // handleInsertAnchor((InsertAnchorNode) node, writer);
  // retval = true;
  // } else if (node instanceof SoftLineBreak) {
  // handleSoftLineBreak((SoftLineBreak) node, writer);
  // retval = true;
  // } else if (node instanceof HtmlInline) {
  // handleHtmlInline((HtmlInline) node, writer);
  // retval = true;
  // }
  // return retval;
  // }

  // protected boolean processBlockElements(Node node, WRITER writer) throws XMLStreamException {
  // boolean retval = false;
  // if (node instanceof Paragraph) {
  // handleBasicElement(node, writer, "p");
  // retval = true;
  // } else if (node instanceof Heading) {
  // handleHeading((Heading) node, writer);
  // retval = true;
  // } else if (node instanceof OrderedList) {
  // handleBasicElement(node, writer, "ol");
  // retval = true;
  // } else if (node instanceof BulletList) {
  // handleBasicElement(node, writer, "ul");
  // retval = true;
  // } else if (node instanceof TableBlock) {
  // handleTable((TableBlock) node, writer);
  // retval = true;
  // } else if (node instanceof HtmlBlock) {
  // handleHtmlBlock((HtmlBlock) node, writer);
  // retval = true;
  // }
  // return retval;
  // }

  // protected abstract void handleHtmlInline(HtmlInline node, WRITER writer) throws
  // XMLStreamException;
  // // {
  // // throw new UnsupportedOperationException(
  // // String.format("Unable to process inline HTML characters: %s", node.getChars().toString()));
  // // // String htmlText = node.getChars().toString();
  // // //
  // // // QName name = new QName(getNamespace(), "name");
  // // // handleBasicElementStart(node, writer, name);
  // // //
  // // // if (node.hasChildren()) {
  // // // visitChildren(node, writer);
  // // // }
  // // //
  // // // handleBasicElementEnd(node, writer, name);
  // // }
}
