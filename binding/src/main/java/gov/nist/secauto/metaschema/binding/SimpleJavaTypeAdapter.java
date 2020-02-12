package gov.nist.secauto.metaschema.binding;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public abstract class SimpleJavaTypeAdapter<TYPE> implements JavaTypeAdapter<TYPE> {
	@Override
	public boolean isParsingStartElement() {
		return false;
	}

//	@Override
//	public boolean isParsingEndElement() {
//		return true;
//	}

	@Override
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always consuming a simple string value.
		return false;
	}

	@Override
	public TYPE parse(XmlParsingContext parsingContext) throws BindingException {
		XMLEventReader2 reader = parsingContext.getEventReader();
		StringBuilder builder = new StringBuilder();
		try {
			XMLEvent nextEvent;
			while (!(nextEvent = reader.peek()).isEndElement()) {
				if (nextEvent.isCharacters()) {
					Characters characters = nextEvent.asCharacters();
					builder.append(characters.getData());
					// advance past current event
					reader.nextEvent();
				} else {
					throw new BindingException(String.format("Invalid content '%s' at %s", XmlEventUtil.toString(nextEvent), XmlEventUtil.toString(nextEvent.getLocation())));
				}
			}
			// trim leading and trailing whitespace
			return parse(builder.toString().trim());
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	/**
	 * This default implementation will parse the value as a string and delegate to the string-based parsing method.
	 */
	@Override
	public TYPE parse(JsonParsingContext parsingContext) throws BindingException {
		JsonParser parser = parsingContext.getEventReader();
		String value;
		try {
			value = parser.getValueAsString();
			// skip over value
			parser.nextToken();
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		if (value == null) {
			throw new BindingException("Unable to parse field value as text");
		}
		return parse(value);
	}

	@Override
	public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		XMLEventFactory2 eventFactory = writingContext.getXMLEventFactory();
		XMLEventWriter writer = writingContext.getEventWriter();
		try {
			if (valueQName != null) {
				StartElement start = eventFactory.createStartElement(valueQName, null, null);
				writer.add(start);
			}
	
			String content = value.toString();
			Characters characters = eventFactory.createCharacters(content);
			try {
				writer.add(characters);
			} catch (XMLStreamException ex) {
				throw new BindingException(ex);
			}
	
			if (valueQName != null) {
				EndElement end = eventFactory.createEndElement(valueQName, null);
				writer.add(end);
			}
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeString(value.toString());
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
		
	}

	@Override
	public String getDefaultJsonFieldName() {
		return "STRVALUE";
	}

	@Override
	public boolean isUnrappedValueAllowedInXml() {
		return false;
	}
}
