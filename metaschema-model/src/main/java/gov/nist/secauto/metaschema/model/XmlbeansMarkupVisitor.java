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

import gov.nist.secauto.metaschema.model.common.datatype.markup.AbstractMarkupWriter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupString;
import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupVisitor;
import gov.nist.secauto.metaschema.model.common.datatype.markup.IMarkupWriter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupVisitor;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlbeansMarkupVisitor
    extends AbstractMarkupWriter<XmlCursor, IllegalArgumentException> {

  @SuppressWarnings("resource")
  public static void visit(@NonNull IMarkupString<?> markup, @NonNull String namespace, @NonNull XmlObject obj) {
    try (XmlCursor cursor = ObjectUtils.notNull(obj.newCursor())) {
      visit(markup, namespace, cursor);
    }
  }

  public static void visit(@NonNull IMarkupString<?> markup, @NonNull String namespace, @NonNull XmlCursor cursor) {
    IMarkupWriter<XmlCursor, IllegalArgumentException> writer = new XmlbeansMarkupVisitor(
        namespace,
        cursor);
    IMarkupVisitor<XmlCursor, IllegalArgumentException> visitor = new MarkupVisitor<>(markup.isBlock());
    visitor.visitDocument(markup.getDocument(), writer);
  }

  protected XmlbeansMarkupVisitor(
      @NonNull String namespace,
      @NonNull XmlCursor writer) {
    super(namespace, writer);
  }

  @Override
  public void writeEmptyElement(QName qname, Map<String, String> attributes) throws IllegalArgumentException {
    XmlCursor cursor = getStream();
    cursor.beginElement(qname);
    
    attributes.forEach((name, value) -> cursor.insertAttributeWithValue(name, value));

    // go to the end of the new element
    cursor.toEndToken();

    // state advance past the end element
    cursor.toNextToken();
  }

  @Override
  public void writeElementStart(QName qname, Map<String, String> attributes) throws IllegalArgumentException {
    XmlCursor cursor = getStream();
    cursor.beginElement(qname);
    
    attributes.forEach((name, value) -> cursor.insertAttributeWithValue(name, value));

    // save the current location state
    cursor.push();
  }

  @Override
  public void writeElementEnd(QName qname) throws IllegalArgumentException {
    XmlCursor cursor = getStream();

    // restore location to end of start element
    cursor.pop();

    // go to the end of the new element
    cursor.toEndToken();

    // state advance past the end element
    cursor.toNextToken();
  }

  @Override
  public void writeText(String text) throws IllegalArgumentException {
    getStream().insertChars(text);
  }
}
