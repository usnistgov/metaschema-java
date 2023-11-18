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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;

import java.io.IOException;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IBoundFlagInstance
    extends IBoundJavaProperty,
    IFlagInstance,
    IFeatureNamedInstance,
    IFeatureScalarItemValueHandler {

  /**
   * Given a bound flag value, get that value as a {@link String}.
   *
   * @param value
   *          the bound flag value, which may be {@code null}
   * @return the bound flag value as a string, or {@code null} if the flag is not
   *         defined
   */
  // REFACTOR: remove if possible
  String getValueAsString(Object value);

  @Override
  default Object getValue(@NonNull Object parent) {
    return IBoundJavaProperty.super.getValue(parent);
  }

  @Override
  default void setValue(@NonNull Object parent, Object value) {
    IBoundJavaProperty.super.setValue(parent, value);
  }

  @Override
  default String getJsonName() {
    return IFeatureNamedInstance.super.getJsonName();
  }

  @Override
  default boolean canHandleJsonPropertyName(String name) {
    return name.equals(getJsonName());
  }

  @Override
  default boolean canHandleXmlQName(QName qname) {
    return qname.equals(getXmlQName());
  }

  @Override
  default Object readValue(Object parentInstance, IJsonParsingContext context) throws IOException {
    return readItem(parentInstance, context, null);
  }
}
