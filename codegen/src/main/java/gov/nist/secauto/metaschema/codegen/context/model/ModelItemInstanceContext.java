package gov.nist.secauto.metaschema.codegen.context.model;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.context.InstanceContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;

public interface ModelItemInstanceContext extends InstanceContext {

	JavaType getJavaType();
	String getInstanceName();
	Object getXmlNamespace();
	void writeVariableAnnotations(PrintWriter writer);
	Class<?> getSerializerClass();
	Class<?> getDeserializerClass();
}
