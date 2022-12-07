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

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlEntity;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.LinkNode;
import com.vladsch.flexmark.ast.ListItem;
import com.vladsch.flexmark.ast.OrderedList;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.SoftLineBreak;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.escaped.character.EscapedCharacter;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.typographic.TypographicSmarts;
import com.vladsch.flexmark.util.ast.Node;

import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.DoubleQuoteNode;
import gov.nist.secauto.metaschema.model.common.datatype.markup.flexmark.InsertAnchorNode;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_THROWABLE")
public abstract class AbstractMarkupXmlVisitor<T, E extends Throwable>
    extends AbstractMarkupVisitor<T, E> {
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

  public AbstractMarkupXmlVisitor(@NonNull String namespace, boolean handleBlockElements) {
    super(handleBlockElements);
    this.namespace = namespace;
  }

  @NonNull
  protected String getNamespace() {
    return namespace;
  }

  protected abstract void writeText(@NonNull String text, T state) throws E;

  protected abstract void writeHtmlEntity(@NonNull String entityText, T state) throws E;

  protected void handleHtmlEntity(@NonNull String entityText, T state) throws E {
    String replacement = ENTITY_MAP.get(entityText);
    if (replacement != null) {
      writeText(replacement, state);
    } else {
      Matcher matcher = ENTITY_PATTERN.matcher(entityText);
      if (matcher.matches()) {
        writeHtmlEntity(ObjectUtils.notNull(matcher.group(1)), state);
      } else {
        writeText(entityText, state);
      }
    }
  }

  @NonNull
  protected QName newQName(@NonNull String localName) {
    return new QName(getNamespace(), localName);
  }

  protected abstract void handleBasicElement(@NonNull String localName, @NonNull Node node, T state)
      throws E;

  @Override
  protected void visitText(@NonNull Text node, T state) throws E {
    String text = node.getChars().toString();
    assert text != null;
    writeText(text, state);
  }

  @Override
  protected void visitEscapedCharacter(EscapedCharacter node, T state) throws E {
    writeText(node.getChars().unescape(), state);
  }

  @Override
  protected void visitHtmlEntity(HtmlEntity node, T state) throws E {
    String text = node.getChars().toString();
    assert text != null;
    handleHtmlEntity(text, state);
  }

  @Override
  protected void visitTypographicSmarts(TypographicSmarts node, T state) throws E {
    handleHtmlEntity(ObjectUtils.requireNonNull(node.getTypographicText()), state);
  }

  @Override
  protected void visitTypographicQuotes(TypographicQuotes node, T state) throws E {
    if (node instanceof DoubleQuoteNode) {
      handleBasicElement("q", node, state);
    } else {
      String opening = node.getTypographicOpening();
      if (opening != null && !opening.isEmpty()) {
        handleHtmlEntity(opening, state);
      }

      visitChildren(node, state);

      String closing = node.getTypographicClosing();
      if (closing != null && !closing.isEmpty()) {
        handleHtmlEntity(closing, state);
      }
    }
  }

  @Override
  protected void visitCode(@NonNull Code node, T state) throws E {
    handleBasicElement("code", node, state);
  }

  @Override
  protected void visitStrong(StrongEmphasis node, T state) throws E {
    handleBasicElement("strong", node, state);
  }

  @Override
  protected void visitEmphasis(Emphasis node, T state) throws E {
    handleBasicElement("em", node, state);
  }

  @Override
  protected void visitListItem(ListItem node, T state) throws E {
    handleBasicElement("li", node, state);
  }

  @Override
  protected abstract void visitLink(LinkNode node, T state) throws E;

  @Override
  protected void visitSubscript(Subscript node, T state) throws E {
    handleBasicElement("sub", node, state);
  }

  @Override
  protected void visitSuperscript(Superscript node, T state) throws E {
    handleBasicElement("sup", node, state);
  }

  @Override
  protected abstract void visitImage(@NonNull Image node, T state) throws E;

  @Override
  protected abstract void visitInsertAnchor(@NonNull InsertAnchorNode node, T state) throws E;

  @Override
  protected void visitSoftLineBreak(SoftLineBreak node, T state)
      throws E {
    writeText(" ", state);
  }

  @Override
  protected void visitParagraph(@NonNull Paragraph node, T state) throws E {
    handleBasicElement("p", node, state);
  }

  @Override
  protected void visitHeading(@NonNull Heading node, T state) throws E {
    int level = node.getLevel();

    handleBasicElement(ObjectUtils.notNull(String.format("h%d", level)), node, state);
  }

  @Override
  protected void visitOrderedList(@NonNull OrderedList node, T state) throws E {
    handleBasicElement("ol", node, state);
  }

  @Override
  protected void visitBulletList(@NonNull BulletList node, T state) throws E {
    handleBasicElement("ul", node, state);
  }
}