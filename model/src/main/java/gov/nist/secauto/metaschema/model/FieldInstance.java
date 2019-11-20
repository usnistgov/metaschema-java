package gov.nist.secauto.metaschema.model;

public interface FieldInstance extends ModelInstance, Field {
	/**
	 * Retrieve the definition of the field for this instance. A field can be
	 * locally or globally defined. In the former case {@link #isReference()} will
	 * be {@code false}, and in the latter case {@link #isReference()} will be
	 * {@code true}.
	 * 
	 * @return the corresponding field definition
	 */
	FieldDefinition getFieldDefinition();
}
