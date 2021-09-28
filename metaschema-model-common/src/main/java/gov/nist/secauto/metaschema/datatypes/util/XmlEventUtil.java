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

package gov.nist.secauto.metaschema.datatypes.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamReader2;

import java.util.Objects;
import java.util.regex.Pattern;
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

  private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s+$");

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

  /**
   * Generate a message suitable for logging that describes the provided {@link XMLEvent}.
   * 
   * @param xmlEvent
   *          the event to generate the message for
   * @return the message
   */
  public static String toString(XMLEvent xmlEvent) {
    int type = xmlEvent.getEventType();
    StringBuilder builder = new StringBuilder()
        .append(toEventName(type));
    QName name = toQName(xmlEvent);
    if (name != null) {
      builder.append(": ").append(name.toString());
    }
    if (xmlEvent.isCharacters()) {
      String text = xmlEvent.asCharacters().getData();
      if (text != null) {
        builder.append(" '").append(escape(text)).append('\'');
      }
    }
    Location location = toLocation(xmlEvent);
    if (location != null) {
      builder.append(" at ").append(toString(location));
    }
    return builder.toString();
  }

  /**
   * Generates a message for the provided {@link Location}.
   * 
   * @param location
   *          the location to generate the message for
   * @return the message
   */
  public static String toString(Location location) {
    StringBuilder builder = new StringBuilder();
    builder.append(location.getLineNumber());
    builder.append(':');
    builder.append(location.getColumnNumber());
    return builder.toString();
  }

  public static String toString(XMLStreamReader2 reader) {
    int type = reader.getEventType();
    StringBuilder builder = new StringBuilder().append(toEventName(type));
    QName name = reader.getName();
    if (name != null) {
      builder.append(": ").append(name.toString());
    }
    if (XMLEvent.CHARACTERS == type) {
      String text = reader.getText();
      if (text != null) {
        builder.append(" '").append(escape(text)).append('\'');
      }
    }
    Location location = reader.getLocation();
    if (location != null) {
      builder.append(" at ").append(toString(location));
    }
    return builder.toString();
  }

  public static Location toLocation(XMLEvent event) {
    Location retval = null;
    if (event.isStartElement()) {
      StartElement start = event.asStartElement();
      retval = start.getLocation();
    } else if (event.isEndElement()) {
      EndElement end = event.asEndElement();
      retval = end.getLocation();
    } else if (event.isCharacters()) {
      Characters characters = event.asCharacters();
      retval = characters.getLocation();
    }
    return retval;
  }

  public static QName toQName(XMLEvent event) {
    QName retval = null;
    if (event.isStartElement()) {
      StartElement start = event.asStartElement();
      retval = start.getName();
    } else if (event.isEndElement()) {
      EndElement end = event.asEndElement();
      retval = end.getName();
    }
    return retval;
  }

  public static String toEventName(XMLEvent event) {
    return toEventName(event.getEventType());
  }

  public static String toEventName(int eventType) {
    String retval;
    switch (eventType) {
    case XMLStreamConstants.START_ELEMENT:
      retval = "START_ELEMENT";
      break;
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
      retval = "unknown event '" + Integer.toString(eventType) + "'";
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
      if (WHITESPACE_ONLY.matcher(data).matches()) {
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

  public static XMLEvent consumeAndAssert(XMLEventReader2 eventReader, int presumedEventType)
      throws XMLStreamException {
    return consumeAndAssert(eventReader, presumedEventType, null);
  }

  public static XMLEvent consumeAndAssert(XMLEventReader2 eventReader, int presumedEventType, QName presumedName)
      throws XMLStreamException {
    XMLEvent retval = eventReader.nextEvent();

    int eventType = retval.getEventType();
    QName name = toQName(retval);
    assert eventType == presumedEventType
        && (presumedName == null
            || presumedName.equals(name)) : generateAssertMessage(
                retval,
                presumedEventType,
                presumedName);
    return retval;
  }

  public static void assertNext(XMLEventReader2 eventReader, int presumedEventType) throws XMLStreamException {
    assertNext(eventReader, presumedEventType, null);
  }

  public static void assertNext(XMLEventReader2 eventReader, int presumedEventType, QName presumedName)
      throws XMLStreamException {
    XMLEvent nextEvent = eventReader.peek();

    int eventType = nextEvent.getEventType();
    QName name = toQName(nextEvent);
    assert eventType == presumedEventType
        && (presumedName == null || presumedName.equals(name)) : generateAssertMessage(nextEvent, presumedEventType,
            presumedName);
  }

  private static String generateAssertMessage(XMLEvent retval, int presumedEventType, QName presumedName) {
    StringBuilder builder = new StringBuilder();
    builder
        .append("Expected XML ")
        .append(toEventName(presumedEventType));

    if (presumedName != null) {
      builder.append(" for QName '")
          .append(presumedName.toString());
    }
    builder.append("', instead found ")
        .append(toString(retval));
    return builder.toString();
  }
}
