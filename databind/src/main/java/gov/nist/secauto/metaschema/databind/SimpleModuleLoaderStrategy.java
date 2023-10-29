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

package gov.nist.secauto.metaschema.databind;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext.IModuleLoaderStrategy;
import gov.nist.secauto.metaschema.databind.model.annotations.Module;
import gov.nist.secauto.metaschema.databind.strategy.IBindingStrategyFactory;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.BindingStrategyFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;

public class SimpleModuleLoaderStrategy implements IModuleLoaderStrategy {
  @NonNull
  private final IBindingStrategyFactory bindingStrategyfactory = BindingStrategyFactory.instance();

  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Map<Class<?>, IModule> modulesByClass = new ConcurrentHashMap<>();

  /**
   * Construct a new basic module loader.
   *
   * @param bindingContext
   *          the binding context used to initialize new modules
   */
  public SimpleModuleLoaderStrategy(@NonNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
  }

  /**
   * Get the factory for binding information objects.
   *
   * @return the factory
   */
  @NonNull
  protected IBindingStrategyFactory getBindingStrategyfactory() {
    return bindingStrategyfactory;
  }

  /**
   * Get the binding context for use in initializing new modules.
   *
   * @return the binding context
   */
  @NonNull
  protected IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public IModule loadModule(Class<? extends IModule> clazz) {
    IModule retval = modulesByClass.get(clazz);
    if (retval == null) {
      retval = loadModuleInternal(clazz);
      modulesByClass.put(clazz, retval);
    }
    return retval;
  }

  /**
   * Create a new Module instance for a given class annotated by the
   * {@link Module} annotation.
   * <p>
   * Will also load any imported Metaschemas.
   *
   * @param clazz
   *          the Module class
   * @return the new Module instance
   */
  @NonNull
  protected IModule loadModuleInternal(Class<? extends IModule> clazz) {

    if (!clazz.isAnnotationPresent(Module.class)) {
      throw new IllegalStateException(String.format("The class '%s' is missing the '%s' annotation",
          clazz.getCanonicalName(), Module.class.getCanonicalName()));
    }

    Module moduleAnnotation = clazz.getAnnotation(Module.class);

    List<IModule> importedModules;
    if (moduleAnnotation.imports().length > 0) {
      importedModules = new ArrayList<>(moduleAnnotation.imports().length);
      for (Class<? extends IModule> importClass : moduleAnnotation.imports()) {
        assert importClass != null;
        IModule moduleImport = loadModule(importClass);
        importedModules.add(moduleImport);
      }
    } else {
      importedModules = CollectionUtil.emptyList();
    }
    return createInstance(clazz, importedModules);
  }

  @NonNull
  private IModule createInstance(
      @NonNull Class<? extends IModule> clazz,
      @NonNull List<? extends IModule> importedModules) {

    Constructor<? extends IModule> constructor;
    try {
      constructor = clazz.getDeclaredConstructor(List.class, IBindingContext.class);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex);
    }

    try {
      // instantiate the module
      return ObjectUtils.notNull(constructor.newInstance(importedModules, getBindingContext()));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Override
  public IClassBindingStrategy<?> getClassBindingStrategy(Class<?> clazz) {
    return getBindingStrategyfactory().newClassBindingStrategy(clazz);
  }
}
