package gov.nist.secauto.metaschema.datatype.binding;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.datatype.annotations.Assembly;
import gov.nist.secauto.metaschema.datatype.annotations.FieldValue;
import gov.nist.secauto.metaschema.datatype.binding.adapter.DataTypes;
import gov.nist.secauto.metaschema.datatype.binding.adapter.JavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.binding.adapter.ObjectJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.CommentFilter;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultXmlParsingContext;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlEventUtil;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.datatype.writer.xml.DefaultXmlWritingContext;
import gov.nist.secauto.metaschema.datatype.writer.xml.XmlWriter;
import gov.nist.secauto.metaschema.datatype.writer.xml.XmlWritingContext;

public class DefaultBindingContext implements BindingContext {
	private static final Logger logger = LogManager.getLogger(DefaultBindingContext.class);

	private XMLInputFactory2 xmlInputFactory;
	private XMLOutputFactory2 xmlOutputFactory;
	private XMLEventFactory2 xmlEventFactory;

	private final Map<Class<?>, ClassBinding<?>> classBindingsByClass = new HashMap<>();
	private final Map<Class<?>, XmlParsePlan<?>> xmlParsePlansByClass = new HashMap<>();
	private final Map<Class<?>, XmlWriter<?>> xmlWriterByClass = new HashMap<>();
	private final Map<Type, JavaTypeAdapter<?>> xmlJavaTypeAdapters = new HashMap<>();

	public DefaultBindingContext() {
		xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
		xmlInputFactory.configureForXmlConformance();
		xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, true);
//		xmlInputFactory.configureForSpeed();

		xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
		xmlOutputFactory.configureForSpeed();

		// register all known types
		for (DataTypes dts : DataTypes.values()) {
			JavaTypeAdapter<?> adapter = dts.getJavaTypeAdapter();
			if (adapter != null) {
				xmlJavaTypeAdapters.put(dts.getJavaClass(), adapter);
			}
		}
	}

	protected XMLInputFactory2 getXMLInputFactory() {
		return xmlInputFactory;
	}

	protected void setXMLInputFactory(XMLInputFactory2 factory) {
		this.xmlInputFactory = factory;
	}

	protected XMLOutputFactory2 getXMLOutputFactory() {
		return xmlOutputFactory;
	}

	protected void setXMLOutputFactory(XMLOutputFactory2 xmlOutputFactory) {
		this.xmlOutputFactory = xmlOutputFactory;
	}

	protected XMLEventFactory2 getXmlEventFactory() {
		return xmlEventFactory;
	}

	protected void setXmlEventFactory(XMLEventFactory2 xmlEventFactory) {
		this.xmlEventFactory = xmlEventFactory;
	}

	public <CLASS> JavaTypeAdapter<?> registerJavaTypeAdapter(Class<CLASS> clazz, JavaTypeAdapter<CLASS> adapter) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(adapter, "adapter");

		synchronized (adapter) {
			return xmlJavaTypeAdapters.put(clazz, adapter);
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
	public <CLASS> CLASS parseXml(Reader reader, Class<CLASS> clazz) throws BindingException {
		XMLEventReader2 eventReader = newXMLEventReader2(reader);
		XmlParsingContext parsingContext = new DefaultXmlParsingContext(eventReader, this);
		return parseXmlInternal(parsingContext, clazz);
	}

	protected <CLASS> CLASS parseXmlInternal(XmlParsingContext parsingContext, Class<CLASS> clazz) throws BindingException {
		XMLEventReader2 reader = parsingContext.getEventReader();

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
			XmlParsePlan<CLASS> plan = getXmlParsePlan(clazz);
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

	protected XMLEventWriter newXMLEventWriter(Writer writer) throws BindingException {

		try {
			return getXMLOutputFactory().createXMLEventWriter(writer);
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public <CLASS> void writeXml(Writer writer, CLASS obj) throws BindingException {
		XMLEventWriter eventWriter = newXMLEventWriter(writer);
		XMLEventFactory2 eventFactory = getXmlEventFactory();
		XmlWritingContext writingContext = new DefaultXmlWritingContext(eventFactory, eventWriter, this);
		writeXmlInternal(obj, writingContext);
	}

	protected <CLASS> void writeXmlInternal(CLASS obj, XmlWritingContext writingContext) throws BindingException {
		@SuppressWarnings("unchecked")
		Class<CLASS> clazz = (Class<CLASS>) obj.getClass();
		XmlWriter<CLASS> writer = getXmlWriter(clazz);
		writer.writeXml(obj, null, writingContext);
	}

	protected <CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			ClassBinding<CLASS> retval = (ClassBinding<CLASS>) classBindingsByClass.get(clazz);
			if (retval == null) {
				boolean hasFieldValue = false;
				boolean hasModelProperty = false;
				for (Field javaField : clazz.getDeclaredFields()) {
					if (javaField.isAnnotationPresent(FieldValue.class)) {
						hasFieldValue = true;
					} else if (javaField
							.isAnnotationPresent(gov.nist.secauto.metaschema.datatype.annotations.Field.class)
							|| javaField.isAnnotationPresent(Assembly.class)) {
						hasModelProperty = true;
					}
				}

				if (hasFieldValue && hasModelProperty) {
					throw new BindingException(String.format(
							"Class '%s' contains a FieldValue annotation and Field and/or Assembly annotations. FieldValue can only be used with Flag annotations.",
							clazz.getName()));
				}

				if (hasFieldValue) {
					retval = new FieldClassBinding<CLASS>(clazz);
				} else {
					retval = new AssemblyClassBinding<CLASS>(clazz);
				}
				classBindingsByClass.put(clazz, retval);
			}
			return retval;
		}
	}

	@Override
	public <CLASS> XmlParsePlan<CLASS> getXmlParsePlan(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			XmlParsePlan<CLASS> retval = (XmlParsePlan<CLASS>) xmlParsePlansByClass.get(clazz);
			if (retval == null) {

				ClassBinding<CLASS> classBinding = getClassBinding(clazz);
				retval = classBinding.newXmlParsePlan(this);
				xmlParsePlansByClass.put(clazz, retval);
			}
			return retval;
		}
	}

	public <CLASS> XmlWriter<CLASS> getXmlWriter(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			XmlWriter<CLASS> retval = (XmlWriter<CLASS>) xmlWriterByClass.get(clazz);
			if (retval == null) {

				ClassBinding<CLASS> classBinding = getClassBinding(clazz);
				retval = classBinding.newXmlWriter(this);
				xmlWriterByClass.put(clazz, retval);
			}
			return retval;
		}
	}

	@Override
	public <CLASS> JavaTypeAdapter<CLASS> getJavaTypeAdapter(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			// First try to find a simple data binding
			@SuppressWarnings("unchecked")
			JavaTypeAdapter<CLASS> retval = (JavaTypeAdapter<CLASS>) xmlJavaTypeAdapters.get(clazz);
			if (retval == null) {
				// no simple binding exists, try to bind to the object

				// TODO: handle binding exception, which may be caused if the class cannot be
				// bound for any reason
				ClassBinding<CLASS> classBinding = getClassBinding(clazz);
				retval = new ObjectJavaTypeAdapter<CLASS>(classBinding, this);
				xmlJavaTypeAdapters.put(clazz, retval);
			}
			return retval;
		}
	}


}
