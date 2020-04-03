/**
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

import gov.nist.itl.metaschema.model.xml.AssemblyDocument;
import gov.nist.itl.metaschema.model.xml.ChoiceDocument;
import gov.nist.itl.metaschema.model.xml.FieldDocument;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.instances.AbstractChoiceInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlChoiceInstance extends AbstractChoiceInstance {
  private final ChoiceDocument.Choice xmlChoice;
  private final Map<String, ModelInstance> namedModelInstances;
  private final Map<String, XmlFieldInstance> fieldInstances;
  private final Map<String, XmlAssemblyInstance> assemblyInstances;

  /**
   * Constructs a mutually exclusive choice between two possible objects.
   * 
   * @param xmlChoice
   *          the XML for the choice definition bound to Java objects
   * @param containingAssembly
   *          the parent assembly definition that contains this choice
   */
  public XmlChoiceInstance(ChoiceDocument.Choice xmlChoice, AssemblyDefinition containingAssembly) {
    super(containingAssembly);
    this.xmlChoice = xmlChoice;

    XmlCursor cursor = xmlChoice.newCursor();
    cursor.selectPath(
        "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" + "$this/m:assembly|$this/m:field");

    Map<String, ModelInstance> namedModelInstances = new LinkedHashMap<>();
    Map<String, XmlFieldInstance> fieldInstances = new LinkedHashMap<>();
    Map<String, XmlAssemblyInstance> assemblyInstances = new LinkedHashMap<>();
    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FieldDocument.Field) {
        XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this.getContainingDefinition());
        fieldInstances.put(field.getName(), field);
        namedModelInstances.put(field.getName(), field);
      } else if (obj instanceof AssemblyDocument.Assembly) {
        XmlAssemblyInstance assembly
            = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this.getContainingDefinition());
        assemblyInstances.put(assembly.getName(), assembly);
        namedModelInstances.put(assembly.getName(), assembly);
      }
    }

    this.namedModelInstances
        = namedModelInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(namedModelInstances);
    this.fieldInstances
        = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
    this.assemblyInstances
        = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
  }

  @Override
  public Map<String, ModelInstance> getNamedModelInstances() {
    return namedModelInstances;
  }

  @Override
  public Map<String, XmlFieldInstance> getFieldInstances() {
    return fieldInstances;
  }

  @Override
  public Map<String, XmlAssemblyInstance> getAssemblyInstances() {
    return assemblyInstances;
  }

  @Override
  public List<InfoElementInstance> getInstances() {
    return namedModelInstances.values().stream().collect(Collectors.toList());
  }

  protected ChoiceDocument.Choice getXmlChoice() {
    return xmlChoice;
  }
}
