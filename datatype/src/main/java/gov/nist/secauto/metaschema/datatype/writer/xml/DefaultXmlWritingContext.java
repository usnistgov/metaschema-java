package gov.nist.secauto.metaschema.datatype.writer.xml;

import java.util.Objects;

import javax.xml.stream.XMLEventWriter;

import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;

public class DefaultXmlWritingContext implements XmlWritingContext {
	private final BindingContext bindingContext;
	private final XMLEventFactory2 eventFactory;
	private final XMLEventWriter eventWriter;

	public DefaultXmlWritingContext(XMLEventFactory2 eventFactory, XMLEventWriter eventWriter,
			BindingContext bindingContext) {
		Objects.requireNonNull(eventFactory, "eventFactory");
		Objects.requireNonNull(eventWriter, "eventWriter");
		Objects.requireNonNull(bindingContext, "bindingContext");
		this.eventFactory = eventFactory;
		this.eventWriter = eventWriter;
		this.bindingContext = bindingContext;
	}

	@Override
	public BindingContext getBindingContext() {
		return bindingContext;
	}

	@Override
	public XMLEventFactory2 getXMLEventFactory() {
		return eventFactory;
	}

	@Override
	public XMLEventWriter getEventWriter() {
		return eventWriter;
	}

}
