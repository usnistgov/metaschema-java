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
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeVisitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used to write HTML-based Markup to various types of streams.
 * 
 * @param <T>
 *          the type of stream to write to
 * @param <E>
 *          the type of exception that can be thrown when a writing error occurs
 */
public abstract class AbstractMarkupWriter<T, E extends Throwable> implements IMarkupWriter<T, E> {
  private static final Pattern ENTITY_PATTERN = Pattern.compile("^&([^;]+);$");
  private static final Map<String, String> ENTITY_MAP;

  static {
    ENTITY_MAP = new HashMap<>();
    ENTITY_MAP.put("&npsb;", "&npsb;");
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

  @NonNull
  private final T stream;

  public AbstractMarkupWriter(@NonNull String namespace, T stream) {
    this.namespace = namespace;
    this.stream = ObjectUtils.requireNonNull(stream);
  }

  @NonNull
  protected String getNamespace() {
    return namespace;
  }

  @NonNull
  public T getStream() {
    return stream;
  }

  @NonNull
  protected QName newQName(@NonNull String localName) {
    return new QName(getNamespace(), localName);
  }

  @Override
  public final void writeElement(
      String localName,
      Node node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    writeElement(localName, node, CollectionUtil.emptyMap(), childHandler);
  }

  @Override
  public final void writeElement(
      String localName,
      Node node,
      Map<String, String> attributes,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    QName qname = newQName(localName);
    writeElement(qname, node, attributes, childHandler);
  }

  @Override
  public final void writeElement(
      QName qname,
      Node node,
      Map<String, String> attributes,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    if (node.hasChildren()) {
      writeElementStart(qname, attributes);
      if (childHandler != null) {
        childHandler.accept(node, this);
      }
      writeElementEnd(qname);
    } else {
      writeEmptyElement(qname, attributes);
    }
  }

  @Override
  public void writeEmptyElement(
      String localName,
      Map<String, String> attributes) throws E {
    QName qname = newQName(localName);
    writeEmptyElement(qname, attributes);
  }

  @Override
  public abstract void writeEmptyElement(
      QName qname,
      Map<String, String> attributes) throws E;

  @Override
  public final void writeElementStart(
      QName qname) throws E {
    writeElementStart(qname, CollectionUtil.emptyMap());
  }

  @Override
  public abstract void writeElementStart(
      QName qname,
      Map<String, String> attributes) throws E;

  @Override
  public abstract void writeElementEnd(QName qname) throws E;
  
  @Override
  public abstract void writeText(String text) throws E;

  @Override
  public final void writeHtmlEntity(String entityText) throws E {
    String replacement = ENTITY_MAP.get(entityText);
    if (replacement != null) {
      writeText(replacement);
    } else {
      String value = StringEscapeUtils.unescapeHtml4(entityText);
      writeText(value);
//      Matcher matcher = ENTITY_PATTERN.matcher(entityText);
//      if (matcher.matches()) {
//        writeHtmlEntity(ObjectUtils.notNull(matcher.group(1)));
//      } else {
//        writeHtmlEntityInternal(entityText);
//      }
    }
  }
  
  protected void writeHtmlEntityInternal(@NonNull String text) throws E {
    writeText(text);
  }

  @SuppressWarnings("unchecked")
  private void writeHtml(Node node) throws E {
    Document doc = Jsoup.parse(node.getChars().toString());
    try {
      doc.body().traverse(new MarkupNodeVisitor());
    } catch (NodeVisitorException ex) {
      throw (E) ex.getCause();
    }
  }
  
  // ===============
  // Utility Methods
  // ===============
  
  @Override
  public final void writeLink(
      LinkNode node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    Map<String, String> attributes = new LinkedHashMap<>();
    String href = ObjectUtils.requireNonNull(node.getUrl().toString());
    try {
      attributes.put("href", new URI(href).toASCIIString());
    } catch (URISyntaxException ex) {
      throw new IllegalStateException(ex);
    }

    if (node.getTitle() != null) {
      attributes.put("title", ObjectUtils.requireNonNull(node.getTitle().toString()));
    }

    writeElement("a", node, attributes, childHandler);
  }

  @Override
  public final void writeText(Text node) throws E {
    String text = node.getChars().toString();
    assert text != null;
    writeText(text);
  }

  @Override
  public final void writeText(EscapedCharacter node) throws E {
    String text = node.getChars().unescape();
    assert text != null;
    writeText(text);
  }
  
  @Override
  public final void writeHtmlEntity(HtmlEntity node) throws E {
    String text = node.getChars().toString();
    assert text != null;
    writeHtmlEntity(text);
  }
  
  @Override
  public final void writeHtmlEntity(TypographicSmarts node) throws E {
    String text = ObjectUtils.requireNonNull(node.getTypographicText());
    assert text != null;
    writeHtmlEntity(text);
  }
  
  @Override
  public final void writeTypographicQuotes(
      TypographicQuotes node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    if (node instanceof DoubleQuoteNode) {
      writeElement("q", node, childHandler);
    } else {
      String opening = node.getTypographicOpening();
      if (opening != null && !opening.isEmpty()) {
        writeHtmlEntity(opening);
      }

      childHandler.accept(node, this);

      String closing = node.getTypographicClosing();
      if (closing != null && !closing.isEmpty()) {
        writeHtmlEntity(closing);
      }
    }
  }
  
  @Override
  public final void writeInlineHtml(HtmlInline node) throws E {
    writeHtml(node);
  }

  @Override
  public final void writeBlockHtml(HtmlBlock node) throws E {
    writeHtml(node);
  }


  @Override
  public final void writeTable(
      TableBlock node,
      Handler<T, E, AbstractMarkupWriter<T, E>> cellChildHandler) throws E {
    QName qname = newQName("table");
    writeElementStart(qname);


    TableHead head = (TableHead) node.getChildOfType(TableHead.class);

    if (head != null) {
      for (Node childNode : head.getChildren()) {
        if (childNode instanceof TableRow) {
          writeTableRow((TableRow) childNode, cellChildHandler);
        }
      }
    }

    TableBody body = (TableBody) node.getChildOfType(TableBody.class);

    if (body != null) {
      for (Node childNode : body.getChildren()) {
        if (childNode instanceof TableRow) {
          writeTableRow((TableRow) childNode, cellChildHandler);
        }
      }
    }

    writeElementEnd(qname);
  }

  private void writeTableRow(
      @NonNull TableRow node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> cellChildHandler) throws E {
    QName qname = newQName("tr");
    writeElementStart(qname);

    for (Node childNode : node.getChildren()) {
      if (childNode instanceof TableCell) {
        writeTableCell((TableCell) childNode, cellChildHandler);
      }
    }

    writeElementEnd(qname);
  }

  private void writeTableCell(
      @NonNull TableCell node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> cellChildHandler) throws E {
    QName qname = node.isHeader() ? newQName("th") : newQName("td");

    Map<String, String> attributes = new LinkedHashMap<>();
    if (node.getAlignment() != null) {
      attributes.put("align", ObjectUtils.requireNonNull(node.getAlignment().toString()));
    }

    writeElementStart(qname, attributes);
    cellChildHandler.accept(node, this);
    writeElementEnd(qname);
  }

  @Override
  public final void writeImage(
      Image node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    Map<String, String> attributes = new LinkedHashMap<>();
    String href = ObjectUtils.requireNonNull(node.getUrl().toString());
    try {
      attributes.put("src", new URI(href).toASCIIString());
    } catch (URISyntaxException ex) {
      throw new IllegalStateException(ex);
    }

    if (node.getText() != null) {
      attributes.put("alt", ObjectUtils.requireNonNull(node.getText().toString()));
    }

    if (node.getTitle() != null) {
      attributes.put("title", ObjectUtils.requireNonNull(node.getTitle().toString()));
    }

    writeEmptyElement("img", attributes);
  }

  @Override
  public final void writeInsertAnchor(InsertAnchorNode node) throws E  {
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("type", ObjectUtils.requireNonNull(node.getType().toString()));
    attributes.put("id-ref", ObjectUtils.requireNonNull(node.getIdReference().toString()));

    writeElement("insert", node, attributes, null);
  }
  
  @Override
  public final void writeHeading(
      Heading node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    int level = node.getLevel();

    writeElement(ObjectUtils.notNull(String.format("h%d", level)), node, childHandler);
  }

  @Override
  public final void writeCodeBlock(
      IndentedCodeBlock node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    writeIndentedOrFencedCodeBlock(node, childHandler);
  }

  @Override
  public final void writeCodeBlock(
      FencedCodeBlock node,
      Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    writeIndentedOrFencedCodeBlock(node, childHandler);
  }
  
  private void writeIndentedOrFencedCodeBlock(
      @NonNull Block node,
      @NonNull Handler<T, E, AbstractMarkupWriter<T, E>> childHandler) throws E {
    QName preQName = newQName("pre");
    
    writeElementStart(preQName);

    QName codeQName = newQName("code");

    writeElementStart(codeQName);

    childHandler.accept(node, this);

    writeElementEnd(codeQName);

    writeElementEnd(preQName);
  }

  // =================================
  // Supporting interfaces and classes
  // =================================

  /**
   * Provides a callback to handle node children.
   * 
   * @param <T>
   *          the type of stream to write to
   * @param <E>
   *          the type of exception that can be thrown when a writing error occurs
   * @param <U>
   *          the writer
   */
  @FunctionalInterface
  public interface Handler<T, E extends Throwable, U extends AbstractMarkupWriter<T, E>> {
    void accept(@NonNull Node node, U writer) throws E;
  }
  
  protected static class NodeVisitorException extends IllegalStateException {
    /**
     * the serial version uid.
     */
    private static final long serialVersionUID = 1L;

    public NodeVisitorException(Throwable cause) {
      super(cause);
    }
  }
  
  private final class MarkupNodeVisitor implements NodeVisitor {
    @Override
    public void head(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0) {
        try {
          if (node instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;

            Attributes attributes = element.attributes();

            Map<String, String> attrMap;
            if (attributes.isEmpty()) {
              attrMap = CollectionUtil.emptyMap();
            } else {
              attrMap = new LinkedHashMap<>();
              for (org.jsoup.nodes.Attribute attr : attributes) {
                attrMap.put(attr.getKey(), attr.getValue());
              }
            }

            QName qname = newQName(ObjectUtils.notNull(element.tagName()));
            if (element.childNodes().isEmpty()) {
              writeEmptyElement(qname, attrMap);
            } else {
              writeElementStart(qname, attrMap);
            }
          } else if (node instanceof org.jsoup.nodes.TextNode) {
            org.jsoup.nodes.TextNode text = (org.jsoup.nodes.TextNode) node;
            writeText(ObjectUtils.requireNonNull(text.text()));
          }
        } catch (Throwable ex) {
          throw new NodeVisitorException(ex);
        }
      }
    }

    @Override
    public void tail(org.jsoup.nodes.Node node, int depth) {
      if (depth > 0 && node instanceof org.jsoup.nodes.Element) {
        org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
        if (!element.childNodes().isEmpty()) {
          QName qname = newQName(ObjectUtils.notNull(element.tagName()));
          try {
            writeElementEnd(qname);
          } catch (Throwable ex) {
            throw new NodeVisitorException(ex);
          }
        }
      }
    }
  }
}
