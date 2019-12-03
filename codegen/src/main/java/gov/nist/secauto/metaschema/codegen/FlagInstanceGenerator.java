package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	protected void writeVariableAnnotations(PrintWriter writer) {
		boolean isRequired = getInstance().isRequired();
		writer.print("\t@JsonProperty(value = \"");
		writer.print(instance.getName());
		writer.print("\"");
		if (isRequired) {
			writer.print(", required = true");
		}
		writer.println(")");
//		
//		writer.print("\t@XmlAttribute(name = \"");
//		writer.print(instance.getName());
//		writer.print("\"");
//		if (isRequired) {
//			writer.print(", required = true");
//		}
//		writer.println(")");
	}

}
