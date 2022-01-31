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

import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractMetaschema implements IMetaschema {
  private static final Logger logger = LogManager.getLogger(AbstractMetaschema.class);

  @NotNull
  private final URI location;
  @NotNull
  private final Map<@NotNull URI, ? extends IMetaschema> importedMetaschemaByUri;
  @NotNull
  private final Map<@NotNull String, ? extends IMetaschema> importedMetaschemaByName;
  private Map<@NotNull String, IFlagDefinition> exportedFlagDefinitions;
  private Map<@NotNull String, IFieldDefinition> exportedFieldDefinitions;
  private Map<@NotNull String, IAssemblyDefinition> exportedAssemblyDefinitions;

  /**
   * Constructs a new {@link IMetaschema} instance.
   * 
   * @param metaschemaResource
   *          the resource to load the Metaschema from
   * @param importedMetaschema
   *          all previously imported Metaschema that this Metaschema might import
   */
  @SuppressWarnings("null")
  public AbstractMetaschema(
      @NotNull URI metaschemaResource,
      @NotNull Map<@NotNull URI, ? extends IMetaschema> importedMetaschema) {
    Objects.requireNonNull(metaschemaResource, "metaschemaResource");
    Objects.requireNonNull(importedMetaschema, "importedMetaschema");
    this.location = metaschemaResource;
    this.importedMetaschemaByUri = Collections.unmodifiableMap(importedMetaschema);
    this.importedMetaschemaByName = Collections.unmodifiableMap(
        importedMetaschema.values().stream().collect(Collectors.toMap(IMetaschema::getShortName, Function.identity())));
    logger.trace("Creating metaschema '{}'", metaschemaResource);
  }

  @Override
  public Map<@NotNull URI, ? extends IMetaschema> getImportedMetaschemaMap() {
    return importedMetaschemaByUri;
  }

  @Override
  public Map<@NotNull String, ? extends IMetaschema> getImportedMetaschemaByShortNames() {
    return importedMetaschemaByName;
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, ? extends IFlagDefinition> getExportedFlagDefinitionMap() {
    return Collections.unmodifiableMap(exportedFlagDefinitions);
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, ? extends IFieldDefinition> getExportedFieldDefinitionMap() {
    return Collections.unmodifiableMap(exportedFieldDefinitions);
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, ? extends IAssemblyDefinition> getExportedAssemblyDefinitionMap() {
    return Collections.unmodifiableMap(exportedAssemblyDefinitions);
  }

  private static class ExportedDefinitionsFilter<DEF extends IDefinition> implements Predicate<@NotNull DEF> {
    @Override
    public boolean test(@NotNull DEF definition) {
      return ModuleScopeEnum.INHERITED.equals(definition.getModuleScope())
          || ModelType.ASSEMBLY.equals(definition.getModelType()) && ((IAssemblyDefinition) definition).isRoot();
    }
  }

  @Override
  public Collection<@NotNull ? extends IModelElement> getInfoElementsByMetapath(String path) {
    // TODO: implement path evaluation
    throw new UnsupportedOperationException();
  }

  private static <DEF extends IDefinition> void addToMap(@NotNull Collection<@NotNull ? extends DEF> items,
      @NotNull Map<@NotNull String, DEF> existingMap) {
    for (DEF item : items) {
      DEF oldItem = existingMap.put(item.getName(), item);
      if (oldItem != null && oldItem != item && logger.isWarnEnabled()) {
        logger.warn("The {} '{}' from metaschema '{}' is shadowing '{}' from metaschema '{}'",
            item.getModelType().name().toLowerCase(), item.getName(), item.getContainingMetaschema().getShortName(),
            oldItem.getName(), oldItem.getContainingMetaschema().getShortName());
      }
    }
  }

  /**
   * Processes the definitions exported by the Metaschema, saving a list of all exported by specific
   * model types.
   * 
   * @throws MetaschemaException
   *           if a parsing error occurs
   */
  @SuppressWarnings("null")
  protected void parseExportedDefinitions() throws MetaschemaException {
    logger.debug("Processing metaschema '{}'", this.getLocation());

    this.exportedFlagDefinitions = new LinkedHashMap<>();
    this.exportedFieldDefinitions = new LinkedHashMap<>();
    this.exportedAssemblyDefinitions = new LinkedHashMap<>();

    // handle definitions from any included metaschema first
    if (!getImportedMetaschemas().isEmpty()) {
      for (IMetaschema metaschema : getImportedMetaschemas()) {
        addToMap(metaschema.getExportedFlagDefinitions(), this.exportedFlagDefinitions);
        addToMap(metaschema.getExportedFieldDefinitions(), this.exportedFieldDefinitions);
        addToMap(metaschema.getExportedAssemblyDefinitions(), this.exportedAssemblyDefinitions);
      }
    }

    // now handle top-level definitions from this metaschema. Start first with filtering for globals,
    // then add these to the maps
    addToMap(getFlagDefinitions().stream().filter(new ExportedDefinitionsFilter<>()).collect(Collectors.toList()),
        this.exportedFlagDefinitions);
    addToMap(getFieldDefinitions().stream().filter(new ExportedDefinitionsFilter<>()).collect(Collectors.toList()),
        this.exportedFieldDefinitions);
    addToMap(getAssemblyDefinitions().stream().filter(new ExportedDefinitionsFilter<>()).collect(Collectors.toList()),
        this.exportedAssemblyDefinitions);
  }

  @Override
  public URI getLocation() {
    return location;
  }
}
