package gov.nist.secauto.metaschema.codegen.item;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.ModelInstance;

public interface InstanceItemContext {
	ModelInstance getModelInstance();
	JavaType getJavaType();
	String getInstanceName();
	Object getXmlNamespace();
	Class<? extends JsonSerializer<?>> getJsonSerializerClass();
	Class<? extends JsonDeserializer<?>> getJsonDeserializerClass();
	MarkupString getDescription();
}
