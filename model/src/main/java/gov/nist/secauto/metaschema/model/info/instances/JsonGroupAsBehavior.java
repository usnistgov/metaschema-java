package gov.nist.secauto.metaschema.model.info.instances;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum JsonGroupAsBehavior {
	KEYED(gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.BY_KEY),
	SINGLETON_OR_LIST(gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.SINGLETON_OR_ARRAY),
	LIST(gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.ARRAY);

	private static final Map<gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum, JsonGroupAsBehavior> modelToEnumMap;

	static {
		Map<gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum, JsonGroupAsBehavior> _modelToEnumMap = new HashMap<>();
		for (JsonGroupAsBehavior e : values()) {
			_modelToEnumMap.put(e.getModelValue(), e);
		}
		modelToEnumMap = Collections.unmodifiableMap(_modelToEnumMap);
	}
	
	public static JsonGroupAsBehavior lookup(gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum value) {
		return modelToEnumMap.get(value);
	}

	private final gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum modelValue;

	private JsonGroupAsBehavior(gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum modelValue) {
		this.modelValue = modelValue;
	}

	protected gov.nist.itl.metaschema.model.xml.JsonGroupBehavior.Enum getModelValue() {
		return modelValue;
	}
}
