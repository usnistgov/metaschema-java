package gov.nist.secauto.metaschema.binding.writer.json;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;

// TODO: remove this class
public class FieldJsonWriter<CLASS> extends AbstractJsonWriter<CLASS, FieldClassBinding<CLASS>> {

	public FieldJsonWriter(FieldClassBinding<CLASS> classBinding) {
		super(classBinding);
	}

	@Override
	public void writeJson(Object obj, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		FieldClassBinding<CLASS> classBinding = getClassBinding();
		FieldValuePropertyBinding fieldValue = getClassBinding().getFieldValuePropertyBinding();

		// first figure out the method used for the value's field name
		String valueFieldName = null;
		FlagPropertyBinding jsonValueKeyFlag = classBinding.getJsonValueKeyFlagPropertyBinding();
		if (jsonValueKeyFlag != null) {
			// use the value key flag if it exists
			valueFieldName = jsonValueKeyFlag.getPropertyInfo().getValue(obj).toString();
			filter = FlagUtil.adjustFilterToIncludeFlag(filter, jsonValueKeyFlag);
		} else {
			// use the value name instead
			valueFieldName = fieldValue.getJsonValueName();
		}
		
		JsonGenerator generator = writingContext.getEventWriter();
		try {
			List<FlagPropertyBinding> flags = FlagUtil.filterFlags(obj, classBinding.getFlagPropertyBindings(), filter);

			if (!flags.isEmpty()) {
				generator.writeStartObject();

				// Output flags
				FlagUtil.writeFlags(obj, flags, writingContext);
			}

			Object value = fieldValue.getPropertyInfo().getValue(obj);
			Class<?> itemClass = fieldValue.getPropertyInfo().getItemType();
			JavaTypeAdapter<?> adapter = writingContext.getBindingContext().getJavaTypeAdapter(itemClass);
			adapter.writeJsonFieldValue(value, null, writingContext);
			

			if (!flags.isEmpty()) {
				generator.writeFieldName(valueFieldName);
				generator.writeEndObject();
			}
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}


}
