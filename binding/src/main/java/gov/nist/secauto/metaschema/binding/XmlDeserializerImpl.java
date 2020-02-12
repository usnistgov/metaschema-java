package gov.nist.secauto.metaschema.binding;

import java.io.Reader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import com.ctc.wstx.stax.WstxInputFactory;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.xml.parser.CommentFilter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

class XmlDeserializerImpl<CLASS> extends AbstractDeserializer<CLASS> {
	private static final Logger logger = LogManager.getLogger(XmlDeserializerImpl.class);

	private XMLInputFactory2 xmlInputFactory;

	public XmlDeserializerImpl(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
			Configuration configuration) {
		super(bindingContext, classBinding, configuration);
	}

	protected XMLInputFactory2 getXMLInputFactory() {
		synchronized (this) {
			if (xmlInputFactory == null) {
				xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
				xmlInputFactory.configureForXmlConformance();
				xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);
//				xmlInputFactory.configureForSpeed();
			}
			return xmlInputFactory;
		}
	}

	protected void setXMLInputFactory(XMLInputFactory2 factory) {
		synchronized (this) {
			this.xmlInputFactory = factory;
		}
	}

	protected XMLEventReader2 newXMLEventReader2(Reader reader) throws BindingException {

		try {
			XMLEventReader eventReader = getXMLInputFactory().createXMLEventReader(reader);
			EventFilter filter = new CommentFilter();
			return (XMLEventReader2) getXMLInputFactory().createFilteredReader(eventReader, filter);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public CLASS deserialize(Reader reader) throws BindingException {
		XMLEventReader2 eventReader = newXMLEventReader2(reader);
		return parseXmlInternal(eventReader);
	}

	protected CLASS parseXmlInternal(XMLEventReader2 reader)
			throws BindingException {
		
		BindingContext bindingContext = getBindingContext();

		CLASS retval;
		// we may be at the START_DOCUMENT
		try {
			if (reader.peek().isStartDocument()) {
				while (reader.hasNextEvent() && !reader.peek().isStartElement()) {
					// advance to the START_ELEMENT
					// TODO: remove
					logger.debug("Skip: {}", XmlEventUtil.toString(reader.nextEvent()));
				}
			}
			ClassBinding<CLASS> classBinding = getClassBinding();
			XmlParsePlan<CLASS> plan = classBinding.getXmlParsePlan(bindingContext);
			XmlParsingContext parsingContext = new DefaultXmlParsingContext(reader, bindingContext);
			retval = (CLASS) plan.parse(parsingContext);
			if (reader.hasNext()) {
				logger.debug("After Parse: {}", XmlEventUtil.toString(reader.peek()));

				assert XmlEventUtil.isNextEventEndDocument(reader) : XmlEventUtil.toString(reader.peek());
//				XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_DOCUMENT);
			}
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
		return retval;
	}
}
