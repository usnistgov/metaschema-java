package gov.nist.secauto.metaschema.model.info.instances;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gov.nist.itl.metaschema.model.xml.XmlGroupBehavior;

public enum XmlGroupAsBehavior {
	GROUPED(XmlGroupBehavior.GROUPED),
	UNGROUPED(XmlGroupBehavior.UNGROUPED);

	private static final Map<XmlGroupBehavior.Enum, XmlGroupAsBehavior> modelToEnumMap;

	static {
		Map<XmlGroupBehavior.Enum, XmlGroupAsBehavior> _modelToEnumMap = new HashMap<>();
		for (XmlGroupAsBehavior e : values()) {
			_modelToEnumMap.put(e.getModelValue(), e);
		}
		modelToEnumMap = Collections.unmodifiableMap(_modelToEnumMap);
	}
	
	public static XmlGroupAsBehavior lookup(XmlGroupBehavior.Enum value) {
		return modelToEnumMap.get(value);
	}

	private final XmlGroupBehavior.Enum modelValue;

	private XmlGroupAsBehavior(XmlGroupBehavior.Enum modelValue) {
		this.modelValue = modelValue;
	}

	protected XmlGroupBehavior.Enum getModelValue() {
		return modelValue;
	}
}
