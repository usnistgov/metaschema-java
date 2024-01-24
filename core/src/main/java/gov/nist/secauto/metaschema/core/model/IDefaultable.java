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

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A marker interface for Metaschema constructs that can have a default value.
 */
public interface IDefaultable {

  /**
   * Retrieves the default data value for this model construct.
   * <p>
   * Child implementations are expected to override this method to provide a more
   * reasonable default value.
   *
   * @return the default value or {@code null} if there is no default
   */
  // from: IModelElement
  @Nullable
  default Object getDefaultValue() {
    // no value by default
    return null;
  }

  /**
   * Get the effective default value for the model construct.
   * <p>
   * This should consider default values in any related referenced definitions or
   * child constructs as needed to determine the default to use.
   *
   * @return the effective default value or {@code null} if there is no effective
   *         default value
   */
  // from IInstance
  @Nullable
  default Object getEffectiveDefaultValue() {
    return getDefaultValue();
  }

  /**
   * Get the actual default value to use for the model construct.
   * <p>
   * This will consider the effective default value in the use context to
   * determine the appropriate default to use. Factors such as the required
   * instance cardinality may affect if the effective default or an empty
   * collection is used.
   *
   * @return the actual default value or {@code null} if there is no actual
   *         default value
   */
  @Nullable
  default Object getResolvedDefaultValue() {
    return getEffectiveDefaultValue();
  }
}
