package gov.nist.secauto.metaschema.codegen.context.model;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class SimpleModelInstanceContext extends AbstractModelInstanceContext<JavaType> implements ModelInstanceContext {

	public SimpleModelInstanceContext(ModelItemInstanceContext itemInstanceContext, AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator, itemInstanceContext.getJavaType());
	}

	@Override
	public Set<String> getImports() {
		Set<String> retval = new HashSet<>(super.getImports());
		boolean addDatabind = false;

		Class<?> serializer = getItemInstanceContext().getSerializerClass();
		if (serializer != null) {
			retval.add(serializer.getCanonicalName());
			addDatabind = true;
		}

		Class<?> deserializer = getItemInstanceContext().getDeserializerClass();
		if (deserializer != null) {
			retval.add(deserializer.getCanonicalName());
			addDatabind = true;
		}

		if (addDatabind) {
			retval.add("com.fasterxml.jackson.databind.annotation.*");
		}
		return Collections.unmodifiableSet(retval);
	}

	@Override
	protected void writeVariableAnnotations(PrintWriter writer) {
		super.writeVariableAnnotations(writer);

		// --- JSON ---
		Class<?> serializer = getItemInstanceContext().getSerializerClass();
		if (serializer != null) {
			writer.printf("\t@JsonSerialize(using = %s.class)%n", serializer.getSimpleName());
		}

		Class<?> deserializer = getItemInstanceContext().getDeserializerClass();
		if (deserializer != null) {
			writer.printf("\t@JsonDeserialize(using = %s.class)%n", deserializer.getSimpleName());
		}
		writer.printf("\t@JsonProperty(value = \"%s\", required = true)%n", getInstanceName());
	}

}
