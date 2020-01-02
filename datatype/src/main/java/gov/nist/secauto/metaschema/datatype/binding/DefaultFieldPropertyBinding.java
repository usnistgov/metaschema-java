package gov.nist.secauto.metaschema.datatype.binding;

import java.util.Objects;

import gov.nist.secauto.metaschema.datatype.annotations.Field;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;

public class DefaultFieldPropertyBinding extends AbstractModelItemPropertyBinding implements FieldPropertyBinding {
	
	private final Field fieldAnnotation;

	public DefaultFieldPropertyBinding(CollectionPropertyInfo propertyInfo, Field fieldAnnotation) {
		super(propertyInfo);
		Objects.requireNonNull(fieldAnnotation,"fieldAnnotation");
		this.fieldAnnotation = fieldAnnotation;
	}

	@Override
	public boolean isRequired() {
		return getFieldAnnotation().required();
	}

	@Override
	public boolean isWrappedInXml() {
		return getFieldAnnotation().inXmlWrapped();
	}

	@Override
	public Field getFieldAnnotation() {
		return fieldAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(XmlParser parser) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, parser);
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getFieldAnnotation().name(), getPropertyInfo().getPropertyAccessor());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getFieldAnnotation().namespace(), getPropertyInfo().getPropertyAccessor());
	}

}
