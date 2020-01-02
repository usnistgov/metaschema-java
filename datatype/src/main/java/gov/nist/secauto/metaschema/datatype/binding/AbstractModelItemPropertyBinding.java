package gov.nist.secauto.metaschema.datatype.binding;

import gov.nist.secauto.metaschema.datatype.parser.PropertyCollector;

public abstract class AbstractModelItemPropertyBinding extends AbstractPropertyBinding<CollectionPropertyInfo>
		implements ModelItemPropertyBinding {

	public AbstractModelItemPropertyBinding(CollectionPropertyInfo propertyInfo) {
		super(propertyInfo);
	}

	@Override
	public PropertyCollector newPropertyCollector() {
		return getPropertyInfo().newPropertyCollector();
	}

}
