package gov.nist.secauto.metaschema.codegen.builder;

import java.util.function.Function;

public abstract class AbstractMemberBuilder<T extends AbstractMemberBuilder<T>> extends AbstractBuilder<T> {
	private final AbstractClassBuilder<?> classBuilder;

	public AbstractMemberBuilder(AbstractClassBuilder<?> classBuilder) {
		this.classBuilder = classBuilder;
	}

	@Override
	public Function<String, Boolean> getClashEvaluator() {
		return getClassBuilder().getClashEvaluator();
	}

	@Override
	public AbstractClassBuilder<?> getClassBuilder() {
		return classBuilder;
	}

	@Override
	protected String getPadding() {
		return "    ";
	}

}
