package gov.nist.secauto.metaschema.codegen.item;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class DataTypeInstanceItemContext extends AbstractInstanceItemContext<FieldInstance> {
	private final DataType dataType;

	public DataTypeInstanceItemContext(FieldInstance instance, AssemblyClassGenerator classContext, DataType dataType) {
		super(instance, classContext);
		this.dataType = dataType;
	}

	public DataType getDataType() {
		return dataType;
	}

	@Override
	public JavaType getJavaType() {
		return getDataType().getJavaType();
	}

	@Override
	public Class<? extends JsonSerializer<?>> getJsonSerializerClass() {
		return getDataType().getJsonSerializerClass();
	}

	@Override
	public Class<? extends JsonDeserializer<?>> getJsonDeserializerClass() {
		return getDataType().getJsonDeserializerClass();
	}
	
}
