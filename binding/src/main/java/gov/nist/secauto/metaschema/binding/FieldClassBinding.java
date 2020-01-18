package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.binding.annotations.Collapsible;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.FieldXmlParsePlan;
import gov.nist.secauto.metaschema.binding.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.writer.json.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.writer.xml.FieldXmlWriter;

// TODO: handle collapsible
public class FieldClassBinding<CLASS>
		extends AbstractClassBinding<CLASS, FieldXmlParsePlan<CLASS>, FieldXmlWriter<CLASS>> {
	private final FieldValuePropertyBinding fieldValuePropertyBinding;
	private final FlagPropertyBinding jsonValueKeyFlagPropertyBinding;

	public FieldClassBinding(Class<CLASS> clazz) throws BindingException {
		super(clazz);
		this.fieldValuePropertyBinding = ClassIntrospector.getFieldValueBinding(clazz);

		FlagPropertyBinding jsonValueKeyFlag = null;
		for (FlagPropertyBinding flag : getFlagPropertyBindings()) {
			if (flag.isJsonValueKey()) {
				jsonValueKeyFlag = flag;
				break;
			}
		}
		this.jsonValueKeyFlagPropertyBinding = jsonValueKeyFlag;
	}

	public FieldValuePropertyBinding getFieldValuePropertyBinding() {
		return fieldValuePropertyBinding;
	}

	public FlagPropertyBinding getJsonValueKeyFlagPropertyBinding() {
		return jsonValueKeyFlagPropertyBinding;
	}

	public boolean isCollapsible() {
		return getClazz().isAnnotationPresent(Collapsible.class);
	}

	@Override
	public AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldXmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
		return new FieldXmlParsePlan<CLASS>(this, bindingContext);
	}

	@Override
	public FieldXmlWriter<CLASS> newXmlWriter() {
		return new FieldXmlWriter<CLASS>(this);
	}
}
