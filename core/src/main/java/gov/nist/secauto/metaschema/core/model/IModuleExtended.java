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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * The API for accessing information about a given Metaschema module.
 * <p>
 * A Metaschem module may import another Metaschema module. This import graph
 * can be accessed using {@link #getImportedModules()}.
 * <p>
 * Global scoped Metaschema definitions can be accessed using
 * {@link #getScopedAssemblyDefinitionByName(QName)},
 * {@link #getScopedFieldDefinitionByName(QName)}, and
 * {@link #getScopedFlagDefinitionByName(QName)}. These methods take into
 * consideration the import order to provide the global definitions that are in
 * scope within the given Metschema module.
 * <p>
 * Global scoped definitions exported by this Metaschema module, available for
 * use by importing Metaschema modules, can be accessed using
 * {@link #getExportedAssemblyDefinitions()},
 * {@link #getExportedFieldDefinitions()}, and
 * {@link #getExportedFlagDefinitions()}.
 * <p>
 * Global scoped definitions defined directly within the given Metaschema module
 * can be accessed using {@link #getAssemblyDefinitions()},
 * {@link #getFieldDefinitions()}, and {@link #getFlagDefinitions()}, along with
 * similarly named access methods.
 *
 * @param <M>
 *          the imported module Java type
 * @param <D>
 *          the model definition Java type
 * @param <FL>
 *          the flag definition Java type
 * @param <FI>
 *          the field definition Java type
 * @param <A>
 *          the assembly definition Java type
 */
public interface IModuleExtended<
    M extends IModuleExtended<M, D, FL, FI, A>,
    D extends IModelDefinition,
    FL extends IFlagDefinition,
    FI extends IFieldDefinition,
    A extends IAssemblyDefinition> extends IModule {

  /**
   * Get a filter that will match all definitions that are not locally defined.
   *
   * @param <DEF>
   *          the type of definition
   * @return a predicate implementing the filter
   */
  static <DEF extends IDefinition> Predicate<DEF> allNonLocalDefinitions() {
    return definition -> {
      return ModuleScopeEnum.INHERITED.equals(definition.getModuleScope())
          || ModelType.ASSEMBLY.equals(definition.getModelType()) && ((IAssemblyDefinition) definition).isRoot();
    };
  }

  /**
   * Get a filter that will match all definitions that are root assemblies.
   *
   * @param <DEF>
   *          the type of definition
   * @return a predicate implementing the filter
   */
  static <DEF extends IDefinition> Predicate<DEF> allRootAssemblyDefinitions() {
    return definition -> {
      return ModelType.ASSEMBLY.equals(definition.getModelType()) && ((IAssemblyDefinition) definition).isRoot();
    };
  }

  @Override
  @NonNull
  List<? extends M> getImportedModules();

  @Override
  @Nullable
  M getImportedModuleByShortName(String name);

  @Override
  @NonNull
  Collection<FL> getFlagDefinitions();

  @Override
  @Nullable
  FL getFlagDefinitionByName(@NonNull QName name);

  @Override
  @NonNull
  Collection<A> getAssemblyDefinitions();

  @Override
  @Nullable
  A getAssemblyDefinitionByName(@NonNull QName name);

  @Override
  @NonNull
  Collection<FI> getFieldDefinitions();

  @Override
  @Nullable
  FI getFieldDefinitionByName(@NonNull QName name);

  @Override
  @SuppressWarnings("unchecked")
  @NonNull
  default List<D> getAssemblyAndFieldDefinitions() {
    return ObjectUtils.notNull(
        Stream.concat(
            (Stream<D>) getAssemblyDefinitions().stream(),
            (Stream<D>) getFieldDefinitions().stream())
            .collect(Collectors.toList()));
  }

  @Override
  @Nullable
  default A getScopedAssemblyDefinitionByName(@NonNull QName name) {
    // first try local/global top-level definitions from current metaschema module
    A retval = getAssemblyDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported Metaschema modules
      retval = getExportedAssemblyDefinitionByName(name);
    }
    return retval;
  }

  @Override
  @Nullable
  default FI getScopedFieldDefinitionByName(@NonNull QName name) {
    // first try local/global top-level definitions from current metaschema module
    FI retval = getFieldDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported metaschema modules
      retval = getExportedFieldDefinitionByName(name);
    }
    return retval;
  }

  @Override
  @Nullable
  default FL getScopedFlagDefinitionByName(@NonNull QName name) {
    // first try local/global top-level definitions from current metaschema module
    FL retval = getFlagDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported metaschema modules
      retval = getExportedFlagDefinitionByName(name);
    }
    return retval;
  }

  @Override
  @NonNull
  default Collection<? extends A> getExportedRootAssemblyDefinitions() {
    return ObjectUtils.notNull(getExportedAssemblyDefinitions().stream()
        .filter(allRootAssemblyDefinitions())
        .collect(Collectors.toList()));
  }

  @Override
  @NonNull
  default Collection<? extends A> getRootAssemblyDefinitions() {
    return ObjectUtils.notNull(getAssemblyDefinitions().stream()
        .filter(allRootAssemblyDefinitions())
        .collect(Collectors.toList()));
  }

  @Override
  @NonNull
  Collection<? extends FL> getExportedFlagDefinitions();

  @Override
  @Nullable
  FL getExportedFlagDefinitionByName(QName name);

  @Override
  @NonNull
  Collection<? extends FI> getExportedFieldDefinitions();

  @Override
  @Nullable
  FI getExportedFieldDefinitionByName(QName name);

  @Override
  @NonNull
  Collection<? extends A> getExportedAssemblyDefinitions();

  @Override
  @Nullable
  A getExportedAssemblyDefinitionByName(QName name);
}
