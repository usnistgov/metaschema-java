package gov.nist.secauto.metaschema.model;

public interface FieldDefinition extends ManagedObject, Field {
	boolean hasJsonValueKey();
	/**
	 * 
	 * @return a string or a FlagInstance value
	 */
	Object getJsonValueKey();
	FlagInstance getJsonValueKeyFlagInstance();
	String getJsonValueKeyName();
	boolean isCollapsible();
}
