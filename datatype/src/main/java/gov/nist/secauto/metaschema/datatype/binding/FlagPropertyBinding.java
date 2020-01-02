package gov.nist.secauto.metaschema.datatype.binding;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.annotations.Flag;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.DefaultXmlAttributePropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlAttributePropertyParser;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParser;

public class FlagPropertyBinding extends AbstractPropertyBinding<SingletonPropertyInfo> implements NamedPropertyBinding {
	private final Flag flagAnnotation;
	private final QName name;

	public FlagPropertyBinding(SingletonPropertyInfo propertyInfo, Flag flagAnnotation) {
		super(propertyInfo);
		this.flagAnnotation = flagAnnotation;
		// Currently assumes attribute unqualified
		// TODO: Handle attribute namespace qualified from package info
		this.name = new QName(getLocalName());
	}

	protected Flag getFlagAnnotation() {
		return flagAnnotation;
	}

	@Override
	public XmlAttributePropertyParser newXmlPropertyParser(XmlParser parser) {
		return new DefaultXmlAttributePropertyParser(this, parser);
	}

	public QName getXmlQName() {
		return name;
	}

	@Override
	public String getLocalName() {
		return ModelUtil.resolveLocalName(getFlagAnnotation().name(), getPropertyInfo().getPropertyAccessor());
	}

	@Override
	public String getNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getFlagAnnotation().namespace(), getPropertyInfo().getPropertyAccessor());
	}
}
