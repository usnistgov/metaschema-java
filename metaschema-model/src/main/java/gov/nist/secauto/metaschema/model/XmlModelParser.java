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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.model.xmlbeans.AssemblyReferenceType;
import gov.nist.secauto.metaschema.model.xmlbeans.ChoiceType;
import gov.nist.secauto.metaschema.model.xmlbeans.FieldReferenceType;
import gov.nist.secauto.metaschema.model.xmlbeans.InlineAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.InlineFieldDefinitionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class XmlModelParser {
  private Map<String, INamedModelInstance> namedModelInstances;
  private Map<String, IFieldInstance> fieldInstances;
  private Map<String, IAssemblyInstance> assemblyInstances;
  private List<? extends IModelInstance> modelInstances;

  // TODO: move back to calling location
  void parseChoice(XmlObject xmlObject, @NonNull IAssemblyDefinition parent) {
    try (XmlCursor cursor = xmlObject.newCursor()) {
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:assembly|$this/m:define-assembly|$this/m:field"
          + "|$this/m:define-field");
      parseInternal(cursor, parent);
    }
  }

  // TODO: move back to calling location
  void parseModel(XmlObject xmlObject, @NonNull IAssemblyDefinition parent) {
    // handle the model
    try (XmlCursor cursor = xmlObject.newCursor()) {
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';"
          + "$this/m:model/m:assembly|$this/m:model/m:define-assembly|$this/m:model/m:field"
          + "|$this/m:model/m:define-field|$this/m:model/m:choice");

      parseInternal(cursor, parent);
    }
  }

  @SuppressWarnings("null")
  @NonNull
  protected <S> Stream<S> append(@NonNull Stream<S> original, @NonNull S item) {
    Stream<S> newStream = Stream.of(item).sequential();
    return Stream.concat(original, newStream);
  }

  @SuppressWarnings("null")
  private void parseInternal(XmlCursor cursor, @NonNull IAssemblyDefinition parent) {

    // ensure the streams are treated as sequential, since concatenated streams will only be sequential
    // if both streams are sequential
    Stream<IFieldInstance> fieldInstances = Stream.empty();
    fieldInstances = fieldInstances.sequential();
    Stream<IAssemblyInstance> assemblyInstances = Stream.empty();
    assemblyInstances = assemblyInstances.sequential();
    Stream<INamedModelInstance> namedModelInstances = Stream.empty();
    namedModelInstances = namedModelInstances.sequential();
    Stream<IModelInstance> modelInstances = Stream.empty();
    modelInstances = modelInstances.sequential();

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof FieldReferenceType) {
        XmlFieldInstance field
            = new XmlFieldInstance((FieldReferenceType) obj, parent); // NOPMD - intentional
        fieldInstances = append(fieldInstances, field);
        namedModelInstances = append(namedModelInstances, field);
        modelInstances = append(modelInstances, field);
      } else if (obj instanceof InlineFieldDefinitionType) {
        XmlInlineFieldDefinition field
            = new XmlInlineFieldDefinition((InlineFieldDefinitionType) obj, parent); // NOPMD - intentional
        fieldInstances = append(fieldInstances, field);
        namedModelInstances = append(namedModelInstances, field);
        modelInstances = append(modelInstances, field);
      } else if (obj instanceof AssemblyReferenceType) {
        XmlAssemblyInstance assembly
            = new XmlAssemblyInstance((AssemblyReferenceType) obj, parent); // NOPMD - intentional
        assemblyInstances = append(assemblyInstances, assembly);
        namedModelInstances = append(namedModelInstances, assembly);
        modelInstances = append(modelInstances, assembly);
      } else if (obj instanceof InlineAssemblyDefinitionType) {
        XmlInlineAssemblyDefinition assembly
            = new XmlInlineAssemblyDefinition((InlineAssemblyDefinitionType) obj, parent); // NOPMD - intentional
        assemblyInstances = append(assemblyInstances, assembly);
        namedModelInstances = append(namedModelInstances, assembly);
        modelInstances = append(modelInstances, assembly);
      } else if (obj instanceof ChoiceType) {
        XmlChoiceInstance choice
            = new XmlChoiceInstance((ChoiceType) obj, parent); // NOPMD - intentional
        // assemblyInstances.putAll(choice.getAssemblyInstanceMap());
        // fieldInstances..putAll(choice.getFieldInstanceMap());
        modelInstances = append(modelInstances, choice);
      }
    }

    this.fieldInstances = fieldInstances
        .collect(Collectors.toMap(IFieldInstance::getEffectiveName, Function.identity(),
            CustomCollectors.useFirstMapper(), LinkedHashMap::new));
    this.assemblyInstances = assemblyInstances
        .collect(Collectors.toMap(IAssemblyInstance::getEffectiveName, Function.identity(),
            CustomCollectors.useFirstMapper(), LinkedHashMap::new));
    this.modelInstances = modelInstances
        .collect(Collectors.toUnmodifiableList());
    this.namedModelInstances = namedModelInstances
        .collect(Collectors.toMap(INamedModelInstance::getEffectiveName, Function.identity(),
            CustomCollectors.useFirstMapper(), LinkedHashMap::new));
  }

  @SuppressWarnings("null")
  @NonNull
  public Map<String, ? extends IFieldInstance> getFieldInstances() {
    return fieldInstances == null ? Collections.emptyMap() : fieldInstances;
  }

  @SuppressWarnings("null")
  @NonNull
  public Map<String, ? extends IAssemblyInstance> getAssemblyInstances() {
    return assemblyInstances == null ? Collections.emptyMap() : assemblyInstances;
  }

  @SuppressWarnings("null")
  @NonNull
  public Map<String, ? extends INamedModelInstance> getNamedModelInstances() {
    return namedModelInstances == null ? Collections.emptyMap() : namedModelInstances;
  }

  @SuppressWarnings("null")
  @NonNull
  protected List<? extends IModelInstance> getModelInstances() {
    return modelInstances == null ? Collections.emptyList() : modelInstances;
  }
}
