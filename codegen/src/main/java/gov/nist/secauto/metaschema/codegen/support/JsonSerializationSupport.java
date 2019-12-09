package gov.nist.secauto.metaschema.codegen.support;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.JavaType;

public class JsonSerializationSupport {

	private JsonSerializationSupport() {
		// disable construction
	}

	public static void generateJsonSerializerAnnotations(FieldBuilder builder, Class<? extends JsonSerializer<?>> serializerClass) {
		if (serializerClass != null) {
			JavaType type = JavaType.create(serializerClass);
			builder.annotation(JsonSerialize.class, String.format("using = %s.class", type.getType(builder.getClassBuilder().getJavaType())));
			builder.importEntry(type);
		}
	}

	public static void generateJsonDeserializerAnnotations(FieldBuilder builder, Class<? extends JsonDeserializer<?>> deserializerClass) {
		if (deserializerClass != null) {
			JavaType type = JavaType.create(deserializerClass);
			builder.annotation(JsonDeserialize.class, String.format("using = %s.class", type.getType(builder.getClassBuilder().getJavaType())));
			builder.importEntry(type);
		}
	}
}
