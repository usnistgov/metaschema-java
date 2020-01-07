package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.ClassBinding;
import gov.nist.secauto.metaschema.datatype.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.AbstractParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.util.Util;

public abstract class AbstractXmlParsePlan<CLASS> extends AbstractParsePlan<XmlParsingContext, XMLEventReader2, CLASS> implements XmlParsePlan<CLASS>{
	private static final Logger logger = LogManager.getLogger(AbstractXmlParsePlan.class);

	private final Map<QName, XmlAttributePropertyParser> attributeParsers;

	public AbstractXmlParsePlan(ClassBinding<CLASS> classBinding, BindingContext bindingContext) throws BindingException {
		this(classBinding.getClazz(), newXmlAttributeParsers(classBinding, bindingContext));
	}

	public AbstractXmlParsePlan(Class<CLASS> clazz, Map<QName, XmlAttributePropertyParser> attributeParsers) throws BindingException {
		super(clazz);
		Objects.requireNonNull(attributeParsers, "attributeParsers");
		this.attributeParsers = attributeParsers;
	}

	protected Map<QName, XmlAttributePropertyParser> getAttributeParsers() {
		return attributeParsers;
	}

	protected static <CLASS> Map<QName, XmlAttributePropertyParser> newXmlAttributeParsers(ClassBinding<CLASS> classBinding, BindingContext bindingContext) throws BindingException {
		List<FlagPropertyBinding> bindings = classBinding.getFlagPropertyBindings();
		Map<QName, XmlAttributePropertyParser> retval;
		if (bindings.isEmpty()) {
			retval = Collections.emptyMap();
		} else {
			retval = new LinkedHashMap<>();
			for (FlagPropertyBinding binding : bindings) {
				XmlAttributePropertyParser propertyParser = binding.newXmlPropertyParser(bindingContext);
				// for an attribute, only a single QName should be handled
				QName handledQName = propertyParser.getHandledQName();
				retval.put(handledQName, propertyParser);
			}
			retval = Collections.unmodifiableMap(retval);
		}
		return retval;
	}

	/**
	 * This parse plan is expected to parse the content between the next START_ELEMENT
	 * and its corresponding END_ELEMENT, leaving the next (peeked) event at the
	 * position after the END_ELEMENT corresponding to the parsed START_ELEMENT.
	 */
	@Override
	public CLASS parse(XmlParsingContext parsingContext) throws BindingException {
		XMLEventReader2 reader = parsingContext.getEventReader();
		CLASS obj = newInstance();
		try {

			if (logger.isDebugEnabled()) {
				logger.debug("ParsePlan: {}", XmlEventUtil.toString(reader.peek()));
			}
			// consume the field or assembly START_ELEMENT
			XMLEvent event = reader.nextEvent();
			if (!event.isStartElement()) {
				throw new BindingException(
						String.format("START_ELEMENT expected, but found %s", XmlEventUtil.toString(event)));
			}

			StartElement start = event.asStartElement();
			QName name = start.getName();

			if (logger.isDebugEnabled()) {
				logger.debug("ParsePlan(start): {}", name.toString());
			}

			Set<QName> matchedAttributes = new HashSet<>();
			for (Attribute attribute : Util.toIterable(start.getAttributes())) {
				QName attributeName = attribute.getName();

//				parseAttribute(obj, parsingContext, attribute);
				XmlAttributePropertyParser propertyParser = getAttributeParsers().get(attributeName);
				if (propertyParser == null) {
					XmlProblemHandler handler = parsingContext.getProblemHandler();
					if (!handler.handleUnknownAttribute(attributeName, parsingContext)) {
						throw new BindingException(
								String.format("Unknown attribute '%s' for class '%s' at location: %s", attributeName,
										getClazz().getName(), XmlEventUtil.toString(event.getLocation())));
					}
				} else {
					propertyParser.parse(obj, parsingContext, attribute);
				}
				matchedAttributes.add(attributeName);
			}
			// TODO: handle unmatched attributes

			parseBody(obj, parsingContext, start);

			// Check for the END_ELEMENT of the assembly/field
			if (logger.isDebugEnabled()) {
				logger.debug("ParsePlan(check): {}", XmlEventUtil.toString(reader.peek()));
			}
			assert XmlEventUtil.isNextEventEndElement(reader, name) : XmlEventUtil.toString(reader.peek());

			// move past the END_ELEMENT of the assembly/field
			reader.nextEvent();
			XmlEventUtil.skipWhitespace(reader);
			if (logger.isDebugEnabled()) {
				logger.debug("ParsePlan(end): {}", XmlEventUtil.toString(reader.peek()));
			}
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}
		return obj;
	}

//	private void parseAttribute(CLASS obj, XmlParsingContext parsingContext, Attribute attribute) {
//		
//		JavaTypeAdapter<?> typeAdapter = parsingContext.getBindingContext().getJavaTypeAdapter((Class<?>)getPropertyBinding().getPropertyInfo().getItemType());
//		try {
//			Object value = typeAdapter.parseValue(attribue.getValue());
//			getPropertyBinding().getPropertyInfo().getPropertyAccessor().setValue(obj, value);
//		} catch (IllegalArgumentException | IllegalAccessException ex) {
//			throw new BindingException(ex);
//		} catch (UnsupportedOperationException ex) {
//			throw new BindingException(ex);
//		}
//	}

	/**
	 * Parse the contents of the field or assembly.
	 * 
	 * @param obj    the instance to parse into
	 * @param parsingContext the XML parser
	 * @param start  the START_ELEMENT of the field or assembly
	 * @throws BindingException if a parse error occurs
	 */
	protected abstract void parseBody(CLASS obj, XmlParsingContext parsingContext, StartElement start) throws BindingException;
}