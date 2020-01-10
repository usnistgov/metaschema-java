package gov.nist.secauto.metaschema.binding.parser.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import gov.nist.secauto.metaschema.binding.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;

public class AssemblyXmlParsePlan<CLASS> extends AbstractXmlParsePlan<CLASS> {
	private static final Logger logger = LogManager.getLogger(AssemblyXmlParsePlan.class);

	private final List<XmlObjectPropertyParser> modelParsers;

	public AssemblyXmlParsePlan(AssemblyClassBinding<CLASS> classBinding, BindingContext bindingContext) throws BindingException {
		this(classBinding.getClazz(), newXmlAttributeParsers(classBinding, bindingContext), newModelParsers(classBinding, bindingContext));
	}

	public AssemblyXmlParsePlan(Class<CLASS> clazz, Map<QName, XmlAttributePropertyParser> attributeParsers, List<XmlObjectPropertyParser> modelParsers) throws BindingException {
		super(clazz, attributeParsers);
		Objects.requireNonNull(modelParsers, "modelParsers");
		this.modelParsers = modelParsers;
	}

	protected List<XmlObjectPropertyParser> getModelParsers() {
		return modelParsers;
	}

	protected static <CLASS> List<XmlObjectPropertyParser> newModelParsers(AssemblyClassBinding<CLASS> classBinding, BindingContext bindingContext) throws BindingException {
		List<ModelItemPropertyBinding> bindings = classBinding.getModelItemPropertyBindings();
		List<XmlObjectPropertyParser> retval;
		if (bindings.isEmpty()) {
			retval = Collections.emptyList();
		} else {
			retval = new ArrayList<>(bindings.size());
			for (ModelItemPropertyBinding binding : bindings) {
				retval.add(binding.newXmlPropertyParser(bindingContext));
			}
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	/**
	 * This will be called on the next element after the assembly START_ELEMENT
	 * after any attributes have been parsed. The parser will continue until the end
	 * element for the assembly is reached.
	 */
	@Override
	protected void parseBody(CLASS obj, XmlParsingContext parsingContext, StartElement start) throws BindingException {
		XMLEventReader2 reader = parsingContext.getEventReader();
		try {
			XMLEvent nextEvent;
			for (XmlObjectPropertyParser modelParser : getModelParsers()) {

				nextEvent = XmlEventUtil.skipWhitespace(reader);

				if (logger.isDebugEnabled()) {
					logger.debug("Assembly Body: {}", XmlEventUtil.toString(nextEvent));
				}

				if (nextEvent.isEndElement()) {
					// TODO: handle unparsed elements
					break;
				}

				StartElement nextStart = nextEvent.asStartElement();
				QName nextName = nextStart.getName();

				if (modelParser.canConsume(nextName)) {
					// the parser will consume the START_ELEMENT event
					modelParser.parse(obj, parsingContext);
					nextEvent = reader.peek();
//				} else {
//					logger.debug("Assembly Body Element(skipping): {}", nextName.toString());
//					nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
				}

				// skip inter-element whitespace
				nextEvent = XmlEventUtil.skipWhitespace(reader);

				// the parser should be now at the next child START_ELEMENT or the assembly's END_ELEMENT
				assert nextEvent.isStartElement() || XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil
						.toString(nextEvent);

				if (logger.isDebugEnabled()) {
					logger.debug("Assembly Body Element(after parse): {}", XmlEventUtil.toString(nextEvent));
				}
//
//				// Advance only if the child is wrapped
//				if (modelParser.isChildWrappedInXml()) {
//					nextEvent = reader.nextEvent();
//				}
			}

			nextEvent = reader.peek();
			if (!nextEvent.isEndElement()) {
				nextEvent = XmlEventUtil.advanceTo(reader, XMLStreamConstants.END_ELEMENT);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Assembly Body(check): {}", XmlEventUtil.toString(nextEvent));
			}
			// the parser is now at the END_ELEMENT for this assembly
			assert XmlEventUtil.isNextEventEndElement(reader, start.getName()) : XmlEventUtil.toString(nextEvent);

			// the AbstractXmlParsePlan caller will advance past the END_ELEMENT for this assembly
			if (logger.isDebugEnabled()) {
				logger.debug("Assembly Body(end): {}", XmlEventUtil.toString(reader.peek()));
			}
		} catch (XMLStreamException ex) {
			throw new BindingException("Parse error", ex);
		}
	}
}
