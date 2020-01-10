package gov.nist.secauto.metaschema.binding.parser.xml;

import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class FieldXmlParsePlan<CLASS> extends AbstractXmlParsePlan<CLASS> {
	private static final Logger logger = LogManager.getLogger(FieldXmlParsePlan.class);

	private final FieldValueXmlPropertyParser fieldValueParser;

	public FieldXmlParsePlan(FieldClassBinding<CLASS> classBinding, BindingContext bindingContext) throws BindingException {
		this(classBinding.getClazz(), newXmlAttributeParsers(classBinding, bindingContext), classBinding.getFieldValuePropertyBinding().newXmlPropertyParser(bindingContext));
	}

	public FieldXmlParsePlan(Class<CLASS> clazz, Map<QName, XmlAttributePropertyParser> attributeParsers, FieldValueXmlPropertyParser fieldValueParser) throws BindingException {
		super(clazz, attributeParsers);
		Objects.requireNonNull(fieldValueParser, "fieldValueParser");
		this.fieldValueParser = fieldValueParser;
	}

	protected FieldValueXmlPropertyParser getFieldValueParser() {
		return fieldValueParser;
	}

	/**
	 * This will be called on the next element after the field START_ELEMENT
	 * after any attributes have been parsed. The parser will continue until the end
	 * element for the field is reached.
	 */
	@Override
	protected void parseBody(CLASS obj, XmlParsingContext parsingContext, StartElement start) throws BindingException {
		XMLEventReader2 reader = parsingContext.getEventReader();
		try {
			XMLEvent nextEvent = XmlEventUtil.skipWhitespace(reader);
			if (logger.isDebugEnabled()) {
				logger.debug("Field Body: {}", XmlEventUtil.toString(nextEvent));
			}

			FieldValueXmlPropertyParser fieldValueParser = getFieldValueParser();
			fieldValueParser.parse(obj, parsingContext);

			nextEvent = reader.peek();

			// skip inter-element whitespace
			nextEvent = XmlEventUtil.skipWhitespace(reader);

			if (logger.isDebugEnabled()) {
				logger.debug("Field Body(after): {}", XmlEventUtil.toString(nextEvent));
			}

			if (!nextEvent.isEndElement()) {
				// skip extra content
				// TODO: problem handler?
				nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
			}

			// the parser is now at the END_ELEMENT for this field
			assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(nextEvent);

			// the AbstractXmlParsePlan caller will advance past the END_ELEMENT for this field
			if (logger.isDebugEnabled()) {
				logger.debug("Field Body(end): {}", XmlEventUtil.toString(reader.peek()));
			}
		} catch (XMLStreamException ex) {
			throw new BindingException("Parse error", ex);
		}
	}
}
