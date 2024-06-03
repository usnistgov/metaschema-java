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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import com.vladsch.flexmark.parser.ListOptions;

import gov.nist.secauto.metaschema.core.datatype.markup.IMarkupString;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.IMarkupVisitor;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.IMarkupWriter;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.MarkupVisitor;
import gov.nist.secauto.metaschema.core.datatype.markup.flexmark.impl.AbstractMarkupWriter;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures") // intended
public class XmlbeansMarkupVisitor // TODO: rename
    extends AbstractMarkupWriter<XmlCursor, IllegalArgumentException> {

  /**
   * Write the provided markup to the provided object.
   *
   * @param markup
   *          the markup to write
   * @param namespace
   *          the XML namespace to use for markup elements
   * @param obj
   *          the XML beans object to write to
   */
  @SuppressWarnings("resource")
  public static void visit(@NonNull IMarkupString<?> markup, @NonNull String namespace,
      @NonNull XmlObject obj) {
    try (XmlCursor cursor = ObjectUtils.notNull(obj.newCursor())) {
      visit(markup, namespace, cursor);
    }
  }

  /**
   * Write the provided markup to the provided object.
   *
   * @param markup
   *          the markup to write
   * @param namespace
   *          the XML namespace to use for markup elements
   * @param cursor
   *          the XML beans cursor to write to
   */
  public static void visit(@NonNull IMarkupString<?> markup, @NonNull String namespace,
      @NonNull XmlCursor cursor) {
    IMarkupWriter<XmlCursor, IllegalArgumentException> writer = new XmlbeansMarkupVisitor(
        namespace,
        markup.getFlexmarkFactory().getListOptions(),
        cursor);
    IMarkupVisitor<XmlCursor, IllegalArgumentException> visitor = new MarkupVisitor<>(markup.isBlock());
    visitor.visitDocument(markup.getDocument(), writer);
  }

  /**
   * Construct a new XML beans markup visitor used for writing XML.
   *
   * @param namespace
   *          the XML namespace to use for markup elements
   * @param options
   *          Flexmark-based formatting options to control output formatting
   * @param writer
   *          the XML beans cursor to write to
   */
  protected XmlbeansMarkupVisitor(
      @NonNull String namespace,
      @NonNull ListOptions options,
      @NonNull XmlCursor writer) {
    super(namespace, options, writer);
  }

  @Override
  public void writeEmptyElement(QName qname, Map<String, String> attributes)
      throws IllegalArgumentException {
    @SuppressWarnings({ "resource", "PMD.CloseResource" }) // not owned
    XmlCursor cursor = getStream();
    cursor.beginElement(qname);

    attributes.forEach(cursor::insertAttributeWithValue);

    // go to the end of the new element
    cursor.toEndToken();

    // state advance past the end element
    cursor.toNextToken();
  }

  @Override
  public void writeElementStart(QName qname, Map<String, String> attributes)
      throws IllegalArgumentException {
    @SuppressWarnings({ "resource", "PMD.CloseResource" }) // not owned
    XmlCursor cursor = getStream();
    cursor.beginElement(qname);

    attributes.forEach(cursor::insertAttributeWithValue);

    // save the current location state
    cursor.push();
  }

  @Override
  public void writeElementEnd(QName qname) throws IllegalArgumentException {
    @SuppressWarnings({ "resource", "PMD.CloseResource" }) // not owned
    XmlCursor cursor = getStream();

    // restore location to end of start element
    cursor.pop();

    // go to the end of the new element
    cursor.toEndToken();

    // state advance past the end element
    cursor.toNextToken();
  }

  @Override
  public void writeText(CharSequence text) throws IllegalArgumentException {
    @SuppressWarnings({ "resource", "PMD.CloseResource" }) // not owned
    XmlCursor cursor = getStream();
    cursor.insertChars(text.toString());
  }

  @Override
  protected void writeComment(CharSequence text) throws IllegalArgumentException {
    @SuppressWarnings({ "resource", "PMD.CloseResource" }) // not owned
    XmlCursor cursor = getStream();
    cursor.insertComment(text.toString());
  }
}
