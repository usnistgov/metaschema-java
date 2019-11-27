package gov.nist.secauto.metaschema.codegen.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;

public abstract class AbstractCollectionJavaType extends AbstractJavaType {
	private final JavaType itemClass;

	public AbstractCollectionJavaType(Class<?> collectionClass, JavaType itemClass) {
		super(collectionClass);
		Objects.requireNonNull(collectionClass, "collectionClass");
		Objects.requireNonNull(itemClass, "itemClass");
		this.itemClass = itemClass;
	}

	@Override
	public String getType(AbstractClassGenerator<?> classContext) {
		return String.format("%s<%s>", super.getType(classContext), getGenerics(classContext));
	}

	@Override
	public Set<String> getImports(AbstractClassGenerator<?> classContext) {
		Set<String> retval = new HashSet<>(super.getImports(classContext));
		retval.addAll(getItemClass().getImports(classContext));
		return Collections.unmodifiableSet(retval);
	}

	protected JavaType getItemClass() {
		return itemClass;
	}

	protected abstract Object getGenerics(AbstractClassGenerator<?> classContext);
}
