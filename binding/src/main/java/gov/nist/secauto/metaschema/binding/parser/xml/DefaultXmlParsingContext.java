package gov.nist.secauto.metaschema.binding.parser.xml;

import java.util.Objects;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.binding.BindingContext;

public class DefaultXmlParsingContext implements XmlParsingContext {
	private final BindingContext bindingContext;
	private final XMLEventReader2 eventReader;
	private final XmlProblemHandler xmlProblemHandler;

	public DefaultXmlParsingContext(XMLEventReader2 eventReader, BindingContext bindingContext) {
		this(eventReader, bindingContext, new XmlProblemHandler());
	}

	public DefaultXmlParsingContext(XMLEventReader2 eventReader, BindingContext bindingContext, XmlProblemHandler xmlProblemHandler) {
		Objects.requireNonNull(eventReader, "eventReader");
		Objects.requireNonNull(bindingContext, "bindingContext");
		Objects.requireNonNull(xmlProblemHandler, "xmlProblemHandler");
		this.eventReader = eventReader;
		this.bindingContext = bindingContext;
		this.xmlProblemHandler = xmlProblemHandler;
	}

	@Override
	public BindingContext getBindingContext() {
		return bindingContext;
	}

	@Override
	public XmlProblemHandler getProblemHandler() {
		return xmlProblemHandler;
	}

	@Override
	public XMLEventReader2 getEventReader() {
		return eventReader;
	}

}
