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
import gov.nist.secauto.metaschema.model.definitions.DataType;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;
import gov.nist.secauto.metaschema.model.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.instances.XmlGroupAsBehavior;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Metaschema {
  boolean DEFAULT_REQUIRED = false;
  DataType DEFAULT_DATA_TYPE = DataType.STRING;
  int DEFAULT_GROUP_AS_MIN_OCCURS = 0;
  int DEFAULT_GROUP_AS_MAX_OCCURS = 1;
  boolean DEFAULT_FIELD_XML_WRAPPER = true;
  ModuleScopeEnum DEFAULT_MODEL_SCOPE = ModuleScopeEnum.INHERITED;
  boolean DEFAULT_COLLAPSIBLE = true;
  JsonGroupAsBehavior DEFAULT_JSON_GROUP_AS_BEHAVIOR = JsonGroupAsBehavior.SINGLETON_OR_LIST;
  XmlGroupAsBehavior DEFAULT_XML_GROUP_AS_BEHAVIOR = XmlGroupAsBehavior.UNGROUPED;

  /**
   * Retrieves the location where the Metaschema was loaded from.
   * 
   * @return the location
   */
  URI getLocation();

  /**
   * Retrieves the short name for the Metaschema, which provides a textual identifier for the
   * Metaschema instance.
   * 
   * @return the short name
   */
  String getShortName();

  /**
   * Retrieves the XML namespace associated with the Metaschema.
   * 
   * @return a namespace
   */
  URI getXmlNamespace();

  /**
   * Retrieves all Metaschema imported by this Metaschema.
   * 
   * @return a mapping of locations to Metaschema
   */
  Map<URI, Metaschema> getImportedMetaschema();

  /**
   * Retrieves the top-level assembly definitions in this Metaschema.
   * 
   * @return a mapping of name to assembly definition
   */
  Map<String, ? extends AssemblyDefinition> getAssemblyDefinitions();

  /**
   * Retrieves the top-level field definitions in this Metaschema.
   * 
   * @return a mapping of name to field definition
   */
  Map<String, ? extends FieldDefinition> getFieldDefinitions();

  /**
   * Retrieves the top-level assembly and field definitions in this Metaschema.
   * 
   * @return a listing of assembly and field definitions
   */
  List<? extends ObjectDefinition> getAssemblyAndFieldDefinitions();

  /**
   * Retrieves the top-level flag definitions in this Metaschema.
   * 
   * @return a mapping of name to flag definition
   */
  Map<String, ? extends FlagDefinition> getFlagDefinitions();

  /**
   * Retrieves the information elements matching the path.
   * 
   * @param path
   *          a MetaPath expression
   * @return the matching information elements or an empty collection
   */
  Collection<InfoElement> getInfoElementByMetaPath(String path);

  /**
   * Retrieves the assembly definition with a matching name from either: 1) the top-level assembly
   * definitions from this Metaschema, or 2) global assembly definitions from each imported Metaschema
   * in reverse order of import.
   * 
   * @param name
   *          the name of the assembly to find
   * @return the assembly definition
   */
  AssemblyDefinition getAssemblyDefinitionByName(String name);

  /**
   * Retrieves the field definition with a matching name from either: 1) the top-level field
   * definitions from this Metaschema, or 2) global field definitions from each imported Metaschema in
   * reverse order of import.
   * 
   * @param name
   *          the name of the field definition to find
   * @return the field definition
   */
  FieldDefinition getFieldDefinitionByName(String name);

  /**
   * Retrieves the flag definition with a matching name from either: 1) the top-level flag definitions
   * from this Metaschema, or 2) global flag definitions from each imported Metaschema in reverse
   * order of import.
   * 
   * @param name
   *          the name of the flag definition to find
   * @return the flag definition
   */
  FlagDefinition getFlagDefinitionByName(String name);

  /**
   * Retrieves a mapping of the root name to top-level assembly definitions that are marked as roots
   * from the current Metaschema.
   * 
   * @return a map of root names to assembly definitions marked as root
   */
  Map<String, AssemblyDefinition> getRootAssemblyDefinitions();

  /**
   * Retrieve the top-level flag definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to flag definition
   */
  Map<String, FlagDefinition> getExportedFlagDefinitions();

  /**
   * Retrieve the top-level field definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to field definition
   */
  Map<String, FieldDefinition> getExportedFieldDefinitions();

  /**
   * Retrieve the top-level assembly definitions that are marked global in this metaschema or in any
   * imported Metaschema. The resulting map is built by adding global definitions from each imported
   * metaschema in order of import, then adding global definitions from the current Metaschema. Such a
   * map is built in this way for each imported Metaschema in the chain. Values for clashing keys will
   * be replaced in this order, giving preference to the "closest" definition
   * 
   * @return a mapping of name to assembly definition
   */
  Map<String, AssemblyDefinition> getExportedAssemblyDefinitions();
}
