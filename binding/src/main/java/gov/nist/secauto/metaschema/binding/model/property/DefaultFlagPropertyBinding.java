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
