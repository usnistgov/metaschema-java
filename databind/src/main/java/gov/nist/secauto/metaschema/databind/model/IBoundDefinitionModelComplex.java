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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a field or assembly instance bound to Java class.
 */
public interface IBoundDefinitionModelComplex
    extends IBoundDefinitionModel<IBoundObject>, IFeatureComplexItemValueHandler {

  @NonNull
  Map<String, IBoundProperty<?>> getJsonProperties(@Nullable Predicate<IBoundInstanceFlag> flagFilter);

  @Override
  default boolean isInline() {
    return getBoundClass().getEnclosingClass() != null;
  }

  @Nullable
  Method getBeforeDeserializeMethod();

  /**
   * Calls the method named "beforeDeserialize" on each class in the object's
   * hierarchy if the method exists on the class.
   * <p>
   * These methods can be used to set the initial state of the target bound object
   * before data is read and applied during deserialization.
   *
   * @param targetObject
   *          the data object target to call the method(s) on
   * @param parentObject
   *          the object target's parent object, which is used as the method
   *          argument
   * @throws BindingException
   *           if an error occurs while calling the method
   */
  @Override
  default void callBeforeDeserialize(IBoundObject targetObject, IBoundObject parentObject) throws BindingException {
    Method beforeDeserializeMethod = getBeforeDeserializeMethod();
    if (beforeDeserializeMethod != null) {
      try {
        beforeDeserializeMethod.invoke(targetObject, parentObject);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
      }
    }
  }

  @Nullable
  Method getAfterDeserializeMethod();

  /**
   * Calls the method named "afterDeserialize" on each class in the object's
   * hierarchy if the method exists.
   * <p>
   * These methods can be used to modify the state of the target bound object
   * after data is read and applied during deserialization.
   *
   * @param targetObject
   *          the data object target to call the method(s) on
   * @param parentObject
   *          the object target's parent object, which is used as the method
   *          argument
   * @throws BindingException
   *           if an error occurs while calling the method
   */
  @Override
  default void callAfterDeserialize(IBoundObject targetObject, IBoundObject parentObject) throws BindingException {
    Method afterDeserializeMethod = getAfterDeserializeMethod();
    if (afterDeserializeMethod != null) {
      try {
        afterDeserializeMethod.invoke(targetObject, parentObject);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
      }
    }
  }

  // @Override
  // public String getJsonKeyFlagName() {
  // // definition items never have a JSON key
  // return null;
  // }

}
