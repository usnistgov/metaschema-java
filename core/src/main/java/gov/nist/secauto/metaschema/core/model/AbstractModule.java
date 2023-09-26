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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides a common, abstract implementation of a {@link IModule}.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public abstract class AbstractModule
    implements IModule {
  private static final Logger LOGGER = LogManager.getLogger(AbstractModule.class);

  @NonNull
  private final List<? extends IModule> importedModules;
  private Map<String, IFlagDefinition> exportedFlagDefinitions;
  private Map<String, IFieldDefinition> exportedFieldDefinitions;
  private Map<String, IAssemblyDefinition> exportedAssemblyDefinitions;

  /**
   * Construct a new Metaschema module object.
   *
   * @param importedModules
   *          the collection of Metaschema module objects this Metaschema module
   *          imports
   */
  public AbstractModule(@NonNull List<? extends IModule> importedModules) {
    this.importedModules
        = CollectionUtil.unmodifiableList(ObjectUtils.requireNonNull(importedModules, "importedModules"));
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "interface doesn't allow modification")
  public List<? extends IModule> getImportedModules() {
    return importedModules;
  }

  private Map<String, ? extends IModule> getImportedModulesByShortName() {
    return importedModules.stream().collect(Collectors.toMap(IModule::getShortName, Function.identity()));
  }

  @Override
  public IModule getImportedModuleByShortName(String name) {
    return getImportedModulesByShortName().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IFlagDefinition> getExportedFlagDefinitions() {
    return getExportedFlagDefinitionMap().values();
  }

  @Override
  public IFlagDefinition getExportedFlagDefinitionByName(String name) {
    return getExportedFlagDefinitionMap().get(name);
  }

  private Map<String, ? extends IFlagDefinition> getExportedFlagDefinitionMap() {
    initExports();
    return exportedFlagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IFieldDefinition> getExportedFieldDefinitions() {
    return getExportedFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getExportedFieldDefinitionByName(String name) {
    return getExportedFieldDefinitionMap().get(name);
  }

  private Map<String, ? extends IFieldDefinition> getExportedFieldDefinitionMap() {
    initExports();
    return exportedFieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyDefinition> getExportedAssemblyDefinitions() {
    return getExportedAssemblyDefinitionMap().values();
  }

  @Override
  public IAssemblyDefinition getExportedAssemblyDefinitionByName(String name) {
    return getExportedAssemblyDefinitionMap().get(name);
  }

  private Map<String, ? extends IAssemblyDefinition> getExportedAssemblyDefinitionMap() {
    initExports();
    return exportedAssemblyDefinitions;
  }

  /**
   * Processes the definitions exported by the Metaschema, saving a list of all
   * exported by specific model types.
   */
  protected void initExports() {
    synchronized (this) {
      if (exportedFlagDefinitions == null) {
        // Populate the stream with the definitions from this module
        Predicate<IDefinition> filter = IModule.allNonLocalDefinitions();
        Stream<? extends IFlagDefinition> flags = getFlagDefinitions().stream()
            .filter(filter);
        Stream<? extends IFieldDefinition> fields = getFieldDefinitions().stream()
            .filter(filter);
        Stream<? extends IAssemblyDefinition> assemblies = getAssemblyDefinitions().stream()
            .filter(filter);

        // handle definitions from any included module
        if (!getImportedModules().isEmpty()) {
          Stream<? extends IFlagDefinition> importedFlags = Stream.empty();
          Stream<? extends IFieldDefinition> importedFields = Stream.empty();
          Stream<? extends IAssemblyDefinition> importedAssemblies = Stream.empty();

          for (IModule module : getImportedModules()) {
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
        Map<String, IFlagDefinition> exportedFlagDefinitions = flags.collect(
            CustomCollectors.toMap(
                IFlagDefinition::getName,
                CustomCollectors.identity(),
                AbstractModule::handleShadowedDefinitions));
        Map<String, IFieldDefinition> exportedFieldDefinitions = fields.collect(
            CustomCollectors.toMap(
                IFieldDefinition::getName,
                CustomCollectors.identity(),
                AbstractModule::handleShadowedDefinitions));
        Map<String, IAssemblyDefinition> exportedAssemblyDefinitions = assemblies.collect(
            CustomCollectors.toMap(
                IAssemblyDefinition::getName,
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
      }
    }
  }

  @SuppressWarnings({ "unused", "PMD.UnusedPrivateMethod" }) // used by lambda
  private static <DEF extends IDefinition> DEF handleShadowedDefinitions(
      @SuppressWarnings("unused") @NonNull String key, @NonNull DEF oldDef, @NonNull DEF newDef) {
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
}
