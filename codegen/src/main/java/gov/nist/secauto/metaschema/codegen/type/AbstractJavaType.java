package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;

public abstract class AbstractJavaType implements JavaType {
	private final String className;
	private final String packageName;
	private final String qualifiedClassName;

	public AbstractJavaType(Class<?> clazz) {
		this(clazz.getPackageName(),clazz.getSimpleName());
	}

	public AbstractJavaType(String packageName, String className) {
		Objects.requireNonNull(packageName, "packageName");
		Objects.requireNonNull(className, "className");

		this.packageName = packageName;
		this.className = className;
		this.qualifiedClassName = getPackageName() + "." + getClassName();
	}

	protected String getClassName() {
		return className;
	}

	protected String getPackageName() {
		return packageName;
	}

	protected String getQualifiedClassName() {
		return qualifiedClassName;
	}

	public String getType(AbstractClassGenerator<?> classContext) {
		String retval;
		if (getClassName().equals(classContext.getClassName())) {
			// qualify the type
			retval = getQualifiedClassName();
		} else {
			// use import
			retval = getClassName();
		}
		return retval;
	}

	@Override
	public Set<String> getImports(AbstractClassGenerator<?> classContext) {
		Set<String> retval = new HashSet<>();

		// check if the class name is the same as the containing class, if not add an import
		if (!getClassName().equals(classContext.getClassName()) && !"java.lang".equals(getPackageName())) {
			retval.add(getQualifiedClassName());
		}
		return Collections.unmodifiableSet(retval);
	}
}
