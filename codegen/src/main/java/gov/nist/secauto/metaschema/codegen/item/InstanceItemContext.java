package gov.nist.secauto.metaschema.codegen.item;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public interface InstanceItemContext {

	JavaType getJavaType();
	String getInstanceName();
	Object getXmlNamespace();
	void writeVariableAnnotations(PrintWriter writer);
	Class<?> getSerializerClass();
	Class<?> getDeserializerClass();
	MarkupString getDescription();
}
