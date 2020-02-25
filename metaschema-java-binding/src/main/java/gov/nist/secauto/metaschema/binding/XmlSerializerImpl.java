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

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.xml.writer.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.binding.io.xml.writer.DefaultXmlWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

class XmlSerializerImpl<CLASS> extends AbstractSerializer<CLASS> {
  private XMLOutputFactory2 xmlOutputFactory;
  private XMLEventFactory2 xmlEventFactory;

  public XmlSerializerImpl(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
      Configuration configuration) {
    super(bindingContext, classBinding, configuration);
  }

  protected XMLOutputFactory2 getXMLOutputFactory() {
    synchronized (this) {
      if (xmlOutputFactory == null) {
        xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
        xmlOutputFactory.configureForSpeed();
        xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        xmlEventFactory = (XMLEventFactory2) WstxEventFactory.newInstance();
      }
      return xmlOutputFactory;
    }
  }

  protected void setXMLOutputFactory(XMLOutputFactory2 xmlOutputFactory) {
    synchronized (this) {
      this.xmlOutputFactory = xmlOutputFactory;
    }
  }

  protected XMLEventFactory2 getXmlEventFactory() {
    return xmlEventFactory;
  }

  protected void setXmlEventFactory(XMLEventFactory2 xmlEventFactory) {
    this.xmlEventFactory = xmlEventFactory;
  }

  protected XMLEventWriter newXMLEventWriter(Writer writer) throws BindingException {

    try {
      return getXMLOutputFactory().createXMLEventWriter(writer);
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  public void serialize(CLASS data, Writer writer) throws BindingException {
    XMLEventWriter eventWriter = newXMLEventWriter(writer);
    XMLEventFactory2 eventFactory = getXmlEventFactory();
    try {
      eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
      writeXmlInternal(data, eventFactory, eventWriter);
      eventWriter.add(eventFactory.createEndDocument());
      eventWriter.close();
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  protected void writeXmlInternal(Object obj, XMLEventFactory2 eventFactory, XMLEventWriter eventWriter)
      throws BindingException {
    BindingContext bindingContext = getBindingContext();
    AssemblyClassBinding<CLASS> classBinding = getClassBinding();
    AssemblyXmlWriter<CLASS> writer = classBinding.getXmlWriter();
    XmlWritingContext writingContext = new DefaultXmlWritingContext(eventFactory, eventWriter, bindingContext);
    writer.writeXml(obj, null, writingContext);
  }

}
