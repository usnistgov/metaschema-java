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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractMetaschema implements Metaschema {
  private static final Logger logger = LogManager.getLogger(AbstractMetaschema.class);

  private final URI location;
  private final Map<URI, Metaschema> importedMetaschema;
  private Map<String, FlagDefinition> exportedFlagDefinitions;
  private Map<String, FieldDefinition> exportedFieldDefinitions;
  private Map<String, AssemblyDefinition> exportedAssemblyDefinitions;

  /**
   * Constructs a new {@link Metaschema} instance.
   * 
   * @param metaschemaResource
   *          the resource to load the Metaschema from
   * @param importedMetaschema
   *          all previously imported Metaschema that this Metaschema might import
   */
  public AbstractMetaschema(URI metaschemaResource, Map<URI, ? extends Metaschema> importedMetaschema) {
    Objects.requireNonNull(metaschemaResource, "metaschemaResource");
    Objects.requireNonNull(importedMetaschema, "importedMetaschema");
    this.location = metaschemaResource;
    this.importedMetaschema = Collections.unmodifiableMap(importedMetaschema);
    logger.trace("Creating metaschema '{}'", metaschemaResource);
  }

  @Override
  public Map<URI, Metaschema> getImportedMetaschema() {
    return importedMetaschema;
  }

  @Override
  public Map<String, FlagDefinition> getExportedFlagDefinitions() {
    return Collections.unmodifiableMap(exportedFlagDefinitions);
  }

  @Override
  public Map<String, FieldDefinition> getExportedFieldDefinitions() {
    return Collections.unmodifiableMap(exportedFieldDefinitions);
  }

  @Override
  public Map<String, AssemblyDefinition> getExportedAssemblyDefinitions() {
    return Collections.unmodifiableMap(exportedAssemblyDefinitions);
  }

  private static class ExportedDefinitionsFilter<DEF extends InfoElementDefinition> implements Predicate<DEF> {
    @Override
    public boolean test(DEF definition) {
      return ModuleScopeEnum.INHERITED.equals(definition.getModuleScope())
          || ModelType.ASSEMBLY.equals(definition.getModelType()) && ((AssemblyDefinition) definition).isRoot();
    }
  }

  @Override
  public Collection<InfoElement> getInfoElementByMetaPath(String path) {
    // TODO: implement path evaluation
    throw new UnsupportedOperationException();
  }

  private static <DEF extends InfoElementDefinition> void addToMap(Collection<DEF> items,
      Map<String, DEF> existingMap) {
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
  protected void parseExportedDefinitions() throws MetaschemaException {
    logger.debug("Processing metaschema '{}'", this.getLocation());

    this.exportedFlagDefinitions = new LinkedHashMap<>();
    this.exportedFieldDefinitions = new LinkedHashMap<>();
    this.exportedAssemblyDefinitions = new LinkedHashMap<>();

    // handle definitions from any included metaschema first
    Map<URI, Metaschema> importedMetaschema = getImportedMetaschema();

    if (!importedMetaschema.isEmpty()) {
      for (Metaschema metaschema : importedMetaschema.values()) {
        addToMap(metaschema.getExportedFlagDefinitions().values(), this.exportedFlagDefinitions);
        addToMap(metaschema.getExportedFieldDefinitions().values(), this.exportedFieldDefinitions);
        addToMap(metaschema.getExportedAssemblyDefinitions().values(), this.exportedAssemblyDefinitions);
      }
    }

    // now handle top-level definitions from this metaschema. Start first with filtering for globals,
    // then add these to the maps
    addToMap(
        getFlagDefinitions().values().stream().filter(new ExportedDefinitionsFilter<>()).collect(Collectors.toList()),
        this.exportedFlagDefinitions);
    addToMap(
        getFieldDefinitions().values().stream().filter(new ExportedDefinitionsFilter<>()).collect(Collectors.toList()),
        this.exportedFieldDefinitions);
    addToMap(getAssemblyDefinitions().values().stream().filter(new ExportedDefinitionsFilter<>())
        .collect(Collectors.toList()), this.exportedAssemblyDefinitions);
  }

  @Override
  public URI getLocation() {
    return location;
  }

  @Override
  public AssemblyDefinition getAssemblyDefinitionByName(String name) {
    // first try local/global top-level definitions from current metaschema
    AssemblyDefinition retval = getAssemblyDefinitions().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = this.exportedAssemblyDefinitions.get(name);
    }
    return retval;
  }

  @Override
  public FieldDefinition getFieldDefinitionByName(String name) {
    // first try local/global top-level definitions from current metaschema
    FieldDefinition retval = getFieldDefinitions().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = this.exportedFieldDefinitions.get(name);
    }
    return retval;
  }

  @Override
  public FlagDefinition getFlagDefinitionByName(String name) {
    // first try local/global top-level definitions from current metaschema
    FlagDefinition retval = getFlagDefinitions().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = this.exportedFlagDefinitions.get(name);
    }
    return retval;
  }
}
