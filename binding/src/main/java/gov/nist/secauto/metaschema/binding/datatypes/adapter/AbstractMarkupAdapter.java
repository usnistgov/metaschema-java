package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.markup.MarkupString;

public abstract class AbstractMarkupAdapter<TYPE extends MarkupString> implements JavaTypeAdapter<TYPE> {
	private final MarkupParser markupParser = new MarkupParser();

	protected MarkupParser getMarkupParser() {
		return markupParser;
	}

	@Override
	public boolean isParsingStartElement() {
		return false;
	}

	@Override
	public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext)
			throws BindingException {
		XMLEventFactory2 eventFactory = writingContext.getXMLEventFactory();
		XMLEventWriter writer = writingContext.getEventWriter();

		try {
			if (valueQName != null) {
				StartElement start = eventFactory.createStartElement(valueQName, null, null);
				writer.add(start);
				parent = start;
			}

			writeXmlElementInternal(value, parent, writingContext);

			if (valueQName != null) {
				EndElement end = eventFactory.createEndElement(valueQName, null);
				writer.add(end);
			}
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected abstract void writeXmlElementInternal(Object value, StartElement parent, XmlWritingContext writingContext)
			throws BindingException;
}
