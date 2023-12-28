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

import gov.nist.secauto.metaschema.databind.io.BindingException;

import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IBoundProperty extends IBoundModuleElement, IFeatureJavaField {
  /**
   * Get the Metaschema module instance associated with this binding.
   *
   * @return the instance
   */
  @NonNull
  IBoundProperty getInstance();

  /**
   * Get the default value for the bound property, considering defaults in any
   * related elements as needed.
   *
   * @return the effective default value
   */
  @Nullable
  Object getEffectiveDefaultValue();

  /**
   * Get the JSON/YAML property/key name to use for serialization-related
   * operations.
   *
   * @return the JSON name
   */
  // REFACTOR: rename to getEffectiveJsonName
  @NonNull
  String getJsonName();

  /**
   * Get the individual item values for this property.
   * <p>
   * A property can be single- or multi-valued. This method gets each value in
   * either case.
   *
   * @param propertyValue
   *          the value for the property, which can be multi-valued
   * @return the item values
   */
  Collection<? extends Object> getItemValues(Object propertyValue);

  /**
   * Copy this instance from one parent object to another.
   *
   * @param fromInstance
   *          the object to copy from
   * @param toInstance
   *          the object to copy to
   * @throws BindingException
   *           if an error occurred while processing the object bindings
   */
  void deepCopy(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException;
}
