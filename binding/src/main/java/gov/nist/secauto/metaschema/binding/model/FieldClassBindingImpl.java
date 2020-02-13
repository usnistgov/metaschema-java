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
package gov.nist.secauto.metaschema.binding.model;

import java.util.Collections;
import java.util.Map;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.CollapsedFieldJsonReader;
import gov.nist.secauto.metaschema.binding.io.json.parser.FieldJsonReader;
import gov.nist.secauto.metaschema.binding.io.json.parser.SingleFieldJsonReader;
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
		FieldJsonReader<CLASS> retval;
		if (isCollapsible()) {
			retval = new SingleFieldJsonReader<CLASS>(this);
		} else {
			retval = new CollapsedFieldJsonReader<CLASS>(this);
		}
		return retval;
	}

}
