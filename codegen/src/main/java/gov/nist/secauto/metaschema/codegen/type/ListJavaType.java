package gov.nist.secauto.metaschema.codegen.type;

import java.util.List;

public class ListJavaType extends AbstractCollectionJavaType {

	ListJavaType(JavaType itemClass) {
		super(List.class, itemClass);
	}

	@Override
	protected String getGenerics(JavaType classType) {
		return getValueClass().getType(classType);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ListJavaType)) {
			return false;
		}
		return true;
	}
}
