package gov.nist.secauto.metaschema.codegen;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gov.nist.secauto.metaschema.codegen.context.ClassContext;

public class AssemblyClassGenerator extends AbstractClassGenerator {
	private final List<ModelInstanceGenerator> modelInstances;

	public List<ModelInstanceGenerator> getModelInstances() {
		return modelInstances;
	}

	public static abstract class Builder<C extends AssemblyClassGenerator, B extends Builder<C, B>>
			extends AbstractClassGenerator.Builder<C, B> {
		private List<ModelInstanceGenerator> modelInstances = new LinkedList<>();;

		@SuppressWarnings("unchecked")
		public B instance(ModelInstanceGenerator modelInstanceGenerator) {
			modelInstances.add(modelInstanceGenerator);
			return (B) this;
		}
	}

	public static Builder<?, ?> builder() {
		return new DefaultBuilder();
	}

	protected AssemblyClassGenerator(Builder<?, ?> builder) {
		super(builder);
		this.modelInstances = Collections.unmodifiableList(builder.modelInstances);
	}

	private static class DefaultBuilder
			extends Builder<AssemblyClassGenerator, DefaultBuilder> {
		@Override
		public AssemblyClassGenerator build() {
			return new AssemblyClassGenerator(this);
		}
	}

	@Override
	protected void processInstances(ClassContext classContext) {
		super.processInstances(classContext);
		for (ModelInstanceGenerator modelGenerator : getModelInstances()) {
			classContext.newModelInstance(modelGenerator);
		}
	}

}