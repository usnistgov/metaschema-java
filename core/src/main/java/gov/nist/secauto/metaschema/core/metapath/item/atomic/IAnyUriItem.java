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

import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidValueForCastFunctionException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IAnyUriItem extends IAnyAtomicItem {
  /**
   * Construct a new URI item using the provided string {@code value}.
   *
   * @param value
   *          a string representing a URI
   * @return the new item
   * @throws IllegalArgumentException
   *           if the given string violates RFC2396
   */
  @NonNull
  static IAnyUriItem valueOf(@NonNull String value) {
    return valueOf(ObjectUtils.notNull(URI.create(value)));
  }

  /**
   * Construct a new URI item using the provided URI {@code value}.
   *
   * @param value
   *          a URI
   * @return the new item as a {@link IAnyUriItem} if the URI is absolute or
   *         opaque, otherwise as an {@link IUriReferenceItem}
   */
  @NonNull
  static IAnyUriItem valueOf(@NonNull URI value) {
    IAnyUriItem retval;
    if (value.isAbsolute() || value.isOpaque()) {
      retval = new AnyUriItemImpl(value);
    } else {
      retval = IUriReferenceItem.valueOf(value);
    }
    return retval;
  }

  /**
   * Cast the provided type to this item type.
   *
   * @param item
   *          the item to cast
   * @return the original item if it is already this type, otherwise a new item
   *         cast to this type
   * @throws InvalidValueForCastFunctionException
   *           if the provided {@code item} cannot be cast to this type
   */
  @NonNull
  static IAnyUriItem cast(@NonNull IAnyAtomicItem item) {
    return MetaschemaDataTypeProvider.URI.cast(item);
  }

  /**
   * Get the "wrapped" URI value.
   *
   * @return the underlying URI value
   */
  @NonNull
  URI asUri();

  /**
   * Determines if this URI has a scheme component, making it absolute.
   *
   * @return {@code true} if the URI is absolute, or {@code false} otherwise
   */
  default boolean isAbsolute() {
    return asUri().isAbsolute();
  }

  /**
   * Determines if this URI is opaque.
   *
   * @return {@code true} if the URI is opaque, or {@code false} otherwise
   * @see URI#isOpaque()
   */
  default boolean isOpaque() {
    return asUri().isOpaque();
  }

  /**
   * Resolve the provided URI against this URI.
   *
   * @param other
   *          the URI to resolve
   * @return the resolved URI
   * @see URI#resolve(URI)
   */
  @NonNull
  default IAnyUriItem resolve(@NonNull IAnyUriItem other) {
    return valueOf(ObjectUtils.notNull(asUri().resolve(other.asUri())));
  }

  @Override
  default IAnyUriItem castAsType(IAnyAtomicItem item) {
    return cast(item);
  }

  @Override
  default int compareTo(IAnyAtomicItem item) {
    return compareTo(cast(item));
  }

  /**
   * Compares this value with the argument.
   *
   * @param item
   *          the item to compare with this value
   * @return a negative integer, zero, or a positive integer if this value is less
   *         than, equal to, or greater than the {@code item}.
   */
  default int compareTo(@NonNull IAnyUriItem item) {
    return asUri().compareTo(item.asUri());
  }
}
