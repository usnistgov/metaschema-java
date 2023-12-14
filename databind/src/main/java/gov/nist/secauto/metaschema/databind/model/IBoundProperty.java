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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IBoundProperty extends IBinding {
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

  @Nullable
  Object getValue(@NonNull Object parentItem);

  /**
   * Set the provided value on the provided object. The provided object must be of
   * the item's type associated with this instance.
   *
   * @param parentObject
   *          the object
   * @param value
   *          a value, which may be a simple {@link Type} or a
   *          {@link ParameterizedType} for a collection
   */
  void setValue(@NonNull Object parentObject, Object value);

  Collection<? extends Object> getItemValues(Object value);

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
