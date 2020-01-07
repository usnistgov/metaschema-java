package gov.nist.secauto.metaschema.datatype.binding.property;

import java.lang.reflect.Type;

import gov.nist.secauto.metaschema.datatype.annotations.GroupAs;
import gov.nist.secauto.metaschema.datatype.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.datatype.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public abstract class AbstractCollectionPropertyInfo<TYPE extends Type> extends BasicPropertyInfo<TYPE>
		implements CollectionPropertyInfo {
	private final GroupAs groupAs;

	public AbstractCollectionPropertyInfo(TYPE type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor);
		this.groupAs = groupAs;
	}

	protected GroupAs getGroupAs() {
		return groupAs;
	}

	@Override
	public boolean isGrouped() {
		return getGroupAs() != null;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isMap() {
		return false;
	}

	@Override
	public String getGroupLocalName() {
		return getGroupAs() != null ? ModelUtil.resolveLocalName(getGroupAs().name(), getPropertyAccessor().getSimpleName()) : null;
	}

	@Override
	public String getGroupNamespace() throws BindingException {
		return getGroupAs() != null ? ModelUtil.resolveNamespace(getGroupAs().namespace(), getPropertyAccessor().getContainingClass()) : null;
	}

	@Override
	public XmlGroupAsBehavior getXmlGroupAsBehavior() {
		return getGroupAs() != null ? getGroupAs().inXml() : XmlGroupAsBehavior.NONE;
	}

	@Override
	public JsonGroupAsBehavior getJsonGroupAsBehavior() {
		return getGroupAs() != null ? getGroupAs().inJson() : JsonGroupAsBehavior.NONE;
	}
}
