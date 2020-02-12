package gov.nist.secauto.metaschema.binding.model;

import java.util.Collections;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.FieldJsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.FieldXmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.FieldXmlWriter;
import gov.nist.secauto.metaschema.binding.model.annotations.Collapsible;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

class FieldClassBindingImpl<CLASS> extends AbstractClassBinding<CLASS, FieldXmlParsePlan<CLASS>, FieldXmlWriter<CLASS>>
		implements FieldClassBinding<CLASS> {
	private final FieldValuePropertyBinding fieldValuePropertyBinding;
	private final FlagPropertyBinding jsonValueKeyFlagPropertyBinding;

	public FieldClassBindingImpl(Class<CLASS> clazz) throws BindingException {
		super(clazz);

		FlagPropertyBinding jsonValueKeyFlag = null;
		for (FlagPropertyBinding flag : getFlagPropertyBindings()) {
			if (flag.isJsonValueKey()) {
				jsonValueKeyFlag = flag;
				break;
			}
		}
		this.jsonValueKeyFlagPropertyBinding = jsonValueKeyFlag;
		this.fieldValuePropertyBinding = ClassIntrospector.getFieldValueBinding(this, clazz);
	}

	@Override
	public Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext,
			PropertyBindingFilter filter) throws BindingException {
		Map<String, PropertyBinding> retval = super.getJsonPropertyBindings(bindingContext, filter);

		FieldValuePropertyBinding fieldValuePropertyBinding = getFieldValuePropertyBinding();
		retval.put(fieldValuePropertyBinding.getJsonFieldName(bindingContext), fieldValuePropertyBinding);
		return Collections.unmodifiableMap(retval);
	}

	@Override
	public FieldValuePropertyBinding getFieldValuePropertyBinding() {
		return fieldValuePropertyBinding;
	}

	@Override
	public FlagPropertyBinding getJsonValueKeyFlagPropertyBinding() {
		return jsonValueKeyFlagPropertyBinding;
	}

	@Override
	public boolean isCollapsible() {
		return getClazz().isAnnotationPresent(Collapsible.class);
	}

	@Override
	public FieldXmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
		return new FieldXmlParsePlan<CLASS>(this, bindingContext);
	}

	@Override
	public FieldXmlWriter<CLASS> newXmlWriter() {
		return new FieldXmlWriter<CLASS>(this);
	}

	@Override
	public RootWrapper getRootWrapper() {
		return null;
	}

	@Override
	public AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldJsonReader<CLASS> getJsonReader(BindingContext bindingContext) throws BindingException {
		return new FieldJsonReader<CLASS>(this);
	}

}
