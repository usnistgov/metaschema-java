package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractInstanceGenerator<GENERATOR extends AbstractClassGenerator<?>> implements InstanceGenerator {
	private final GENERATOR classContext;
	private String propertyName;
	private String variableName;

	public AbstractInstanceGenerator(GENERATOR classContext) {
		this.classContext = classContext;
	}

	protected AbstractClassGenerator<?> getClassContext() {
		return classContext;
	}

	/**
	 * The property name of the instance, which must be unique within the class.
	 * 
	 * @return the name
	 */
	public String getPropertyName() {
		if (this.propertyName == null) {
			String name = NameConverter.standard.toPropertyName(getInstanceName());
			// first check if a property already exists with the same name
			if (classContext.hasInstanceWithName(name)) {
				// append an integer value to make the name unique
				String newName;
				int i = 1;
				do {
					newName = NameConverter.standard.toPropertyName(name + Integer.toString(i));
					i++;
				} while (classContext.hasInstanceWithName(newName));
				name = newName;
			}

			this.propertyName = name;
		}
		return this.propertyName;
	}

	public String getVariableName() {
		if (this.variableName == null) {
			this.variableName = NameConverter.standard.toVariableName("_" + getPropertyName());
		}
		return this.variableName;
	}

	protected abstract JavaType getJavaType();

	protected abstract String getInstanceName();

	public Set<String> getImports() {
		Set<String> retval = new HashSet<>(getJavaType().getImports(getClassContext()));
		retval.add("com.fasterxml.jackson.annotation.*");
		retval.add("javax.xml.bind.annotation.*");
		return Collections.unmodifiableSet(retval);
	}

	public void writeVariable(PrintWriter writer) {
		writeVariableJavadoc(writer);
		writeVariableAnnotations(writer);
		writeVariableJava(writer);
	}

	protected void writeVariableJavadoc(PrintWriter writer) {
		MarkupString description = getDescription();
		if (description != null) {
			writer.println("\t/**");
			writer.println("\t * " + description.toHTML());
			writer.println("\t */");
		}
	}
	protected abstract void writeVariableAnnotations(PrintWriter writer);

	protected void writeVariableJava(PrintWriter writer) {
		ClassUtils.writeVariable(writer, getJavaType().getType(getClassContext()), getVariableName());
	}

	public void writeGetter(PrintWriter writer) {
		ClassUtils.writeGetter(writer, getPropertyName(), getJavaType().getType(getClassContext()), getVariableName());
	}

	public void writeSetter(PrintWriter writer) {
		ClassUtils.writeSetter(writer, getPropertyName(), getJavaType().getType(getClassContext()), getVariableName());
	}

}
