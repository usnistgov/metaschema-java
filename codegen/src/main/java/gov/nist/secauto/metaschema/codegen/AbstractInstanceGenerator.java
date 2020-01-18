package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.builder.MethodBuilder;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.instances.ClassUtils;

public abstract class AbstractInstanceGenerator<GENERATOR extends ClassGenerator> implements InstanceGenerator {
	private final GENERATOR classGenerator;
	private String propertyName;
	private String variableName;

	public AbstractInstanceGenerator(GENERATOR classContext) {
		this.classGenerator = classContext;
	}

	protected ClassGenerator getClassGenerator() {
		return classGenerator;
	}

	/**
	 * The property name of the instance, which must be unique within the class.
	 * 
	 * @return the name
	 */
	@Override
	public String getPropertyName() {
		if (this.propertyName == null) {
			String name = ClassUtils.toPropertyName(getInstanceName());
			// first check if a property already exists with the same name
			if (classGenerator.hasInstanceWithName(name)) {
				// append an integer value to make the name unique
				String newName;
				int i = 1;
				do {
					newName = ClassUtils.toPropertyName(name + Integer.toString(i));
					i++;
				} while (classGenerator.hasInstanceWithName(newName));
				name = newName;
			}

			this.propertyName = name;
		}
		return this.propertyName;
	}

	public final String getVariableName() {
		if (this.variableName == null) {
			this.variableName = "_" + ClassUtils.toVariableName(getPropertyName());
		}
		return this.variableName;
	}

	protected abstract JavaType getJavaType();

	protected abstract String getInstanceName();

//	protected void writeVariableJavadoc(PrintWriter writer) {
//		MarkupString description = getDescription();
//		if (description != null) {
//			writer.println("\t/**");
//			writer.println("\t * " + description.toHTML());
//			writer.println("\t */");
//		}
//	}

	@Override
	public void buildInstance(ClassBuilder builder) {
		buildField(builder.newFieldBuilder(getJavaType(), getVariableName()));

		buildGetter(builder.newMethodBuilder("get" + getPropertyName()).returnType(getJavaType()));
		buildSetter(builder.newMethodBuilder("set" + getPropertyName())
				.arguments(String.format("%s value", getJavaType().getType(builder.getClashEvaluator()))));
	}

	protected abstract void buildField(@SuppressWarnings("unused") FieldBuilder builder);

	protected void buildGetter(MethodBuilder builder) {
		builder.getBodyWriter().format("return %s;%n", getVariableName());
	}

	private void buildSetter(MethodBuilder builder) {
		builder.getBodyWriter().format("this.%s = value;%n", getVariableName());
	}
}
