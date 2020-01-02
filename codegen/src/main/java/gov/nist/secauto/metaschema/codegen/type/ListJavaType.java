package gov.nist.secauto.metaschema.codegen.type;

import java.util.List;
import java.util.function.Function;

public class ListJavaType extends AbstractCollectionJavaType {

	ListJavaType(JavaType itemClass) {
		super(List.class, itemClass);
	}

	@Override
	protected String getGenericArguments(Function<String, Boolean> clashEvaluator) {
		return getValueClass().getType(clashEvaluator);
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
