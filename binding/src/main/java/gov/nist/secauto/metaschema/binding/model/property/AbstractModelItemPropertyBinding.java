package gov.nist.secauto.metaschema.binding.model.property;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;

public abstract class AbstractModelItemPropertyBinding extends AbstractPropertyBinding
		implements ModelItemPropertyBinding {

	private final QName xmlQName;
	private final String jsonFieldName;

	public AbstractModelItemPropertyBinding(PropertyInfo propertyInfo, String localName, String namespace) {
		super(propertyInfo);

		String resolvedLocalName = ModelUtil.resolveLocalName(localName, propertyInfo.getSimpleName());
		String resolvedNamespace = ModelUtil.resolveNamespace(namespace,  getPropertyInfo().getContainingClass());
		this.xmlQName = new QName(resolvedNamespace, resolvedLocalName);

		if (propertyInfo instanceof CollectionPropertyInfo) {
			this.jsonFieldName = ((CollectionPropertyInfo)propertyInfo).getGroupXmlQName().getLocalPart();
		} else {
			this.jsonFieldName = resolvedLocalName;
		}
	}

	@Override
	public QName getXmlQName() {
		return xmlQName;
	}

	@Override
	public String getJsonFieldName(BindingContext bindingContext) {
		return jsonFieldName;
	}

	@Override
	public int getMaximumOccurance() {
		PropertyInfo propertyInfo = getPropertyInfo();

		int retval;
		if (propertyInfo instanceof CollectionPropertyInfo) {
			retval = ((CollectionPropertyInfo)propertyInfo).getMaximumOccurance();
		} else {
			retval = 1;
		}
		return retval;
	}

}
