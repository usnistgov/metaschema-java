package gov.nist.secauto.metaschema.binding.writer.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.annotations.JsonValueKey;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;

public class FieldItemBinding extends AbstractBoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding> {

	public FieldItemBinding(FieldClassBinding<?> classBinding, FieldPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public void writeValue(Object obj, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException, IOException {
		FieldClassBinding<?> classBinding = getClassBinding();

		List<FlagPropertyBinding> flags = FlagUtil.filterFlags(obj, classBinding.getFlagPropertyBindings(), filter);
		FieldValuePropertyBinding fieldValue = classBinding.getFieldValuePropertyBinding();

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
							classBinding.getClazz().getName(), JsonValueKey.class.getName()));
				}
				valueFieldName = valueKeyValue.toString();
				filter = (FlagPropertyBinding f) -> jsonValueKeyFlag.equals(f);
			} else {
				// use the value name instead
				valueFieldName = fieldValue.getJsonValueName();
			}

			// Output flags
			List<FlagPropertyBinding> remainingFlags = FlagUtil.filterFlags(obj, flags, filter);
			if (!remainingFlags.isEmpty()) {
				FlagUtil.writeFlags(obj, remainingFlags, writingContext);
			}

			// now write the field name for the field's value
			generator.writeFieldName(valueFieldName);
		}

		Object value = fieldValue.getPropertyInfo().getValue(obj);
		Class<?> itemClass = fieldValue.getPropertyInfo().getItemType();
		JavaTypeAdapter<?> adapter = writingContext.getBindingContext().getJavaTypeAdapter(itemClass);
		adapter.writeJsonFieldValue(value, null, writingContext);

		if (!flags.isEmpty()) {
			generator.writeEndObject();
		}
	}

}
