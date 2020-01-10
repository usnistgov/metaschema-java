package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface CollectionPropertyInfo extends PropertyInfo {

	boolean isList();
	boolean isMap();

	boolean isGrouped();

	String getGroupLocalName();
	String getGroupNamespace() throws BindingException;

	XmlGroupAsBehavior getXmlGroupAsBehavior();
	JsonGroupAsBehavior getJsonGroupAsBehavior();

}
