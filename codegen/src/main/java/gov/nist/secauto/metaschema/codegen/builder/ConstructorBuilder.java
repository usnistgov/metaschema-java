package gov.nist.secauto.metaschema.codegen.builder;

import java.io.PrintWriter;

public class ConstructorBuilder extends AbstractMethodBuilder<ConstructorBuilder> {
	private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;

	public ConstructorBuilder(ClassBuilder classBuilder) {
		super(classBuilder);
	}

	protected String getName() {
		return getClassBuilder().getJavaType().getClassName();
	}

	@Override
	public void build(PrintWriter out) {
		buildAnnotations(out);
		String arguments = getArguments();
		if (arguments == null) {
			arguments = "";
		}

		out.printf("%s%s%s(%s) {%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), getName(), arguments);
		out.print(getBody());
		out.printf("%s}%n", getPadding());
	}
}
