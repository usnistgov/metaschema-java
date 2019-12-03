package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.model.FieldDefinition;

public class FieldClassGenerator extends AbstractClassGenerator<FieldDefinition> {
	private static final Logger logger = LogManager.getLogger(FieldClassGenerator.class);
	private final FieldValueInstanceGenerator fieldInstance;

	public FieldClassGenerator(FieldDefinition definition) {
		super(definition);
		if (!DataType.EMPTY.equals(getValueDatatype())) {
			this.fieldInstance = newFieldInstance(this);
		} else {
			this.fieldInstance = null;
		}
	}

	public FieldValueInstanceGenerator newFieldInstance(FieldClassGenerator field) {
		FieldValueInstanceGenerator context = new FieldValueInstanceGenerator(field, this);
		addInstance(context);
		return context;
	}

	public DataType getValueDatatype() {
		gov.nist.secauto.metaschema.model.DataType type = getDefinition().getDatatype();
		DataType e = DataType.lookupByDatatype(type);
		if (e == null) {
			logger.warn("Unsupported datatype '{}', using String", type);
			e = DataType.STRING;
		}
		return e;
	}

	public FieldValueInstanceGenerator getFieldInstance() {
		return fieldInstance;
	}

	@Override
	public String getPackageName() {
		return ClassUtils.toPackageName(getDefinition());
	}

	@Override
	public String getClassName() {
		return ClassUtils.toClassName(getDefinition());
	}

	@Override
	protected void writeConstructors(PrintWriter writer) {
		super.writeConstructors(writer);

		FieldValueInstanceGenerator fieldInsatnce = getFieldInstance();
		// no-arg constructor
		writer.println("\t@JsonCreator");
		writer.printf("\tpublic %s(%s value) {%n", getClassName(), fieldInsatnce.getJavaType().getType(this));
		writer.printf("\t\tthis.%s = value;%n", fieldInsatnce.getVariableName());
		writer.println("\t}");
		writer.println();
	}
}