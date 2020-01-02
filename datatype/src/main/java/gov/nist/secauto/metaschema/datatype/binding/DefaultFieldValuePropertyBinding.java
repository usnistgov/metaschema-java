package gov.nist.secauto.metaschema.datatype.binding;

import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.annotations.FieldValue;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.FieldValueXmlPropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding<BasicPropertyInfo<Type>> implements FieldValuePropertyBinding {

	private final FieldValue fieldValueAnnotation;

	public DefaultFieldValuePropertyBinding(BasicPropertyInfo<Type> propertyInfo, FieldValue fieldValueAnnotation) {
		super(propertyInfo);
		this.fieldValueAnnotation = fieldValueAnnotation;
	}

	protected FieldValue getFieldValueAnnotation() {
		return fieldValueAnnotation;
	}

	@Override
	public FieldValueXmlPropertyParser newXmlPropertyParser(XmlParser parser) throws BindingException {
		return new DefaultFieldValuePropertyParser(this, parser);
	}

}
