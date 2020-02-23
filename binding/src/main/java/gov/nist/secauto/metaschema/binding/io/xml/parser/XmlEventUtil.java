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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamReader2;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlEventUtil {
  private static final Logger logger = LogManager.getLogger(XmlEventUtil.class);

  private XmlEventUtil() {
    // disbale construction
  }

  private static Object escape(String data) {
    return data.chars().mapToObj(c -> (char) c).map(c -> escape(c)).collect(Collectors.joining());
  }

  private static String escape(char ch) {
    String retval;
    switch (ch) {
    case '\n':
      retval = "\\n";
      break;
    case '\r':
      retval = "\\r";
      break;
    default:
      retval = String.valueOf(ch);
    }
    return retval;
  }

  public static String toString(XMLEvent xmlEvent) {
    int type = xmlEvent.getEventType();

    String retval;
    switch (type) {
    case XMLStreamConstants.START_ELEMENT: {
      StartElement event = xmlEvent.asStartElement();
      retval = String.format("START_ELEMENT: %s", event.getName());
      break;
    }
    case XMLStreamConstants.END_ELEMENT: {
      EndElement event = xmlEvent.asEndElement();
      retval = String.format("END_ELEMENT: %s", event.getName());
      break;
    }
    case XMLStreamConstants.PROCESSING_INSTRUCTION:
      retval = "PROCESSING_INSTRUCTION";
      break;
    case XMLStreamConstants.CHARACTERS: {
      Characters event = xmlEvent.asCharacters();
      retval = String.format("CHARACTERS: '%s'", escape(event.getData()));
      break;
    }
    case XMLStreamConstants.COMMENT:
      retval = "COMMENT";
      break;
    case XMLStreamConstants.SPACE:
      retval = "SPACE";
      break;
    case XMLStreamConstants.START_DOCUMENT:
      retval = "START_DOCUMENT";
      break;
    case XMLStreamConstants.END_DOCUMENT:
      retval = "END_DOCUMENT";
      break;
    case XMLStreamConstants.ENTITY_REFERENCE:
      retval = "ENTITY_REFERENCE";
      break;
    case XMLStreamConstants.ATTRIBUTE:
      retval = "ATTRIBUTE";
      break;
    case XMLStreamConstants.DTD:
      retval = "DTD";
      break;
    case XMLStreamConstants.CDATA:
      retval = "CDATA";
      break;
    case XMLStreamConstants.NAMESPACE:
      retval = "NAMESPACE";
      break;
    case XMLStreamConstants.NOTATION_DECLARATION:
      retval = "NOTATION_DECLARATION";
      break;
    case XMLStreamConstants.ENTITY_DECLARATION:
      retval = "ENTITY_DECLARATION";
      break;
    default:
      retval = "unknown event '" + Integer.toString(type) + "'";
    }
    return retval;
  }

  public static String toString(Location location) {
    StringBuilder builder = new StringBuilder();
    builder.append(location.getLineNumber());
    builder.append(':');
    builder.append(location.getColumnNumber());
    return builder.toString();
  }

  public static String toString(XMLStreamReader2 reader) {
    int type = reader.getEventType();
    String retval;
    switch (type) {
    case XMLStreamConstants.START_ELEMENT: {
      retval = String.format("START_ELEMENT: %s", reader.getName());
      break;
    }
    case XMLStreamConstants.END_ELEMENT:
      retval = "END_ELEMENT";
      break;
    case XMLStreamConstants.PROCESSING_INSTRUCTION:
      retval = "PROCESSING_INSTRUCTION";
      break;
    case XMLStreamConstants.CHARACTERS:
      retval = "CHARACTERS";
      break;
    case XMLStreamConstants.COMMENT:
      retval = "COMMENT";
      break;
    case XMLStreamConstants.SPACE:
      retval = "SPACE";
      break;
    case XMLStreamConstants.START_DOCUMENT:
      retval = "START_DOCUMENT";
      break;
    case XMLStreamConstants.END_DOCUMENT:
      retval = "END_DOCUMENT";
      break;
    case XMLStreamConstants.ENTITY_REFERENCE:
      retval = "ENTITY_REFERENCE";
      break;
    case XMLStreamConstants.ATTRIBUTE:
      retval = "ATTRIBUTE";
      break;
    case XMLStreamConstants.DTD:
      retval = "DTD";
      break;
    case XMLStreamConstants.CDATA:
      retval = "CDATA";
      break;
    case XMLStreamConstants.NAMESPACE:
      retval = "NAMESPACE";
      break;
    case XMLStreamConstants.NOTATION_DECLARATION:
      retval = "NOTATION_DECLARATION";
      break;
    case XMLStreamConstants.ENTITY_DECLARATION:
      retval = "ENTITY_DECLARATION";
      break;
    default:
      retval = "unknown event '" + Integer.toString(type) + "'";
    }
    return retval;
  }

  public static XMLEvent advanceTo(XMLEventReader2 reader, int eventType) throws XMLStreamException {
    XMLEvent xmlEvent;
    do {
      xmlEvent = reader.nextEvent();
      logger.warn("skipping over: {}", XmlEventUtil.toString(xmlEvent));
      if (xmlEvent.isStartElement()) {
        advanceTo(reader, XMLStreamConstants.END_ELEMENT);
        // skip this end element
        xmlEvent = reader.nextEvent();
        if (logger.isDebugEnabled()) {
          logger.debug("skipping over: {}", XmlEventUtil.toString(xmlEvent));
        }
      }
    } while (reader.hasNext() && (xmlEvent = reader.peek()).getEventType() != eventType);
    return xmlEvent;
  }

  public static XMLEvent skipWhitespace(XMLEventReader2 reader) throws XMLStreamException {
    XMLEvent nextEvent;
    while ((nextEvent = reader.peek()).isCharacters()) {
      Characters characters = nextEvent.asCharacters();
      String data = characters.getData();
      if (data.isBlank()) {
        nextEvent = reader.nextEvent();
      } else {
        break;
      }
    }
    return nextEvent;
  }

  public static boolean isNextEventEndElement(XMLEventReader2 reader, QName name) throws XMLStreamException {
    return isNextEventEndElement(reader, name.getLocalPart(), name.getNamespaceURI());
  }

  public static boolean isNextEventEndElement(XMLEventReader2 reader, String expectedLocalName,
      String expectedNamespace) throws XMLStreamException {
    Objects.requireNonNull(reader, "reader");
    Objects.requireNonNull(expectedLocalName, "expectedLocalName");
    XMLEvent event = reader.peek();

    boolean retval = true;
    if (!event.isEndElement()) {
      retval = false;
    } else {
      EndElement endElement = event.asEndElement();
      QName name = endElement.getName();
      if (!expectedLocalName.equals(name.getLocalPart())) {
        retval = false;
      } else if (expectedNamespace != null && !expectedNamespace.equals(name.getNamespaceURI())) {
        retval = false;
      }
    }
    return retval;
  }

  public static boolean isNextEventStartElement(XMLEventReader2 reader, QName name) throws XMLStreamException {
    XMLEvent nextEvent = reader.peek();
    return nextEvent.isStartElement() && name.equals(nextEvent.asStartElement().getName());
  }

  public static boolean isNextEventEndDocument(XMLEventReader2 reader) throws XMLStreamException {
    return reader.peek().isEndDocument();
  }
}
