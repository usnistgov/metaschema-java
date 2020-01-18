package gov.nist.secauto.metaschema.binding.property;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public abstract class AbstractCollectionPropertyInfo extends AbstractPropertyInfo<ParameterizedType>
		implements CollectionPropertyInfo {
	private final GroupAs groupAs;

	public AbstractCollectionPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor);
		Objects.requireNonNull(groupAs, "groupAs");
		this.groupAs = groupAs;
	}

	protected GroupAs getGroupAs() {
		return groupAs;
	}

	@Override
	public Class<?> getRawType() {
		return (Class<?>)getType().getRawType();
	}

	@Override
	public boolean isGrouped() {
		return getGroupAs() != null;
	}

	@Override
	public String getGroupLocalName() {
		return ModelUtil.resolveLocalName(getGroupAs().name(), getPropertyAccessor().getSimpleName());
	}

	@Override
	public String getGroupNamespace() throws BindingException {
		return ModelUtil.resolveNamespace(getGroupAs().namespace(), getPropertyAccessor().getContainingClass());
	}

	@Override
	public XmlGroupAsBehavior getXmlGroupAsBehavior() {
		return getGroupAs().inXml();
	}

	@Override
	public JsonGroupAsBehavior getJsonGroupAsBehavior() {
		return getGroupAs().inJson();
	}
}
