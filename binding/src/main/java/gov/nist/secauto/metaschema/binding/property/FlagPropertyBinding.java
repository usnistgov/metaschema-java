package gov.nist.secauto.metaschema.binding.property;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.annotations.Flag;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.DefaultXmlAttributePropertyParser;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlAttributePropertyParser;

public class FlagPropertyBinding extends AbstractPropertyBinding implements NamedPropertyBinding {
	private final Flag flagAnnotation;
	private final boolean isJsonKey;
	private final boolean isJsonValueKey;
	private final QName name;

	public FlagPropertyBinding(PropertyInfo propertyInfo, Flag flagAnnotation, boolean isJsonKey, boolean isJsonValueKey) {
		super(propertyInfo);
		this.flagAnnotation = flagAnnotation;
		this.isJsonKey = isJsonKey;
		this.isJsonValueKey = isJsonValueKey;
		// Currently assumes attribute unqualified
		// TODO: Handle attribute namespace qualified from package info
		this.name = new QName(getLocalName());
	}

	protected Flag getFlagAnnotation() {
		return flagAnnotation;
	}

	public boolean isJsonKey() {
		return isJsonKey;
	}

	public boolean isJsonValueKey() {
		return isJsonValueKey;
	}

	@Override
	public XmlAttributePropertyParser newXmlPropertyParser(BindingContext bindingContext) {
		return new DefaultXmlAttributePropertyParser(this, bindingContext);
	}

	public QName getXmlQName() {
		return name;
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getFlagAnnotation().name(), getPropertyInfo().getSimpleName());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getFlagAnnotation().namespace(), getPropertyInfo().getContainingClass());
	}
}
