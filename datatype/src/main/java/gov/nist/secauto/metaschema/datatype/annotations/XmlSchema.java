package gov.nist.secauto.metaschema.datatype.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(PACKAGE)
public @interface XmlSchema {
	/**
	 * Defines the XML namespace prefix to URI bindings to use for this model. If
	 * not provided, the XML prefixes will be auto-generated.
	 */
	XmlNs[] xmlns() default {};

	/**
	 * Name of the XML namespace.
	 */
	String namespace() default "";

	/**
	 * The location of the associated XML schema.
	 */
	String xmlSchemaLocation() default NO_LOCATION;

	/**
	 * The location of the associated JSON schema.
	 */
	String jsonSchemaLocation() default NO_LOCATION;

	XmlNsForm xmlElementFormDefault() default XmlNsForm.UNSET;
	XmlNsForm xmlAttributeFormDefault() default XmlNsForm.UNSET;

    /**
     * The default value of the {@link #location()}, which indicates that no schema will be associated.
     */
    // the actual value is chosen because ## is not a valid
    // sequence in xs:anyURI.
    static final String NO_LOCATION = "##none";
}
