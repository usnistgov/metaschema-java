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
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.MailLink;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.TextBase;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.InsertAnchorExtension.InsertAnchorNode;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Supports the production of HTML content based on a markup syntax tree.
 * <p>
 * This implementation is stateless.
 *
 * @param <T>
 *          the type of stream to write to
 * @param <E>
 *          the type of exception that can be thrown when a writing error occurs
 */
@SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE")
public class MarkupVisitor<T, E extends Throwable> implements IMarkupVisitor<T, E> {
  private final boolean handleBlockElements;

  /**
   * Construct a new visitor.
   *
   * @param handleBlockElements
   *          {@code true} process block content or {@code false} process the tree
   *          as a single line of markup
   */
  public MarkupVisitor(boolean handleBlockElements) {
    this.handleBlockElements = handleBlockElements;
  }

  /**
   * Determine block processing state.
   *
   * @return {@code true} process block content or {@code false} process the tree
   *         as a single line of markup
   */
  protected boolean isHandleBlockElements() {
    return handleBlockElements;
  }

  @Override
  public void visitDocument(Document document, IMarkupWriter<T, E> writer) throws E {
    visitChildren(document, writer);
  }

  /**
   * Process child nodes of the provided parent.
   *
   * @param parent
   *          the node whose children are to be processed
   * @param writer
   *          the markup writer used to write the HTML
   * @throws E
   *           if an error occurred while writing
   */
  protected void visitChildren(@NonNull Node parent, @NonNull IMarkupWriter<T, E> writer) throws E {
    for (Node node : parent.getChildren()) {
      assert node != null;
      visit(node, writer);
    }
  }

  /**
   * Process a node.
   *
   * @param node
   *          the node to process
   * @param writer
   *          the markup writer used to write the HTML
   * @throws E
   *           if an error occurred while writing
   */
  protected void visit(@NonNull Node node, @NonNull IMarkupWriter<T, E> writer) throws E {
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

  /**
   * Process a node that represents an inline element.
   *
   * @param node
   *          the node to process
   * @param writer
   *          the markup writer used to write the HTML
   * @return {@code true} if the node was processed or {@code false} otherwise
   * @throws E
   *           if an error occurred while writing
   */
  @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", "PMD.NcssCount" })
  protected boolean processInlineElements(
      @NonNull Node node,
      @NonNull IMarkupWriter<T, E> writer) throws E { // NOPMD - acceptable
    boolean retval = true;
    if (node instanceof Text) {
      writer.writeText((Text) node);
    } else if (node instanceof TextBase) {
      writer.writeText((TextBase) node);
    } else if (node instanceof HtmlEntity) {
      writer.writeHtmlEntity((HtmlEntity) node);
    } else if (node instanceof TypographicSmarts) {
      writer.writeHtmlEntity((TypographicSmarts) node);
    } else if (node instanceof TypographicQuotes) {
      writer.writeTypographicQuotes((TypographicQuotes) node, this::visit);
    } else if (node instanceof Code) {
      writer.writeCode((Code) node, this::visit);
    } else if (node instanceof StrongEmphasis) {
      writer.writeElement("strong", node, this::visit);
    } else if (node instanceof Emphasis) {
      writer.writeElement("em", node, this::visit);
    } else if (node instanceof ListItem) {
      writer.writeElement("li", node, this::visit);
    } else if (node instanceof Link) {
      writer.writeLink((Link) node, this::visit);
    } else if (node instanceof AutoLink) {
      writer.writeLink((AutoLink) node);
    } else if (node instanceof MailLink) {
      writer.writeLink((MailLink) node);
    } else if (node instanceof Subscript) {
      writer.writeElement("sub", node, this::visit);
    } else if (node instanceof Superscript) {
      writer.writeElement("sup", node, this::visit);
    } else if (node instanceof Image) {
      writer.writeImage((Image) node);
    } else if (node instanceof InsertAnchorNode) {
      writer.writeInsertAnchor((InsertAnchorNode) node);
    } else if (node instanceof SoftLineBreak) {
      writer.writeText("\n");
    } else if (node instanceof HardLineBreak) {
      writer.writeBreak((HardLineBreak) node);
    } else if (node instanceof HtmlInline) {
      writer.writeInlineHtml((HtmlInline) node);
    } else if (node instanceof LinkRef || node instanceof Reference) {
      throw new UnsupportedOperationException(
          String.format(
              "Link references are not supported by Metaschema."
                  + " Perhaps you have an unescaped bracket in the following string? %s",
              ObjectUtils.notNull(node.getParent()).getChars()));
    } else {
      retval = false;
    }
    return retval;
  }

  /**
   * Process a node that represents a block element.
   *
   * @param node
   *          the node to process
   * @param writer
   *          the markup writer used to write the HTML
   * @return {@code true} if the node was processed or {@code false} otherwise
   * @throws E
   *           if an error occurred while writing
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  protected boolean processBlockElements(
      @NonNull Node node,
      @NonNull IMarkupWriter<T, E> writer) throws E {
    boolean retval = true;
    if (node instanceof Paragraph) {
      writer.writeParagraph((Paragraph) node, this::visit);
    } else if (node instanceof Heading) {
      writer.writeHeading((Heading) node, this::visit);
    } else if (node instanceof OrderedList) {
      writer.writeList("ol", (OrderedList) node, this::visit);
    } else if (node instanceof BulletList) {
      writer.writeList("ul", (BulletList) node, this::visit);
    } else if (node instanceof TableBlock) {
      writer.writeTable((TableBlock) node, this::visit);
    } else if (node instanceof HtmlBlock) {
      writer.writeBlockHtml((HtmlBlock) node);
    } else if (node instanceof HtmlCommentBlock) {
      writer.writeComment((HtmlCommentBlock) node);
    } else if (node instanceof IndentedCodeBlock) {
      writer.writeCodeBlock((IndentedCodeBlock) node, this::visit);
    } else if (node instanceof FencedCodeBlock) {
      writer.writeCodeBlock((FencedCodeBlock) node, this::visit);
    } else if (node instanceof CodeBlock) {
      writer.writeCodeBlock((CodeBlock) node, this::visit);
    } else if (node instanceof BlockQuote) {
      writer.writeBlockQuote((BlockQuote) node, this::visit);
    } else if (node instanceof ThematicBreak) {
      writer.writeBreak((ThematicBreak) node);
    } else {
      retval = false;
    }

    // if (retval && node.getNextAny(Block.class) != null) {
    // writer.writeText("\n");
    // }
    return retval;
  }
}
