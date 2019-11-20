package gov.nist.secauto.metaschema.codegen.context;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.codegen.ClassUtils;
import gov.nist.secauto.metaschema.codegen.InstanceGenerator;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractInstanceContext<I extends InstanceGenerator> {
	private final I instanceGenerator;
	private final ClassContext classContext;
	private final String propertyName;
	private final String variableName;
	private final String javaType;
	private final Set<String> imports = new HashSet<>();

	public AbstractInstanceContext(I instanceGenerator, ClassContext classContext) {
		this.instanceGenerator = instanceGenerator;
		this.classContext = classContext;

		String name = NameConverter.standard.toPropertyName(instanceGenerator.getName());
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
		this.variableName = NameConverter.standard.toVariableName("_"+name);
		this.javaType = createJavaType();
	}

	protected I getInstanceGenerator() {
		return instanceGenerator;
	}

	protected ClassContext getClassContext() {
		return classContext;
	}

	/**
	 * The property name of the instance, which must be unique within the class.
	 * @return the name
	 */
	public String getPropertyName() {
		return propertyName;
	}

	protected String getVariableName() {
		return variableName;
	}

	protected String getJavaType() {
		return javaType;
	}

	public Set<String> getImports() {
		return Collections.unmodifiableSet(imports);
	}

	protected void addImport(String importEntry) {
		imports.add(importEntry);
	}

	protected String createJavaType() {
		String javaType = getInstanceGenerator().getJavaType();
		if (javaType != null) {
			boolean sameNameAsClass = javaType.equals(getClassContext().getClassName());
			
			if (!sameNameAsClass) {
				addImport(getInstanceGenerator().getJavaTypePackage() + "." + javaType);
			} else {
				// name conflict, use a fully qualified type
				javaType = getInstanceGenerator().getJavaTypePackage() + "." + javaType;
			}
		}
		return javaType;
	}

	protected String getActualJavaType() {
		return getJavaType();
	}

	public void writeVariable(PrintWriter writer) {
		MarkupString description = getInstanceGenerator().getDescription();
		if (description != null) {
			writer.println("\t/**");
			writer.println("\t * "+description.toHTML());
			writer.println("\t */");
		}
		ClassUtils.writeVariable(writer, getActualJavaType(), getVariableName());
	}

	public void writeGetter(PrintWriter writer) {
		ClassUtils.writeGetter(writer, getPropertyName(), getActualJavaType(), getVariableName());
	}

	public void writeSetter(PrintWriter writer) {
		ClassUtils.writeSetter(writer, getPropertyName(), getActualJavaType(), getVariableName());
	}

}
