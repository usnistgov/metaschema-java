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

import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.definitions.IXmlAssemblyDefinition;
import gov.nist.secauto.metaschema.model.instances.IXmlAssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.IXmlChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.IXmlFieldInstance;
import gov.nist.secauto.metaschema.model.instances.IXmlNamedModelInstance;
import gov.nist.secauto.metaschema.model.xmlbeans.ChoiceDocument.Choice;

import org.apache.xmlbeans.XmlObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlModelContainerSupport {

  @NotNull
  private final Map<@NotNull String, ? extends IXmlFieldInstance> fieldInstances;
  @NotNull
  private final Map<@NotNull String, ? extends IXmlAssemblyInstance> assemblyInstances;
  @NotNull
  private final Map<@NotNull String, ? extends IXmlNamedModelInstance> namedModelInstances;
  @NotNull
  private final List<@NotNull ? extends IModelInstance> modelInstances;

  public XmlModelContainerSupport(@NotNull XmlObject xmlContent, @NotNull IXmlAssemblyDefinition containingAssembly) {
    XmlModelParser parser = new XmlModelParser();
    if (xmlContent instanceof Choice) {
      parser.parseChoice(xmlContent, containingAssembly);
    } else {
      parser.parseModel(xmlContent, containingAssembly);
    }
    this.namedModelInstances = parser.getNamedModelInstances();
    this.fieldInstances = parser.getFieldInstances();
    this.assemblyInstances = parser.getAssemblyInstances();
    this.modelInstances = parser.getModelInstances();
  }

  @NotNull
  public Map<@NotNull String, ? extends IXmlNamedModelInstance> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  @NotNull
  public Map<@NotNull String, ? extends IXmlFieldInstance> getFieldInstanceMap() {
    return fieldInstances;
  }

  @NotNull
  public Map<@NotNull String, ? extends IXmlAssemblyInstance> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  @NotNull
  public List<@NotNull ? extends IXmlChoiceInstance> getChoiceInstances() {
    // this shouldn't get called all that often, so this is better than allocating memory
    return getModelInstances().stream().filter(obj -> obj instanceof IXmlChoiceInstance)
        .map(obj -> (IXmlChoiceInstance) obj).collect(Collectors.toList());
  }

  @NotNull
  public List<@NotNull ? extends IModelInstance> getModelInstances() {
    return modelInstances;
  }
}
