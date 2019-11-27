package gov.nist.secauto.metaschema.codegen.context;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.FieldClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.FieldDefinition;
import gov.nist.secauto.metaschema.model.FlagInstance;

/**
 * Represents the "value" of a field object.
 * @author davidwal
 *
 */
public class FieldValueInstanceContext extends AbstractInstanceContext<FieldClassGenerator> {
	private static final Logger logger = LogManager.getLogger(FieldValueInstanceContext.class);

	private FieldClassGenerator generator;

	public FieldValueInstanceContext(FieldClassGenerator generator, FieldClassGenerator classContext) {
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

	public JsonValueKeyEnum getJsonValueKeyType() {
		JsonValueKeyEnum retval = JsonValueKeyEnum.NONE;
		FieldDefinition definition = getGenerator().getDefinition();
		if (definition.hasJsonValueKey()) {
			FlagInstance valueKeyFlag = definition.getJsonValueKeyFlagInstance();
			if (valueKeyFlag != null) {
				retval = JsonValueKeyEnum.FLAG;
			} else {
				retval = JsonValueKeyEnum.LABEL;
			}
		}
		return retval;
	}

	@Override
	public Set<String> getImports() {
		Set<String> retval = new HashSet<>(super.getImports());
		if (!JsonValueKeyEnum.FLAG.equals(getJsonValueKeyType())) {
			boolean addDatabind = false;

			Class<?> serializer = getValueDataType().getSerializerClass();
			if (serializer != null) {
				retval.add(serializer.getCanonicalName());
				addDatabind = true;
			}

			Class<?> deserializer = getValueDataType().getDeserializerClass();
			if (deserializer != null) {
				retval.add(deserializer.getCanonicalName());
				addDatabind = true;
			}

			if (addDatabind) {
				retval.add("com.fasterxml.jackson.databind.annotation.*");
			}
		}
		return Collections.unmodifiableSet(retval);
	}

	@Override
	protected void writeVariableAnnotations(PrintWriter writer) {
		// --- JSON ---
		// a field object always has a single value
		if (DataType.EMPTY.equals(getGenerator().getValueDatatype())) {
			String msg = String.format("In class '%s', the field has an empty value, but an instance was generated", generator.getQualifiedClassName());
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		// if the value key type is "FLAG", we need to use the any getter/setter, so ignore this field
		boolean useJsonPropertyAnnotations = !JsonValueKeyEnum.FLAG.equals(getJsonValueKeyType());

		if (useJsonPropertyAnnotations) {
			// we need to use an argument constructor
			Class<?> serializer = getValueDataType().getSerializerClass();
			if (serializer != null) {
				writer.printf("\t@JsonSerialize(using = %s.class)%n", serializer.getSimpleName());
			}

			Class<?> deserializer = getValueDataType().getDeserializerClass();
			if (deserializer != null) {
				writer.printf("\t@JsonDeserialize(using = %s.class)%n", deserializer.getSimpleName());
			}
			writer.printf("\t@JsonProperty(value = \"%s\", required = true)%n", getJsonPropertyName());
		} else {
			writer.println("\t@JsonIgnore");
		}
	}
}
