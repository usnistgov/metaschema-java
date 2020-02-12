package gov.nist.secauto.metaschema.binding.io.json.writer;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;

public class CallingContext {
	public static final CallingContext NO_GROUPING;
	public static final CallingContext SINGLETON_OR_LIST;
	public static final CallingContext LIST;
	public static final CallingContext KEYED;

	static {
		NO_GROUPING = new CallingContext(false, JsonGroupAsBehavior.NONE);
		SINGLETON_OR_LIST = new CallingContext(false, JsonGroupAsBehavior.SINGLETON_OR_LIST);
		LIST = new CallingContext(false, JsonGroupAsBehavior.LIST);
		KEYED = new CallingContext(false, JsonGroupAsBehavior.KEYED);
	}

	private final boolean isRootProperty;
	private final JsonGroupAsBehavior groupAsBehavior;

	public CallingContext(boolean isRootProperty, JsonGroupAsBehavior groupAsBehavior) {
		Objects.requireNonNull(groupAsBehavior, "groupAsBehavior");
		this.isRootProperty = isRootProperty;
		this.groupAsBehavior = groupAsBehavior;
	}

	protected boolean isRootProperty() {
		return isRootProperty;
	}

	protected JsonGroupAsBehavior getGroupAsBehavior() {
		return groupAsBehavior;
	}
}
