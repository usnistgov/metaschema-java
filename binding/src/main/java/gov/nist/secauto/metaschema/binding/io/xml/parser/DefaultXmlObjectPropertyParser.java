package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyCollector;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;

public class DefaultXmlObjectPropertyParser extends AbstractXmlPropertyParser<ModelItemPropertyBinding>
		implements XmlObjectPropertyParser {
	private static final Logger logger = LogManager.getLogger(DefaultXmlObjectPropertyParser.class);

	private final QName groupWrapperQName;
	private final QName itemWrapperQName;
	private JavaTypeAdapter<?> typeAdapter;

	public DefaultXmlObjectPropertyParser(ModelItemPropertyBinding propertyBinding, BindingContext bindingContext) {
		super(propertyBinding, bindingContext);
		groupWrapperQName = getGroupWrapperQName();
		itemWrapperQName = getItemWrapperQName();
	}

	@Override
	public <CLASS> void parse(CLASS obj, XmlParsingContext parsingContext) throws BindingException {
		PropertyCollector collector = getPropertyBinding().getPropertyInfo().newPropertyCollector();
		XMLEventReader2 reader = parsingContext.getEventReader();


		if (groupWrapperQName != null) {
			consumeStartElement(reader, groupWrapperQName);
		}


		JavaTypeAdapter<?> typeAdapter = getTypeAdapter();
		if (logger.isDebugEnabled()) {
			logger.debug("Using Adapter '{}'", typeAdapter.getClass().getName());
		}

		XMLEvent nextEvent;
		try {
			// continue while there is more work to do
			do {
				// skip inter-element whitespace
				nextEvent = XmlEventUtil.skipWhitespace(reader);

				/*
				 * In most cases, items will be wrapped. The only exception is an Assembly with
				 * a MarkupMultiline field with inXmlWrapped equal to {@code false}. In the
				 * latter case that object will be parsed in a single loop.
				 */
				if (itemWrapperQName != null && !typeAdapter.isParsingStartElement()) {
					consumeStartElement(reader, itemWrapperQName);
				}

				// Pass in the start element if it exists to allow attributes to be parsed
				Object value = typeAdapter.parse(parsingContext);
				if (value != null) {
					collector.add(value);
				} else {
					// TODO: should we do something here? Maybe a problem handler?
				}

				nextEvent = reader.peek();
				if (itemWrapperQName != null && !typeAdapter.isParsingStartElement()) {
					assert XmlEventUtil.isNextEventEndElement(reader, itemWrapperQName) : XmlEventUtil
							.toString(nextEvent);
					consumeEndElement(reader, itemWrapperQName);
				} else {
					// otherwise, the type adapter is expected to advance to the next child's
					// START_ELEMENT or the parent's END_ELEMENT. We need to trust that the type
					// adapter has not parsed too far.
				}
				nextEvent = XmlEventUtil.skipWhitespace(reader);

				// do not continue if the object is MultilineMarkup
			} while (itemWrapperQName != null && XmlEventUtil.isNextEventStartElement(reader, itemWrapperQName));
		} catch (XMLStreamException ex) {
			throw new BindingException(ex);
		}

		if (groupWrapperQName != null) {
			consumeEndElement(reader, groupWrapperQName);
		}

		collector.applyCollection(obj);
	}

	protected JavaTypeAdapter<?> getTypeAdapter() throws BindingException {
		synchronized (this) {
			if (typeAdapter == null) {
				Class<?> itemClass = getPropertyBinding().getPropertyInfo().getItemType();
				typeAdapter = getBindingContext()
						.getJavaTypeAdapter(itemClass);
			}

			return typeAdapter;
		}
	}

	@Override
	public boolean canConsume(QName nextQName) throws BindingException {
		return (groupWrapperQName != null && groupWrapperQName.equals(nextQName))
				|| (itemWrapperQName != null && itemWrapperQName.equals(nextQName))
				|| getTypeAdapter().canHandleQName(nextQName);
	}

	protected QName getGroupWrapperQName() {
		PropertyInfo propertyInfo = getPropertyBinding().getPropertyInfo();

		QName retval = null;
		if (propertyInfo instanceof CollectionPropertyInfo) {
			CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo)propertyInfo;

			if (XmlGroupAsBehavior.GROUPED.equals(collectionPropertyInfo.getXmlGroupAsBehavior())) {
				retval = collectionPropertyInfo.getGroupXmlQName();
			}
		}
		return retval;
	}

	protected QName getItemWrapperQName() {
		QName retval;
		if (!isChildWrappedInXml()) {
			// TODO: check for non singleton value?
			retval = null;
		} else {
			ModelItemPropertyBinding propertyBinding = getPropertyBinding();
			retval = propertyBinding.getXmlQName();
		}
		return retval;
	}

	@Override
	public boolean isChildWrappedInXml() {
		ModelItemPropertyBinding propertyBinding = getPropertyBinding();
		return !(propertyBinding instanceof FieldPropertyBinding)
				|| ((FieldPropertyBinding) propertyBinding).isWrappedInXml()
				|| !MarkupMultiline.class.isAssignableFrom((Class<?>) propertyBinding.getPropertyInfo().getItemType());
	}

//
//	@Override
//	public List<QName> getHandledQNames() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
