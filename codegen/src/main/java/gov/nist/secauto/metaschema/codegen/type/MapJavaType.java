package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;

public class MapJavaType extends AbstractCollectionJavaType {
	private final JavaType keyClass;

	public MapJavaType(Class<?> keyClass, JavaType itemClass) {
		this(new SimpleJavaType(keyClass), itemClass);
	}

	public MapJavaType(JavaType keyClass, JavaType itemClass) {
		super(Map.class, itemClass);
		Objects.requireNonNull(keyClass);
		this.keyClass = keyClass;
	}

	protected JavaType getKeyClass() {
		return keyClass;
	}

	@Override
	public Set<String> getImports(AbstractClassGenerator<?> classContext) {
		Set<String> retval = new HashSet<>(super.getImports(classContext));
		retval.addAll(getKeyClass().getImports(classContext));
		return Collections.unmodifiableSet(retval);
	}

	@Override
	protected String getGenerics(AbstractClassGenerator<?> classContext) {
		return getKeyClass().getType(classContext) + "," + getItemClass().getType(classContext);
	}
}
