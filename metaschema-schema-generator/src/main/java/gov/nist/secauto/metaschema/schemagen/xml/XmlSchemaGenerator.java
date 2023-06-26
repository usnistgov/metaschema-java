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

package gov.nist.secauto.metaschema.schemagen.xml; // NOPMD

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.AutoCloser;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.xml.datatype.XmlDatatypeManager;
import gov.nist.secauto.metaschema.schemagen.xml.schematype.IXmlType;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class XmlSchemaGenerator
    extends AbstractSchemaGenerator<
        AutoCloser<XMLStreamWriter2, SchemaGenerationException>,
        XmlDatatypeManager,
        XmlGenerationState> {
  // private static final Logger LOGGER = LogManager.getLogger(XmlSchemaGenerator.class);

  @NonNull
  public static final String PREFIX_XML_SCHEMA = "xs";
  @NonNull
  public static final String NS_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  @NonNull
  private static final String PREFIX_XML_SCHEMA_VERSIONING = "vs";
  @NonNull
  private static final String NS_XML_SCHEMA_VERSIONING = "http://www.w3.org/2007/XMLSchema-versioning";
  @NonNull
  public static final String NS_XHTML = "http://www.w3.org/1999/xhtml";

  @NonNull
  private final XMLOutputFactory2 xmlOutputFactory;

  @NonNull
  private static XMLOutputFactory2 defaultXMLOutputFactory() {
    XMLOutputFactory2 xmlOutputFactory = (XMLOutputFactory2) XMLOutputFactory.newInstance();
    assert xmlOutputFactory instanceof WstxOutputFactory;
    xmlOutputFactory.configureForSpeed();
    xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    return xmlOutputFactory;
  }

  public XmlSchemaGenerator() {
    this(defaultXMLOutputFactory());
  }

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public XmlSchemaGenerator(@NonNull XMLOutputFactory2 xmlOutputFactory) {
    this.xmlOutputFactory = xmlOutputFactory;
  }

  protected XMLOutputFactory2 getXmlOutputFactory() {
    return xmlOutputFactory;
  }

  @Override
  protected AutoCloser<XMLStreamWriter2, SchemaGenerationException> newWriter(
      Writer out) {
    XMLStreamWriter2 writer;
    try {
      writer = ObjectUtils.notNull((XMLStreamWriter2) getXmlOutputFactory().createXMLStreamWriter(out));
    } catch (XMLStreamException ex) {
      throw new SchemaGenerationException(ex);
    }
    return AutoCloser.autoClose(writer, t -> {
      try {
        t.close();
      } catch (XMLStreamException ex) {
        throw new SchemaGenerationException(ex);
      }
    });
  }

  @Override
  protected XmlGenerationState newGenerationState(
      IMetaschema metaschema,
      AutoCloser<XMLStreamWriter2, SchemaGenerationException> schemaWriter,
      IConfiguration<SchemaGenerationFeature> configuration) {
    return new XmlGenerationState(metaschema, schemaWriter, configuration);
  }

  @Override
  public void generateFromMetaschema(
      @NonNull IMetaschema metaschema,
      @NonNull Writer out,
      @NonNull IConfiguration<SchemaGenerationFeature> configuration) {
    try (StringWriter stringWriter = new StringWriter()) {
      super.generateFromMetaschema(metaschema, stringWriter, configuration);
      stringWriter.flush();

      try (StringReader stringReader = new StringReader(stringWriter.toString())) {
        Processor processor = new Processor(false);
        XsltCompiler compiler = processor.newXsltCompiler();
        try (InputStream is = getClass().getResourceAsStream("/identity.xsl")) {
          XsltExecutable stylesheet = compiler.compile(new StreamSource(is));
          Xslt30Transformer transformer = stylesheet.load30();
          Serializer serializer = processor.newSerializer(out);
          StreamSource source = new StreamSource(stringReader);
          transformer.transform(source, serializer);
        }
      }
    } catch (IOException | SaxonApiException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  @Override
  protected void generateSchema(XmlGenerationState state) {

    try {
      String targetNS = state.getDefaultNS();

      // analyze all definitions
      Map<String, String> prefixToNamespaceMap = new HashMap<>(); // NOPMD concurrency not needed
      final List<IRootAssemblyDefinition> rootAssemblyDefinitions = analyzeDefinitions(
          state,
          (entry, definition) -> {
            assert entry != null;
            assert definition != null;
            IXmlType type = state.getTypeForDefinition(definition);
            if (!entry.isInline()) {
              QName qname = type.getQName();
              String namespace = qname.getNamespaceURI();
              if (!targetNS.equals(namespace)) {
                // collect namespaces and prefixes for definitions with a different namespace
                prefixToNamespaceMap.computeIfAbsent(qname.getPrefix(), x -> namespace);
              }
            }
          });

      // write some root elements
      XMLStreamWriter2 writer = state.getXMLStreamWriter();
      writer.writeStartDocument("UTF-8", "1.0");
      writer.writeStartElement(PREFIX_XML_SCHEMA, "schema", NS_XML_SCHEMA);
      writer.writeDefaultNamespace(targetNS);
      writer.writeNamespace(PREFIX_XML_SCHEMA_VERSIONING, NS_XML_SCHEMA_VERSIONING);

      // write namespaces for all indexed definitions
      for (Map.Entry<String, String> entry : prefixToNamespaceMap.entrySet()) {
        state.writeNamespace(entry.getKey(), entry.getValue());
      }

      IMetaschema metaschema = state.getMetaschema();

      // write remaining root attributes
      writer.writeAttribute("targetNamespace", targetNS);
      writer.writeAttribute("elementFormDefault", "qualified");
      writer.writeAttribute(NS_XML_SCHEMA_VERSIONING, "minVersion", "1.0");
      writer.writeAttribute(NS_XML_SCHEMA_VERSIONING, "maxVersion", "1.1");
      writer.writeAttribute("version", metaschema.getVersion());

      generateSchemaMetadata(metaschema, state);

      for (IAssemblyDefinition definition : rootAssemblyDefinitions) {
        QName xmlQName = definition.getRootXmlQName();
        if (xmlQName != null
            && (xmlQName.getNamespaceURI() == null || state.getDefaultNS().equals(xmlQName.getNamespaceURI()))) {
          generateRootElement(definition, state);
        }
      }

      state.generateXmlTypes();

      writer.writeEndElement(); // xs:schema
      writer.writeEndDocument();
      writer.flush();
    } catch (XMLStreamException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  protected static void generateSchemaMetadata(
      @NonNull IMetaschema metaschema,
      @NonNull XmlGenerationState state)
      throws XMLStreamException {
    String targetNS = ObjectUtils.notNull(metaschema.getXmlNamespace().toASCIIString());
    state.writeStartElement(PREFIX_XML_SCHEMA, "annotation", NS_XML_SCHEMA);
    state.writeStartElement(PREFIX_XML_SCHEMA, "appinfo", NS_XML_SCHEMA);

    state.writeStartElement(targetNS, "schema-name");

    metaschema.getName().writeXHtml(targetNS, state.getXMLStreamWriter());

    state.writeEndElement();

    state.writeStartElement(targetNS, "schema-version");
    state.writeCharacters(metaschema.getVersion());
    state.writeEndElement();

    state.writeStartElement(targetNS, "short-name");
    state.writeCharacters(metaschema.getShortName());
    state.writeEndElement();

    state.writeEndElement();

    MarkupMultiline remarks = metaschema.getRemarks();
    if (remarks != null) {
      state.writeStartElement(PREFIX_XML_SCHEMA, "documentation", NS_XML_SCHEMA);

      remarks.writeXHtml(targetNS, state.getXMLStreamWriter());
      state.writeEndElement();
    }

    state.writeEndElement();
  }

  private static void generateRootElement(@NonNull IAssemblyDefinition definition, @NonNull XmlGenerationState state)
      throws XMLStreamException {
    assert definition.isRoot();

    XMLStreamWriter2 writer = state.getXMLStreamWriter();
    QName xmlQName = definition.getRootXmlQName();

    writer.writeStartElement(PREFIX_XML_SCHEMA, "element", NS_XML_SCHEMA);
    writer.writeAttribute("name", xmlQName.getLocalPart());
    writer.writeAttribute("type", state.getTypeForDefinition(definition).getTypeReference());

    writer.writeEndElement();
  }
}
