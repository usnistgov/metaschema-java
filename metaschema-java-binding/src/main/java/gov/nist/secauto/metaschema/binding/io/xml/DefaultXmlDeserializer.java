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

package gov.nist.secauto.metaschema.binding.io.xml;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractDeserializer;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.RootDefinitionAssemblyProperty;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class DefaultXmlDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private static final Logger logger = LogManager.getLogger(DefaultXmlDeserializer.class);

  private XMLInputFactory2 xmlInputFactory;

  public DefaultXmlDeserializer(IBindingContext bindingContext, IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  // @Override
  // public Format supportedFromat() {
  // return Format.XML;
  // }

  protected XMLInputFactory2 getXMLInputFactory() {
    synchronized (this) {
      if (xmlInputFactory == null) {
        xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
        xmlInputFactory.configureForXmlConformance();
        xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);
        // xmlInputFactory.configureForSpeed();
      }
      return xmlInputFactory;
    }
  }

  protected void setXMLInputFactory(XMLInputFactory2 factory) {
    synchronized (this) {
      this.xmlInputFactory = factory;
    }
  }

  protected XMLEventReader2 newXMLEventReader2(Reader reader) throws BindingException {

    try {
      XMLEventReader eventReader = getXMLInputFactory().createXMLEventReader(reader);
      EventFilter filter = new CommentFilter();
      return (XMLEventReader2) getXMLInputFactory().createFilteredReader(eventReader, filter);
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  protected IBoundXdmNodeItem deserializeToNodeItemInternal(Reader reader, URI documentUri) throws BindingException {
    XMLEventReader2 eventReader = newXMLEventReader2(reader);
    try {
      return parseXmlInternal(eventReader, documentUri);
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  protected IBoundXdmNodeItem parseXmlInternal(XMLEventReader2 reader, @Nullable URI documentUri)
      throws XMLStreamException, BindingException {

    IAssemblyClassBinding classBinding = getClassBinding();

    DefaultXmlParsingContext parsingContext = new DefaultXmlParsingContext(reader, new DefaultXmlProblemHandler());

    CLASS retval;
    IBoundXdmNodeItem parsedNodeItem;
    if (classBinding.isRoot() && getConfiguration().isFeatureEnabled(Feature.DESERIALIZE_ROOT)) {
      // we may be at the START_DOCUMENT
      if (reader.peek().isStartDocument()) {
        XmlEventUtil.consumeAndAssert(reader, XMLEvent.START_DOCUMENT);
      }

      XmlEventUtil.skipProcessingInstructions(reader);

      RootDefinitionAssemblyProperty property = new RootDefinitionAssemblyProperty(classBinding);
      try {
        @SuppressWarnings("unchecked")
        CLASS value = (CLASS) property.read(parsingContext);
        retval = value;
      } catch (IOException | XMLStreamException ex) {
        throw new BindingException(ex);
      }

      // XmlEventUtil.consumeAndAssert(reader, XMLEvent.END_ELEMENT);
      XmlEventUtil.consumeAndAssert(reader, XMLEvent.END_DOCUMENT);
      parsedNodeItem = IXdmFactory.INSTANCE.newDocumentNodeItem(property, retval, documentUri);
    } else {
      try {
        @SuppressWarnings("unchecked")
        CLASS value = (CLASS) classBinding.readItem(null, null, parsingContext);
        retval = value;
      } catch (IOException | XMLStreamException ex) {
        throw new BindingException(ex);
      }
      parsedNodeItem = IXdmFactory.INSTANCE.newRelativeAssemblyNodeItem(classBinding, retval, documentUri);
    }

    if (reader.hasNext()) {
      if (logger.isDebugEnabled()) {
        logger.debug("After Parse: {}", XmlEventUtil.toString(reader.peek()));
      }
    }

    return parsedNodeItem;
  }
}
