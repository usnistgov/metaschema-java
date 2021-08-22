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

import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.instances.AbstractChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.AssemblyModelInstance;
import gov.nist.secauto.metaschema.model.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.instances.ObjectModelInstance;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.AssemblyDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ChoiceDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.FieldDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.LocalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.LocalFieldDefinitionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlChoiceInstance
    extends AbstractChoiceInstance {
  private final ChoiceDocument.Choice xmlChoice;
  private final Map<String, FieldInstance<?>> fieldInstances;
  private final Map<String, AssemblyInstance<?>> assemblyInstances;
  private final Map<String, ObjectModelInstance<?>> namedModelInstances;
  private final List<AssemblyModelInstance> modelInstances;

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

    // handle the model
    XmlCursor cursor = xmlChoice.newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:assembly|$this/m:define-assembly|$this/m:field|$this/m:define-field|$this/m:choice");

    Map<String, FieldInstance<?>> fieldInstances = new LinkedHashMap<>();
    List<AssemblyInstance<?>> assemblyInstances = new LinkedList<>();
    List<AssemblyModelInstance> modelInstances = new ArrayList<>(cursor.getSelectionCount());
    List<ObjectModelInstance<?>> namedModelInstances = new LinkedList<>();

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FieldDocument.Field) {
        XmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, this.getContainingDefinition());
        fieldInstances.put(field.getEffectiveName(), field);
        modelInstances.add(field);
        namedModelInstances.add(field);
      } else if (obj instanceof LocalFieldDefinitionType) {
        XmlLocalFieldDefinition field
            = new XmlLocalFieldDefinition((LocalFieldDefinitionType) obj, this.getContainingDefinition());
        fieldInstances.put(field.getEffectiveName(), field);
        modelInstances.add(field);
        namedModelInstances.add(field);
      } else if (obj instanceof AssemblyDocument.Assembly) {
        XmlAssemblyInstance assembly
            = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, this.getContainingDefinition());
        assemblyInstances.add(assembly);
        modelInstances.add(assembly);
        namedModelInstances.add(assembly);
      } else if (obj instanceof LocalAssemblyDefinitionType) {
        XmlLocalAssemblyDefinition assembly
            = new XmlLocalAssemblyDefinition((LocalAssemblyDefinitionType) obj, this.getContainingDefinition());
        assemblyInstances.add(assembly);
        modelInstances.add(assembly);
        namedModelInstances.add(assembly);
      } else if (obj instanceof ChoiceDocument.Choice) {
        XmlChoiceInstance choice = new XmlChoiceInstance((ChoiceDocument.Choice) obj, this.getContainingDefinition());
        assemblyInstances.addAll(choice.getAssemblyInstances().values());
        fieldInstances.putAll(choice.getFieldInstances());
        modelInstances.add(choice);
      }
    }

    this.fieldInstances
        = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);

    if (assemblyInstances.isEmpty()) {
      this.assemblyInstances = Collections.emptyMap();
    } else {
      // TODO: build this in one pass
      this.assemblyInstances = Collections.unmodifiableMap(
          assemblyInstances.stream().collect(Collectors.toMap(AssemblyInstance::getEffectiveName, v -> v)));
    }

    this.modelInstances
        = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);

    if (namedModelInstances.isEmpty()) {
      this.namedModelInstances = Collections.emptyMap();
    } else {
      // TODO: build this in one pass
      this.namedModelInstances = Collections.unmodifiableMap(
          namedModelInstances.stream().collect(Collectors.toMap(ObjectModelInstance::getEffectiveName, v -> v)));
    }
  }

  @Override
  public String getGroupAsName() {
    // a choice does not have a name
    return null;
  }

  /**
   * Get the underlying XML data.
   * 
   * @return the underlying XML data
   */
  protected ChoiceDocument.Choice getXmlChoice() {
    return xmlChoice;
  }

  @Override
  public Map<String, ObjectModelInstance<?>> getNamedModelInstances() {
    return namedModelInstances;
  }

  @Override
  public Map<String, FieldInstance<?>> getFieldInstances() {
    return fieldInstances;
  }

  @Override
  public Map<String, AssemblyInstance<?>> getAssemblyInstances() {
    return assemblyInstances;
  }

  @Override
  public List<ChoiceInstance> getChoiceInstances() {
    // this shouldn't get called all that often, so this is better than allocating memory
    return modelInstances.stream().filter(obj -> obj instanceof ChoiceInstance)
        .map(obj -> (ChoiceInstance) obj)
        .collect(Collectors.toList());
  }

  @Override
  public List<AssemblyModelInstance> getModelInstances() {
    return modelInstances;
  }

  @Override
  public MarkupMultiline getRemarks() {
    // TODO: add support if remarks are added
    return null;
  }
}
