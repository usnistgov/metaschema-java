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

import gov.nist.secauto.metaschema.core.model.AbstractModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.annotations.Module;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public abstract class AbstractBoundModule
    extends AbstractModule<
        IBoundModule,
        IBoundDefinitionModelComplex,
        IBoundDefinitionFlag,
        IBoundDefinitionModelField,
        IBoundDefinitionModelAssembly>
    implements IBoundModule {
  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Lazy<Map<String, IBoundDefinitionModelAssembly>> assemblyDefinitions;
  @NonNull
  private final Lazy<Map<String, IBoundDefinitionModelField>> fieldDefinitions;

  /**
   * Create a new Module instance for a given class annotated by the
   * {@link Module} annotation.
   * <p>
   * Will also load any imported Metaschemas.
   *
   *
   * @param clazz
   *          the Module class
   * @param bindingContext
   *          the Module binding context
   * @return the new Module instance
   */
  @NonNull
  public static IBoundModule createInstance(
      @NonNull Class<? extends IBoundModule> clazz,
      @NonNull IBindingContext bindingContext) {

    if (!clazz.isAnnotationPresent(Module.class)) {
      throw new IllegalStateException(String.format("The class '%s' is missing the '%s' annotation",
          clazz.getCanonicalName(), Module.class.getCanonicalName()));
    }

    Module moduleAnnotation = clazz.getAnnotation(Module.class);

    List<IBoundModule> importedModules;
    if (moduleAnnotation.imports().length > 0) {
      importedModules = new ArrayList<>(moduleAnnotation.imports().length);
      for (Class<? extends IBoundModule> importClass : moduleAnnotation.imports()) {
        assert importClass != null;
        IBoundModule moduleImport = bindingContext.registerModule(importClass);
        importedModules.add(moduleImport);
      }
    } else {
      importedModules = CollectionUtil.emptyList();
    }
    return createInstance(clazz, bindingContext, importedModules);
  }

  @NonNull
  private static IBoundModule createInstance(
      @NonNull Class<? extends IBoundModule> clazz,
      @NonNull IBindingContext bindingContext,
      @NonNull List<? extends IBoundModule> importedModules) {

    Constructor<? extends IBoundModule> constructor;
    try {
      constructor = clazz.getDeclaredConstructor(List.class, IBindingContext.class);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex);
    }

    try {
      return ObjectUtils.notNull(constructor.newInstance(importedModules, bindingContext));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Construct a new Module instance.
   *
   * @param importedModules
   *          Module imports associated with the Metaschema module
   * @param bindingContext
   *          the Module binding context
   */
  protected AbstractBoundModule(
      @NonNull List<? extends IBoundModule> importedModules,
      @NonNull IBindingContext bindingContext) {
    super(importedModules);
    this.bindingContext = bindingContext;
    this.assemblyDefinitions = ObjectUtils.notNull(Lazy.lazy(() -> Arrays.stream(getAssemblyClasses())
        .map(clazz -> {
          assert clazz != null;
          return (IBoundDefinitionModelAssembly) ObjectUtils
              .requireNonNull(bindingContext.getBoundDefinitionForClass(clazz));
        })
        .collect(Collectors.toUnmodifiableMap(
            IBoundDefinitionModelAssembly::getName,
            Function.identity()))));
    this.fieldDefinitions = ObjectUtils.notNull(Lazy.lazy(() -> Arrays.stream(getFieldClasses())
        .map(clazz -> {
          assert clazz != null;
          return (IBoundDefinitionModelField) ObjectUtils.requireNonNull(bindingContext.getBoundDefinitionForClass(clazz));
        })
        .collect(Collectors.toUnmodifiableMap(
            IBoundDefinitionModelField::getName,
            Function.identity()))));
  }

  @Override
  @NonNull
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  /**
   * Get the assembly instance annotations associated with this bound choice
   * group.
   *
   * @return the annotations
   */
  @NonNull
  protected Class<?>[] getAssemblyClasses() {
    Class<?>[] retval;
    if (getClass().isAnnotationPresent(Module.class)) {
      Module moduleAnnotation = getClass().getAnnotation(Module.class);
      retval = moduleAnnotation.assemblies();
    } else {
      retval = new Class<?>[] {};
    }
    return retval;
  }

  /**
   * Get the field instance annotations associated with this bound choice group.
   *
   * @return the annotations
   */
  @NonNull
  protected Class<?>[] getFieldClasses() {
    Class<?>[] retval;
    if (getClass().isAnnotationPresent(Module.class)) {
      Module moduleAnnotation = getClass().getAnnotation(Module.class);
      retval = moduleAnnotation.fields();
    } else {
      retval = new Class<?>[] {};
    }
    return retval;
  }

  /**
   * Get the mapping of assembly definition effective name to definition.
   *
   * @return the mapping
   */
  protected Map<String, IBoundDefinitionModelAssembly> getAssemblyDefinitionMap() {
    return assemblyDefinitions.get();
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IBoundDefinitionModelAssembly> getAssemblyDefinitions() {
    return getAssemblyDefinitionMap().values();
  }

  @Override
  public IBoundDefinitionModelAssembly getAssemblyDefinitionByName(@NonNull String name) {
    return getAssemblyDefinitionMap().get(name);
  }

  /**
   * Get the mapping of field definition effective name to definition.
   *
   * @return the mapping
   */
  protected Map<String, IBoundDefinitionModelField> getFieldDefinitionMap() {
    return fieldDefinitions.get();
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IBoundDefinitionModelField> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  @Override
  public IBoundDefinitionModelField getFieldDefinitionByName(@NonNull String name) {
    return getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IBoundDefinitionFlag> getFlagDefinitions() {
    // Flags are always inline, so they do not have separate definitions
    return Collections.emptyList();
  }

  @Override
  public IBoundDefinitionFlag getFlagDefinitionByName(@NonNull String name) {
    // Flags are always inline, so they do not have separate definitions
    return null;
  }
}
