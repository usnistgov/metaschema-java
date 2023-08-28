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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.model.IValuedDefinition;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractGenerationState.AllowedValueCollection;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.xml.XmlGenerationState;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlSimpleTypeDataTypeRestriction
    extends AbstractXmlSimpleType {
  @NonNull
  private final AllowedValueCollection allowedValuesCollection;

  public XmlSimpleTypeDataTypeRestriction(
      @NonNull QName qname,
      @NonNull IValuedDefinition definition,
      @NonNull AllowedValueCollection allowedValuesCollection) {
    super(qname, definition);
    this.allowedValuesCollection = allowedValuesCollection;
  }

  protected AllowedValueCollection getAllowedValuesCollection() {
    return allowedValuesCollection;
  }

  @Override
  public void generateType(XmlGenerationState state, boolean anonymous) {
    try {
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "simpleType", XmlSchemaGenerator.NS_XML_SCHEMA);

      if (!anonymous) {
        state.writeAttribute("name", ObjectUtils.notNull(getQName().getLocalPart()));
      }

      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "restriction", XmlSchemaGenerator.NS_XML_SCHEMA);
      state.writeAttribute("base", state.getSimpleType(getDataTypeAdapter()).getTypeReference());

      for (IAllowedValue allowedValue : getAllowedValuesCollection().getValues()) {
        state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "enumeration", XmlSchemaGenerator.NS_XML_SCHEMA);
        state.writeAttribute("value", allowedValue.getValue());

        MarkupLine description = allowedValue.getDescription();
        generateDescriptionAnnotation(
            description,
            ObjectUtils.notNull(getQName().getNamespaceURI()),
            state);
        // LOGGER.info(String.format("Field:%s:%s: %s",
        // definition.getContainingMetaschema().getLocation(),
        // definition.getName(), allowedValue.getValue()));
        state.writeEndElement(); // xs:enumeration
      }

      state.writeEndElement(); // xs:restriction
      state.writeEndElement(); // xs:simpleType
    } catch (XMLStreamException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  public static void generateDescriptionAnnotation(
      @NonNull MarkupLine description,
      @NonNull String xmlNS,
      @NonNull XmlGenerationState state) throws XMLStreamException {
    XMLStreamWriter2 writer = state.getXMLStreamWriter();
    writer.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "annotation", XmlSchemaGenerator.NS_XML_SCHEMA);
    writer.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "documentation", XmlSchemaGenerator.NS_XML_SCHEMA);

    // write description
    writer.writeStartElement(xmlNS, "p");

    description.writeXHtml(xmlNS, writer);

    writer.writeEndElement(); // p

    writer.writeEndElement(); // xs:documentation
    writer.writeEndElement(); // xs:annotation
  }
}
