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

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jmock.Expectations;

public class MockXmlSupport {
  private MockXmlSupport() {
    // disable construction
  }

  public static void mockElementXMLEvent(Expectations expectations, StartElement startEvent, EndElement endEvent,
      QName name) {

    expectations.allowing(startEvent).getEventType();
    expectations.will(Expectations.returnValue(XMLEvent.START_ELEMENT));
    expectations.allowing(startEvent).asStartElement();
    expectations.will(Expectations.returnValue(startEvent));
    expectations.allowing(startEvent).getName();
    expectations.will(Expectations.returnValue(name));
    expectations.allowing(startEvent).isStartElement();
    expectations.will(Expectations.returnValue(true));
    expectations.allowing(startEvent).isEndElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(startEvent).isAttribute();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(startEvent).isCharacters();
    expectations.will(Expectations.returnValue(false));

    expectations.allowing(endEvent).getEventType();
    expectations.will(Expectations.returnValue(XMLEvent.END_ELEMENT));
    expectations.allowing(endEvent).asEndElement();
    expectations.will(Expectations.returnValue(endEvent));
    expectations.allowing(endEvent).getName();
    expectations.will(Expectations.returnValue(name));
    expectations.allowing(endEvent).isStartElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(endEvent).isEndElement();
    expectations.will(Expectations.returnValue(true));
    expectations.allowing(endEvent).isAttribute();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(endEvent).isCharacters();
    expectations.will(Expectations.returnValue(false));
  }

  static void mockCharactersXMLEvent(Expectations expectations, Characters event, String text) {
    expectations.allowing(event).getEventType();
    expectations.will(Expectations.returnValue(XMLEvent.CHARACTERS));
    expectations.allowing(event).asCharacters();
    expectations.will(Expectations.returnValue(event));
    expectations.allowing(event).isStartElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isEndElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isAttribute();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isCharacters();
    expectations.will(Expectations.returnValue(true));
    expectations.allowing(event).getData();
    expectations.will(Expectations.returnValue(text));
  }

  public static void mockAttributeXMLEvent(Expectations expectations, Attribute event, QName name, String text) {
    expectations.allowing(event).getEventType();
    expectations.will(Expectations.returnValue(XMLEvent.ATTRIBUTE));
    expectations.allowing(event).getName();
    expectations.will(Expectations.returnValue(name));
    expectations.allowing(event).isStartElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isEndElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isAttribute();
    expectations.will(Expectations.returnValue(true));
    expectations.allowing(event).isCharacters();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).getValue();
    expectations.will(Expectations.returnValue(text));
  }

  public static void mockEndDocument(Expectations expectations, EndDocument event) {
    expectations.allowing(event).getEventType();
    expectations.will(Expectations.returnValue(XMLEvent.END_DOCUMENT));
    expectations.allowing(event).isStartElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isEndElement();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isAttribute();
    expectations.will(Expectations.returnValue(false));
    expectations.allowing(event).isCharacters();
    expectations.will(Expectations.returnValue(false));
  }

}
