package gov.nist.secauto.metaschema.model.info.definitions;

import gov.nist.secauto.metaschema.model.info.Field;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public interface FieldDefinition extends ManagedObject, Field {
	boolean hasJsonValueKey();
	/**
	 * 
	 * @return a string or a FlagInstance value
	 */
	Object getJsonValueKey();
	JsonValueKeyEnum getJsonValueKeyType();
	FlagInstance getJsonValueKeyFlagInstance();
	String getJsonValueKeyName();
	boolean isCollapsible();
}
