package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;
import java.util.Objects;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class FieldBuilder extends AbstractMemberBuilder<FieldBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
	private final JavaType javaType;
	private final String name;

	FieldBuilder(ClassBuilder classBuilder, JavaType javaType, String name) {
		super(classBuilder);
		Objects.requireNonNull(javaType, "javaType");
		Objects.requireNonNull(name, "name");
		this.javaType = javaType;
		this.name = name;
		importEntries(javaType.getImports(classBuilder.getJavaType()));
	}

	protected JavaType getJavaType() {
		return javaType;
	}

	protected String getName() {
		return name;
	}

	@Override
	public void build(PrintWriter out) {
		buildAnnotations(out);
		out.printf("%s%s %s %s;%n",getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), getJavaType().getType(getClassBuilder().getJavaType()), getName());
	}
}
