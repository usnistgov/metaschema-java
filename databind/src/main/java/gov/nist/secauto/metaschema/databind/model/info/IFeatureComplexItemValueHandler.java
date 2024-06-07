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

package gov.nist.secauto.metaschema.databind.model.info;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFeatureComplexItemValueHandler extends IItemValueHandler {
  /**
   * Get the Metaschema definition representing the bound complex data.
   *
   * @return the definition
   */
  @NonNull
  IBoundDefinitionModelComplex getDefinition();

  // /**
  // * Get the name of the JSON key, if a JSON key is configured.
  // *
  // * @return the name of the JSON key flag if configured, or {@code null}
  // * otherwise
  // */
  // @Nullable
  // String getJsonKeyFlagName();

  /**
   * Get the mapping of JSON property names to property bindings.
   *
   * @return the mapping
   */
  // REFACTOR: move JSON-specific methods to a binding cache implementation
  @NonNull
  Map<String, IBoundProperty> getJsonProperties();

  // REFACTOR: flatten implementations?
  @Override
  @NonNull
  Object deepCopyItem(
      @NonNull Object item,
      @Nullable Object parentInstance) throws BindingException;

  /**
   * The class this binding is to.
   *
   * @return the bound class
   */
  @NonNull
  Class<?> getBoundClass();

  /**
   * Gets a new instance of the bound class.
   *
   * @param <CLASS>
   *          the type of the bound class
   * @return a Java object for the class
   * @throws RuntimeException
   *           if the instance cannot be created due to a binding error
   */
  @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
  @NonNull
  default <CLASS> CLASS newInstance() {
    Class<?> clazz = getBoundClass();
    try {
      @SuppressWarnings("unchecked") Constructor<CLASS> constructor
          = (Constructor<CLASS>) clazz.getDeclaredConstructor();
      return ObjectUtils.notNull(constructor.newInstance());
    } catch (NoSuchMethodException ex) {
      String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
      throw new RuntimeException(msg, ex);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
  }

  void callBeforeDeserialize(
      @NonNull Object targetObject,
      @Nullable Object parentObject) throws BindingException;

  void callAfterDeserialize(
      @NonNull Object targetObject,
      @Nullable Object parentObject) throws BindingException;
}
