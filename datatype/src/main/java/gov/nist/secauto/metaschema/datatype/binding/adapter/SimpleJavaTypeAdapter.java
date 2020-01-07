package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlEventUtil;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;

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
	public TYPE parseType(XmlParsingContext parsingContext) throws BindingException {
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
			return parseValue(builder.toString().trim());
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
