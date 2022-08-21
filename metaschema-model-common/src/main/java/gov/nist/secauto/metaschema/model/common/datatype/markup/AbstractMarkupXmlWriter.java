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

import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.DoubleQuoteNode;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractMarkupXmlWriter<WRITER> { // NOPMD - acceptable
  private static final Pattern ENTITY_PATTERN = Pattern.compile("^&([^;]+);$");
  private static final Map<String, String> ENTITY_MAP;

  static {
    ENTITY_MAP = new HashMap<>();
    ENTITY_MAP.put("&amp;", "&");
    ENTITY_MAP.put("&lsquo;", "‘");
    ENTITY_MAP.put("&rsquo;", "’");
    ENTITY_MAP.put("&hellip;", "…");
    ENTITY_MAP.put("&mdash;", "—");
    ENTITY_MAP.put("&ndash;", "–");
    ENTITY_MAP.put("&ldquo;", "“");
    ENTITY_MAP.put("&rdquo;", "”");
    ENTITY_MAP.put("&laquo;", "«");
    ENTITY_MAP.put("&raquo;", "»");
  }

  @NonNull
  private final String namespace;
  private final boolean handleBlockElements;

  public AbstractMarkupXmlWriter(@NonNull String namespace, boolean handleBlockElements) {
    this.namespace = namespace;
    this.handleBlockElements = handleBlockElements;
  }

  @NonNull
  protected String getNamespace() {
    return namespace;
  }

  protected boolean isHandleBlockElements() {
    return handleBlockElements;
  }

  public void visitChildren(Node parentNode, WRITER writer) throws XMLStreamException {
    for (Node node : parentNode.getChildren()) {
      visit(node, writer);
    }
  }

  protected void visit(Node node, WRITER writer) throws XMLStreamException {
    boolean handled = processInlineElements(node, writer);
    if (!handled && node instanceof Block) {
      if (isHandleBlockElements()) {
        handled = processBlockElements(node, writer);
      } else {
        visitChildren(node, writer);
        handled = true;
      }
    }

    if (!handled) {
      throw new UnsupportedOperationException(
          String.format("Node '%s' not handled. AST: %s", node.getNodeName(), node.toAstString(true)));
    }
  }

  protected abstract void writeText(WRITER writer, String text) throws XMLStreamException;

  protected abstract void writeHtmlEntity(WRITER writer, String entityText) throws XMLStreamException;

  protected boolean processInlineElements(Node node, WRITER writer) throws XMLStreamException { // NOPMD - acceptable
    boolean retval = false;
    if (node instanceof Text) {
      writeText(writer, node.getChars().toString());
      retval = true;
    } else if (node instanceof EscapedCharacter) {
      handleEscapedCharacter((EscapedCharacter) node, writer);
      retval = true;
    } else if (node instanceof HtmlEntity) {
      handleHtmlEntity(((HtmlEntity) node).getChars().toString(), writer);
      retval = true;
    } else if (node instanceof TypographicSmarts) {
      handleTypographicSmarts((TypographicSmarts) node, writer);
      retval = true;
    } else if (node instanceof TypographicQuotes) {
      if (node instanceof DoubleQuoteNode) {
        handleBasicElement(node, writer, "q");
      } else {
        handleTypographicSmarts((TypographicQuotes) node, writer);
      }
      retval = true;
    } else if (node instanceof Code) {
      handleBasicElement(node, writer, "code");
      retval = true;
    } else if (node instanceof StrongEmphasis) {
      handleBasicElement(node, writer, "strong");
      retval = true;
    } else if (node instanceof Emphasis) {
      handleBasicElement(node, writer, "em");
      retval = true;
    } else if (node instanceof ListItem) {
      handleBasicElement(node, writer, "li");
      retval = true;
    } else if (node instanceof Link || node instanceof AutoLink) {
      handleLink((LinkNode) node, writer);
      retval = true;
    } else if (node instanceof TextBase) {
      // ignore these, but process their children
      visitChildren(node, writer);
      retval = true;
    } else if (node instanceof Subscript) {
      handleBasicElement(node, writer, "sub");
      retval = true;
    } else if (node instanceof Superscript) {
      handleBasicElement(node, writer, "sup");
      retval = true;
    } else if (node instanceof Image) {
      handleImage((Image) node, writer);
      retval = true;
    } else if (node instanceof InsertAnchorNode) {
      handleInsertAnchor((InsertAnchorNode) node, writer);
      retval = true;
    } else if (node instanceof SoftLineBreak) {
      handleSoftLineBreak((SoftLineBreak) node, writer);
      retval = true;
    } else if (node instanceof HtmlInline) {
      handleHtmlInline((HtmlInline) node, writer);
      retval = true;
    }
    return retval;
  }

  protected boolean processBlockElements(Node node, WRITER writer) throws XMLStreamException {
    boolean retval = false;
    if (node instanceof Paragraph) {
      handleBasicElement(node, writer, "p");
      retval = true;
    } else if (node instanceof Heading) {
      handleHeading((Heading) node, writer);
      retval = true;
    } else if (node instanceof OrderedList) {
      handleBasicElement(node, writer, "ol");
      retval = true;
    } else if (node instanceof BulletList) {
      handleBasicElement(node, writer, "ul");
      retval = true;
    } else if (node instanceof TableBlock) {
      handleTable((TableBlock) node, writer);
      retval = true;
    } else if (node instanceof HtmlBlock) {
      handleHtmlBlock((HtmlBlock) node, writer);
      retval = true;
    }
    return retval;
  }

  protected void handleImage(Image node, WRITER writer) throws XMLStreamException {
    QName name = new QName(getNamespace(), "img");
    handleImage(node, writer, name, node.getUrl().toString(), node.getText().toString());
  }

  protected abstract void handleImage(Image node, WRITER writer, QName name, String href, String alt)
      throws XMLStreamException;

  protected void handleTable(TableBlock node, WRITER writer) throws XMLStreamException {
    QName tableQName = new QName(getNamespace(), "table");
    handleBasicElementStart(node, writer, tableQName);

    TableHead head = (TableHead) node.getChildOfType(TableHead.class);

    if (head != null) {
      for (Node childNode : head.getChildren()) {
        if (childNode instanceof TableRow) {
          handleTableRow((TableRow) childNode, writer);
        }
      }
    }

    TableBody body = (TableBody) node.getChildOfType(TableBody.class);

    if (body != null) {
      for (Node childNode : body.getChildren()) {
        if (childNode instanceof TableRow) {
          handleTableRow((TableRow) childNode, writer);
        }
      }
    }

    handleBasicElementEnd(node, writer, tableQName);
  }

  private void handleTableRow(TableRow node, WRITER writer) throws XMLStreamException {
    QName trQName = new QName(getNamespace(), "tr");
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
      cellQName = new QName(getNamespace(), "th");
    } else {
      cellQName = new QName(getNamespace(), "td");
    }
    handleBasicElementStart(node, writer, cellQName);
    visitChildren(node, writer);
    handleBasicElementEnd(node, writer, cellQName);
  }

  protected void handleInsertAnchor(InsertAnchorNode node, WRITER writer) throws XMLStreamException {
    QName name = new QName(getNamespace(), "insert");
    handleInsertAnchor(node, writer, name);
  }

  protected abstract void handleInsertAnchor(InsertAnchorNode node, WRITER writer, QName name)
      throws XMLStreamException;

  protected void handleHeading(Heading node, WRITER writer) throws XMLStreamException {
    int level = node.getLevel();

    handleBasicElement(node, writer, String.format("h%d", level));
  }

  protected void handleEscapedCharacter(EscapedCharacter node, WRITER writer) throws XMLStreamException {
    writeText(writer, node.getChars().unescape());
  }

  protected void handleTypographicSmarts(TypographicQuotes node, WRITER writer) throws XMLStreamException {
    if (node.getTypographicOpening() != null && !node.getTypographicOpening().isEmpty()) {
      handleHtmlEntity(node.getTypographicOpening(), writer);
    }
    visitChildren(node, writer);
    if (node.getTypographicClosing() != null && !node.getTypographicClosing().isEmpty()) {
      handleHtmlEntity(node.getTypographicClosing(), writer);
    }
  }

  protected void handleTypographicSmarts(TypographicSmarts node, WRITER writer) throws XMLStreamException {
    handleHtmlEntity(node.getTypographicText(), writer);
  }

  protected void handleHtmlEntity(String entityText, WRITER writer) throws XMLStreamException {
    String replacement = ENTITY_MAP.get(entityText);
    if (replacement != null) {
      writeText(writer, replacement);
    } else {
      Matcher matcher = ENTITY_PATTERN.matcher(entityText);
      if (matcher.matches()) {
        writeHtmlEntity(writer, matcher.group(1));
      } else {
        writeText(writer, entityText);
      }
    }
  }

  protected void handleBasicElement(Node node, WRITER writer, String localName) throws XMLStreamException {
    QName name = new QName(getNamespace(), localName);
    handleBasicElementStart(node, writer, name);
    visitChildren(node, writer);
    handleBasicElementEnd(node, writer, name);
  }

  protected abstract void handleBasicElementStart(Node node, WRITER writer, QName name) throws XMLStreamException;

  protected abstract void handleBasicElementEnd(Node node, WRITER writer, QName name) throws XMLStreamException;

  protected void handleLink(LinkNode node, WRITER writer) throws XMLStreamException {
    QName name = new QName(getNamespace(), "a");
    handleLinkStart(node, writer, name, node.getUrl().toString());

    visitChildren(node, writer);

    handleLinkEnd(node, writer, name);
  }

  protected abstract void handleLinkStart(LinkNode node, WRITER writer, QName name, String string)
      throws XMLStreamException;

  protected abstract void handleLinkEnd(LinkNode node, WRITER writer, QName name) throws XMLStreamException;

  protected void handleSoftLineBreak(@SuppressWarnings("unused") SoftLineBreak node, WRITER writer)
      throws XMLStreamException {
    writeText(writer, " ");
  }

  protected abstract void handleHtmlBlock(HtmlBlock node, WRITER writer) throws XMLStreamException;

  protected abstract void handleHtmlInline(HtmlInline node, WRITER writer) throws XMLStreamException;
  // {
  // throw new UnsupportedOperationException(
  // String.format("Unable to process inline HTML characters: %s", node.getChars().toString()));
  // // String htmlText = node.getChars().toString();
  // //
  // // QName name = new QName(getNamespace(), "name");
  // // handleBasicElementStart(node, writer, name);
  // //
  // // if (node.hasChildren()) {
  // // visitChildren(node, writer);
  // // }
  // //
  // // handleBasicElementEnd(node, writer, name);
  // }
}
