package gov.nist.secauto.metaschema.binding.io.json.writer;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class CollapseKeyBuilder {
	private final FieldClassBinding<?> classBinding;
	private final FlagPropertyBinding[] flagProperties;

	public CollapseKeyBuilder(FieldClassBinding<?> classBinding) {
		this.classBinding = classBinding;

		List<FlagPropertyBinding> flags = classBinding.getFlagPropertyBindings();

		this.flagProperties = flags.toArray(new FlagPropertyBinding[flags.size()]);
	}

	protected FieldClassBinding<?> getClassBinding() {
		return classBinding;
	}

	public Key build(Object obj) throws BindingException {
		Map<FlagPropertyBinding, Object> flagValues = new LinkedHashMap<>();
		for (int i = 0; i < flagProperties.length; i++) {
			flagValues.put(flagProperties[i], flagProperties[i].getPropertyInfo().getValue(obj));
		}

		return new Key(flagValues);
	}

	public class Key {
		private final Map<FlagPropertyBinding, Object> flagValues;

		public Key(Map<FlagPropertyBinding, Object> flagValues) {
			this.flagValues = Collections.unmodifiableMap(flagValues);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(flagValues);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Key)) {
				return false;
			}
			Key other = (Key) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			return Objects.equals(flagValues, other.flagValues);
		}

		public void write(List<Object> values, JsonWritingContext writingContext) throws BindingException, IOException {
			FieldClassBinding<?> classBinding = getClassBinding();

			NamedPropertyBindingFilter filter = null;
			List<FlagPropertyBinding> flags = classBinding.getFlagPropertyBindings();
			FieldValuePropertyBinding fieldValue = classBinding.getFieldValuePropertyBinding();

			JsonGenerator generator = writingContext.getEventWriter();
			if (!flags.isEmpty()) {
				generator.writeStartObject();

				// figure out the method used for the value's field name
				String valueFieldName = null;
				FlagPropertyBinding jsonValueKeyFlag = classBinding.getJsonValueKeyFlagPropertyBinding();
				if (jsonValueKeyFlag != null) {
					// use the value key flag if it exists
					Object valueKeyValue = flagValues.get(jsonValueKeyFlag);
					if (valueKeyValue == null) {
						throw new BindingException(String.format(
								"The field value binding on class '%s' uses a '%s' annotation, but the flag's value is null.",
								classBinding.getClazz().getName(), JsonFieldValueKey.class.getName()));
					}
					valueFieldName = valueKeyValue.toString();
					filter = (NamedPropertyBinding binding) -> jsonValueKeyFlag.equals(binding);
				} else {
					// use the value name instead
					valueFieldName = fieldValue.getJsonFieldName(writingContext.getBindingContext());
				}

				// Output flags
				for (Map.Entry<FlagPropertyBinding, Object> entry : flagValues.entrySet()) {
					FlagPropertyBinding flag = entry.getKey();

					if (filter == null || !filter.filter(flag)) {
						Object flagValue = entry.getValue();
						if (flagValue != null) {
							FlagUtil.writeFlag(flag, flagValue, writingContext);
						}
					}

				}

				// now write the field name for the field's value
				generator.writeFieldName(valueFieldName);
			}

			if (values.size() > 1) {
				generator.writeStartArray();
			}
			
			for (Object value : values) {
				Class<?> itemClass = fieldValue.getPropertyInfo().getItemType();
				JavaTypeAdapter<?> adapter = writingContext.getBindingContext().getJavaTypeAdapter(itemClass);
				adapter.writeJsonFieldValue(value, null, writingContext);
			}

			if (values.size() > 1) {
				generator.writeEndArray();
			}

			if (!flags.isEmpty()) {
				generator.writeEndObject();
			}
		}

		private CollapseKeyBuilder getEnclosingInstance() {
			return CollapseKeyBuilder.this;
		}
	}
}
