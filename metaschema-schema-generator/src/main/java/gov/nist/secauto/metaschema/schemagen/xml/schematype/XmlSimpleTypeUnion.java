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

import gov.nist.secauto.metaschema.model.common.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.xml.XmlGenerationState;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;

public class XmlSimpleTypeUnion
    extends AbstractXmlSimpleType {
  @NonNull
  private final List<IXmlSimpleType> simpleTypes;

  public XmlSimpleTypeUnion(
      @NonNull QName qname,
      @NonNull IValuedDefinition definition,
      @NonNull IXmlSimpleType... simpleTypes) {
    super(qname, definition);
    this.simpleTypes = CollectionUtil.requireNonEmpty(CollectionUtil.listOrEmpty(simpleTypes));
  }

  @NonNull
  public List<IXmlSimpleType> getSimpleTypes() {
    return simpleTypes;
  }

  @Override
  public void generateType(XmlGenerationState state, boolean anonymous) { // NOPMD unavoidable complexity
    try {
      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "simpleType", XmlSchemaGenerator.NS_XML_SCHEMA);

      if (!anonymous) {
        state.writeAttribute("name", ObjectUtils.notNull(getQName().getLocalPart()));
      }

      state.writeStartElement(XmlSchemaGenerator.PREFIX_XML_SCHEMA, "union", XmlSchemaGenerator.NS_XML_SCHEMA);

      List<IXmlSimpleType> memberTypes = new LinkedList<>();
      List<IXmlSimpleType> inlineTypes = new LinkedList<>();
      for (IXmlSimpleType unionType : simpleTypes) {
        if (unionType.isGeneratedType(state) && unionType.isInline(state)) {
          inlineTypes.add(unionType);
        } else {
          memberTypes.add(unionType);
        }
      }

      if (!memberTypes.isEmpty()) {
        state.writeAttribute(
            "memberTypes",
            ObjectUtils.notNull(memberTypes.stream()
                .map(type -> type.getTypeReference())
                .collect(Collectors.joining(" "))));
      }

      for (IXmlSimpleType inlineType : inlineTypes) {
        inlineType.generateType(state, true);
      }

      state.writeEndElement(); // xs:union
      state.writeEndElement(); // xs:simpleType

      for (IXmlSimpleType memberType : memberTypes) {
        memberType.generateType(state, false);
      }
    } catch (XMLStreamException ex) {
      throw new SchemaGenerationException(ex);
    }
  }
}
