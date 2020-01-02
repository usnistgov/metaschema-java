package gov.nist.secauto.metaschema.datatype.parser.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.binding.PropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public abstract class AbstractXmlPropertyParser<BINDING extends PropertyBinding> implements XmlPropertyParser {
	private static final Logger logger = LogManager.getLogger(AbstractXmlPropertyParser.class);

	private final BINDING propertyBinding;
	private final XmlParser parser;

	public AbstractXmlPropertyParser(BINDING propertyBinding, XmlParser parser) {
		this.propertyBinding = propertyBinding;
		this.parser = parser;
	}

	@Override
	public BINDING getPropertyBinding() {
		return propertyBinding;
	}

	protected XmlParser getParser() {
		return parser;
	}

	protected StartElement consumeStartElement(XMLEventReader2 reader, QName name) throws BindingException {
		XMLEvent event;
		try {
			event = reader.nextEvent();
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Consume: {}",XmlEventUtil.toString(event));
		}
		StartElement startElement = event.asStartElement();
		QName actualName = startElement.getName();
		if (!actualName.equals(name)) {
			throw new BindingException(String.format("Unexpected START ELEMENT '%s' at '%s'. Expected'%s'.", actualName, XmlEventUtil.toString(startElement.getLocation()), name));
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
			logger.debug("Consume: {}",XmlEventUtil.toString(event));
		}
		try {
			EndElement element = event.asEndElement();
			QName actualName = element.getName();
			if (!actualName.equals(name)) {
				throw new BindingException(String.format("Unexpected END ELEMENT name '%s' at '%s'. Expected'%s'.", actualName, XmlEventUtil.toString(element.getLocation()), name));
			}
			return element;
		} catch (ClassCastException ex) {
			throw new BindingException(String.format("Expected END ELEMENT, but found '%s' at '%s'.", XmlEventUtil.toString(event),XmlEventUtil.toString(event.getLocation()), name));
		}
	}

}
