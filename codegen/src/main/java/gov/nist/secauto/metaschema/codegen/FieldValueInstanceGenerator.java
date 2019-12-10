package gov.nist.secauto.metaschema.codegen;

import javax.xml.bind.annotation.XmlValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.support.JsonSerializationSupport;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.JsonValueKeyEnum;

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
		// TODO Auto-generated method stub
		super.buildField(builder);

		// --- JSON ---
		// a field object always has a single value
		if (DataType.EMPTY.equals(getGenerator().getValueDatatype())) {
			String msg = String.format("In class '%s', the field has an empty value, but an instance was generated", getGenerator().getJavaType().getQualifiedClassName());
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		// if the value key type is "FLAG", we need to use the any getter/setter, so ignore this field
		boolean useJsonPropertyAnnotations = !JsonValueKeyEnum.FLAG.equals(getGenerator().getDefinition().getJsonValueKeyType());
		if (useJsonPropertyAnnotations) {
			builder.annotation(JsonProperty.class,String.format("value = \"%s\", required = true", getJsonPropertyName()));

			// we need to use an argument constructor
			JsonSerializationSupport.generateJsonSerializerAnnotations(builder, getValueDataType().getJsonSerializerClass());
			JsonSerializationSupport.generateJsonDeserializerAnnotations(builder, getValueDataType().getJsonDeserializerClass());
		} else {
			builder.annotation(JsonIgnore.class);
		}

		builder.annotation(XmlValue.class);
	}
}
