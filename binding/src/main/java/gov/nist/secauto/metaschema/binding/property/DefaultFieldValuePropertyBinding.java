package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.annotations.JsonFieldName;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.binding.parser.xml.FieldValueXmlPropertyParser;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding implements FieldValuePropertyBinding {

	private final FieldValue fieldValueAnnotation;
	private final JsonFieldName jsonValue;

	public DefaultFieldValuePropertyBinding(BasicPropertyInfo propertyInfo, FieldValue fieldValueAnnotation, JsonFieldName jsonValue) {
		super(propertyInfo);
		this.fieldValueAnnotation = fieldValueAnnotation;
		this.jsonValue = jsonValue;
	}

	protected FieldValue getFieldValueAnnotation() {
		return fieldValueAnnotation;
	}

	protected JsonFieldName getJsonValue() {
		return jsonValue;
	}

	@Override
	public boolean hasJsonValueName() {
		return getJsonValue() != null;
	}

	@Override
	public String  getJsonValueName() {
		JsonFieldName value = getJsonValue();
		return value != null ? value.name() : null;
	}

	@Override
	public FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultFieldValuePropertyParser(this, bindingContext);
	}

}
