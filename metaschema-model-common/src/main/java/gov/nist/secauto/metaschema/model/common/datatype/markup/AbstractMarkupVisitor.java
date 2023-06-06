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
import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.ast.Emphasis;
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
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE")
public abstract class AbstractMarkupVisitor<T, E extends Throwable> implements IMarkupVisitor<T, E> {
  private final boolean handleBlockElements;

  protected AbstractMarkupVisitor(boolean handleBlockElements) {
    this.handleBlockElements = handleBlockElements;
  }

  @Override
  public void visitDocument(Document document, T state) throws E {
    visitChildren(document, state);
  }

  protected boolean isHandleBlockElements() {
    return handleBlockElements;
  }

  public void visitChildren(@NonNull Node parentNode, T state) throws E {
    for (Node node : parentNode.getChildren()) {
      assert node != null;
      visit(node, state);
    }
  }

  protected void visit(@NonNull Node node, T state) throws E {
    boolean handled = processInlineElements(node, state);
    if (!handled && node instanceof Block) {
      if (isHandleBlockElements()) {
        handled = processBlockElements(node, state);
      } else {
        visitChildren(node, state);
        handled = true;
      }
    }

    if (!handled) {
      throw new UnsupportedOperationException(
          String.format("Node '%s' not handled. AST: %s", node.getNodeName(), node.toAstString(true)));
    }
  }

  protected boolean processInlineElements(@NonNull Node node, T state) throws E { // NOPMD - acceptable
    boolean retval = true;
    if (node instanceof Text) {
      visitText((Text) node, state);
    } else if (node instanceof EscapedCharacter) {
      visitEscapedCharacter((EscapedCharacter) node, state);
    } else if (node instanceof HtmlEntity) {
      visitHtmlEntity((HtmlEntity) node, state);
    } else if (node instanceof TypographicSmarts) {
      visitTypographicSmarts((TypographicSmarts) node, state);
    } else if (node instanceof TypographicQuotes) {
      visitTypographicQuotes((TypographicQuotes) node, state);
    } else if (node instanceof Code) {
      visitCode((Code) node, state);
    } else if (node instanceof StrongEmphasis) {
      visitStrong((StrongEmphasis) node, state);
    } else if (node instanceof Emphasis) {
      visitEmphasis((Emphasis) node, state);
    } else if (node instanceof ListItem) {
      visitListItem((ListItem) node, state);
    } else if (node instanceof Link || node instanceof AutoLink) {
      visitLink((LinkNode) node, state);
    } else if (node instanceof TextBase) {
      // ignore these, but process their children
      visitChildren(node, state);
    } else if (node instanceof Subscript) {
      visitSubscript((Subscript) node, state);
    } else if (node instanceof Superscript) {
      visitSuperscript((Superscript) node, state);
    } else if (node instanceof Image) {
      visitImage((Image) node, state);
    } else if (node instanceof InsertAnchorNode) {
      visitInsertAnchor((InsertAnchorNode) node, state);
    } else if (node instanceof SoftLineBreak) {
      visitSoftLineBreak((SoftLineBreak) node, state);
    } else if (node instanceof HardLineBreak) {
      visitHardLineBreak((HardLineBreak) node, state);
    } else if (node instanceof HtmlInline) {
      visitHtmlInline((HtmlInline) node, state);
    } else {
      retval = false;
    }
    return retval;
  }

  protected abstract void visitText(@NonNull Text node, T state) throws E;

  protected abstract void visitEscapedCharacter(@NonNull EscapedCharacter node, T state) throws E;

  protected abstract void visitHtmlEntity(@NonNull HtmlEntity node, T state) throws E;

  protected abstract void visitTypographicSmarts(@NonNull TypographicSmarts node, T state) throws E;

  protected abstract void visitTypographicQuotes(@NonNull TypographicQuotes node, T state) throws E;

  protected abstract void visitCode(@NonNull Code node, T state) throws E;

  protected abstract void visitStrong(@NonNull StrongEmphasis node, T state) throws E;

  protected abstract void visitEmphasis(@NonNull Emphasis node, T state) throws E;

  protected abstract void visitListItem(@NonNull ListItem node, T state) throws E;

  protected abstract void visitLink(@NonNull LinkNode node, T state) throws E;

  protected abstract void visitSubscript(@NonNull Subscript node, T state) throws E;

  protected abstract void visitSuperscript(@NonNull Superscript node, T state) throws E;

  protected abstract void visitImage(@NonNull Image node, T state) throws E;

  protected abstract void visitInsertAnchor(@NonNull InsertAnchorNode node, T state) throws E;

  protected abstract void visitSoftLineBreak(@NonNull SoftLineBreak node, T state) throws E;

  protected abstract void visitHardLineBreak(@NonNull HardLineBreak node, T state) throws E;

  protected abstract void visitHtmlInline(@NonNull HtmlInline node, T state) throws E;

  protected boolean processBlockElements(@NonNull Node node, T state) throws E {
    boolean retval = true;
    if (node instanceof Paragraph) {
      visitParagraph((Paragraph) node, state);
    } else if (node instanceof Heading) {
      visitHeading((Heading) node, state);
    } else if (node instanceof OrderedList) {
      visitOrderedList((OrderedList) node, state);
    } else if (node instanceof BulletList) {
      visitBulletList((BulletList) node, state);
    } else if (node instanceof TableBlock) {
      visitTable((TableBlock) node, state);
    } else if (node instanceof HtmlBlock) {
      visitHtmlBlock((HtmlBlock) node, state);
    } else if (node instanceof HtmlCommentBlock) {
      // ignore
    } else if (node instanceof IndentedCodeBlock) {
      visitIndentedOrFencedCodeBlock((IndentedCodeBlock) node, state);
    } else if (node instanceof FencedCodeBlock) {
      visitIndentedOrFencedCodeBlock((FencedCodeBlock) node, state);
    } else if (node instanceof CodeBlock) {
      visitCodeBlock((CodeBlock) node, state);
    } else {
      retval = false;
    }
    return retval;
  }

  protected abstract void visitParagraph(@NonNull Paragraph node, T state) throws E;

  protected abstract void visitHeading(@NonNull Heading node, T state) throws E;

  protected abstract void visitOrderedList(@NonNull OrderedList node, T state) throws E;

  protected abstract void visitBulletList(@NonNull BulletList node, T state) throws E;

  protected void visitTable(@NonNull TableBlock node, T state) throws E {

    TableHead head = (TableHead) node.getChildOfType(TableHead.class);

    if (head != null) {
      visitTableHead(head, state);
    }

    TableBody body = (TableBody) node.getChildOfType(TableBody.class);

    if (body != null) {
      visitTableBody(body, state);
    }
  }

  protected void visitTableHead(@NonNull TableHead head, T state) throws E {
    for (Node childNode : head.getChildren()) {
      if (childNode instanceof TableRow) {
        visitTableRow((TableRow) childNode, state);
      }
    }
  }

  protected void visitTableBody(@NonNull TableBody body, T state) throws E {
    for (Node childNode : body.getChildren()) {
      if (childNode instanceof TableRow) {
        visitTableRow((TableRow) childNode, state);
      }
    }
  }

  protected abstract void visitTableRow(@NonNull TableRow row, T state) throws E;

  protected abstract void visitHtmlBlock(@NonNull HtmlBlock node, T state) throws E;

  protected abstract void visitIndentedOrFencedCodeBlock(@NonNull Block node, T state) throws E;

  private void visitCodeBlock(CodeBlock node, T state) throws E {
    visitChildren(node, state);
  }
}
