package gov.nist.secauto.metaschema.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.binding.annotations.Collapsible;
import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
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
	protected void buildClass(ClassBuilder builder) {
		super.buildClass(builder);

		builder.annotation(Collapsible.class);
//		FieldValueInstanceGenerator fieldInsatnce = getFieldInstance();
//		// no-arg constructor
//		writer.println("\t@JsonCreator");
//		writer.printf("\tpublic %s(%s value) {%n", getJavaType().getClassName(), fieldInsatnce.getJavaType().getType(getJavaType()));
//		writer.printf("\t\tthis.%s = value;%n", fieldInsatnce.getVariableName());
//		writer.println("\t}");
//		writer.println();
	}
}