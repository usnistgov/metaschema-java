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

package gov.nist.secauto.metaschema.model.common;

import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a common, abstract implementation of a {@link IMetaschema}.
 */
public abstract class AbstractMetaschema
    implements IMetaschema {
  private static final Logger LOGGER = LogManager.getLogger(AbstractMetaschema.class);

  @NotNull
  private final List<@NotNull ? extends IMetaschema> importedMetaschemas;
  private Map<@NotNull String, IFlagDefinition> exportedFlagDefinitions;
  private Map<@NotNull String, IFieldDefinition> exportedFieldDefinitions;
  private Map<@NotNull String, IAssemblyDefinition> exportedAssemblyDefinitions;

  public AbstractMetaschema(@NotNull List<@NotNull ? extends IMetaschema> importedMetaschemas) {
    this.importedMetaschemas
        = CollectionUtil.unmodifiableList(ObjectUtils.requireNonNull(importedMetaschemas, "importedMetaschema"));
  }

  @Override
  public @NotNull List<@NotNull ? extends IMetaschema> getImportedMetaschemas() {
    return importedMetaschemas;
  }

  protected Map<@NotNull String, ? extends IMetaschema> getImportedMetaschemaByShortNames() {
    return importedMetaschemas.stream().collect(Collectors.toMap(IMetaschema::getShortName, Function.identity()));
  }

  @Override
  public IMetaschema getImportedMetaschemaByShortName(String name) {
    return getImportedMetaschemaByShortNames().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFlagDefinition> getExportedFlagDefinitions() {
    return getExportedFlagDefinitionMap().values();
  }

  @Override
  public @Nullable IFlagDefinition getExportedFlagDefinitionByName(String name) {
    return getExportedFlagDefinitionMap().get(name);
  }

  protected Map<@NotNull String, ? extends IFlagDefinition> getExportedFlagDefinitionMap() {
    initExports();
    return exportedFlagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFieldDefinition> getExportedFieldDefinitions() {
    return getExportedFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getExportedFieldDefinitionByName(String name) {
    return getExportedFieldDefinitionMap().get(name);
  }

  protected Map<@NotNull String, ? extends IFieldDefinition> getExportedFieldDefinitionMap() {
    initExports();
    return exportedFieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IAssemblyDefinition> getExportedAssemblyDefinitions() {
    return getExportedAssemblyDefinitionMap().values();
  }

  @Override
  public IAssemblyDefinition getExportedAssemblyDefinitionByName(String name) {
    return getExportedAssemblyDefinitionMap().get(name);
  }

  protected Map<@NotNull String, ? extends IAssemblyDefinition> getExportedAssemblyDefinitionMap() {
    initExports();
    return exportedAssemblyDefinitions;
  }

  @Override
  public Collection<@NotNull ? extends IModelElement> getInfoElementsByMetapath(String path) {
    // TODO: implement path evaluation
    throw new UnsupportedOperationException();
  }

  /**
   * Processes the definitions exported by the Metaschema, saving a list of all exported by specific
   * model types.
   */
  protected void initExports() {
    synchronized (this) {

      if (exportedFlagDefinitions == null) {
        // Populate the stream with the definitions from this metaschema
        Predicate<gov.nist.secauto.metaschema.model.common.IDefinition> filter = IMetaschema.allNonLocalDefinitions();
        Stream<@NotNull ? extends IFlagDefinition> flags = getFlagDefinitions().stream()
            .filter(filter);
        Stream<@NotNull ? extends IFieldDefinition> fields = getFieldDefinitions().stream()
            .filter(filter);
        Stream<@NotNull ? extends IAssemblyDefinition> assemblies = getAssemblyDefinitions().stream()
            .filter(filter);

        // handle definitions from any included metaschema
        if (!getImportedMetaschemas().isEmpty()) {
          @SuppressWarnings("null")
          Stream<@NotNull ? extends IFlagDefinition> importedFlags = Stream.empty();
          @SuppressWarnings("null")
          Stream<@NotNull ? extends IFieldDefinition> importedFields = Stream.empty();
          @SuppressWarnings("null")
          Stream<@NotNull ? extends IAssemblyDefinition> importedAssemblies = Stream.empty();

          for (IMetaschema metaschema : getImportedMetaschemas()) {
            importedFlags = Stream.concat(importedFlags, metaschema.getExportedFlagDefinitions().stream());
            importedFields = Stream.concat(importedFields, metaschema.getExportedFieldDefinitions().stream());
            importedAssemblies
                = Stream.concat(importedAssemblies, metaschema.getExportedAssemblyDefinitions().stream());
          }

          flags = Stream.concat(importedFlags, flags);
          fields = Stream.concat(importedFields, fields);
          assemblies = Stream.concat(importedAssemblies, assemblies);
        }

        // Build the maps. Definitions from this Metaschema will take priority, with shadowing being
        // reported when a definition from this Metaschema has the same name as an imported one
        @SuppressWarnings("null")
        Map<@NotNull String, IFlagDefinition> exportedFlagDefinitions = flags.collect(
            CustomCollectors.toMap(
                IFlagDefinition::getName,
                Function.identity(),
                AbstractMetaschema::handleShadowedDefinitions));
        @SuppressWarnings("null")
        Map<@NotNull String, IFieldDefinition> exportedFieldDefinitions = fields.collect(
            CustomCollectors.toMap(
                IFieldDefinition::getName,
                Function.identity(),
                AbstractMetaschema::handleShadowedDefinitions));
        @SuppressWarnings("null")
        Map<@NotNull String, IAssemblyDefinition> exportedAssemblyDefinitions = assemblies.collect(
            CustomCollectors.toMap(
                IAssemblyDefinition::getName,
                Function.identity(),
                AbstractMetaschema::handleShadowedDefinitions));

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

  private static <DEF extends IDefinition> DEF handleShadowedDefinitions(
      @SuppressWarnings("unused") @NotNull String key, @NotNull DEF oldDef, @NotNull DEF newDef) {
    if (oldDef != newDef && LOGGER.isWarnEnabled()) {
      LOGGER.warn("The {} '{}' from metaschema '{}' is shadowing '{}' from metaschema '{}'",
          newDef.getModelType().name().toLowerCase(Locale.ROOT),
          newDef.getName(),
          newDef.getContainingMetaschema().getShortName(),
          oldDef.getName(),
          oldDef.getContainingMetaschema().getShortName());
    }
    return newDef;
  }
}