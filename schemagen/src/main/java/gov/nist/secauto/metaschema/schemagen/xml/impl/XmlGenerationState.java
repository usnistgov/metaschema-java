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

package gov.nist.secauto.metaschema.schemagen.xml.impl;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IModelElement;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IValuedDefinition;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.util.AutoCloser;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractGenerationState;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.xml.datatype.XmlDatatypeManager;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.IXmlComplexType;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.IXmlSimpleType;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.IXmlType;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.XmlComplexTypeAssemblyDefinition;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.XmlComplexTypeFieldDefinition;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.XmlSimpleTypeDataTypeReference;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.XmlSimpleTypeDataTypeRestriction;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.XmlSimpleTypeUnion;

import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class XmlGenerationState
    extends AbstractGenerationState<AutoCloser<XMLStreamWriter2, SchemaGenerationException>, XmlDatatypeManager> {
  @NonNull
  private final String defaultNS;
  @NonNull
  private final Map<String, String> namespaceToPrefixMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<IDataTypeAdapter<?>, IXmlSimpleType> dataTypeToSimpleTypeMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<IValuedDefinition, IXmlSimpleType> definitionToSimpleTypeMap = new ConcurrentHashMap<>();
  @NonNull
  private final Map<IDefinition, IXmlType> definitionToTypeMap = new ConcurrentHashMap<>();

  private int prefixNum; // 0

  public XmlGenerationState(
      @NonNull IModule module,
      @NonNull AutoCloser<XMLStreamWriter2, SchemaGenerationException> writer,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> configuration) {
    super(module, writer, configuration, new XmlDatatypeManager());
    this.defaultNS = ObjectUtils.notNull(module.getXmlNamespace().toASCIIString());
  }

  @SuppressWarnings("resource")
  @NonNull
  public XMLStreamWriter2 getXMLStreamWriter() {
    return getWriter().getResource();
  }

  @NonNull
  public String getDefaultNS() {
    return defaultNS;
  }

  @NonNull
  public String getDatatypeNS() {
    return getDefaultNS();
  }

  @SuppressWarnings("null")
  @NonNull
  public String getNS(@NonNull IModelElement modelElement) {
    return modelElement.getContainingModule().getXmlNamespace().toASCIIString();
  }

  public String getNSPrefix(String namespace) {
    String retval = null;
    if (!getDefaultNS().equals(namespace)) {
      synchronized (this) {
        retval = namespaceToPrefixMap.get(namespace);
        if (retval == null) {
          retval = String.format("ns%d", ++prefixNum);
          namespaceToPrefixMap.put(namespace, retval);
        }
      }
    }
    return retval;
  }

  @NonNull
  protected QName newQName(
      @NonNull String localName,
      @NonNull String namespace) {
    String prefix = null;
    if (!getDefaultNS().equals(namespace)) {
      prefix = getNSPrefix(namespace);
    }

    return ObjectUtils.notNull(
        prefix == null ? new QName(namespace, localName) : new QName(namespace, localName, prefix));
  }

  @NonNull
  protected QName newQName(
      @NonNull IDefinition definition,
      @Nullable String suffix) {
    return newQName(
        getTypeNameForDefinition(definition, suffix),
        getNS(definition));
  }

  public IXmlType getXmlForDefinition(@NonNull IDefinition definition) {
    IXmlType retval = definitionToTypeMap.get(definition);
    if (retval == null) {
      switch (definition.getModelType()) {
      case FIELD: {
        IFieldDefinition field = (IFieldDefinition) definition;
        if (field.getFlagInstances().isEmpty()) {
          retval = getSimpleType(field);
        } else {
          retval = newComplexType(field);
        }
        break;
      }
      case ASSEMBLY: {
        retval = newComplexType((IAssemblyDefinition) definition);
        break;
      }
      case FLAG:
        retval = getSimpleType((IFlagDefinition) definition);
        break;
      case CHOICE_GROUP:
      case CHOICE:
      default:
        throw new UnsupportedOperationException(definition.getModelType().toString());
      }
      definitionToTypeMap.put(definition, retval);
    }
    return retval;
  }

  @NonNull
  public IXmlSimpleType getSimpleType(@NonNull IDataTypeAdapter<?> dataType) {
    IXmlSimpleType type = dataTypeToSimpleTypeMap.get(dataType);
    if (type == null) {
      // lazy initialize and cache the type
      QName qname = newQName(
          getDatatypeManager().getTypeNameForDatatype(dataType),
          getDatatypeNS());
      type = new XmlSimpleTypeDataTypeReference(qname, dataType);
      dataTypeToSimpleTypeMap.put(dataType, type);
    }
    return type;
  }

  @NonNull
  public IXmlSimpleType getSimpleType(@NonNull IValuedDefinition definition) {
    IXmlSimpleType simpleType = definitionToSimpleTypeMap.get(definition);
    if (simpleType == null) {
      AllowedValueCollection allowedValuesCollection = getContextIndependentEnumeratedValues(definition);
      List<IAllowedValue> allowedValues = allowedValuesCollection.getValues();

      IDataTypeAdapter<?> dataType = definition.getJavaTypeAdapter();
      if (allowedValues.isEmpty()) {
        // just use the built-in type
        simpleType = getSimpleType(dataType);
      } else {

        // generate a restriction on the built-in type for the enumerated values
        simpleType = new XmlSimpleTypeDataTypeRestriction(
            newQName(definition, null),
            definition,
            allowedValuesCollection);

        if (!allowedValuesCollection.isClosed()) {
          // if other values are allowed, we need to make a union of the restriction type
          // and the base
          // built-in type
          simpleType = new XmlSimpleTypeUnion(
              newQName(definition, "Union"),
              definition,
              getSimpleType(dataType),
              simpleType);
        }
      }

      definitionToSimpleTypeMap.put(definition, simpleType);
    }
    return simpleType;
  }

  @NonNull
  protected IXmlComplexType newComplexType(@NonNull IFieldDefinition definition) {
    QName qname = newQName(definition, null);
    return new XmlComplexTypeFieldDefinition(qname, definition);
  }

  @NonNull
  protected IXmlComplexType newComplexType(@NonNull IAssemblyDefinition definition) {
    QName qname = newQName(definition, null);
    return new XmlComplexTypeAssemblyDefinition(qname, definition);
  }

  public void generateXmlTypes() throws XMLStreamException {

    for (IXmlType type : definitionToTypeMap.values()) {
      if (!type.isInline(this) && type.isGeneratedType(this) && type.isReferenced(this)) {
        type.generate(this);
      } else {
        assert !type.isGeneratedType(this) || type.isInline(this) || !type.isReferenced(this);
      }
    }
    getDatatypeManager().generateDatatypes(getXMLStreamWriter());
  }

  public void writeAttribute(@NonNull String localName, @NonNull String value) throws XMLStreamException {
    getXMLStreamWriter().writeAttribute(localName, value);
  }

  public void writeStartElement(@NonNull String namespaceUri, @NonNull String localName) throws XMLStreamException {
    getXMLStreamWriter().writeStartElement(namespaceUri, localName);
  }

  public void writeStartElement(
      @NonNull String prefix,
      @NonNull String localName,
      @NonNull String namespaceUri) throws XMLStreamException {
    getXMLStreamWriter().writeStartElement(prefix, localName, namespaceUri);

  }

  public void writeEndElement() throws XMLStreamException {
    getXMLStreamWriter().writeEndElement();
  }

  public void writeCharacters(@NonNull String text) throws XMLStreamException {
    getXMLStreamWriter().writeCharacters(text);
  }

  public void writeNamespace(String prefix, String namespaceUri) throws XMLStreamException {
    getXMLStreamWriter().writeNamespace(prefix, namespaceUri);
  }

  @Override
  public void flushWriter() throws IOException {
    try {
      getWriter().getResource().flush();
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }
}
