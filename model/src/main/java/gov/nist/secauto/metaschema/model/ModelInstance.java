package gov.nist.secauto.metaschema.model;

public interface ModelInstance extends InfoElementInstance {
	int getMinOccurs();
	int getMaxOccurs();
	String getInstanceName();
	JsonGroupAsBehavior getJsonGroupAsBehavior();
	XmlGroupAsBehavior getXmlGroupAsBehavior();
}
