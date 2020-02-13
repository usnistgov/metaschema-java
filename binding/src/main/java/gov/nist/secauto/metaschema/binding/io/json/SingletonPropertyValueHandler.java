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
package gov.nist.secauto.metaschema.binding.io.json;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public class SingletonPropertyValueHandler extends AbstractPropertyValueHandler {

	private Object value;

	public SingletonPropertyValueHandler(ClassBinding<?> classBinding,
			PropertyItemHandler propertyItemHandler) {
		super(classBinding, propertyItemHandler);
	}

	@Override
	public boolean parseNextFieldValue(JsonParsingContext parsingContext) throws BindingException, IOException {
		// Parse the value at the current token; after this the current token is
		// expected to be the end of the value (e.g., VALUE, END_OBJECT
		PropertyItemHandler propertyItemHandler = getPropertyItemHandler();
		List<Object> values = propertyItemHandler.parse(parsingContext, null);

		if (values == null) {
			throw new BindingException(String.format(
					"Expected a singleton value for property '%s' on class '%s', but no value was parsed.",
					getPropertyBinding().getPropertyInfo().getSimpleName(), getClassBinding().getClazz().getSimpleName()));
		} else if (values.size() != 1) {
			throw new BindingException(
					String.format("Expected a singleton value for property '%s' on class '%s', but parsed %d values.",
							getPropertyBinding().getPropertyInfo().getSimpleName(),  getClassBinding().getClazz().getSimpleName(),
							values.size()));
		} else {
			value = values.get(0);
		}
		return false;
	}

	@Override
	public Supplier<Object> getObjectSupplier() {
		return () -> value;
	}
}
