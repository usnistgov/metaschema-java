package gov.nist.secauto.metaschema.codegen.context.model.item;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class DataTypeItemInstance extends AbstractModelItemInstanceContext<FieldInstance> {
	private final DataType dataType;

	public DataTypeItemInstance(FieldInstance instance, AssemblyClassGenerator classContext, DataType dataType) {
		super(instance, classContext);
		this.dataType = dataType;
	}

	protected DataType getDataType() {
		return dataType;
	}

	@Override
	public JavaType getJavaType() {
		return getDataType().getJavaType();
	}

	@Override
	public Class<?> getSerializerClass() {
		return getDataType().getSerializerClass();
	}

	@Override
	public Class<?> getDeserializerClass() {
		return getDataType().getDeserializerClass();
	}

}
