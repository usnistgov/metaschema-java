package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractJavaType implements JavaType {

	public AbstractJavaType() {
	}

	public abstract String getClassName();

	public abstract String getPackageName();

	public abstract String getQualifiedClassName();

	public abstract int hashCode();
	public abstract boolean equals(Object obj);

	
	@Override
	public String getType() {
		return getQualifiedClassName();
	}

	@Override
	public String getType(Function<String, Boolean> clashEvaluator) {
		String retval;
		if (clashEvaluator.apply(this.getClassName())) {
			// qualify the type
			retval = getQualifiedClassName();
		} else {
			// use import
			retval = getClassName();
		}
		return retval;
	}

	public String getType(JavaType classType) {
		String retval;
		if (getClassName().equals(classType.getClassName())) {
			// qualify the type
			retval = getQualifiedClassName();
		} else {
			// use import
			retval = getClassName();
		}
		return retval;
	}

	public Set<JavaType> getImports(JavaType classType) {
		return Collections.singleton(this);
	}

	@Override
	public String getImportValue(JavaType classJavaType) {
		// check if the class name is the same as the containing class, if not add an import
		if (!getClassName().equals(classJavaType.getClassName())) {
			return getQualifiedClassName();
		}
		return null;
	}

	
}
