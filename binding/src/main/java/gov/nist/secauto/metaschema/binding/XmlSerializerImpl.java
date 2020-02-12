package gov.nist.secauto.metaschema.binding;

import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.xml.writer.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.binding.io.xml.writer.DefaultXmlWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

class XmlSerializerImpl<CLASS> extends AbstractSerializer<CLASS> {
	private XMLOutputFactory2 xmlOutputFactory;
	private XMLEventFactory2 xmlEventFactory;

	public XmlSerializerImpl(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
			Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	protected XMLOutputFactory2 getXMLOutputFactory() {
		synchronized (this) {
			if (xmlOutputFactory == null) {
				xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
				xmlOutputFactory.configureForSpeed();
				xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
				xmlEventFactory = (XMLEventFactory2) WstxEventFactory.newInstance();
			}
			return xmlOutputFactory;
		}
	}

	protected void setXMLOutputFactory(XMLOutputFactory2 xmlOutputFactory) {
		synchronized (this) {
			this.xmlOutputFactory = xmlOutputFactory;
		}
	}

	protected XMLEventFactory2 getXmlEventFactory() {
		return xmlEventFactory;
	}

	protected void setXmlEventFactory(XMLEventFactory2 xmlEventFactory) {
		this.xmlEventFactory = xmlEventFactory;
	}

	protected XMLEventWriter newXMLEventWriter(Writer writer) throws BindingException {

		try {
			return getXMLOutputFactory().createXMLEventWriter(writer);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public void serialize(CLASS data, Writer writer) throws BindingException {
		XMLEventWriter eventWriter = newXMLEventWriter(writer);
		XMLEventFactory2 eventFactory = getXmlEventFactory();
		try {
			eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
			writeXmlInternal(data, eventFactory, eventWriter);
			eventWriter.add(eventFactory.createEndDocument());
			eventWriter.close();
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected void writeXmlInternal(Object obj, XMLEventFactory2 eventFactory, XMLEventWriter eventWriter) throws BindingException {
		BindingContext bindingContext = getBindingContext();
		AssemblyClassBinding<CLASS> classBinding = getClassBinding();
		AssemblyXmlWriter<CLASS> writer = classBinding.getXmlWriter();
		XmlWritingContext writingContext = new DefaultXmlWritingContext(eventFactory, eventWriter, bindingContext);
		writer.writeXml(obj, null, writingContext);
	}

	
}
