package gov.nist.secauto.metaschema.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKey;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.JsonValueKeyEnum;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public class FlagInstanceGenerator extends AbstractInstanceGenerator<AbstractClassGenerator<?>> {
	private static final Logger logger = LogManager.getLogger(FlagInstanceGenerator.class);

	private final FlagInstance instance;
	private final DataType dataType;

	public FlagInstanceGenerator(FlagInstance instance, AbstractClassGenerator<?> classContext) {
		super(classContext);
		this.instance = instance;

		gov.nist.secauto.metaschema.model.info.definitions.DataType type = instance.getDatatype();
		DataType e = DataType.lookupByDatatype(type);
		if (e == null) {
			logger.warn("Unsupported datatype '{}', using String", type);
			e = DataType.STRING;
		}
		this.dataType = e;
	}

	protected FlagInstance getInstance() {
		return instance;
	}

	public DataType getDataType() {
		return dataType;
	}

	@Override
	protected String getInstanceName() {
		return instance.getName();
	}

	@Override
	protected JavaType getJavaType() {
		return getDataType().getJavaType();
	}

	@Override
	public MarkupLine getDescription() {
		return instance.getDescription();
	}

	public boolean isJsonValueKeyFlag() {
		FlagInstance instance = getInstance();
		ManagedObject parent = instance.getContainingDefinition();
		FieldDefinition parentField = (FieldDefinition)parent;
		return parentField.hasJsonValueKey() && JsonValueKeyEnum.FLAG.equals(parentField.getJsonValueKeyType()) && getInstance().equals(parentField.getJsonValueKeyFlagInstance());
	}

	@Override
	protected void buildField(FieldBuilder builder) {
		StringBuilder arguments = new StringBuilder();
		arguments.append("name = \"");
		arguments.append(instance.getName());
		arguments.append('"');
		if (getInstance().isRequired()) {
			arguments.append(", required = true");
		}

		builder.annotation(Flag.class, arguments.toString());

		FlagInstance instance = getInstance();
		ManagedObject parent = instance.getContainingDefinition();
		if (parent.hasJsonKey() && instance.equals(parent.getJsonKeyFlagInstance())) {
			builder.annotation(JsonKey.class);
		}

		if (parent instanceof FieldDefinition) {
			if (isJsonValueKeyFlag()) {
				builder.annotation(JsonFieldValueKey.class);
			}
		}
		
	}

}
