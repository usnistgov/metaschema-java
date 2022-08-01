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
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
  String METASCHEMA_XML_NS = "http://csrc.nist.gov/ns/oscal/metaschema/1.0";

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

  /**
   * Retrieves the location where the Metaschema was loaded from.
   * 
   * @return the location, or {@code null} if this information is not available
   */
  URI getLocation();

  /**
   * Get the long name for the Metaschema.
   * 
   * @return the name
   */
  @NonNull
  MarkupLine getName();

  /**
   * Get the revision of the Metaschema.
   * 
   * @return the revision
   */
  @NonNull
  String getVersion();

  /**
   * Retrieve the remarks associated with this Metaschema, if any.
   * 
   * @return the remarks or {@code null} if no remarks are defined
   */
  @Nullable
  MarkupMultiline getRemarks();

  /**
   * Retrieves the unique short name for the Metaschema, which provides a textual identifier for the
   * Metaschema instance.
   * 
   * @return the short name
   */
  @NonNull
  String getShortName();

  /**
   * Retrieves the XML namespace associated with the Metaschema.
   * 
   * @return a namespace
   */
  @NonNull
  URI getXmlNamespace();

  /**
   * Retrieve the JSON schema base URI associated with the Metaschema.
   * 
   * @return the base URI
   */
  @NonNull
  URI getJsonBaseUri();

  /**
   * Get the qualified name associated with the Metaschema.
   * 
   * @return the qualified name
   */
  default QName getQName() {
    return new QName(getXmlNamespace().toString(), getShortName());
  }

  /**
   * Retrieves all Metaschema imported by this Metaschema.
   * 
   * @return a list of imported Metaschema
   */
  @NonNull
  List<? extends IMetaschema> getImportedMetaschemas();

  /**
   * Retrieve the imported Metaschema with the specified name, if it exists.
   * 
   * @param name
   *          the short name of the Metschema to retrieve
   * @return the imported Metaschema or {@code null} if it doesn't exist
   */
  @Nullable
  IMetaschema getImportedMetaschemaByShortName(String name);

  /**
   * Retrieves the top-level assembly definitions in this Metaschema.
   * 
   * @return the collection of assembly definitions
   */
  @NonNull
  Collection<? extends IAssemblyDefinition> getAssemblyDefinitions();

  /**
   * Retrieves the top-level assembly definition in this Metaschema with the matching name, if it
   * exists.
   * 
   * @param name
   *          the definition name
   * 
   * @return the matching assembly definition, or {@code null} if none match
   */
  @Nullable
  IAssemblyDefinition getAssemblyDefinitionByName(@NonNull String name);

  /**
   * Retrieves the top-level field definitions in this Metaschema.
   * 
   * @return the collection of field definitions
   */
  @NonNull
  Collection<? extends IFieldDefinition> getFieldDefinitions();

  /**
   * Retrieves the top-level field definition in this Metaschema with the matching name, if it exists.
   * 
   * @param name
   *          the definition name
   * 
   * @return the matching field definition, or {@code null} if none match
   */
  @Nullable
  IFieldDefinition getFieldDefinitionByName(@NonNull String name);

  /**
   * Retrieves the top-level assembly and field definitions in this Metaschema.
   * 
   * @return a listing of assembly and field definitions
   */
  @NonNull
  default List<? extends IModelDefinition> getAssemblyAndFieldDefinitions() {
    return ObjectUtils.notNull(
        Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
            .collect(Collectors.toList()));
  }

  /**
   * Retrieves the top-level flag definitions in this Metaschema.
   * 
   * @return the collection of flag definitions
   */
  @NonNull
  Collection<? extends IFlagDefinition> getFlagDefinitions();

  /**
   * Retrieves the top-level flag definition in this Metaschema with the matching name, if it exists.
   * 
   * @param name
   *          the definition name
   * 
   * @return the matching flag definition, or {@code null} if none match
   */
  @Nullable
  IFlagDefinition getFlagDefinitionByName(@NonNull String name);

  // /**
  // * Retrieves the information elements matching the path.
  // *
  // * @param path
  // * a MetaPath expression
  // * @return the matching information elements or an empty collection
  // */
  // @NonNull
  // Collection<@NonNull ? extends IModelElement> getInfoElementsByMetapath(@NonNull String path);

  /**
   * Retrieves the assembly definition with a matching name from either: 1) the top-level assembly
   * definitions from this Metaschema, or 2) global assembly definitions from each imported Metaschema
   * in reverse order of import.
   * 
   * @param name
   *          the name of the assembly to find
   * @return the assembly definition
   */
  @Nullable
  default IAssemblyDefinition getScopedAssemblyDefinitionByName(@NonNull String name) {
    // first try local/global top-level definitions from current metaschema
    IAssemblyDefinition retval = getAssemblyDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedAssemblyDefinitionByName(name);
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
  @Nullable
  default IFieldDefinition getScopedFieldDefinitionByName(@NonNull String name) {
    // first try local/global top-level definitions from current metaschema
    IFieldDefinition retval = getFieldDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedFieldDefinitionByName(name);
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
  @Nullable
  default IFlagDefinition getScopedFlagDefinitionByName(@NonNull String name) {
    // first try local/global top-level definitions from current metaschema
    IFlagDefinition retval = getFlagDefinitionByName(name);
    if (retval == null) {
      // try global definitions from imported metaschema
      retval = getExportedFlagDefinitionByName(name);
    }
    return retval;
  }

  /**
   * Retrieves the top-level assembly definitions that are marked as roots from the current
   * Metaschema.
   * 
   * @return a listing of assembly definitions marked as root
   */
  @NonNull
  default Collection<? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return ObjectUtils.notNull(getExportedAssemblyDefinitions().stream()
        .filter(allRootAssemblyDefinitions())
        .collect(Collectors.toList()));
  }

  /**
   * Retrieve the top-level flag definitions that are marked global in this Metaschema or in any
   * imported Metaschema. The resulting collection is built by adding global definitions from each
   * imported Metaschema in order of import, then adding global definitions from the current
   * Metaschema. Such a map is built in this way for each imported Metaschema in the chain. Values for
   * clashing keys will be replaced in this order, giving preference to the "closest" definition.
   * 
   * @return the collection of exported flag definitions
   */
  @NonNull
  Collection<? extends IFlagDefinition> getExportedFlagDefinitions();

  /**
   * Retrieves the exported named flag definition, if it exists.
   * <p>
   * For information about how flag definitions are exported see
   * {@link #getExportedFlagDefinitions()}.
   * 
   * @param name
   *          the definition name
   * @return the flag definition, or {@code null} if it doesn't exist.
   */
  @Nullable
  IFlagDefinition getExportedFlagDefinitionByName(String name);

  /**
   * Retrieve the top-level field definitions that are marked global in this Metaschema or in any
   * imported Metaschema. The resulting collection is built by adding global definitions from each
   * imported Metaschema in order of import, then adding global definitions from the current
   * Metaschema. Such a map is built in this way for each imported Metaschema in the chain. Values for
   * clashing keys will be replaced in this order, giving preference to the "closest" definition
   * 
   * @return the collection of exported field definitions
   */
  @NonNull
  Collection<? extends IFieldDefinition> getExportedFieldDefinitions();

  /**
   * Retrieves the exported named field definition, if it exists.
   * <p>
   * For information about how field definitions are exported see
   * {@link #getExportedFieldDefinitions()}.
   * 
   * @param name
   *          the definition name
   * @return the field definition, or {@code null} if it doesn't exist.
   */
  @Nullable
  IFieldDefinition getExportedFieldDefinitionByName(String name);

  /**
   * Retrieve the top-level assembly definitions that are marked global in this Metaschema or in any
   * imported Metaschema. The resulting collection is built by adding global definitions from each
   * imported Metaschema in order of import, then adding global definitions from the current
   * Metaschema. This collection is built in this way for each imported Metaschema in the chain. Items
   * with duplicate names will be replaced in this order, giving preference to the "closest"
   * definition
   * 
   * @return the collection of exported assembly definitions
   */
  @NonNull
  Collection<? extends IAssemblyDefinition> getExportedAssemblyDefinitions();

  /**
   * Retrieves the exported named assembly definition, if it exists.
   * <p>
   * For information about how assembly definitions are exported see
   * {@link #getExportedFieldDefinitions()}.
   * 
   * @param name
   *          the definition name
   * @return the assembly definition, or {@code null} if it doesn't exist.
   */
  @Nullable
  IAssemblyDefinition getExportedAssemblyDefinitionByName(String name);
}
