package gov.nist.secauto.metaschema.datatype.writer.xml;

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

import gov.nist.secauto.metaschema.datatype.binding.ClassBinding;
import gov.nist.secauto.metaschema.datatype.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public abstract class AbstractXmlWriter<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements XmlWriter<CLASS> {
	private final CLASS_BINDING classBinding;

	protected AbstractXmlWriter(CLASS_BINDING classBinding) {
		this.classBinding = classBinding;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	@Override
	public void writeXml(CLASS obj, QName name, XmlWritingContext writingContext) throws BindingException {
		if (name == null) {
			throw new BindingException("Unspecified QName");
		}

		XMLEventFactory2 factory = writingContext.getXMLEventFactory();
		StartElement start = factory.createStartElement(name, gatherAttributes(obj, factory).iterator(), null);

		EndElement end = factory.createEndElement(name, null);

		try {
			XMLEventWriter writer = writingContext.getEventWriter();
			writer.add(start);

			writeBody(obj, writingContext);

			writer.add(end);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected abstract void writeBody(CLASS obj, XmlWritingContext writingContext);

	public List<Attribute> gatherAttributes(CLASS obj, XMLEventFactory2 factory) throws BindingException {
		List<Attribute> retval = new LinkedList<>();
		for (FlagPropertyBinding flagBinding : getClassBinding().getFlagPropertyBindings()) {
			QName name = flagBinding.getXmlQName();
			String value;
			try {
				value = flagBinding.getPropertyInfo().getPropertyAccessor().getValue(obj).toString();
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new BindingException("Unable to access flag value.", ex);
			}
			Attribute attribute = factory.createAttribute(name, value);
			retval.add(attribute);
		}
		return retval.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(retval);
	}
}
