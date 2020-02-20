/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.io.xml.writer;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;

import org.codehaus.stax2.evt.XMLEventFactory2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public abstract class AbstractXmlWriter<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements XmlWriter {
  private final CLASS_BINDING classBinding;

  protected AbstractXmlWriter(CLASS_BINDING classBinding) {
    this.classBinding = classBinding;
  }

  protected CLASS_BINDING getClassBinding() {
    return classBinding;
  }

  @Override
  public void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException {
    if (name == null) {
      throw new BindingException("Unspecified QName");
    }

    XMLEventFactory2 factory = writingContext.getXMLEventFactory();
    StartElement start = factory.createStartElement(name, gatherAttributes(obj, factory).iterator(), null);

    EndElement end = factory.createEndElement(name, null);

    try {
      XMLEventWriter writer = writingContext.getEventWriter();
      writer.add(start);

      writeBody(obj, start, writingContext);

      writer.add(end);
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  protected abstract void writeBody(Object obj, StartElement parent, XmlWritingContext writingContext)
      throws BindingException;

  public List<Attribute> gatherAttributes(Object obj, XMLEventFactory2 factory) throws BindingException {
    List<Attribute> retval = new LinkedList<>();
    for (FlagPropertyBinding flagBinding : getClassBinding().getFlagPropertyBindings()) {
      QName name = flagBinding.getXmlQName();
      String value;

      Object objectValue = flagBinding.getPropertyInfo().getValue(obj);
      if (objectValue != null) {
        value = objectValue.toString();
      } else {
        value = null;
      }

      if (value != null) {
        Attribute attribute = factory.createAttribute(name, value);
        retval.add(attribute);
      }
    }
    return retval.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(retval);
  }
}
