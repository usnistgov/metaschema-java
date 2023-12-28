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

package gov.nist.secauto.metaschema.schemagen.xml.schematype;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupDataTypeProvider;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IContainerFlag;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.impl.DocumentationGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.impl.XmlGenerationState;

import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlComplexTypeAssemblyDefinition
    extends AbstractXmlComplexType<IAssemblyDefinition> {

  public XmlComplexTypeAssemblyDefinition(
      @NonNull QName qname,
      @NonNull IAssemblyDefinition definition) {
    super(qname, definition);
  }

  @Override
  protected void generateTypeBody(XmlGenerationState state) throws XMLStreamException {
    IAssemblyDefinition definition = getDefinition();

    Collection<? extends IModelInstance> modelInstances = definition.getModelInstances();
    if (!modelInstances.isEmpty()) {
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "sequence", XmlSchemaGenerator.NS_XML_SCHEMA);
      for (IModelInstance modelInstance : modelInstances) {
        assert modelInstance != null;
        generateModelInstance(modelInstance, state);
      }
      state.writeEndElement();
    }

    Collection<? extends IFlagInstance> flagInstances = definition.getFlagInstances();
    if (!flagInstances.isEmpty()) {
      for (IFlagInstance flagInstance : flagInstances) {
        assert flagInstance != null;
        generateFlagInstance(flagInstance, state);
      }
    }
  }

  protected void generateModelInstance( // NOPMD acceptable complexity
      @NonNull IModelInstance modelInstance,
      @NonNull XmlGenerationState state)
      throws XMLStreamException {

    boolean grouped = false;
    if (XmlGroupAsBehavior.GROUPED.equals(modelInstance.getXmlGroupAsBehavior())) {
      // handle grouping
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "element", XmlSchemaGenerator.NS_XML_SCHEMA);

      QName groupAsQName = ObjectUtils.requireNonNull(modelInstance.getXmlGroupAsQName());

      if (state.getDefaultNS().equals(groupAsQName.getNamespaceURI())) {
        state.writeAttribute("name", ObjectUtils.requireNonNull(groupAsQName.getLocalPart()));
      } else {
        throw new SchemaGenerationException(
            String.format("Attempt to create element '%s' on definition '%s' with different namespace", groupAsQName,
                getDefinition().toCoordinates()));
      }

      if (modelInstance.getMinOccurs() == 0) {
        // this is an optional instance group
        state.writeAttribute("minOccurs", "0");
      }

      // now generate the child elements of the group
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "complexType", XmlSchemaGenerator.NS_XML_SCHEMA);
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "sequence", XmlSchemaGenerator.NS_XML_SCHEMA);

      // mark that we need to close these elements
      grouped = true;
    }

    switch (modelInstance.getModelType()) {
    case ASSEMBLY:
      generateNamedModelInstance((INamedModelInstance) modelInstance, grouped, state);
      break;
    case FIELD: {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;
      if (fieldInstance.isEffectiveValueWrappedInXml()) {
        generateNamedModelInstance(fieldInstance, grouped, state);
      } else {
        generateUnwrappedFieldInstance(fieldInstance, grouped, state);
      }
      break;
    }
    case CHOICE:
      generateChoiceModelInstance((IChoiceInstance) modelInstance, state);
      break;
    default:
      throw new UnsupportedOperationException(modelInstance.getModelType().toString());
    }

    if (grouped) {
      state.writeEndElement(); // xs:sequence
      state.writeEndElement(); // xs:complexType
      state.writeEndElement(); // xs:element
    }
  }

  protected void generateNamedModelInstance(
      @NonNull INamedModelInstance modelInstance,
      boolean grouped,
      @NonNull XmlGenerationState state) throws XMLStreamException {
    state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "element", XmlSchemaGenerator.NS_XML_SCHEMA);

    state.writeAttribute("name", modelInstance.getEffectiveName());

    // state.generateElementNameOrRef(modelInstance);

    if (!grouped && modelInstance.getMinOccurs() != 1) {
      state.writeAttribute("minOccurs", ObjectUtils.notNull(Integer.toString(modelInstance.getMinOccurs())));
    }

    if (modelInstance.getMaxOccurs() != 1) {
      state.writeAttribute("maxOccurs",
          modelInstance.getMaxOccurs() == -1 ? "unbounded"
              : ObjectUtils.notNull(Integer.toString(modelInstance.getMaxOccurs())));
    }

    IContainerFlag definition = modelInstance.getDefinition();
    IXmlType type = state.getTypeForDefinition(definition);

    if (state.isInline(definition)) {
      DocumentationGenerator.generateDocumentation(modelInstance, state);
      type.generateType(state, true);
    } else {
      state.writeAttribute("type", type.getTypeReference());
      DocumentationGenerator.generateDocumentation(modelInstance, state);
    }
    state.writeEndElement(); // xs:element
  }

  protected static void generateUnwrappedFieldInstance(
      @NonNull IFieldInstance fieldInstance,
      boolean grouped,
      @NonNull XmlGenerationState state) throws XMLStreamException {

    if (!MarkupDataTypeProvider.MARKUP_MULTILINE.equals(fieldInstance.getDefinition().getJavaTypeAdapter())) {
      throw new IllegalStateException();
    }

    state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "group", XmlSchemaGenerator.NS_XML_SCHEMA);

    state.writeAttribute("ref", "blockElementGroup");

    // minOccurs=1 is the schema default
    if (!grouped && fieldInstance.getMinOccurs() != 1) {
      state.writeAttribute("minOccurs", ObjectUtils.notNull(Integer.toString(fieldInstance.getMinOccurs())));
    }

    // if (fieldInstance.getMaxOccurs() != 1) {
    // state.writeAttribute("maxOccurs",
    // fieldInstance.getMaxOccurs() == -1 ? "unbounded"
    // : ObjectUtils.notNull(Integer.toString(fieldInstance.getMaxOccurs())));
    // }

    // unwrapped fields always have a max-occurance of 1. Since the markup multiline
    // is unbounded, this
    // value is unbounded.
    state.writeAttribute("maxOccurs", "unbounded");

    DocumentationGenerator.generateDocumentation(fieldInstance, state);

    state.writeEndElement(); // xs:group
  }

  protected void generateChoiceModelInstance(@NonNull IChoiceInstance choice,
      @NonNull XmlGenerationState state) throws XMLStreamException {
    state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "choice", XmlSchemaGenerator.NS_XML_SCHEMA);

    for (IModelInstance instance : choice.getModelInstances()) {
      assert instance != null;

      if (instance instanceof IChoiceInstance) {
        generateChoiceModelInstance((IChoiceInstance) instance, state);
      } else {
        generateModelInstance(instance, state);
      }
    }

    state.writeEndElement(); // xs:choice
  }
}
