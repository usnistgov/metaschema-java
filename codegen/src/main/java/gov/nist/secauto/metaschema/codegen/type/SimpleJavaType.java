package gov.nist.secauto.metaschema.codegen.type;

import java.util.Objects;

import gov.nist.secauto.metaschema.codegen.ClassUtils;
import gov.nist.secauto.metaschema.model.ManagedObject;

public class SimpleJavaType extends AbstractJavaType {
	private final String className;
	private final String packageName;
	private final String qualifiedClassName;

	SimpleJavaType(String packageName, String className) {
		Objects.requireNonNull(packageName, "packageName");
		Objects.requireNonNull(className, "className");
		this.className = className;
		this.packageName = packageName;
		this.qualifiedClassName = packageName+"."+className;
	}

	SimpleJavaType(ManagedObject obj) {
		this(ClassUtils.toPackageName(obj), ClassUtils.toClassName(obj));
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String getQualifiedClassName() {
		return qualifiedClassName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + qualifiedClassName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleJavaType)) {
			return false;
		}
		SimpleJavaType other = (SimpleJavaType) obj;
		if (qualifiedClassName == null) {
			if (other.qualifiedClassName != null) {
				return false;
			}
		} else if (!qualifiedClassName.equals(other.qualifiedClassName)) {
			return false;
		}
		return true;
	}

}
