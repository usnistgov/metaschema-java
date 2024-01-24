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

import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IBoundInstanceModelNamed extends IBoundInstanceModel, INamedModelInstanceAbsolute {

  @Override
  @NonNull
  IBoundDefinitionModel getDefinition();

  @Override
  default String getName() {
    // delegate to the definition
    return getDefinition().getName();
  }

  @Override
  default Integer getIndex() {
    // delegate to the definition
    return getDefinition().getIndex();
  }

  @Override
  default String getJsonName() {
    return INamedModelInstanceAbsolute.super.getJsonName();
  }

  @Nullable
  default IBoundInstanceFlag getJsonKey() {
    String jsonKeyName = getJsonKeyFlagName();
    return JsonGroupAsBehavior.KEYED.equals(getJsonGroupAsBehavior())
        ? ObjectUtils.requireNonNull(getDefinition().getFlagInstanceByName(
            ObjectUtils.requireNonNull(jsonKeyName)))
        : null;
  }

  @Override
  default IBoundInstanceFlag getItemJsonKey(Object item) {
    return getJsonKey();
  }

  @Override
  default Collection<? extends Object> getItemValues(Object value) {
    return getCollectionInfo().getItemsFromValue(value);
  }

  @Override
  default boolean canHandleXmlQName(QName qname) {
    return qname.equals(getXmlQName());
  }

  @Override
  default boolean canHandleJsonPropertyName(@NonNull String name) {
    return name.equals(getJsonName());
  }
}
