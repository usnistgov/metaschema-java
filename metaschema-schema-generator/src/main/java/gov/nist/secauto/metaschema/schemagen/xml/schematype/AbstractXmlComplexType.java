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

import gov.nist.secauto.metaschema.model.common.IFlagContainer;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.xml.DocumentationGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlGenerationState;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractXmlComplexType<D extends IFlagContainer>
    extends AbstractXmlType
    implements IXmlComplexType {
  @NonNull
  private final D definition;

  public AbstractXmlComplexType(
      @NonNull QName qname,
      @NonNull D definition) {
    super(qname);
    this.definition = definition;
  }

  @Override
  @NonNull
  public D getDefinition() {
    return definition;
  }

  @Override
  public void generateType(@NonNull XmlGenerationState state, boolean anonymous) {
    try {
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "complexType", XmlSchemaGenerator.NS_XML_SCHEMA);

      if (!anonymous) {
        state.writeAttribute("name", getTypeName());
      }

      DocumentationGenerator.generateDocumentation(getDefinition(), state);

      generateTypeBody(state);

      state.writeEndElement(); // complexType
    } catch (XMLStreamException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  protected abstract void generateTypeBody(@NonNull XmlGenerationState state) throws XMLStreamException;

  protected static void generateFlagInstance(@NonNull IFlagInstance instance, @NonNull XmlGenerationState state)
      throws XMLStreamException {
    state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "attribute", XmlSchemaGenerator.NS_XML_SCHEMA);

    state.writeAttribute("name", instance.getEffectiveName());

    if (instance.isRequired()) {
      state.writeAttribute("use", "required");
    }

    IFlagDefinition definition = instance.getDefinition();

    IXmlType type = state.getTypeForDefinition(definition);

    if (state.isInline(definition) && type.isGeneratedType(state)) {
      DocumentationGenerator.generateDocumentation(instance, state);

      type.generateType(state, true);
    } else {
      state.writeAttribute("type", type.getTypeReference());

      DocumentationGenerator.generateDocumentation(instance, state);
    }

    state.writeEndElement(); // xs:attribute
  }

  @Override
  public boolean isInline(XmlGenerationState state) {
    return state.isInline(getDefinition());
  }
}
