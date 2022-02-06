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
import gov.nist.secauto.metaschema.model.xmlbeans.AssemblyDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.ChoiceDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.FieldDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.LocalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.LocalFieldDefinitionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class XmlModelParser {
  private Map<@NotNull String, IXmlNamedModelInstance> namedModelInstances;
  private Map<@NotNull String, IXmlFieldInstance> fieldInstances;
  private Map<@NotNull String, IXmlAssemblyInstance> assemblyInstances;
  private List<@NotNull ? extends IModelInstance> modelInstances;

  public XmlModelParser() {
    // TODO Auto-generated constructor stub
  }

  // TODO: move back to calling location
  void parseChoice(XmlObject xmlObject, @NotNull IXmlAssemblyDefinition parent) {
    XmlCursor cursor = xmlObject.newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:assembly|$this/m:define-assembly|$this/m:field"
        + "|$this/m:define-field");
    parseInternal(cursor, parent);
  }

  // TODO: move back to calling location
  void parseModel(XmlObject xmlObject, @NotNull IXmlAssemblyDefinition parent) {
    // handle the model
    XmlCursor cursor = xmlObject.newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
        + "$this/m:model/m:assembly|$this/m:model/m:define-assembly|$this/m:model/m:field"
        + "|$this/m:model/m:define-field|$this/m:model/m:choice");

    parseInternal(cursor, parent);
  }

  private void parseInternal(XmlCursor cursor, @NotNull IXmlAssemblyDefinition parent) {
    Map<@NotNull String, IXmlFieldInstance> fieldInstances = new LinkedHashMap<>();
    Map<@NotNull String, IXmlAssemblyInstance> assemblyInstances = new LinkedHashMap<>();
    List<IModelInstance> modelInstances = new ArrayList<>(cursor.getSelectionCount());
    List<@NotNull IXmlNamedModelInstance> namedModelInstances = new LinkedList<>();

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FieldDocument.Field) {
        IXmlFieldInstance field = new XmlFieldInstance((FieldDocument.Field) obj, parent);
        fieldInstances.put(field.getEffectiveName(), field);
        modelInstances.add(field);
        namedModelInstances.add(field);
      } else if (obj instanceof LocalFieldDefinitionType) {
        IXmlFieldInstance field = new XmlLocalFieldDefinition((LocalFieldDefinitionType) obj, parent);
        fieldInstances.put(field.getEffectiveName(), field);
        modelInstances.add(field);
        namedModelInstances.add(field);
      } else if (obj instanceof AssemblyDocument.Assembly) {
        IXmlAssemblyInstance assembly = new XmlAssemblyInstance((AssemblyDocument.Assembly) obj, parent);
        assemblyInstances.put(assembly.getEffectiveName(), assembly);
        modelInstances.add(assembly);
        namedModelInstances.add(assembly);
      } else if (obj instanceof LocalAssemblyDefinitionType) {
        IXmlAssemblyInstance assembly = new XmlLocalAssemblyDefinition((LocalAssemblyDefinitionType) obj, parent);
        assemblyInstances.put(assembly.getEffectiveName(), assembly);
        modelInstances.add(assembly);
        namedModelInstances.add(assembly);
      } else if (obj instanceof ChoiceDocument.Choice) {
        IXmlChoiceInstance choice = new XmlChoiceInstance((ChoiceDocument.Choice) obj, parent);
        assemblyInstances.putAll(choice.getAssemblyInstanceMap());
        fieldInstances.putAll(choice.getFieldInstanceMap());
        modelInstances.add(choice);
      }
    }

    this.fieldInstances
        = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);

    this.assemblyInstances
        = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);

    this.modelInstances
        = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);

    if (namedModelInstances.isEmpty()) {
      this.namedModelInstances = Collections.emptyMap();
    } else {
      // TODO: build this in one pass
      this.namedModelInstances = Collections.unmodifiableMap(
          namedModelInstances.stream().collect(Collectors.toMap(IXmlNamedModelInstance::getEffectiveName, v -> v)));
    }
  }

  @SuppressWarnings("null")
  @NotNull
  public Map<@NotNull String, ? extends IXmlNamedModelInstance> getNamedModelInstances() {
    return namedModelInstances == null ? Collections.emptyMap() : namedModelInstances;
  }

  @SuppressWarnings("null")
  @NotNull
  public Map<@NotNull String, ? extends IXmlFieldInstance> getFieldInstances() {
    return fieldInstances == null ? Collections.emptyMap() : fieldInstances;
  }

  @SuppressWarnings("null")
  @NotNull
  public Map<@NotNull String, ? extends IXmlAssemblyInstance> getAssemblyInstances() {
    return assemblyInstances == null ? Collections.emptyMap() : assemblyInstances;
  }

  @SuppressWarnings("null")
  @NotNull
  protected List<@NotNull ? extends IModelInstance> getModelInstances() {
    return modelInstances == null ? Collections.emptyList() : modelInstances;
  }
}
