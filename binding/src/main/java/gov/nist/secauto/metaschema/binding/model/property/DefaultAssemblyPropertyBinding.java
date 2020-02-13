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
package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;

public class DefaultAssemblyPropertyBinding extends AbstractModelItemPropertyBinding implements AssemblyPropertyBinding {

	private final Assembly assemblyAnnotation;

	public DefaultAssemblyPropertyBinding(PropertyInfo propertyInfo, Assembly assemblyAnnotation) {
		super(propertyInfo, assemblyAnnotation.name(), assemblyAnnotation.namespace());
		this.assemblyAnnotation = assemblyAnnotation;
	}

	@Override
	public PropertyBindingType getPropertyBindingType() {
		return PropertyBindingType.ASSEMBLY;
	}

	protected Assembly getAssemblyAnnotation() {
		return assemblyAnnotation;
	}

	@Override
	public DefaultXmlObjectPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
		return new DefaultXmlObjectPropertyParser(this, bindingContext);
	}

	@Override
	public int getMinimumOccurance() {
		PropertyInfo propertyInfo = getPropertyInfo();

		int retval;
		if (propertyInfo instanceof CollectionPropertyInfo) {
			retval = ((CollectionPropertyInfo)propertyInfo).getMinimumOccurance();
		} else {
			retval = getAssemblyAnnotation().required() ? 1 : 0;
		}
		return retval;
	}

}
