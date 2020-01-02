package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.util.HashSet;
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

import gov.nist.secauto.metaschema.datatype.parser.AbstractParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.util.Util;

public abstract class AbstractXmlParsePlan<CLASS> extends AbstractParsePlan<XMLEventReader2, CLASS> {
	private static final Logger logger = LogManager.getLogger(AbstractXmlParsePlan.class);

	private final XmlParser parser;
	private final Map<QName, XmlAttributePropertyParser> attributeParsers;

	public AbstractXmlParsePlan(XmlParser parser, Class<CLASS> clazz,
			Map<QName, XmlAttributePropertyParser> attributeParsers) {
		super(clazz);
		Objects.requireNonNull(attributeParsers, "attributeParsers");
		this.parser = parser;
		this.attributeParsers = attributeParsers;
	}

	public XmlParser getXmlParser() {
		return parser;
	}

	protected Map<QName, XmlAttributePropertyParser> getAttributeParsers() {
		return attributeParsers;
	}

	/**
	 * This parse plan is expected to parse the content between the next START_ELEMENT
	 * and its corresponding END_ELEMENT, leaving the next (peeked) event at the
	 * position after the END_ELEMENT corresponding to the parsed START_ELEMENT.
	 */
	@Override
	public CLASS parse(XMLEventReader2 reader) throws BindingException {

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

				XmlAttributePropertyParser propertyParser = getAttributeParsers().get(attributeName);
				if (propertyParser == null) {
					XmlProblemHandler handler = getXmlParser().getProblemHandler();
					if (!handler.handleUnknownAttribute(attributeName, getXmlParser(), reader)) {
						throw new BindingException(
								String.format("Unknown attribute '%s' for class '%s' at location: %s", attributeName,
										getClazz().getName(), XmlEventUtil.toString(event.getLocation())));
					}
				} else {
					propertyParser.parse(obj, attribute);
				}
				matchedAttributes.add(attributeName);
			}
			// TODO: handle unmatched attributes

			parseBody(obj, reader, start);

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

	/**
	 * Parse the contents of the field or assembly.
	 * 
	 * @param obj    the instance to parse into
	 * @param reader the parser
	 * @param start  the START_ELEMENT of the field or assembly
	 * @throws BindingException if a parse error occurs
	 */
	protected abstract void parseBody(CLASS obj, XMLEventReader2 reader, StartElement start) throws BindingException;
}