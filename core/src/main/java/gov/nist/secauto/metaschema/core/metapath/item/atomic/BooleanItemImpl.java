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

package gov.nist.secauto.metaschema.core.metapath.item.atomic;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapKey;

import edu.umd.cs.findbugs.annotations.NonNull;

class BooleanItemImpl implements IBooleanItem {
  @NonNull
  private static final String TRUE_STRING = "true";
  @NonNull
  private static final String FALSE_STRING = "false";
  @NonNull
  private static final IStringItem TRUE_STRING_ITEM = IStringItem.valueOf(TRUE_STRING);
  @NonNull
  private static final IStringItem FALSE_STRING_ITEM = IStringItem.valueOf(FALSE_STRING);

  private final boolean booleanValue;

  BooleanItemImpl(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  @Override
  public Boolean getValue() {
    return toBoolean();
  }

  @Override
  public boolean toBoolean() {
    return booleanValue;
  }

  @Override
  public String asString() {
    return toBoolean() ? TRUE_STRING : FALSE_STRING;
  }

  @Override
  public IStringItem asStringItem() {
    return toBoolean() ? TRUE_STRING_ITEM : FALSE_STRING_ITEM;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    return MetaschemaDataTypeProvider.BOOLEAN;
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public IMapKey asMapKey() {
    return new MapKey();
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(booleanValue);
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override
  public boolean equals(Object obj) {
    return this == obj
        || (obj instanceof IBooleanItem && compareTo((IBooleanItem) obj) == 0);
  }

  private final class MapKey implements IMapKey {
    @Override
    public IBooleanItem getKey() {
      return BooleanItemImpl.this;
    }

    @Override
    public int hashCode() {
      return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj ||
          (obj instanceof MapKey
              && getKey().equals(((MapKey) obj).getKey()));
    }
  }
}
