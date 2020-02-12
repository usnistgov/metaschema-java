package gov.nist.secauto.metaschema.binding.model.property;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;

public abstract class AbstractCollectionPropertyInfo extends AbstractPropertyInfo<ParameterizedType>
		implements CollectionPropertyInfo {
	private final GroupAs groupAs;
	private final QName groupXmlQName;

	public AbstractCollectionPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
		super(type, propertyAccessor);
		Objects.requireNonNull(groupAs, "groupAs");
		this.groupAs = groupAs;

		String resolvedLocalName = ModelUtil.resolveLocalName(getGroupAs().name(), propertyAccessor.getSimpleName());
		String resolvedNamespace = ModelUtil.resolveNamespace(getGroupAs().namespace(), propertyAccessor.getContainingClass());
		this.groupXmlQName = new QName(resolvedNamespace, resolvedLocalName);
	}

	protected GroupAs getGroupAs() {
		return groupAs;
	}

	@Override
	public Class<?> getRawType() {
		return (Class<?>)getType().getRawType();
	}

	@Override
	public int getMinimumOccurance() {
		return groupAs.minOccurs();
	}

	@Override
	public int getMaximumOccurance() {
		return groupAs.maxOccurs();
	}

	@Override
	public QName getGroupXmlQName() {
		return groupXmlQName;
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
