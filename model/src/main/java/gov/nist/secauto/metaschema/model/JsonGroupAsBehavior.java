package gov.nist.secauto.metaschema.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum;

public enum JsonGroupAsBehavior {
	KEYED(gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.BY_KEY),
	SINGLETON_OR_LIST(gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.SINGLETON_OR_ARRAY),
	LIST(gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.ARRAY);

	private static final Map<gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum, JsonGroupAsBehavior> modelToEnumMap;

	static {
		Map<gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum, JsonGroupAsBehavior> _modelToEnumMap = new HashMap<>();
		for (JsonGroupAsBehavior e : values()) {
			_modelToEnumMap.put(e.getModelValue(), e);
		}
		modelToEnumMap = Collections.unmodifiableMap(_modelToEnumMap);
	}
	
	public static JsonGroupAsBehavior lookup(gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum value) {
		return modelToEnumMap.get(value);
	}

	private final gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum modelValue;

	private JsonGroupAsBehavior(Enum modelValue) {
		this.modelValue = modelValue;
	}

	protected gov.nist.csrc.ns.oscal.metaschema.x10.JsonGroupBehavior.Enum getModelValue() {
		return modelValue;
	}
}
