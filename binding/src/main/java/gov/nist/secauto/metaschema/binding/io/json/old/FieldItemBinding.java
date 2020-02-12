package gov.nist.secauto.metaschema.binding.io.json.old;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.writer.FlagUtil;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class FieldItemBinding extends AbstractBoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding> {

	public FieldItemBinding(FieldClassBinding<?> classBinding, FieldPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public void writeValue(Object obj, NamedPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException, IOException {
		FieldClassBinding<?> classBinding = getClassBinding();

		List<FlagPropertyBinding> flags = FlagUtil.filterFlags(obj, classBinding.getFlagPropertyBindings(), filter);
		FieldValuePropertyBinding fieldValueBinding = classBinding.getFieldValuePropertyBinding();

		JsonGenerator generator = writingContext.getEventWriter();
		if (!flags.isEmpty()) {
			generator.writeStartObject();

			// figure out the method used for the value's field name
			String valueFieldName = null;
			FlagPropertyBinding jsonValueKeyFlag = classBinding.getJsonValueKeyFlagPropertyBinding();
			if (jsonValueKeyFlag != null) {
				// use the value key flag if it exists
				Object valueKeyValue = jsonValueKeyFlag.getPropertyInfo().getValue(obj);
				if (valueKeyValue == null) {
					throw new BindingException(String.format(
							"The field value binding on class '%s' uses a '%s' annotation, but the flag's value is null.",
							classBinding.getClazz().getName(), JsonFieldValueKey.class.getName()));
				}
				valueFieldName = valueKeyValue.toString();
				filter = (NamedPropertyBinding binding) -> jsonValueKeyFlag.equals(binding);
			} else {
				// use the value name instead
				valueFieldName = fieldValueBinding.getJsonFieldName(writingContext.getBindingContext());
			}

			// Output flags
			List<FlagPropertyBinding> remainingFlags = FlagUtil.filterFlags(obj, flags, filter);
			if (!remainingFlags.isEmpty()) {
				FlagUtil.writeFlags(obj, remainingFlags, writingContext);
			}

			// now write the field name for the field's value
			generator.writeFieldName(valueFieldName);
		}

		Object value = fieldValueBinding.getPropertyInfo().getValue(obj);
		Class<?> itemClass = fieldValueBinding.getPropertyInfo().getItemType();
		JavaTypeAdapter<?> adapter = writingContext.getBindingContext().getJavaTypeAdapter(itemClass);
		adapter.writeJsonFieldValue(value, null, writingContext);

		if (!flags.isEmpty()) {
			generator.writeEndObject();
		}
	}

}
