package gov.nist.secauto.metaschema.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.annotations.FieldValue;
import gov.nist.secauto.metaschema.markup.MarkupString;

/**
 * Represents the "value" of a field object.
 * 
 * @author davidwal
 *
 */
public class FieldValueInstanceGenerator extends AbstractInstanceGenerator<FieldClassGenerator> {
	private static final Logger logger = LogManager.getLogger(FieldValueInstanceGenerator.class);

	private FieldClassGenerator generator;

	public FieldValueInstanceGenerator(FieldClassGenerator generator, FieldClassGenerator classContext) {
		super(classContext);
		this.generator = generator;
	}

	protected FieldClassGenerator getGenerator() {
		return generator;
	}

	private DataType getValueDataType() {
		return getGenerator().getValueDatatype();
	}

	@Override
	public JavaType getJavaType() {
		return getValueDataType().getJavaType();
	}

	@Override
	protected String getInstanceName() {
		return "value";
	}

	@Override
	public MarkupString getDescription() {
		return getGenerator().getDefinition().getDescription();
	}

	protected String getJsonPropertyName() {
		String retval = getGenerator().getDefinition().getJsonValueKeyName();
		if (retval == null) {
			throw new RuntimeException("Unable to determine property name");
		}
		return retval;
	}

	@Override
	protected void buildField(FieldBuilder builder) {
		// a field object always has a single value
		if (DataType.EMPTY.equals(getGenerator().getValueDatatype())) {
			String msg = String.format("In class '%s', the field has an empty value, but an instance was generated", getGenerator().getJavaType().getQualifiedClassName());
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		builder.annotation(FieldValue.class);
	}
}
