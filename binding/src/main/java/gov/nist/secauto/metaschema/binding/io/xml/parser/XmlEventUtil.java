package gov.nist.secauto.metaschema.binding.io.xml.parser;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamReader2;

public class XmlEventUtil {
	private static final Logger logger = LogManager.getLogger(XmlEventUtil.class);

	private XmlEventUtil() {
		// disbale construction
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

	private static Object escape(String data) {
		return data.chars().mapToObj(c -> (char) c).map(c -> escape(c)).collect(Collectors.joining());
	}

	private static String escape(char c) {
		String retval;
		switch (c) {
		case '\n':
			retval = "\\n";
			break;
		case '\r':
			retval = "\\r";
			break;
		default:
			retval = String.valueOf(c);
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

	public static boolean isNextEventEndElement(XMLEventReader2 reader, String expectedLocalName, String expectedNamespace) throws XMLStreamException {
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
			} else if (expectedNamespace != null && !expectedNamespace.equals(name.getNamespaceURI())){
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
