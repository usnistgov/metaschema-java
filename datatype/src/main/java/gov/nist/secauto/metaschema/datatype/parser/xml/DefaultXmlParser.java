package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;

import gov.nist.secauto.metaschema.datatype.binding.adapter.DataTypes;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.binding.adapter.ObjectJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.ParsePlan;

public class DefaultXmlParser implements XmlParser {
	private static final Logger logger = LogManager.getLogger(XmlParser.class);

	private final XMLInputFactory2 factory;
	private final Map<Class<?>, ParsePlan<XMLEventReader2, ?>> classParsePlans = new HashMap<>();
	private XmlProblemHandler xmlProblemHandler = new XmlProblemHandler();
	private Map<Type, JavaTypeAdapter<?>> xmlJavaTypeAdapters = new HashMap<>();

	protected DefaultXmlParser(XMLInputFactory2 factory) {
		this.factory = factory;
		factory.configureForXmlConformance();
		factory.setProperty(XMLInputFactory2.IS_COALESCING, true);
//		factory.configureForSpeed();
		for (DataTypes dts : DataTypes.values()) {
			JavaTypeAdapter<?> adapter = dts.getJavaTypeAdapter();
			if (adapter != null) {
				xmlJavaTypeAdapters.put(dts.getJavaClass(), adapter);
			}
		}
	}

	protected XMLInputFactory2 getFactory() {
		return factory;
	}

	@Override
	public <T> T parse(Reader reader, Class<T> clazz) throws BindingException {
		return parse(newReader(reader), clazz);
	}

	protected XMLEventReader2 newReader(Reader reader) throws BindingException {

		try {
			XMLEventReader eventReader = getFactory().createXMLEventReader(reader);
			EventFilter filter = new CommentFilter();
			return (XMLEventReader2) getFactory().createFilteredReader(eventReader, filter);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected <CLASS> CLASS parse(XMLEventReader2 reader, Class<CLASS> clazz) throws BindingException {
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
			ParsePlan<XMLEventReader2, CLASS> plan = getParsePlan(clazz);
			retval = (CLASS) plan.parse(reader);
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

	protected <CLASS> ParsePlan<XMLEventReader2, CLASS> getParsePlan(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			ParsePlan<XMLEventReader2, CLASS> retval = (ParsePlan<XMLEventReader2, CLASS>) classParsePlans.get(clazz);
			if (retval == null) {
//				if (clazz.isAnnotationPresent(Collapsible.class)) {
//					// TODO: handle collapsible
//					throw new UnsupportedOperationException();
//				} else {
					XmlParsePlanBuilder<CLASS> classPlanBuilder = new XmlParsePlanBuilder<CLASS>(clazz);
					retval = classPlanBuilder.build(this);
//				}
				classParsePlans.put(clazz, retval);
			}
			return retval;
		}
	}

	@Override
	public XmlProblemHandler getProblemHandler() {
		return xmlProblemHandler;
	}

	@Override
	public <CLASS> JavaTypeAdapter<CLASS> getXmlTypeAdapter(Class<CLASS> itemClass) throws BindingException {
		synchronized (this) {
			// First try to find a simple data binding
			@SuppressWarnings("unchecked")
			JavaTypeAdapter<CLASS> retval = (JavaTypeAdapter<CLASS>)xmlJavaTypeAdapters.get(itemClass);
			if (retval == null) {
				// no simple binding exists, try to bind to the object
				ParsePlan<XMLEventReader2, CLASS> parsePlan = getParsePlan(itemClass);
				retval = new ObjectJavaTypeAdapter<CLASS>(itemClass, parsePlan);
				xmlJavaTypeAdapters.put(itemClass, retval);
			}
			return retval;
		}
	}
}
