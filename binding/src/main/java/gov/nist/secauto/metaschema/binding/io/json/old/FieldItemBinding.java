/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
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
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class FieldItemBinding extends AbstractBoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding> {

	public FieldItemBinding(FieldClassBinding<?> classBinding, FieldPropertyBinding propertyBinding) {
		super(classBinding, propertyBinding);
	}

	@Override
	public void writeValue(Object obj, PropertyBindingFilter filter, JsonWritingContext writingContext)
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
				filter = (PropertyBinding binding) -> jsonValueKeyFlag.equals(binding);
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
