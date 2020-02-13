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

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlAttributePropertyParser;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlAttributePropertyParser;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;

public class DefaultFlagPropertyBinding extends AbstractPropertyBinding implements FlagPropertyBinding {
	private final Flag flagAnnotation;
	private final boolean isJsonKey;
	private final boolean isJsonValueKey;
	private final QName xmlQName;

	public DefaultFlagPropertyBinding(PropertyInfo propertyInfo, Flag flagAnnotation, boolean isJsonKey, boolean isJsonValueKey) {
		super(propertyInfo);
		this.flagAnnotation = flagAnnotation;
		this.isJsonKey = isJsonKey;
		this.isJsonValueKey = isJsonValueKey;
		// Currently assumes attribute unqualified
		// TODO: Handle attribute namespace qualified from package info
		String resolvedLocalName = ModelUtil.resolveLocalName(getFlagAnnotation().name(), propertyInfo.getSimpleName());
//		String resolvedNamespace = ModelUtil.resolveNamespace(getFlagAnnotation().namespace(),  getPropertyInfo().getContainingClass());
		this.xmlQName = new QName(resolvedLocalName);
	}

	protected Flag getFlagAnnotation() {
		return flagAnnotation;
	}

	@Override
	public PropertyBindingType getPropertyBindingType() {
		return PropertyBindingType.FLAG;
	}

	@Override
	public QName getXmlQName() {
		return xmlQName;
	}

	@Override
	public String getJsonFieldName(BindingContext bindingContext) {
		return getXmlQName().getLocalPart();
	}

	@Override
	public boolean isJsonKey() {
		return isJsonKey;
	}

	@Override
	public boolean isJsonValueKey() {
		return isJsonValueKey;
	}

	@Override
	public XmlAttributePropertyParser newXmlPropertyParser(BindingContext bindingContext) {
		return new DefaultXmlAttributePropertyParser(this, bindingContext);
	}

}
