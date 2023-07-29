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

package gov.nist.secauto.metaschema.core.model.constraint;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a rule requiring the value of a field or flag to match the name of one entry in a set
 * of enumerated values.
 */
public interface IAllowedValuesConstraint extends IConstraint {
  boolean DEFAULT_ALLOW_OTHER = false;
  @NonNull
  Extensible DEFAULT_EXTENSIBLE = Extensible.MODEL;

  enum Extensible {
    /**
     * Can be extended by external constraints. The most permissive level.
     */
    EXTERNAL,
    /**
     * Can be extended by constraints in the same model.
     */
    MODEL,
    /**
     * Cannot be extended. The most restrictive level.
     */
    NONE;
  }

  /**
   * Get the collection allowed values associated with this constraint.
   *
   * @return a mapping of value to the associated {@link IAllowedValue} item
   */
  @NonNull
  Map<String, ? extends IAllowedValue> getAllowedValues();

  /**
   * Get a specific allowed value by name, if it is defined for this constraint.
   *
   * @param name
   *          the value name
   * @return the allowed value or {@code null} if the value is not defined
   */
  @Nullable
  default IAllowedValue getAllowedValue(String name) {
    return getAllowedValues().get(name);
  }

  /**
   * Determines if this allowed value constraint is open-ended ({@code true}) or closed. If
   * "open-ended", the constraint allows the target's value to by any additional unspecified value. If
   * "closed", the constraint requries the target's value to be one of the specified values.
   *
   * @return {@code true} if the constraint is "open-ended", or {@code false} otherwise
   */
  boolean isAllowedOther();

  /**
   * Determines the degree to which this constraint can be extended by other constraints applied to
   * the same value.
   *
   * @return the enumeration value
   */
  @NonNull
  Extensible getExtensible();
}
