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

package gov.nist.secauto.metaschema.databind.io.xml;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.util.AutoCloser;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.AbstractDeserializer;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DefaultXmlDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private XMLInputFactory2 xmlInputFactory;

  @NonNull
  private final IBoundDefinitionModelAssembly rootDefinition;

  /**
   * Construct a new Module binding-based deserializer that reads XML-based Module
   * content.
   *
   * @param definition
   *          the assembly class binding describing the Java objects this
   *          deserializer parses data into
   */
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public DefaultXmlDeserializer(@NonNull IBoundDefinitionModelAssembly definition) {
    super(definition);
    this.rootDefinition = definition;
    if (!definition.isRoot()) {
      throw new UnsupportedOperationException(
          String.format("The assembly '%s' is not a root assembly.", definition.getBoundClass().getName()));
    }
  }

  /**
   * Get the XML input factory instance used to create XML parser instances.
   * <p>
   * Uses a built-in default if a user specified factory is not provided.
   *
   * @return the factory instance
   * @see #setXMLInputFactory(XMLInputFactory2)
   */
  @NonNull
  private XMLInputFactory2 getXMLInputFactory() {

    synchronized (this) {
      if (xmlInputFactory == null) {
        xmlInputFactory = (XMLInputFactory2) XMLInputFactory.newInstance();
        assert xmlInputFactory instanceof WstxInputFactory;
        xmlInputFactory.configureForXmlConformance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
        // xmlInputFactory.configureForSpeed();

        if (isFeatureEnabled(DeserializationFeature.DESERIALIZE_XML_ALLOW_ENTITY_RESOLUTION)) {
          xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
          xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
          xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
          xmlInputFactory.setProperty(XMLInputFactory.RESOLVER,
              (XMLResolver) (publicID, systemID, baseURI, namespace) -> {
                URI base = URI.create(baseURI);
                URI resource = base.resolve(systemID);
                try {
                  return resource.toURL().openStream();
                } catch (IOException ex) {
                  throw new XMLStreamException(ex);
                }
              });
        }
      }
      return ObjectUtils.notNull(xmlInputFactory);
    }
  }

  /**
   * Provide a XML input factory instance that will be used to create XML parser
   * instances.
   *
   * @param factory
   *          the factory instance
   */
  protected void setXMLInputFactory(@NonNull XMLInputFactory2 factory) {
    synchronized (this) {
      this.xmlInputFactory = factory;
    }
  }

  @NonNull
  private XMLEventReader2 newXMLEventReader2(
      @NonNull URI documentUri,
      @NonNull Reader reader) throws XMLStreamException {
    XMLEventReader2 eventReader
        = (XMLEventReader2) getXMLInputFactory().createXMLEventReader(documentUri.toASCIIString(), reader);
    EventFilter filter = new CommentFilter();
    return ObjectUtils.notNull((XMLEventReader2) getXMLInputFactory().createFilteredReader(eventReader, filter));
  }

  @Override
  protected final IDocumentNodeItem deserializeToNodeItemInternal(Reader reader, URI documentUri) throws IOException {
    Object value = deserializeToValue(reader, documentUri);
    return INodeItemFactory.instance().newDocumentNodeItem(rootDefinition, documentUri, value);
  }

  @Override
  public final CLASS deserializeToValue(Reader reader, URI documentUri) throws IOException {
    // doesn't auto close the underlying reader
    try (AutoCloser<XMLEventReader2, XMLStreamException> closer = new AutoCloser<>(
        newXMLEventReader2(documentUri, reader), XMLEventReader::close)) {
      return parseXmlInternal(closer.getResource());
    } catch (XMLStreamException ex) {
      throw new IOException("Unable to create a new XMLEventReader2 instance.", ex);
    }
  }

  @NonNull
  private CLASS parseXmlInternal(@NonNull XMLEventReader2 reader)
      throws IOException {

    MetaschemaXmlReader parser = new MetaschemaXmlReader(reader, new DefaultXmlProblemHandler());

    try {
      return parser.read(rootDefinition);
    } catch (IOException | AssertionError ex) {
      throw new IOException(
          String.format("An unexpected error occured during parsing: %s", ex.getMessage()),
          ex);
    }
  }
}
