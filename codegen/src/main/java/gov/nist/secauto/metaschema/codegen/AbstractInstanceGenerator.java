/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
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
