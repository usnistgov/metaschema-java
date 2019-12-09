package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;
import java.util.Objects;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class MethodBuilder extends AbstractMethodBuilder<MethodBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;
	private final String name;
	private JavaType returnType;

	public MethodBuilder(ClassBuilder classBuilder, String name) {
		super(classBuilder);
		Objects.requireNonNull(name, "name");
		this.name = name;
	}

	public MethodBuilder returnType(Class<?> clazz) {
		return returnType(JavaType.create(clazz));
	}

	public MethodBuilder returnType(JavaType type) {
		this.returnType = type;
		importEntries(type.getImports(getClassBuilder().getJavaType()));
		return this;
	}

	protected JavaType getReturnType() {
		return returnType;
	}

	protected String getName() {
		return name;
	}

	@Override
	public void build(PrintWriter out) {
		buildAnnotations(out);
		JavaType returnType = getReturnType();
		String returnTypeValue;
		if (returnType == null) {
			returnTypeValue = "void";
		} else {
			returnTypeValue = returnType.getType(getClassBuilder().getJavaType());
		}
		String arguments = getArguments();
		if (arguments == null) {
			arguments = "";
		}
		out.printf("%s%s%s %s(%s) {%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), returnTypeValue, getName(), arguments);
		out.print(getBody());
		out.printf("%s}%n", getPadding());
	}

}
