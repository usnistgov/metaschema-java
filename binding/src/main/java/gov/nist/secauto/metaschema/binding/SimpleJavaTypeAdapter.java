package gov.nist.secauto.metaschema.binding;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;

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

	@Override
	public void write(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext) throws BindingException {
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
	public boolean canHandleQName(QName nextQName) {
		// This adapter is always consuming a simple string value.
		return false;
	}
}
