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

package gov.nist.secauto.metaschema.core.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFieldDefinition extends IValuedDefinition, IFlagContainer, IField {

  @Override
  default IFieldDefinition getOwningDefinition() {
    return this;
  }

  @Override
  default boolean isInline() {
    // reasonable default
    return false;
  }

  @Override
  default IFieldInstance getInlineInstance() {
    // reasonable default
    return null;
  }

  /**
   * Retrieves the key to use as the field name for this field's value in JSON.
   *
   * @return a string or a FlagInstance value
   */
  @Nullable
  default Object getJsonValueKey() {
    Object retval = getJsonValueKeyFlagInstance();
    if (retval == null) {
      retval = getJsonValueKeyName();
    }
    return retval;
  }

  /**
   * Check if a JSON value key flag is configured.
   *
   * @return {@code true} if a JSON value key flag is configured, or {@code false}
   *         otherwise
   */
  default boolean hasJsonValueKeyFlagInstance() {
    return getJsonValueKeyFlagInstance() != null;
  }

  /**
   * Retrieves the flag instance who's value will be used as the "value key".
   *
   * @return the configured flag instance, or {@code null} if a flag is not
   *         configured as the "value key"
   */
  @Nullable
  IFlagInstance getJsonValueKeyFlagInstance();

  /**
   * Retrieves the configured static label to use as the value key, or the type
   * specific name if a label is not configured.
   *
   * @return the value key label
   */
  @NonNull
  String getJsonValueKeyName();

  /**
   * Get the value of the field's value from the field item object.
   *
   * @param item
   *          the field item
   * @return the field's value or {@code null} if it has no value
   */
  Object getFieldValue(@NonNull Object item);
}
