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

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlaggedDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The API for accessing information about a given Metaschema.
 * <p>
 * A Metaschema may import another Metaschema. This import graph can be accessed using
 * {@link #getImportedMetaschemas()}.
 * <p>
 * Global scoped Metaschema definitions can be accessed using
 * {@link #getScopedAssemblyDefinitionByName(String)},
 * {@link #getScopedFieldDefinitionByName(String)}, and
 * {@link #getScopedFlagDefinitionByName(String)}. These methods take into consideration the import
 * order to provide the global definitions that are in scope within the given Metschema.
 * <p>
 * Global scoped definitions exported by this Metaschema, available for use by importing
 * Metaschemas, can be accessed using {@link #getExportedAssemblyDefinitions()},
 * {@link #getExportedFieldDefinitions()}, and {@link #getExportedFlagDefinitions()}.
 * <p>
 * Global scoped definitions defined direclty within the given Metaschema can be accessed using
 * {@link #getAssemblyDefinitions()}, {@link #getFieldDefinitions()}, and
 * {@link #getFlagDefinitions()}, along with similarly named accessors.
 */
public interface IMetaschema {
  /**
   * Retrieves the location where the Metaschema was loaded from.
   * 
   * @return the location
   */
  @NotNull
  URI getLocation();

  /**
   * Get the long name for the Metaschema.
   * 
   * @return the name
   */
  @NotNull
  MarkupLine getName();

  /**
   * Get the revision of the Metaschema.
   * 
   * @return the revision
   */
  @NotNull
  String getVersion();

  /**
   * Retrieve the remarks associated with this Metaschema, if any.
   * 
   * @return the remarks or {@code null} if no remarks are defined
   */
  @Nullable
  MarkupMultiline getRemarks();

  /**
   * Retrieves the short name for the Metaschema, which provides a textual identifier for the
   * Metaschema instance.
   * 
   * @return the short name
   */
  @NotNull
  String getShortName();

  /**
   * Retrieves the XML namespace associated with the Metaschema.
   * 
   * @return a namespace
   */
  @NotNull
  URI getXmlNamespace();

  /**
   * Retrieve the JSON base URI associated with the Metaschema.
   * 
   * @return the base URI
   */
  @NotNull
  URI getJsonBaseUri();

  /**
   * Retrieves all Metaschema imported by this Metaschema.
   * 
   * @return a mapping of locations to Metaschema
   */
  @NotNull
  Map<@NotNull URI, ? extends IMetaschema> getImportedMetaschemaMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IMetaschema> getImportedMetaschemas() {
    return getImportedMetaschemaMap().values();
  }

  @NotNull
  Map<@NotNull String, ? extends IMetaschema> getImportedMetaschemaByShortNames();

  default IMetaschema getImportedMetaschemaByShortName(@NotNull String name) {
    return getImportedMetaschemaByShortNames().get(name);
  }

  /**
   * Retrieves the top-level assembly definitions in this Metaschema.
   * 
   * @return a mapping of name to assembly definition
   */
  @NotNull
  Map<@NotNull String, ? extends IAssemblyDefinition> getAssemblyDefinitionMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IAssemblyDefinition> getAssemblyDefinitions() {
    return getAssemblyDefinitionMap().values();
  }

  /**
   * Retrieves the top-level field definitions in this Metaschema.
   * 
   * @return a mapping of name to field definition
   */
  Map<String, ? extends IFieldDefinition> getFieldDefinitionMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IFieldDefinition> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  /**
   * Retrieves the top-level assembly and field definitions in this Metaschema.
   * 
   * @return a listing of assembly and field definitions
   */
  @NotNull
  List<@NotNull ? extends IFlaggedDefinition> getAssemblyAndFieldDefinitions();

  /**
   * Retrieves the top-level flag definitions in this Metaschema.
   * 
   * @return a mapping of name to flag definition
   */
  @NotNull
  Map<@NotNull String, ? extends IFlagDefinition> getFlagDefinitionMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IFlagDefinition> getFlagDefinitions() {
    return getFlagDefinitionMap().values();
  }

  /**
   * Retrieves the information elements matching the path.
   * 
   * @param path
   *          a MetaPath expression
   * @return the matching information elements or an empty collection
   */
  @NotNull
  Collection<@NotNull ? extends IModelElement> getInfoElementsByMetapath(@NotNull String path);

  /**
   * Retrieves the assembly definition with a matching name from either: 1) the top-level assembly
   * definitions from this Metaschema, or 2) global assembly definitions from each imported Metaschema
   * in reverse order of import.
   * 
   * @param name
   *          the name of the assembly to find
   * @return the assembly definition
   */
  default IAssemblyDefinition getScopedAssemblyDefinitionByName(@NotNull String name) {
    // first try local/global top-level definitions from current metaschema
    IAssemblyDefinition retval = getAssemblyDefinitionMap().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedAssemblyDefinitionMap().get(name);
    }
    return retval;
  }

  /**
   * Retrieves the field definition with a matching name from either: 1) the top-level field
   * definitions from this Metaschema, or 2) global field definitions from each imported Metaschema in
   * reverse order of import.
   * 
   * @param name
   *          the name of the field definition to find
   * @return the field definition
   */
  default IFieldDefinition getScopedFieldDefinitionByName(@NotNull String name) {
    // first try local/global top-level definitions from current metaschema
    IFieldDefinition retval = getFieldDefinitionMap().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedFieldDefinitionMap().get(name);
    }
    return retval;
  }

  /**
   * Retrieves the flag definition with a matching name from either: 1) the top-level flag definitions
   * from this Metaschema, or 2) global flag definitions from each imported Metaschema in reverse
   * order of import.
   * 
   * @param name
   *          the name of the flag definition to find
   * @return the flag definition
   */
  default IFlagDefinition getScopedFlagDefinitionByName(@NotNull String name) {
    // first try local/global top-level definitions from current metaschema
    IFlagDefinition retval = getFlagDefinitionMap().get(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedFlagDefinitionMap().get(name);
    }
    return retval;
  }

  /**
   * Retrieves a mapping of the root name to top-level assembly definitions that are marked as roots
   * from the current Metaschema.
   * 
   * @return a map of root names to assembly definitions marked as root
   */
  @NotNull
  Map<@NotNull String, ? extends IAssemblyDefinition> getRootAssemblyDefinitionMap();

  /**
   * Retrieves the top-level assembly definitions that are marked as roots from the current
   * Metaschema.
   * 
   * @return a map of root names to assembly definitions marked as root
   */
  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return getRootAssemblyDefinitionMap().values();
  }

  /**
   * Retrieve the top-level flag definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to flag definition
   */
  @NotNull
  Map<@NotNull String, ? extends IFlagDefinition> getExportedFlagDefinitionMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IFlagDefinition> getExportedFlagDefinitions() {
    return getExportedFlagDefinitionMap().values();
  }

  /**
   * Retrieve the top-level field definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to field definition
   */
  @NotNull
  Map<@NotNull String, ? extends IFieldDefinition> getExportedFieldDefinitionMap();

  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IFieldDefinition> getExportedFieldDefinitions() {
    return getExportedFieldDefinitionMap().values();
  }

  /**
   * Retrieve the top-level assembly definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to assembly definition
   */
  @NotNull
  Map<@NotNull String, ? extends IAssemblyDefinition> getExportedAssemblyDefinitionMap();

  /**
   * Retrieve the top-level assembly definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting collection is built by adding global definitions from each
   * imported metaschema in order of import, then adding global definitions from the current
   * Metaschema. This collection is built in this way for each imported Metaschema in the chain. Items
   * with duplicate names will be replaced in this order, giving preference to the "closest"
   * definition
   * 
   * @return a mapping of name to assembly definition
   */
  @SuppressWarnings("null")
  @NotNull
  default Collection<@NotNull ? extends IAssemblyDefinition> getExportedAssemblyDefinitions() {
    return getExportedAssemblyDefinitionMap().values();
  }
}
