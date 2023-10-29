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

package gov.nist.secauto.metaschema.databind.strategy;

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.databind.io.BindingException;

import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a method of binding a type of Metaschema module definition.
 *
 * @param <DEF>
 *          the Java type of the bound definition
 */
public interface IClassBindingStrategy<DEF extends IDefinition>
    extends IBindingStrategy, IItemValueHandler {
  /**
   * Get the Metaschema definition bound by this strategy.
   *
   * @return the bound Metaschema definition
   */
  @NonNull
  DEF getDefinition();

  /**
   * Generate a new instance of this class with no data.
   *
   * @return the new Java object
   * @throws BindingException
   *           if an error occurred while generating the Java object
   */
  @NonNull
  Object newInstance() throws BindingException;

  /**
   * The Java class this binding represents.
   *
   * @return the bound class
   */
  @NonNull
  Class<?> getBoundClass();

  /**
   * Invokes a call on an object instance of the bound class before data is loaded
   * into the object.
   * <p>
   * This can be used to initialize the Java object.
   *
   * @param item
   *          an object instance of the bound class
   * @param parent
   *          the object instance's parent object or {@code null} if the object
   *          has no parent
   * @throws BindingException
   *           if an error occurred while invoking the call on the object instance
   */
  void callBeforeDeserialize(
      @NonNull Object item,
      @Nullable Object parent) throws BindingException;

  /**
   * Invokes a call on an object instance of the bound class before data is loaded
   * into the object.
   * <p>
   * This can be used to post-process the Java object.
   *
   * @param item
   *          an object instance of the bound class
   * @param parent
   *          the object instance's parent object or {@code null} if the object
   *          has no parent
   * @throws BindingException
   *           if an error occurred while invoking the call on the object instance
   */
  void callAfterDeserialize(
      @NonNull Object item,
      @Nullable Object parent) throws BindingException;

  /**
   * Create a deep copy of the provided bound object.
   *
   * @param item
   *          the bound object to copy
   * @param parent
   *          the new object's parent object or {@code null}
   * @return the copy
   * @throws BindingException
   *           if an error occurred copying content between java instances
   */
  @Override
  @NonNull
  Object deepCopy(@NonNull Object item, Object parent) throws BindingException;

  /**
   * Check that the binding information pertains to a specific definition type.
   *
   * @param <T>
   *          the Java type of the required Metaschema definition
   * @param clazz
   *          the Java class of the required Metaschema definition
   * @return this instance cast to the requested definition type
   * @throws BindingException
   *           if this instance is not associated with the requested definition
   *           type
   */
  @SuppressWarnings("unchecked")
  @NonNull
  default <T extends IDefinition> IClassBindingStrategy<T> checkType(Class<T> clazz)
      throws BindingException {
    if (clazz.isInstance(getDefinition())) {
      return (IClassBindingStrategy<T>) this;
    }
    throw new BindingException(
        String.format("Binding for class '%s' bound to '%s' is not assignment compatible to '%s'.",
            getClass().getName(),
            getDefinition().toCoordinates(),
            clazz.getName()));
  }

  String toCoordinates();

  Collection<? extends IFlagInstanceBindingStrategy> getFlagInstances();

}
