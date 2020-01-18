package gov.nist.secauto.metaschema.model.info.instances;

public interface ModelInstance extends InfoElementInstance {
	int getMinOccurs();
	int getMaxOccurs();
	String getInstanceName();
	JsonGroupAsBehavior getJsonGroupAsBehavior();
	XmlGroupAsBehavior getXmlGroupAsBehavior();
}
