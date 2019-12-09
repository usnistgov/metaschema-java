package gov.nist.secauto.metaschema.codegen.builder;

public abstract class AbstractMemberBuilder<T extends AbstractMemberBuilder<T>> extends AbstractBuilder<T> {
	private final ClassBuilder classBuilder;

	public AbstractMemberBuilder(ClassBuilder classBuilder) {
		this.classBuilder = classBuilder;
	}

	@Override
	public ClassBuilder getClassBuilder() {
		return classBuilder;
	}

	@Override
	protected String getPadding() {
		return "    ";
	}

}
