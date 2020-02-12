package gov.nist.secauto.metaschema.binding.model.property;

import java.util.Objects;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.binding.io.xml.parser.FieldValueXmlPropertyParser;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding implements FieldValuePropertyBinding {

	private final FieldClassBinding<?> classBinding;
	private final FieldValue fieldValueAnnotation;
	private final JsonFieldValueName jsonFieldValueName;

	public DefaultFieldValuePropertyBinding(FieldClassBinding<?> classBinding, BasicPropertyInfo propertyInfo, FieldValue fieldValueAnnotation, JsonFieldValueName jsonFieldValueName) {
		super(propertyInfo);
		Objects.requireNonNull(classBinding, "classBinding");
		Objects.requireNonNull(fieldValueAnnotation, "fieldValueAnnotation");
		this.classBinding = classBinding;
		this.fieldValueAnnotation = fieldValueAnnotation;
		this.jsonFieldValueName = jsonFieldValueName;
	}

	protected FieldClassBinding<?> getClassBinding() {
		return classBinding;
	}

	@Override
	public PropertyBindingType getPropertyBindingType() {
		return PropertyBindingType.FIELD_VALUE;
	}

	protected FieldValue getFieldValueAnnotation() {
		return fieldValueAnnotation;
	}

	protected JsonFieldValueName getJsonFieldValueName() {
		return jsonFieldValueName;
	}

	@Override
	public FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultFieldValuePropertyParser(this, bindingContext);
	}

	@Override
	public QName getXmlQName() {
		// always null
		return null;
	}

	@Override
	public String getJsonFieldName(BindingContext bindingContext) throws BindingException {
		String retval;
		if (getClassBinding().getJsonValueKeyFlagPropertyBinding() != null) {
			retval = null;
		} else if (getJsonFieldValueName() != null) {
			retval = getJsonFieldValueName().name();
		} else {
			// use the default from the java type binding
			retval = bindingContext.getJavaTypeAdapter(getPropertyInfo().getItemType()).getDefaultJsonFieldName();
		}
		return retval;
	}

}
