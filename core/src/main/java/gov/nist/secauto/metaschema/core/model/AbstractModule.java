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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Provides a common, abstract implementation of a {@link IModule}.
 *
 * @param <M>
 *          the imported module Java type
 * @param <D>
 *          the definition Java type
 * @param <FL>
 *          the flag definition Java type
 * @param <FI>
 *          the field definition Java type
 * @param <A>
 *          the assembly definition Java type
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public abstract class AbstractModule<
    M extends IModuleExtended<M, D, FL, FI, A>,
    D extends IModelDefinition,
    FL extends IFlagDefinition,
    FI extends IFieldDefinition,
    A extends IAssemblyDefinition>
    implements IModuleExtended<M, D, FL, FI, A> {
  private static final Logger LOGGER = LogManager.getLogger(AbstractModule.class);

  @NonNull
  private final List<? extends M> importedModules;
  @NonNull
  private final Lazy<Exports> exports;

  /**
   * Construct a new Metaschema module object.
   *
   * @param importedModules
   *          the collection of Metaschema module objects this Metaschema module
   *          imports
   */
  public AbstractModule(@NonNull List<? extends M> importedModules) {
    this.importedModules
        = CollectionUtil.unmodifiableList(ObjectUtils.requireNonNull(importedModules, "importedModules"));
    this.exports = ObjectUtils.notNull(Lazy.lazy(() -> new Exports(importedModules)));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "interface doesn't allow modification")
  public List<? extends M> getImportedModules() {
    return importedModules;
  }

  @SuppressWarnings("null")
  @NonNull
  private Exports getExports() {
    return exports.get();
  }

  private Map<String, ? extends M> getImportedModulesByShortName() {
    return importedModules.stream().collect(Collectors.toMap(IModule::getShortName, Function.identity()));
  }

  @Override
  public M getImportedModuleByShortName(String name) {
    return getImportedModulesByShortName().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<FL> getExportedFlagDefinitions() {
    return getExports().getExportedFlagDefinitionMap().values();
  }

  @Override
  public FL getExportedFlagDefinitionByName(QName name) {
    return getExports().getExportedFlagDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<FI> getExportedFieldDefinitions() {
    return getExports().getExportedFieldDefinitionMap().values();
  }

  @Override
  public FI getExportedFieldDefinitionByName(QName name) {
    return getExports().getExportedFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<A> getExportedAssemblyDefinitions() {
    return getExports().getExportedAssemblyDefinitionMap().values();
  }

  @Override
  public A getExportedAssemblyDefinitionByName(QName name) {
    return getExports().getExportedAssemblyDefinitionMap().get(name);
  }

  @Override
  public A getExportedRootAssemblyDefinitionByName(QName name) {
    return getExports().getExportedRootAssemblyDefinitionMap().get(name);
  }

  @SuppressWarnings({ "unused", "PMD.UnusedPrivateMethod" }) // used by lambda
  private static <DEF extends IDefinition> DEF handleShadowedDefinitions(
      @SuppressWarnings("unused") @NonNull QName key,
      @NonNull DEF oldDef,
      @NonNull DEF newDef) {
    if (!oldDef.equals(newDef) && LOGGER.isWarnEnabled()) {
      LOGGER.warn("The {} '{}' from metaschema '{}' is shadowing '{}' from metaschema '{}'",
          newDef.getModelType().name().toLowerCase(Locale.ROOT),
          newDef.getName(),
          newDef.getContainingModule().getShortName(),
          oldDef.getName(),
          oldDef.getContainingModule().getShortName());
    }
    return newDef;
  }

  private class Exports {
    @NonNull
    private final Map<QName, FL> exportedFlagDefinitions;
    @NonNull
    private final Map<QName, FI> exportedFieldDefinitions;
    @NonNull
    private final Map<QName, A> exportedAssemblyDefinitions;
    @NonNull
    private final Map<QName, A> exportedRootAssemblyDefinitions;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public Exports(@NonNull List<? extends M> importedModules) {
      // Populate the stream with the definitions from this module
      Predicate<IDefinition> filter = IModuleExtended.allNonLocalDefinitions();
      Stream<FL> flags = getFlagDefinitions().stream()
          .filter(filter);
      Stream<FI> fields = getFieldDefinitions().stream()
          .filter(filter);
      Stream<A> assemblies = getAssemblyDefinitions().stream()
          .filter(filter);

      // handle definitions from any included module
      if (!importedModules.isEmpty()) {
        Stream<FL> importedFlags = Stream.empty();
        Stream<FI> importedFields = Stream.empty();
        Stream<A> importedAssemblies = Stream.empty();

        for (M module : importedModules) {
          importedFlags = Stream.concat(importedFlags, module.getExportedFlagDefinitions().stream());
          importedFields = Stream.concat(importedFields, module.getExportedFieldDefinitions().stream());
          importedAssemblies
              = Stream.concat(importedAssemblies, module.getExportedAssemblyDefinitions().stream());
        }

        flags = Stream.concat(importedFlags, flags);
        fields = Stream.concat(importedFields, fields);
        assemblies = Stream.concat(importedAssemblies, assemblies);
      }

      // Build the maps. Definitions from this module will take priority, with
      // shadowing being reported when a definition from this module has the same name
      // as an imported one
      Map<QName, FL> exportedFlagDefinitions = flags.collect(
          CustomCollectors.toMap(
              IFlagDefinition::getDefinitionQName,
              CustomCollectors.identity(),
              AbstractModule::handleShadowedDefinitions));
      Map<QName, FI> exportedFieldDefinitions = fields.collect(
          CustomCollectors.toMap(
              IFieldDefinition::getDefinitionQName,
              CustomCollectors.identity(),
              AbstractModule::handleShadowedDefinitions));
      Map<QName, A> exportedAssemblyDefinitions = assemblies.collect(
          CustomCollectors.toMap(
              IAssemblyDefinition::getDefinitionQName,
              CustomCollectors.identity(),
              AbstractModule::handleShadowedDefinitions));

      this.exportedFlagDefinitions = exportedFlagDefinitions.isEmpty()
          ? CollectionUtil.emptyMap()
          : CollectionUtil.unmodifiableMap(exportedFlagDefinitions);
      this.exportedFieldDefinitions = exportedFieldDefinitions.isEmpty()
          ? CollectionUtil.emptyMap()
          : CollectionUtil.unmodifiableMap(exportedFieldDefinitions);
      this.exportedAssemblyDefinitions = exportedAssemblyDefinitions.isEmpty()
          ? CollectionUtil.emptyMap()
          : CollectionUtil.unmodifiableMap(exportedAssemblyDefinitions);
      this.exportedRootAssemblyDefinitions = exportedAssemblyDefinitions.isEmpty()
          ? CollectionUtil.emptyMap()
          : CollectionUtil.unmodifiableMap(ObjectUtils.notNull(exportedAssemblyDefinitions.values().stream()
              .filter(IAssemblyDefinition::isRoot)
              .collect(CustomCollectors.toMap(
                  IAssemblyDefinition::getRootXmlQName,
                  CustomCollectors.identity(),
                  AbstractModule::handleShadowedDefinitions))));
    }

    @NonNull
    public Map<QName, FL> getExportedFlagDefinitionMap() {
      return this.exportedFlagDefinitions;
    }

    @NonNull
    public Map<QName, FI> getExportedFieldDefinitionMap() {
      return this.exportedFieldDefinitions;
    }

    @NonNull
    public Map<QName, A> getExportedAssemblyDefinitionMap() {
      return this.exportedAssemblyDefinitions;
    }

    @NonNull
    public Map<QName, A> getExportedRootAssemblyDefinitionMap() {
      return this.exportedRootAssemblyDefinitions;
    }
  }
}
