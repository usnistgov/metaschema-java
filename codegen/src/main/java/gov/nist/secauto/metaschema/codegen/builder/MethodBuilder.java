package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class MethodBuilder extends AbstractMethodBuilder<MethodBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;
	private final String name;
	private JavaType returnType;
	private List<Class<? extends Throwable>> exceptionClasses = new LinkedList<>();

	public MethodBuilder(AbstractClassBuilder<?> classBuilder, String name) {
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

	public MethodBuilder throwsDeclaration(Class<? extends Throwable> exceptionClass) {
		this.exceptionClasses.add(exceptionClass);
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
			returnTypeValue = returnType.getType(getClashEvaluator());
		}

		String arguments = getArguments();
		if (arguments == null) {
			arguments = "";
		}

		String throwsClause = null;
		if (!exceptionClasses.isEmpty()) {
			throwsClause = " throws " +exceptionClasses.stream().map(c -> c.getName()).collect(Collectors.joining(", "));
		} else {
			throwsClause = "";
		}

		out.printf("%s%s%s %s(%s)%s {%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), returnTypeValue, getName(), arguments, throwsClause);
		out.print(getBody());
		out.printf("%s}%n", getPadding());
	}

}
