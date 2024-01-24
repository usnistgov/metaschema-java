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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

abstract class AbstractClassBinding implements IClassBinding {
  // private static final Logger logger =
  // LogManager.getLogger(AbstractClassBinding.class);

  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Class<?> clazz;
  @NonNull
  private Lazy<IModule> module;
  private final Method beforeDeserializeMethod;
  private final Method afterDeserializeMethod;

  /**
   * Construct a new class binding for the provided class.
   *
   * @param clazz
   *          the bound class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  public AbstractClassBinding(@NonNull Class<?> clazz, @NonNull IBindingContext bindingContext) {
    this.bindingContext = ObjectUtils.requireNonNull(bindingContext, "bindingContext");
    this.clazz = ObjectUtils.requireNonNull(clazz, "clazz");
    this.module = ObjectUtils.notNull(Lazy.lazy(() -> getBindingContext().loadModule(getModuleClass())));
    this.beforeDeserializeMethod = ClassIntrospector.getMatchingMethod(clazz, "beforeDeserialize", Object.class);
    this.afterDeserializeMethod = ClassIntrospector.getMatchingMethod(clazz, "afterDeserialize", Object.class);
  }

  @Override
  public boolean isInline() {
    return getBoundClass().getEnclosingClass() != null;
  }

  @Override
  public Class<?> getBoundClass() {
    return clazz;
  }

  @Override
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public String getUseName() { // NOPMD
    // a use name is never provided
    return null;
  }

  @Override
  public Integer getUseIndex() {
    // a use name index is never provided
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s IClassBinding(%s): %s", getModelType().name().toLowerCase(Locale.ROOT), getName(),
        getBoundClass().getName());
  }

  @Override
  public @NonNull ModuleScopeEnum getModuleScope() {
    // TODO: is this the right value?
    return ModuleScopeEnum.INHERITED;
  }

  @NonNull
  protected abstract Class<? extends IModule> getModuleClass();

  @Override
  public IModule getContainingModule() {
    return ObjectUtils.notNull(module.get());
  }

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
  @Override
  @NonNull
  public <CLASS> CLASS newInstance() {
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
  public void callBeforeDeserialize(Object targetObject, Object parentObject) throws BindingException {
    if (beforeDeserializeMethod != null) {
      try {
        beforeDeserializeMethod.invoke(targetObject, parentObject);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
      }
    }
  }

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
  public void callAfterDeserialize(Object targetObject, Object parentObject) throws BindingException {
    if (afterDeserializeMethod != null) {
      try {
        afterDeserializeMethod.invoke(targetObject, parentObject);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
      }
    }
  }

  @Override
  public Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    deepCopyItemInternal(item, instance);

    callAfterDeserialize(instance, parentInstance);

    return instance;
  }

  protected void deepCopyItemInternal(@NonNull Object fromObject, @NonNull Object toObject)
      throws BindingException {
    for (IBoundFlagInstance instance : getFlagInstances()) {
      instance.deepCopy(fromObject, toObject);
    }
  }
}
