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

package gov.nist.secauto.metaschema.model;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeVisitor;

import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.util.ast.Node;

import edu.umd.cs.findbugs.annotations.NonNull;
import gov.nist.secauto.metaschema.model.common.datatype.markup.AbstractMarkupXmlVisitor;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

public class XmlbeansMarkupVisitor
    extends AbstractMarkupXmlVisitor<XmlCursor, IllegalArgumentException> {

  public static void visit(@NonNull MarkupLine markup, @NonNull String namespace, @NonNull XmlObject obj) {
    visit(markup, namespace, obj.newCursor());
  }

  public static void visit(@NonNull MarkupLine markup, @NonNull String namespace, @NonNull XmlCursor cursor) {
    new XmlbeansMarkupVisitor(namespace, false).visitDocument(markup.getDocument(), cursor);
  }

  public static void visit(@NonNull MarkupMultiline markup, @NonNull String namespace, @NonNull XmlObject obj) {
    visit(markup, namespace, obj.newCursor());
  }

  public static void visit(@NonNull MarkupMultiline markup, @NonNull String namespace, @NonNull XmlCursor cursor) {
    new XmlbeansMarkupVisitor(namespace, true).visitDocument(markup.getDocument(), cursor);
  }

  protected XmlbeansMarkupVisitor(@NonNull String namespace, boolean handleBlockElements) {
    super(namespace, handleBlockElements);
  }
  //
  // @Override
  // protected void visitText(Text node, XmlCursor state) throws IllegalArgumentException {
  // state.insertChars(node.getChars().toString());
  // }

  @Override
  protected void handleBasicElement(String localName, Node node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName(localName);
    state.beginElement(qname);
    state.push();

    visitChildren(node, state);

    state.pop();

    // go to the end of the new element
    state.toEndToken();

    // state advance past the end element
    state.toNextToken();
  }

  @Override
  protected void writeText(String text, XmlCursor state) throws IllegalArgumentException {
    state.insertChars(text);
  }

  @Override
  protected void writeHtmlEntity(String entityText, XmlCursor state) throws IllegalArgumentException {
    state.insertChars(entityText);
  }

  @Override
  protected void visitLink(LinkNode node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName("a");
    state.beginElement(qname);

    state.insertAttributeWithValue("href", ObjectUtils.requireNonNull(node.getUrl().toString()));

    visitElementChildren(node, state);
  }

  private void visitElementChildren(@NonNull Node node, XmlCursor state) {
    state.push();

    visitChildren(node, state);

    state.pop();

    // go to the end of the new element
    state.toEndToken();

    // state advance past the end element
    state.toNextToken();
  }

  @Override
  protected void visitImage(Image node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName("img");
    state.beginElement(qname);

    state.insertAttributeWithValue("src", ObjectUtils.requireNonNull(node.getUrl().toString()));

    if (node.getText() != null) {
      state.insertAttributeWithValue("alt", ObjectUtils.requireNonNull(node.getText().toString()));
    }

    visitElementChildren(node, state);
  }

  @Override
  protected void visitInsertAnchor(InsertAnchorNode node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName("insert");
    state.beginElement(qname);

    state.insertAttributeWithValue("type", ObjectUtils.requireNonNull(node.getType().toString()));
    state.insertAttributeWithValue("id-ref", ObjectUtils.requireNonNull(node.getIdReference().toString()));

    visitElementChildren(node, state);
  }

  @Override
  protected void visitHtmlInline(HtmlInline node, XmlCursor state) throws IllegalArgumentException {
    handleHtml(node, state);
  }

  private void handleHtml(Node node, XmlCursor state) {
    Document doc = Jsoup.parse(node.getChars().toString());
    try {
      doc.body().traverse(new XmlBeansNodeVisitor(state));
    } catch (InlineHtmlXmlException ex) {
      throw (IllegalArgumentException) ex.getCause();
    }
  }

  @Override
  protected void visitHtmlBlock(HtmlBlock node, XmlCursor state) throws IllegalArgumentException {
    handleHtml(node, state);
  }

  @Override
  protected void visitTable(@NonNull TableBlock node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName("table");
    state.beginElement(qname);

    // save the current location state
    state.push();

    super.visitTable(node, state);

    // get the saved location state
    state.pop();

    // go to the end of the new element
    state.toEndToken();

    // state advance past the end element
    state.toNextToken();
  }

  @Override
  protected void visitTableRow(@NonNull TableRow node, XmlCursor state) throws IllegalArgumentException {
    QName qname = newQName("tr");
    state.beginElement(qname);

    // save the current location state
    state.push();

    for (Node childNode : node.getChildren()) {
      if (childNode instanceof TableCell) {
        handleTableCell((TableCell) childNode, state);
      }
    }

    // get the saved location state
    state.pop();

    // go to the end of the new element
    state.toEndToken();

    // state advance past the end element
    state.toNextToken();
  }

  private void handleTableCell(TableCell node, XmlCursor state) throws IllegalArgumentException {
    QName qname;
    if (node.isHeader()) {
      qname = newQName("th");
    } else {
      qname = newQName("td");
    }

    state.beginElement(qname);

    // save the current location state
    state.push();

    visitChildren(node, state);

    // get the saved location state
    state.pop();

    // go to the end of the new element
    state.toEndToken();

    // state advance past the end element
    state.toNextToken();
  }

  private class XmlBeansNodeVisitor implements NodeVisitor {
    @NonNull
    private final XmlCursor cursor;

    public XmlBeansNodeVisitor(@NonNull XmlCursor cursor) {
      this.cursor = cursor;
    }

    @Override
    public void head(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0) {
        try {
          if (node instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
            // if (element.childNodes().isEmpty()) {
            // cursor.writeEmptyElement(getNamespace(), element.tagName());
            // } else {
            // writer.writeStartElement(getNamespace(), element.tagName());
            // }

            cursor.beginElement(newQName(element.tagName()));

            for (org.jsoup.nodes.Attribute attr : element.attributes()) {
              cursor.insertAttributeWithValue(attr.getKey(), attr.getValue());
            }

            if (!element.childNodes().isEmpty()) {
              // save the current location state
              cursor.push();
            }
          } else if (node instanceof org.jsoup.nodes.TextNode) {
            org.jsoup.nodes.TextNode text = (org.jsoup.nodes.TextNode) node;
            cursor.insertChars(text.text());
          }
        } catch (IllegalArgumentException ex) {
          throw new InlineHtmlXmlException(ex);
        }
      }
    }

    @Override
    public void tail(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0 && node instanceof org.jsoup.nodes.Element) {
        org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
        if (!element.childNodes().isEmpty()) {
          // get the saved location state
          cursor.pop();

          // go to the end of the new element
          cursor.toEndToken();

          // state advance past the end element
          cursor.toNextToken();
        }
      }
    }
  }

  private static class InlineHtmlXmlException
      extends IllegalStateException {

    /**
     * the serial version uid.
     */
    private static final long serialVersionUID = 1L;

    public InlineHtmlXmlException(Throwable cause) {
      super(cause);
    }
  }
}
