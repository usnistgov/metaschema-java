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

package gov.nist.secauto.metaschema.schemagen;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedValuedDefinition;
import gov.nist.secauto.metaschema.model.common.ModelType;
import gov.nist.secauto.metaschema.model.common.UsedDefinitionModelWalker;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

public class XmlSchemaGenerator
    extends AbstractSchemaGenerator {
  private static final String NS_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  private static final String NS_XML_SCHEMA_VERSIONING = "http://www.w3.org/2007/XMLSchema-versioning";

  @NotNull
  private static XMLOutputFactory2 defaultXMLOutputFactory() {
    XMLOutputFactory2 xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
    xmlOutputFactory.configureForSpeed();
    xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    return xmlOutputFactory;
  }

  @NotNull
  private final XMLOutputFactory2 xmlOutputFactory;

  public XmlSchemaGenerator() {
    this(defaultXMLOutputFactory());
  }

  public XmlSchemaGenerator(@NotNull XMLOutputFactory2 xmlOutputFactory) {
    this.xmlOutputFactory = xmlOutputFactory;
  }

  protected XMLOutputFactory2 getXmlOutputFactory() {
    return xmlOutputFactory;
  }

  @Override
  public void generateFromMetaschema(@NotNull IMetaschema metaschema, @NotNull Writer out,
      @NotNull IConfiguration<SchemaGenerationFeature> configuration) throws IOException {

    StringWriter stringWriter = new StringWriter();
    try (PrintWriter writer = new PrintWriter(stringWriter)) {
      generateDocument(metaschema, writer, configuration);
      writer.flush();
    }

    try (StringReader stringReader = new StringReader(stringWriter.toString())) {
      Processor processor = new Processor(false);
      XsltCompiler compiler = processor.newXsltCompiler();
      XsltExecutable stylesheet
          = compiler.compile(new StreamSource(getClass().getClassLoader().getResourceAsStream("identity.xsl")));
      Xslt30Transformer transformer = stylesheet.load30();
      Serializer serializer = processor.newSerializer(out);
      StreamSource source = new StreamSource(stringReader);
      transformer.transform(source, serializer);
    } catch (SaxonApiException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected void generateDocument(@NotNull IMetaschema metaschema, @NotNull Writer out,
      @NotNull IConfiguration<SchemaGenerationFeature> configuration) throws IOException {
    try {
      @SuppressWarnings("null")
      @NotNull
      XMLStreamWriter2 writer = (XMLStreamWriter2) xmlOutputFactory.createXMLStreamWriter(out);
      @SuppressWarnings("null")
      @NotNull
      String targetNS = metaschema.getXmlNamespace().toASCIIString();

      Collection<@NotNull ? extends INamedDefinition> definitions
          = UsedDefinitionModelWalker.collectUsedDefinitionsFromMetaschema(metaschema);

      Set<String> visitedNamespaces = new HashSet<>();
      visitedNamespaces.add(targetNS);

      IInlineStrategy inlineStrategy = newInlineStrategy(configuration, definitions);
      GenerationState state = new GenerationState(writer, targetNS, inlineStrategy);

      writer.writeStartDocument("UTF-8", "1.0");
      writer.writeStartElement("xs", "schema", NS_XML_SCHEMA);
      writer.writeDefaultNamespace(targetNS);
      writer.writeNamespace("vc", NS_XML_SCHEMA_VERSIONING);

      Collection<@NotNull IAssemblyDefinition> rootAssemblyDefinitions
          = new LinkedList<>();
      Set<@NotNull INamedDefinition> globalDefinitions = new LinkedHashSet<>();
      for (INamedDefinition definition : definitions) {
        String xmlNS = definition.getContainingMetaschema().getXmlNamespace().toASCIIString();

        if (!(definition instanceof IFlagDefinition) && !targetNS.equals(xmlNS)) {
          if (!visitedNamespaces.contains(xmlNS)) {
            writer.writeNamespace(state.getNSPrefix(xmlNS), xmlNS);
          }
          // this definition is not in this schema's namespace
          continue;
        }

        if (definition instanceof IAssemblyDefinition && ((IAssemblyDefinition) definition).isRoot()) {
          IAssemblyDefinition assemblyDefinition = (IAssemblyDefinition) definition;
          rootAssemblyDefinitions.add(assemblyDefinition);
        } else if (state.isInline(definition)) {
          // the definition is to be inlined
          continue;
        }
        globalDefinitions.add(definition);
      }
      definitions = globalDefinitions;

      writer.writeAttribute("targetNamespace", targetNS);
      writer.writeAttribute("elementFormDefault", "qualified");
      writer.writeAttribute(NS_XML_SCHEMA_VERSIONING, "minVersion", "1.0");
      writer.writeAttribute(NS_XML_SCHEMA_VERSIONING, "maxVersion", "1.1");
      writer.writeAttribute("version", metaschema.getVersion());

      generateMetadata(metaschema, writer);

      for (IAssemblyDefinition definition : rootAssemblyDefinitions) {
        QName xmlQName = definition.getRootXmlQName();
        if (xmlQName != null
            && (xmlQName.getNamespaceURI() == null || state.getDefaultNS().equals(xmlQName.getNamespaceURI()))) {
          generateRootElement(definition, state);
        }
      }

      for (INamedDefinition definition : definitions) {
        generateComplexType(definition, state);
      }

      state.getDatatypeManager().generateDatatypes(writer);

      writer.writeEndElement(); // xs:schema
      writer.writeEndDocument();
      writer.flush();
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
  }

  protected void generateMetadata(@NotNull IMetaschema metaschema, XMLStreamWriter2 writer) throws XMLStreamException {
    String targetNS = metaschema.getXmlNamespace().toASCIIString();
    writer.writeStartElement(NS_XML_SCHEMA, "annotation");
    writer.writeStartElement(NS_XML_SCHEMA, "appinfo");

    writer.writeStartElement(targetNS, "schema-name");
    metaschema.getName().toHtmlAsStream(writer, targetNS);
    writer.writeEndElement();

    writer.writeStartElement(targetNS, "schema-version");
    writer.writeCharacters(metaschema.getVersion());
    writer.writeEndElement();

    writer.writeStartElement(targetNS, "short-name");
    writer.writeCharacters(metaschema.getShortName());
    writer.writeEndElement();

    writer.writeEndElement();

    MarkupMultiline remarks = metaschema.getRemarks();
    if (remarks != null) {
      writer.writeStartElement(NS_XML_SCHEMA, "documentation");
      remarks.toHtmlAsStream(writer, targetNS);
      writer.writeEndElement();
    }

    writer.writeEndElement();
  }

  private void generateMetadata(@NotNull INamedDefinition definition, @NotNull GenerationState state)
      throws XMLStreamException {
    String formalName = definition.getFormalName();
    MarkupLine description = definition.getDescription();
    MarkupMultiline remarks = definition.getRemarks();

    if (formalName != null || description != null || remarks != null) {
      XMLStreamWriter2 writer = state.getWriter();
      String xmlNS = state.getNS(definition);
      writer.writeStartElement(NS_XML_SCHEMA, "annotation");
      if (formalName != null || description != null) {
        writer.writeStartElement(NS_XML_SCHEMA, "appinfo");

        if (formalName != null) {
          writer.writeStartElement(xmlNS, "formal-name");
          writer.writeCharacters(formalName);
          writer.writeEndElement();
        }

        if (description != null) {
          writer.writeStartElement(xmlNS, "description");
          description.toHtmlAsStream(writer, xmlNS);
          writer.writeEndElement();
        }

        writer.writeEndElement(); // xs:appInfo
      }

      writer.writeStartElement(NS_XML_SCHEMA, "documentation");

      if (description != null) {
        // write description
        writer.writeStartElement(xmlNS, "p");

        if (formalName != null) {
          writer.writeStartElement(xmlNS, "b");
          writer.writeCharacters(formalName);
          writer.writeEndElement();
          writer.writeCharacters(": ");
        }

        description.toHtmlAsStream(writer, xmlNS);
        writer.writeEndElement(); // p
      }

      if (remarks != null) {
        remarks.toHtmlAsStream(writer, xmlNS);
      }

      writer.writeEndElement(); // xs:documentation
      writer.writeEndElement(); // xs:annotation
    }
  }

  private void generateRootElement(@NotNull IAssemblyDefinition definition, @NotNull GenerationState state)
      throws XMLStreamException {
    assert definition.isRoot();

    XMLStreamWriter2 writer = state.getWriter();
    QName xmlQName = definition.getRootXmlQName();

    writer.writeStartElement("xs", "element", NS_XML_SCHEMA);
    writer.writeAttribute("name", xmlQName.getLocalPart());
    writer.writeAttribute("type", state.getDatatypeManager().getTypeNameForDefinition(definition, state));

    writer.writeEndElement();
  }

  private void generateComplexType(@NotNull INamedDefinition definition, @NotNull GenerationState state)
      throws XMLStreamException {
    switch (definition.getModelType()) {
    case ASSEMBLY:
      generateAssemblyDefinitionComplexType((IAssemblyDefinition) definition, state);
      break;
    case FIELD: {
      generateFieldDefinitionComplexType((IFieldDefinition) definition, state);
      break;
    }
    default:
      // do nothing
      break;
    }
  }

  private void generateAssemblyDefinitionComplexType(@NotNull IAssemblyDefinition definition,
      @NotNull GenerationState state)
      throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "complexType", NS_XML_SCHEMA);

    boolean inline = state.isInline(definition);
    if (!inline) {
      writer.writeAttribute("name", state.getDatatypeManager().getTypeNameForDefinition(definition, state));
      generateMetadata(definition, state);
    } // otherwise the metadata will appear on the element ref

    Collection<@NotNull ? extends IModelInstance> modelInstances = definition.getModelInstances();
    if (!modelInstances.isEmpty()) {
      writer.writeStartElement("xs", "sequence", NS_XML_SCHEMA);
      for (IModelInstance modelInstance : modelInstances) {
        generateModelInstance(modelInstance, state);
      }
      writer.writeEndElement();
    }

    Collection<@NotNull ? extends IFlagInstance> flagInstances = definition.getFlagInstances();
    if (!flagInstances.isEmpty()) {
      for (IFlagInstance flagInstance : flagInstances) {
        generateFlagInstance(flagInstance, state);
      }
    }

    writer.writeEndElement();
  }

  private void generateFieldDefinitionComplexType(@NotNull IFieldDefinition definition, @NotNull GenerationState state)
      throws XMLStreamException {
    Collection<@NotNull ? extends IFlagInstance> flagInstances = definition.getFlagInstances();
    if (flagInstances.isEmpty()) {
      // this is a simple field with only a datatype. no type definition needed
      return;
    }

    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "complexType", NS_XML_SCHEMA);

    boolean inline = state.isInline(definition);

    if (!inline) {
      writer.writeAttribute("name", state.getDatatypeManager().getTypeNameForDefinition(definition, state));
      generateMetadata(definition, state);
    } // otherwise the metadata will appear on the element ref

    IJavaTypeAdapter<?> datatype = definition.getJavaTypeAdapter();
    String xmlContentType;
    if (datatype.isXmlMixed()) {
      xmlContentType = "complexContent";
    } else {
      xmlContentType = "simpleContent";
    }

    writer.writeStartElement("xs", xmlContentType, NS_XML_SCHEMA);
    writer.writeStartElement("xs", "extension", NS_XML_SCHEMA);
    writer.writeAttribute("base", state.getDatatypeManager().getTypeNameForDatatype(datatype));

    for (IFlagInstance flagInstance : flagInstances) {
      generateFlagInstance(flagInstance, state);
    }
    writer.writeEndElement(); // xs:extension
    writer.writeEndElement(); // xs:simpleContent/xs:complexContent

    writer.writeEndElement(); // xs:complexType
  }

  private void generateModelInstance(@NotNull IModelInstance modelInstance, @NotNull GenerationState state)
      throws XMLStreamException {

    XMLStreamWriter2 writer = state.getWriter();
    boolean grouped = false;
    if (XmlGroupAsBehavior.GROUPED.equals(modelInstance.getXmlGroupAsBehavior())) {
      // handle grouping
      writer.writeStartElement("xs", "element", NS_XML_SCHEMA);

      QName groupAsQName = ObjectUtils.requireNonNull(modelInstance.getXmlGroupAsQName());

      if (generateElementNameOrRef(groupAsQName, state)) {
        if (modelInstance.getMinOccurs() == 0) {
          // this is an optional instance group
          writer.writeAttribute("minOccurs", "0");
        }

        // now generate the child elements of the group
        writer.writeStartElement("xs", "complexType", NS_XML_SCHEMA);
        writer.writeStartElement("xs", "sequence", NS_XML_SCHEMA);

        // mark that we need to close these elements
        grouped = true;
      }
    }

    switch (modelInstance.getModelType()) {
    case ASSEMBLY:
      generateNamedModelInstance((INamedModelInstance) modelInstance, grouped, state);
      break;
    case FIELD: {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;
      if (!fieldInstance.isInXmlWrapped()
          && fieldInstance.getDefinition().getJavaTypeAdapter().isUnrappedValueAllowedInXml()) {
        generateUnwrappedFieldInstance(fieldInstance, grouped, state);
      } else {
        generateNamedModelInstance(fieldInstance, grouped, state);
      }
      break;
    }
    case CHOICE:
      generateChoiceModelInstance((IChoiceInstance) modelInstance, state);
      break;
    default:
      throw new UnsupportedOperationException();
    }

    if (grouped) {
      writer.writeEndElement(); // xs:sequence
      writer.writeEndElement(); // xs:complexType
      writer.writeEndElement(); // xs:element
    }
  }

  private void generateNamedModelInstance(@NotNull INamedModelInstance modelInstance, boolean grouped,
      @NotNull GenerationState state) throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "element", NS_XML_SCHEMA);

    generateElementNameOrRef(modelInstance, state);

    if (!grouped) {
      if (modelInstance.getMinOccurs() != 1) {
        writer.writeAttribute("minOccurs", String.format("%d", modelInstance.getMinOccurs()));
      }
    }

    if (modelInstance.getMaxOccurs() != 1) {
      writer.writeAttribute("maxOccurs",
          modelInstance.getMaxOccurs() == -1 ? "unbounded" : String.format("%d", modelInstance.getMaxOccurs()));
    }

    INamedModelDefinition definition = modelInstance.getDefinition();
    if (ModelType.FIELD.equals(definition.getModelType()) && definition.getFlagInstances().isEmpty()) {
      // always generate a datatype reference for fields with no flags
      generateTypeReferenceForDefinition(definition, state);
      generateInstanceMetadata(modelInstance, true, state);
    } else if (state.isInline(definition)) {
      generateInstanceMetadata(modelInstance, false, state);
      // generate an inline complex type if able
      generateComplexType(definition, state);
    } else {
      // generate a type reference instead
      generateTypeReferenceForDefinition(definition, state);
      generateInstanceMetadata(modelInstance, false, state);
    }
    writer.writeEndElement(); // xs:element
  }

  private void generateTypeReferenceForDefinition(@NotNull INamedDefinition definition, @NotNull GenerationState state)
      throws XMLStreamException {
    CharSequence ref;
    switch (definition.getModelType()) {
    case FIELD: {
      IFieldDefinition field = (IFieldDefinition) definition;
      if (field.getFlagInstances().isEmpty()) {
        ref = getTypeReferenceForSimpleValuedDefinition(field, state);
      } else {
        ref = getTypeReferenceForComplexDefinition(field, state);
      }
      break;
    }
    case ASSEMBLY: {
      ref = getTypeReferenceForComplexDefinition((IAssemblyDefinition) definition, state);
      break;
    }
    case FLAG:
      ref = getTypeReferenceForSimpleValuedDefinition((IFieldDefinition) definition, state);
      break;
    default:
      throw new UnsupportedOperationException();
    }

    XMLStreamWriter2 writer = state.getWriter();
    writer.writeAttribute("type", ref.toString());
  }

  private CharSequence getTypeReferenceForSimpleValuedDefinition(@NotNull INamedValuedDefinition definition,
      @NotNull GenerationState state) {
    StringBuilder builder = new StringBuilder();
    String namespace = state.getDatatypeNS();
    if (!state.getDefaultNS().equals(namespace)) {
      builder.append(state.getNSPrefix(namespace));
      builder.append(':');
    }
    builder.append(state.getDatatypeManager().getTypeNameForDatatype(definition.getJavaTypeAdapter()));
    return builder.toString();
  }

  private CharSequence getTypeReferenceForComplexDefinition(@NotNull INamedModelDefinition definition,
      @NotNull GenerationState state) {
    StringBuilder builder = new StringBuilder();
    String xmlNS = state.getNS(definition);
    if (!state.getDefaultNS().equals(xmlNS)) {
      builder.append(state.getNSPrefix(xmlNS));
      builder.append(':');
    }
    builder.append(state.getDatatypeManager().getTypeNameForDefinition(definition, state));
    return builder.toString();
  }

  private void generateUnwrappedFieldInstance(@NotNull IFieldInstance fieldInstance, boolean grouped,
      @NotNull GenerationState state) throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "sequence", NS_XML_SCHEMA);

    if (!grouped) {
      if (fieldInstance.getMinOccurs() != 1) {
        writer.writeAttribute("minOccurs", String.format("%d", fieldInstance.getMinOccurs()));
      }
    }

    writer.writeAttribute("maxOccurs", "unbounded");
    //
    // if (fieldInstance.getMaxOccurs() != 1) {
    // writer.writeAttribute("maxOccurs",
    // fieldInstance.getMaxOccurs() == -1 ? "unbounded" : String.format("%d",
    // fieldInstance.getMaxOccurs()));
    // }

    generateInstanceMetadata(fieldInstance, true, state);

    // TODO: generalize this production for all types that do this
    writer.writeStartElement("xs", "group", NS_XML_SCHEMA);
    writer.writeAttribute("ref", "blockElementGroup");
    writer.writeEndElement(); // xs:group
    writer.writeEndElement(); // xs:sequence
  }

  private void generateInstanceMetadata(@NotNull INamedInstance instance, boolean full, @NotNull GenerationState state)
      throws XMLStreamException {
    String formalName = instance.getFormalName();
    MarkupLine description = instance.getDescription();
    MarkupMultiline definitionRemarks = null;

    if (full) {
      INamedDefinition definition = instance.getDefinition();
      if (formalName == null) {
        formalName = definition.getFormalName();
      }

      if (description == null) {
        description = definition.getDescription();
      }

      definitionRemarks = definition.getRemarks();
    }

    MarkupMultiline remarks = instance.getRemarks();

    if (formalName != null || description != null || definitionRemarks != null || remarks != null) {
      XMLStreamWriter2 writer = state.getWriter();
      String xmlNS = state.getNS(instance);

      writer.writeStartElement(NS_XML_SCHEMA, "annotation");
      if (formalName != null || description != null) {
        writer.writeStartElement(NS_XML_SCHEMA, "appinfo");

        if (formalName != null) {
          writer.writeStartElement(xmlNS, "formal-name");
          writer.writeCharacters(formalName);
          writer.writeEndElement();
        }

        if (description != null) {
          writer.writeStartElement(xmlNS, "description");
          description.toHtmlAsStream(writer, xmlNS);
          writer.writeEndElement();
        }

        writer.writeEndElement(); // xs:appInfo
      }

      writer.writeStartElement(NS_XML_SCHEMA, "documentation");

      if (description != null) {
        // write description
        writer.writeStartElement(xmlNS, "p");

        if (formalName != null) {
          writer.writeStartElement(xmlNS, "b");
          writer.writeCharacters(formalName);
          writer.writeEndElement();
          writer.writeCharacters(": ");
        }

        description.toHtmlAsStream(writer, xmlNS);
        writer.writeEndElement(); // p
      }

      if (remarks != null) {
        remarks.toHtmlAsStream(writer, xmlNS);
      }

      if (definitionRemarks != null) {
        definitionRemarks.toHtmlAsStream(writer, xmlNS);
      }

      writer.writeEndElement(); // xs:documentation
      writer.writeEndElement(); // xs:annotation
    }
  }

  private void generateChoiceModelInstance(@NotNull IChoiceInstance choice,
      @NotNull GenerationState state) throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement(NS_XML_SCHEMA, "choice");

    for (IModelInstance instance : choice.getModelInstances()) {
      generateModelInstance(instance, state);
    }

    writer.writeEndElement(); // xs:choice
  }

  /**
   * Generate name or ref attributes for an element.
   * 
   * @param qname
   *          the XML qualified name of the element
   * @param state
   *          the generation state
   * @return {@code true} if the result is a name, or {@code false} otherwise
   * @throws XMLStreamException
   *           if an error occurred while writing the XML
   */
  private boolean generateElementNameOrRef(@NotNull QName qname, @NotNull GenerationState state)
      throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    String groupAsNamespace = qname.getNamespaceURI();
    boolean retval;
    if (!state.getDefaultNS().equals(groupAsNamespace)) {
      writer.writeAttribute("ref", String.format("%s:%s", state.getNSPrefix(groupAsNamespace), qname.getLocalPart()));
      retval = false;
    } else {
      writer.writeAttribute("name", qname.getLocalPart());
      retval = true;
    }
    return retval;
  }

  /**
   * Generate name or ref attributes for an element.
   * 
   * @param modelInstance
   *          the XML qualified name of the element
   * @param state
   *          the generation state
   * @return {@code true} if the result is a name, or {@code false} otherwise
   * @throws XMLStreamException
   *           if an error occurred while writing the XML
   */
  private boolean generateElementNameOrRef(@NotNull INamedInstance modelInstance, @NotNull GenerationState state)
      throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    QName qname = modelInstance.getXmlQName();
    String namespace = state.getNS(modelInstance);

    boolean retval;
    if (!state.getDefaultNS().equals(namespace)) {
      writer.writeAttribute("ref", String.format("%s:%s", state.getNSPrefix(namespace), qname.getLocalPart()));
      retval = false;
    } else {
      writer.writeAttribute("name", qname.getLocalPart());
      retval = true;
    }
    return retval;
  }

  private void generateFlagInstance(@NotNull IFlagInstance instance, @NotNull GenerationState state)
      throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "attribute", NS_XML_SCHEMA);
    boolean named = generateElementNameOrRef(instance, state);

    if (instance.isRequired()) {
      writer.writeAttribute("use", "required");
    }

    IFlagDefinition definition = instance.getDefinition();
    boolean inline = state.isInline(definition);

    if (named && !inline) {
      // write out datatype
      writer.writeAttribute("type", getTypeReferenceForSimpleValuedDefinition(definition, state).toString());
    }

    generateInstanceMetadata(instance, inline, state);

    if (inline) {
      generateFlagDefinitionSimpleType(instance, state);
    }

    writer.writeEndElement(); // xs:attribute
  }

  private void generateFlagDefinitionSimpleType(@NotNull IFlagInstance instance, @NotNull GenerationState state)
      throws XMLStreamException {
    XMLStreamWriter2 writer = state.getWriter();
    writer.writeStartElement("xs", "simpleType", NS_XML_SCHEMA);
    writer.writeStartElement("xs", "restriction", NS_XML_SCHEMA);
    writer.writeAttribute("base",
        getTypeReferenceForSimpleValuedDefinition(instance.getDefinition(), state).toString());

    writer.writeEndElement(); // xs:restriction
    writer.writeEndElement(); // xs:simpleType
  }

  public static class GenerationState
      extends AbstractGenerationState<XMLStreamWriter2, XmlDatatypeManager> {
    @NotNull
    private final String defaultNS;
    @NotNull
    private final Map<String, String> namespaceToPrefixMap = new HashMap<>();

    private int prefixNum = 0;

    public GenerationState(@NotNull XMLStreamWriter2 writer, @NotNull String defaultNS,
        @NotNull IInlineStrategy inlineStrategy) {
      super(writer, new XmlDatatypeManager(), inlineStrategy);
      this.defaultNS = defaultNS;
    }

    @NotNull
    public String getDefaultNS() {
      return defaultNS;
    }

    public String getDatatypeNS() {
      return getDefaultNS();
    }

    public String getNS(@NotNull INamedDefinition definition) {
      return definition.getContainingMetaschema().getXmlNamespace().toASCIIString();
    }

    public String getNS(@NotNull INamedInstance instance) {
      String namespace = instance.getXmlNamespace();
      if (namespace == null) {
        namespace = instance.getContainingMetaschema().getXmlNamespace().toASCIIString();
      }
      return namespace;
    }

    public String getNSPrefix(String namespace) {
      String retval;
      if (getDefaultNS().equals(namespace)) {
        retval = null;
      } else {
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
  }
}
