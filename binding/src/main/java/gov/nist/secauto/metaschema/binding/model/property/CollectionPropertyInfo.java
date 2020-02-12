package gov.nist.secauto.metaschema.binding.model.property;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;

public interface CollectionPropertyInfo extends PropertyInfo {

	boolean isList();
	boolean isMap();

	QName getGroupXmlQName();

	XmlGroupAsBehavior getXmlGroupAsBehavior();
	JsonGroupAsBehavior getJsonGroupAsBehavior();
	int getMinimumOccurance();
	int getMaximumOccurance();
}
