package gov.nist.secauto.metaschema.binding.writer.xml;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.binding.ClassBinding;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;

public abstract class AbstractXmlWriter<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements XmlWriter {
	private final CLASS_BINDING classBinding;

	protected AbstractXmlWriter(CLASS_BINDING classBinding) {
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	@Override
	public void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException {
		if (name == null) {
			throw new BindingException("Unspecified QName");
		}

		XMLEventFactory2 factory = writingContext.getXMLEventFactory();
		StartElement start = factory.createStartElement(name, gatherAttributes(obj, factory).iterator(), null);

		EndElement end = factory.createEndElement(name, null);

		try {
			XMLEventWriter writer = writingContext.getEventWriter();
			writer.add(start);

			writeBody(obj, start, writingContext);

			writer.add(end);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected abstract void writeBody(Object obj, StartElement parent, XmlWritingContext writingContext)
			throws BindingException;

	public List<Attribute> gatherAttributes(Object obj, XMLEventFactory2 factory) throws BindingException {
		List<Attribute> retval = new LinkedList<>();
		for (FlagPropertyBinding flagBinding : getClassBinding().getFlagPropertyBindings()) {
			QName name = flagBinding.getXmlQName();
			String value;

			Object objectValue = flagBinding.getPropertyInfo().getValue(obj);
			if (objectValue != null) {
				value = objectValue.toString();
			} else {
				value = null;
			}

			if (value != null) {
				Attribute attribute = factory.createAttribute(name, value);
				retval.add(attribute);
			}
		}
		return retval.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(retval);
	}
}
