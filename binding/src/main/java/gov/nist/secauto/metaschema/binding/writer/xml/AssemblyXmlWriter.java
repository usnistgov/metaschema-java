package gov.nist.secauto.metaschema.binding.writer.xml;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.codehaus.stax2.evt.XMLEventFactory2;

import gov.nist.secauto.metaschema.binding.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.property.PropertyInfo;
import gov.nist.secauto.metaschema.markup.MarkupMultiline;

public class AssemblyXmlWriter<CLASS> extends AbstractXmlWriter<CLASS, AssemblyClassBinding<CLASS>> {
	public AssemblyXmlWriter(AssemblyClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	public void writeXml(Object obj, QName name, XmlWritingContext writingContext) throws BindingException {
		if (name == null && getClassBinding().isRootElement()) {
			name = getClassBinding().getRootQName();
		}
		super.writeXml(obj, name, writingContext);
	}

	@Override
	protected void writeBody(Object obj, StartElement parent, XmlWritingContext writingContext) throws BindingException {
		for (ModelItemPropertyBinding propertyBinding : getClassBinding().getModelItemPropertyBindings()) {
			PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();

			XMLEventFactory2 factory = writingContext.getXMLEventFactory();
			XMLEventWriter writer = writingContext.getEventWriter();

			QName itemWrapperQName = null;
			// Need to emit the item wrapper
			if (!(propertyBinding instanceof FieldPropertyBinding)
					|| ((FieldPropertyBinding) propertyBinding).isWrappedInXml()
					|| !MarkupMultiline.class.isAssignableFrom((Class<?>) propertyBinding.getPropertyInfo().getItemType())) {
				itemWrapperQName = new QName(propertyBinding.getNamespace(), propertyBinding.getLocalName());
			}

			Object value = propertyInfo.getValue(obj);

			if (value != null) {
				JavaTypeAdapter<?> typeAdapter = writingContext.getBindingContext()
						.getJavaTypeAdapter(propertyBinding.getPropertyInfo().getItemType());

				try {
					StartElement propertyParent = parent;
					Iterable<? extends Object> iterable; 
					QName groupWrapperQName = null;
					if (propertyInfo instanceof CollectionPropertyInfo) {
						CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo) propertyInfo;
		
						if (XmlGroupAsBehavior.GROUPED.equals(collectionPropertyInfo.getXmlGroupAsBehavior())) {
							String localName = collectionPropertyInfo.getGroupLocalName();
							String namespace = collectionPropertyInfo.getGroupNamespace();
							groupWrapperQName = new QName(namespace, localName);
	
							propertyParent = factory.createStartElement(groupWrapperQName, null, null);
							writer.add(propertyParent);
						}

						if (collectionPropertyInfo.isMap()) {
							@SuppressWarnings("unchecked")
							Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
							iterable = map.values();
						} else if (collectionPropertyInfo.isList()) {
							@SuppressWarnings("unchecked")
							List<? extends Object> list = (List<? extends Object>)value;
							iterable = list;
						} else {
							throw new BindingException("Unknown collection type: "+value.getClass());
						}
					} else {
						iterable = Collections.singleton(value);
					}
	
					for (Object child : iterable) {
						typeAdapter.writeXmlElement(child, itemWrapperQName, propertyParent, writingContext);
					}
	
					if (groupWrapperQName != null) {
						writer.add(factory.createEndElement(groupWrapperQName, null));
					}
				} catch (XMLStreamException ex) {
					throw new BindingException(ex);
				}
			}
		}
	}
}
