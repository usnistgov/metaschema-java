package gov.nist.secauto.metaschema.model.info;

import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;

public class Util {
	public static String toCoordinates(InfoElementInstance instance) {
		return String.format("%s:%s:%s@%d(%d)",
				instance.getContainingMetaschema().getShortName(),
				instance.getType(),
				instance.getName(),
				instance.hashCode(),
				instance.isReference() ? instance.getDefinition().hashCode() : 0);
	}

	public static String toCoordinates(InfoElementDefinition definition) {
		return String.format("%s:%s:%s@%d", definition.getContainingMetaschema().getShortName(), definition.getType(),
				definition.getName(),definition.hashCode());
	}
}
