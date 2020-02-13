/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.binding.io.xml.parser;

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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.util.Util;

public abstract class AbstractXmlParsePlan<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements XmlParsePlan<CLASS>{
	private static final Logger logger = LogManager.getLogger(AbstractXmlParsePlan.class);

	private final CLASS_BINDING classBinding;
	private final Map<QName, XmlAttributePropertyParser> attributeParsers;

	public AbstractXmlParsePlan(CLASS_BINDING classBinding, BindingContext bindingContext) {
		this(classBinding, newXmlAttributeParsers(classBinding, bindingContext));
	}

	public AbstractXmlParsePlan(CLASS_BINDING classBinding, Map<QName, XmlAttributePropertyParser> attributeParsers) {
		Objects.requireNonNull(classBinding, "classBinding");
		Objects.requireNonNull(attributeParsers, "attributeParsers");

		this.classBinding = classBinding;
		this.attributeParsers = attributeParsers;
	}

	protected CLASS_BINDING getClassBinding() {
		return classBinding;
	}

	protected Map<QName, XmlAttributePropertyParser> getAttributeParsers() {
		return attributeParsers;
	}

	protected static <CLASS> Map<QName, XmlAttributePropertyParser> newXmlAttributeParsers(ClassBinding<CLASS> classBinding, BindingContext bindingContext) {
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
		// TODO: handle start element in parent, but handle root here
		XMLEventReader2 reader = parsingContext.getEventReader();
		CLASS obj = getClassBinding().newInstance();
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
					if (!handler.handleUnknownAttribute(obj, attributeName, parsingContext)) {
						throw new BindingException(
								String.format("Unknown attribute '%s' for class '%s' at location: %s", attributeName,
										getClassBinding().getClazz().getName(), XmlEventUtil.toString(event.getLocation())));
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