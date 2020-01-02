package gov.nist.secauto.metaschema.codegen.type;

public class ClassJavaType extends AbstractJavaType {
	private final Class<?> clazz;

	ClassJavaType(Class<?> clazz) {
		this.clazz = clazz;
	}

	protected Class<?> getClazz() {
		return clazz;
	}

	@Override
	public String getClassName() {
		return getClazz().getSimpleName();
	}

	@Override
	public String getPackageName() {
		return getClazz().getPackageName();
	}

	@Override
	public String getQualifiedClassName() {
		return getClazz().getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ClassJavaType)) {
			return false;
		}
		ClassJavaType other = (ClassJavaType) obj;
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.getName().equals(other.clazz.getName())) {
			return false;
		}
		return true;
	}
}
