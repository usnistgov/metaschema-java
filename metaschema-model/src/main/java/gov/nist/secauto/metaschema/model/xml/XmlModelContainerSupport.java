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

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.xmlbeans.ChoiceDocument.Choice;

import org.apache.xmlbeans.XmlObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlModelContainerSupport {

  @NotNull
  private final List<@NotNull ? extends IModelInstance> modelInstances;
  @NotNull
  private final Map<@NotNull String, ? extends INamedModelInstance> namedModelInstances;
  @NotNull
  private final Map<@NotNull String, ? extends IFieldInstance> fieldInstances;
  @NotNull
  private final Map<@NotNull String, ? extends IAssemblyInstance> assemblyInstances;

  /**
   * Construct a new model container.
   * 
   * @param xmlContent
   *          the XMLBeans content to parse
   * @param containingAssembly
   *          the assembly containing this model
   */
  public XmlModelContainerSupport(@NotNull XmlObject xmlContent, @NotNull IAssemblyDefinition containingAssembly) {
    XmlModelParser parser = new XmlModelParser();
    if (xmlContent instanceof Choice) {
      parser.parseChoice(xmlContent, containingAssembly);
    } else {
      parser.parseModel(xmlContent, containingAssembly);
    }
    this.modelInstances = parser.getModelInstances();
    this.namedModelInstances = parser.getNamedModelInstances();
    this.fieldInstances = parser.getFieldInstances();
    this.assemblyInstances = parser.getAssemblyInstances();
  }

  /**
   * Get a listing of all model instances.
   * 
   * @return the listing
   */
  @NotNull
  public List<@NotNull ? extends IModelInstance> getModelInstances() {
    return modelInstances;
  }

  /**
   * Get a mapping of all named model instances, mapped from their effective name to the instance.
   * 
   * @return the mapping
   */
  @NotNull
  public Map<@NotNull String, ? extends INamedModelInstance> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  /**
   * Get a mapping of all field instances, mapped from their effective name to the instance.
   * 
   * @return the mapping
   */
  @NotNull
  public Map<@NotNull String, ? extends IFieldInstance> getFieldInstanceMap() {
    return fieldInstances;
  }

  /**
   * Get a mapping of all assembly instances, mapped from their effective name to the instance.
   * 
   * @return the mapping
   */
  @NotNull
  public Map<@NotNull String, ? extends IAssemblyInstance> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  /**
   * Get a listing of all choice instances.
   * 
   * @return the listing
   */
  @SuppressWarnings("null")
  @NotNull
  public List<@NotNull ? extends IChoiceInstance> getChoiceInstances() {
    // this shouldn't get called all that often, so this is better than allocating memory
    return getModelInstances().stream()
        .filter(obj -> obj instanceof IChoiceInstance)
        .map(obj -> (IChoiceInstance) obj)
        .collect(Collectors.toList());
  }
}
