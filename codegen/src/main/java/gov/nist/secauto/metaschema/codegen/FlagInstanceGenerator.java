package gov.nist.secauto.metaschema.codegen;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.FlagInstance;

public class FlagInstanceGenerator extends AbstractInstanceGenerator<AbstractClassGenerator<?>> {
	private static final Logger logger = LogManager.getLogger(FlagInstanceGenerator.class);

	private final FlagInstance instance;
	private final DataType dataType;

	public FlagInstanceGenerator(FlagInstance instance, AbstractClassGenerator<?> classContext) {
		super(classContext);
		this.instance = instance;

		gov.nist.secauto.metaschema.model.DataType type = instance.getDatatype();
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
	public MarkupString getDescription() {
		return instance.getDescription();
	}

	@Override
	protected void buildField(FieldBuilder builder) {
		StringBuilder arguments = new StringBuilder();
		arguments.append("value = \"");
		arguments.append(instance.getName());
		arguments.append('"');
		if (getInstance().isRequired()) {
			arguments.append(", required = true");
		}

		builder.annotation(JsonProperty.class, arguments.toString());

		arguments = new StringBuilder();
		arguments.append("name = \"");
		arguments.append(instance.getName());
		arguments.append('"');
		if (getInstance().isRequired()) {
			arguments.append(", required = true");
		}
		builder.annotation(XmlAttribute.class, arguments.toString());
		super.buildField(builder);
	}

}
