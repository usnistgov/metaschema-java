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
import gov.nist.secauto.metaschema.core.metapath.IStringValued;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IAnyAtomicItem extends IAtomicValuedItem, IStringValued {
  @NonNull
  Set<Class<? extends IAnyAtomicItem>> PRIMITIVE_ITEM_TYPES = ObjectUtils.notNull(Set.of(
      IStringItem.class,
      IBooleanItem.class,
      IDecimalItem.class,
      IDurationItem.class,
      IDateTimeItem.class,
      IDateItem.class,
      IBase64BinaryItem.class,
      IAnyUriItem.class));

  @Override
  @NonNull
  default IAnyAtomicItem toAtomicItem() {
    return this;
  }

  /**
   * Get the "wrapped" value represented by this item.
   *
   * @return the value
   */
  @Override
  @NonNull
  Object getValue();

  @Override
  @NonNull
  String toString();

  /**
   * Get the item's string value.
   *
   * @return the string value value of the item
   */
  @Override
  @NonNull
  String asString();

  /**
   * Get a new {@link IStringItem} based on the the textual value of the item's
   * "wrapped" value.
   *
   * @return a new string item
   */
  @NonNull
  default IStringItem asStringItem() {
    return IStringItem.valueOf(asString());
  }

  /**
   * Get the item's type adapter.
   *
   * @return the type adapter for the item
   */
  @NonNull
  IDataTypeAdapter<?> getJavaTypeAdapter();
  //
  // <T extends IValuedItem> T cast(IValuedItem item);

  /**
   * Cast the provided {@code item} to be the same type as this item.
   *
   * @param item
   *          the item to cast
   * @return the result from casting
   */
  @NonNull
  IAnyAtomicItem castAsType(@NonNull IAnyAtomicItem item);

  /**
   * Compares this value with the argument. Ordering is item type dependent.
   *
   * @param other
   *          the item to compare with this value
   * @return a negative integer, zero, or a positive integer if this value is less
   *         than, equal to, or greater than the {@code item}.
   */
  int compareTo(@NonNull IAnyAtomicItem other);
}
