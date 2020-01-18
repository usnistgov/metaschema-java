package gov.nist.secauto.metaschema.binding;

import java.io.IOException;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.nist.secauto.metaschema.binding.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.annotations.Flag;
import gov.nist.secauto.metaschema.binding.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.datatypes.adapter.DataTypes;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.CommentFilter;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultXmlParsingContext;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlEventUtil;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.writer.json.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.writer.json.DefaultJsonWritingContext;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWriter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.DefaultXmlWritingContext;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWriter;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWritingContext;

public class DefaultBindingContext implements BindingContext {
	private static final Logger logger = LogManager.getLogger(DefaultBindingContext.class);

	private XMLInputFactory2 xmlInputFactory;
	private XMLOutputFactory2 xmlOutputFactory;
	private XMLEventFactory2 xmlEventFactory;
	private JsonFactory jsonFactory;

	private final Map<Class<?>, ClassBinding<?>> classBindingsByClass = new HashMap<>();
//	private final Map<Class<?>, XmlParsePlan<?>> xmlParsePlansByClass = new HashMap<>();
//	private final Map<Class<?>, XmlWriter> xmlWriterByClass = new HashMap<>();
//	private final Map<Class<?>, JsonWriter> jsonWriterByClass = new HashMap<>();
	private final Map<Type, JavaTypeAdapter<?>> xmlJavaTypeAdapters = new HashMap<>();

	public DefaultBindingContext() {
		xmlInputFactory = (XMLInputFactory2) WstxInputFactory.newInstance();
		xmlInputFactory.configureForXmlConformance();
		xmlInputFactory.setProperty(XMLInputFactory2.IS_COALESCING, false);
//		xmlInputFactory.configureForSpeed();

		xmlOutputFactory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
		xmlOutputFactory.configureForSpeed();
		xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		xmlEventFactory = (XMLEventFactory2) WstxEventFactory.newInstance();

		jsonFactory = new JsonFactory();
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

	protected JsonFactory getJsonFactory() {
		return jsonFactory;
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
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

	private JsonGenerator newJsonGenerator(Writer writer) throws BindingException {
		try {
			JsonFactory factory = getJsonFactory();
			JsonGenerator retval = factory.createGenerator(writer);
			retval.setPrettyPrinter(new DefaultPrettyPrinter());
			 return retval;
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	protected <CLASS> ClassBinding<CLASS> getBoundClassBinding(Class<CLASS> clazz) throws BindingException {
		ClassBinding<CLASS> classBinding = getClassBinding(clazz);
		if (classBinding == null) {
			throw new BindingException(String.format("Class '%s' is not a bound Java class.", clazz.getName()));
		}
		return classBinding;
	}
	@Override
	public <CLASS> CLASS parseXml(Reader reader, Class<CLASS> clazz) throws BindingException {
		XMLEventReader2 eventReader = newXMLEventReader2(reader);
		XmlParsingContext parsingContext = new DefaultXmlParsingContext(eventReader, this);
		return parseXmlInternal(parsingContext, clazz);
	}

	protected <CLASS> CLASS parseXmlInternal(XmlParsingContext parsingContext, Class<CLASS> clazz)
			throws BindingException {
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
			ClassBinding<CLASS> classBinding = getBoundClassBinding(clazz);
			XmlParsePlan<CLASS> plan = classBinding.getXmlParsePlan(this);
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
	public void writeXml(Writer writer, Object data) throws BindingException {
		XMLEventWriter eventWriter = newXMLEventWriter(writer);
		XMLEventFactory2 eventFactory = getXmlEventFactory();
		XmlWritingContext writingContext = new DefaultXmlWritingContext(eventFactory, eventWriter, this);
		try {
			eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
			writeXmlInternal(data, writingContext);
			eventWriter.add(eventFactory.createEndDocument());
			eventWriter.close();
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
	}

	protected void writeXmlInternal(Object obj, XmlWritingContext writingContext) throws BindingException {
		Class<?> clazz = obj.getClass();
		ClassBinding<?> classBinding = getBoundClassBinding(clazz);
		XmlWriter writer = classBinding.getXmlWriter();
		writer.writeXml(obj, null, writingContext);
	}

	@Override
	public void writeJson(Writer writer, Object data) throws BindingException {
		JsonGenerator eventWriter = newJsonGenerator(writer);
		JsonWritingContext writingContext = new DefaultJsonWritingContext(eventWriter, this);
		try {
			eventWriter.writeStartObject();
			writeJsonInternal(data, writingContext);
			eventWriter.writeEndObject();
			eventWriter.close();
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	protected void writeJsonInternal(Object obj, JsonWritingContext writingContext) throws BindingException {
		Class<?> clazz = obj.getClass();
		ClassBinding<?> classBinding = getBoundClassBinding(clazz);
		JsonWriter writer = classBinding.getAssemblyJsonWriter(this);

		if (writer instanceof AssemblyJsonWriter) {
			AssemblyClassBinding<?> assemblyClassBinding = (AssemblyClassBinding<?>)classBinding;
			if (assemblyClassBinding.isRootElement()) {
				// write root property
				String name = assemblyClassBinding.getRootQName().getLocalPart();
				try {
					writingContext.getEventWriter().writeFieldName(name);
				} catch (IOException ex) {
					throw new BindingException(ex);
				}
			} else {
				throw new UnsupportedOperationException(String.format(
						"The target assembly class '%s' is not a root. Please use a class with the '%s' annotation.",
						classBinding.getClazz().getName(), RootWrapper.class.getName()));
			}
		}

		writer.writeJson(obj, null, writingContext);
	}

	@Override
	public boolean hasClassBinding(Class<?> clazz) throws BindingException {
		return getClassBinding(clazz) != null;
	}

	@Override
	public <CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException {
		synchronized (this) {
			@SuppressWarnings("unchecked")
			ClassBinding<CLASS> retval = (ClassBinding<CLASS>) classBindingsByClass.get(clazz);
			if (retval == null) {
				boolean hasFlag = false;
				boolean hasFieldValue = false;
				boolean hasModelProperty = false;
				for (Field javaField : clazz.getDeclaredFields()) {
					if (javaField.isAnnotationPresent(FieldValue.class)) {
						hasFieldValue = true;
					} else if (javaField.isAnnotationPresent(Flag.class)) {
						hasFlag = true;
					} else if (javaField.isAnnotationPresent(gov.nist.secauto.metaschema.binding.annotations.Field.class)
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
				} else if (hasFlag || hasModelProperty) {
					retval = new AssemblyClassBinding<CLASS>(clazz);
				} else {
					retval = null;
				}

				if (retval != null) {
					classBindingsByClass.put(clazz, retval);
				}
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
				retval = new ObjectJavaTypeAdapter<CLASS>(classBinding);
				xmlJavaTypeAdapters.put(clazz, retval);
			}
			return retval;
		}
	}

}
