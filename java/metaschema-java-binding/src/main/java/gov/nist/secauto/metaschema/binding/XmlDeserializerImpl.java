/**
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

package gov.nist.secauto.metaschema.binding;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.xml.parser.CommentFilter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import java.io.Reader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

class XmlDeserializerImpl<CLASS> extends AbstractDeserializer<CLASS> {
  private static final Logger logger = LogManager.getLogger(XmlDeserializerImpl.class);

  private XMLInputFactory2 xmlInputFactory;

  public XmlDeserializerImpl(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
      Configuration configuration) {
    super(bindingContext, classBinding, configuration);
  }

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
  public CLASS deserialize(Reader reader) throws BindingException {
    XMLEventReader2 eventReader = newXMLEventReader2(reader);
    return parseXmlInternal(eventReader);
  }

  protected CLASS parseXmlInternal(XMLEventReader2 reader) throws BindingException {

    BindingContext bindingContext = getBindingContext();

    CLASS retval;
    // we may be at the START_DOCUMENT
    try {
      if (reader.peek().isStartDocument()) {
        while (reader.hasNextEvent() && !reader.peek().isStartElement()) {
          // advance to the START_ELEMENT
          // TODO: remove
          logger.debug("Skip: {}", XmlEventUtil.toString(reader.nextEvent()));
        }
      }
      ClassBinding<CLASS> classBinding = getClassBinding();
      XmlParsePlan<CLASS> plan = classBinding.getXmlParsePlan(bindingContext);
      XmlParsingContext parsingContext = new DefaultXmlParsingContext(reader, bindingContext);
      retval = (CLASS) plan.parse(parsingContext);
      if (reader.hasNext()) {
        logger.debug("After Parse: {}", XmlEventUtil.toString(reader.peek()));

        assert XmlEventUtil.isNextEventEndDocument(reader) : XmlEventUtil.toString(reader.peek());
        // XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_DOCUMENT);
      }
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
    return retval;
  }
}
