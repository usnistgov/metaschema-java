package gov.nist.secauto.metaschema.codegen.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class InnerClassBuilder extends AbstractClassBuilder<InnerClassBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;

	private static JavaType makeInnerClassJavaType(ClassBuilder outerClassBuilder, String className) {
		JavaType outerType = outerClassBuilder.getJavaType();
		StringBuilder name = new StringBuilder();
		name.append(outerType.getClassName());
		name.append('.');
		name.append(className);
		return JavaType.create(outerType.getPackageName(), name.toString());
		
	}

	private final ClassBuilder outerClassBuilder;
	private final String className;
	private String extendsClass;

	public InnerClassBuilder(ClassBuilder outerClassBuilder, String className) {
		super(makeInnerClassJavaType(outerClassBuilder, className));
		this.outerClassBuilder = outerClassBuilder; 
		this.className = className;
	}

	public InnerClassBuilder extendsClass(String value) {
		this.extendsClass = value;
		return this;
	}

	protected String getExtendsClass() {
		return extendsClass;
	}

	public String getClassName() {
		return className;
	}

	@Override
	protected String getPadding() {
		return "    ";
	}

	@Override
	public ClassBuilder getActualClassBuilder() {
		return getClassBuilder();
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return outerClassBuilder;
	}

	@Override
	public Set<JavaType> getImports() {
		// Handle Imports
		Set<JavaType> imports = new HashSet<>(getImports());
		for (FieldBuilder field : getFields().values()) {
			imports.addAll(field.getImports());
		}
		for (ConstructorBuilder constructor : getConstructors()) {
			imports.addAll(constructor.getImports());
		}
		for (MethodBuilder method : getMethods().values()) {
			imports.addAll(method.getImports());
		}
		return Collections.unmodifiableSet(imports);
	}

	@Override
	public void build(PrintWriter out) throws IOException {
		// class declaration
		buildAnnotations(out);

		String extendsClass = getExtendsClass();
		if (extendsClass == null) {
			extendsClass = "";
		} else {
			extendsClass = "extends " + extendsClass + " ";
		}

		out.printf("%s%sstatic class %s %s{%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), getClassName(), extendsClass);

		for (FieldBuilder field : getFields().values()) {
			field.build(out);
			out.println();
		}

		for (ConstructorBuilder constructor : getConstructors()) {
			constructor.build(out);
			out.println();
		}

		for (MethodBuilder method : getMethods().values()) {
			method.build(out);
			out.println();
		}
		
		out.printf("%s}%n", getPadding());
	}

	@Override
	public Function<String, Boolean> getClashEvaluator() {
		return getClassBuilder().getClashEvaluator();
	}
}
