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

import gov.nist.secauto.metaschema.core.model.IResourceLocation;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides location information for an XMLBeans object.
 */
public final class XmlBeansLocation implements IResourceLocation {
  @NonNull
  private final XmlLineNumber lineNumber;

  /**
   * Get the location information for an XMLBeans object if the location is
   * available.
   *
   * @param xmlObject
   *          the XMLBeans object to get the location information for
   * @return the location information or {@code null} if the location information
   *         is unavailable
   */
  @Nullable
  public static IResourceLocation toLocation(@NonNull XmlObject xmlObject) {
    try (XmlCursor cursor = xmlObject.newCursor()) {
      XmlBookmark bookmark = cursor.getBookmark(XmlLineNumber.class);
      return bookmark == null ? null : new XmlBeansLocation((XmlLineNumber) bookmark);
    }
  }

  private XmlBeansLocation(@NonNull XmlLineNumber lineNumber) {
    this.lineNumber = lineNumber;
  }

  @Override
  public int getLine() {
    return lineNumber.getLine();
  }

  @Override
  public int getColumn() {
    return lineNumber.getColumn();
  }

  @Override
  public long getCharOffset() {
    return lineNumber.getOffset();
  }

  @Override
  public long getByteOffset() {
    // not supported
    return -1;
  }
}
