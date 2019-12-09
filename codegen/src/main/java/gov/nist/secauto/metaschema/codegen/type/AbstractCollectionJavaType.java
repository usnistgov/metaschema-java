package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractCollectionJavaType extends AbstractJavaType {
	private final JavaType collectionClass;
	private final JavaType valueClass;

	public AbstractCollectionJavaType(Class<?> collectionClass, JavaType valueClass) {
		Objects.requireNonNull(collectionClass, "collectionClass");
		Objects.requireNonNull(valueClass, "itemClass");
		this.collectionClass = new ClassJavaType(collectionClass);
		this.valueClass = valueClass;
	}

	protected JavaType getCollectionClass() {
		return collectionClass;
	}

	protected JavaType getValueClass() {
		return valueClass;
	}

	@Override
	public String getType(JavaType classType) {
		return String.format("%s<%s>", super.getType(classType), getGenerics(classType));
	}

	@Override
	public Set<JavaType> getImports(JavaType classType) {
		Set<JavaType> retval = new HashSet<>(super.getImports(classType));
		retval.addAll(getValueClass().getImports(classType));
		retval.addAll(getCollectionClass().getImports(classType));
		return Collections.unmodifiableSet(retval);
	}

	protected abstract Object getGenerics(JavaType classType);


	@Override
	public String getClassName() {
		return getCollectionClass().getClassName();
	}

	@Override
	public String getPackageName() {
		return getCollectionClass().getPackageName();
	}

	@Override
	public String getQualifiedClassName() {
		return getCollectionClass().getQualifiedClassName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + collectionClass.hashCode();
		result = prime * result + valueClass.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractCollectionJavaType)) {
			return false;
		}
		AbstractCollectionJavaType other = (AbstractCollectionJavaType) obj;
		if (!collectionClass.equals(other.collectionClass)) {
			return false;
		}
		if (!valueClass.equals(other.valueClass)) {
			return false;
		}
		return true;
	}

}
