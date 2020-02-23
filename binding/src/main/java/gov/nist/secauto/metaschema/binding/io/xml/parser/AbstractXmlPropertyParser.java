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
package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractXmlPropertyParser<BINDING extends PropertyBinding> implements XmlPropertyParser {
  private static final Logger logger = LogManager.getLogger(AbstractXmlPropertyParser.class);

  private final BINDING propertyBinding;
  private final BindingContext bindingContext;

  public AbstractXmlPropertyParser(BINDING propertyBinding, BindingContext bindingContext) {
    this.propertyBinding = propertyBinding;
    this.bindingContext = bindingContext;
  }

  @Override
  public BINDING getPropertyBinding() {
    return propertyBinding;
  }

  protected BindingContext getBindingContext() {
    return bindingContext;
  }

  protected StartElement consumeStartElement(XMLEventReader2 reader, QName name) throws BindingException {
    XMLEvent event;
    try {
      event = reader.nextEvent();
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Consume: {}", XmlEventUtil.toString(event));
    }
    StartElement startElement = event.asStartElement();
    QName actualName = startElement.getName();
    if (!actualName.equals(name)) {
      throw new BindingException(String.format("Unexpected START ELEMENT '%s' at '%s'. Expected'%s'.", actualName,
          XmlEventUtil.toString(startElement.getLocation()), name));
    }
    return startElement;
  }

  protected EndElement consumeEndElement(XMLEventReader2 reader, QName name) throws BindingException {
    XMLEvent event;
    try {
      event = reader.nextEvent();
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Consume: {}", XmlEventUtil.toString(event));
    }
    try {
      EndElement element = event.asEndElement();
      QName actualName = element.getName();
      if (!actualName.equals(name)) {
        throw new BindingException(String.format("Unexpected END ELEMENT name '%s' at '%s'. Expected'%s'.", actualName,
            XmlEventUtil.toString(element.getLocation()), name));
      }
      return element;
    } catch (ClassCastException ex) {
      throw new BindingException(String.format("Expected END ELEMENT, but found '%s' at '%s'.",
          XmlEventUtil.toString(event), XmlEventUtil.toString(event.getLocation()), name));
    }
  }

}
