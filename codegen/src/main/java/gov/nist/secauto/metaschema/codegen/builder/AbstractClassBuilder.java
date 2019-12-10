package gov.nist.secauto.metaschema.codegen.builder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public abstract class AbstractClassBuilder<T extends AbstractClassBuilder<T>> extends AbstractBuilder<T> {
	private final JavaType javaType;
	private Map<String, FieldBuilder> fields = new LinkedHashMap<>();
	private List<ConstructorBuilder> constructors = new LinkedList<>();
	private Map<String, MethodBuilder> methods = new LinkedHashMap<>();
	
	public AbstractClassBuilder(JavaType classJavaType) {
		this.javaType = classJavaType;
	}

	public abstract ClassBuilder getActualClassBuilder();

	public JavaType getJavaType() {
		return javaType;
	}

	public FieldBuilder newFieldBuilder(JavaType javaType, String name) {
		FieldBuilder retval = new FieldBuilder(this, javaType, name);
		fields.put(retval.getName(), retval);
		return retval;
	}

	public ConstructorBuilder newConstructorBuilder() {
		ConstructorBuilder retval = new ConstructorBuilder(this);
		constructors.add(retval);
		return retval;
	}

	public MethodBuilder newMethodBuilder(String name) {
		MethodBuilder retval = new MethodBuilder(this, name);
		methods.put(retval.getName(), retval);
		return retval;
	}

	protected Map<String, FieldBuilder> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	protected List<ConstructorBuilder> getConstructors() {
		return Collections.unmodifiableList(constructors);
	}

	protected Map<String, MethodBuilder> getMethods() {
		return Collections.unmodifiableMap(methods);
	}

}
