package gov.nist.secauto.metaschema.binding.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Flag {
	/**
	 * Name of the XML Schema element.
	 * <p>
	 * If the value is "##default", then element name is derived from the JavaBean
	 * property name.
	 */
	String name() default "##default";

	/**
	 * XML target namespace of the XML Schema element.
	 */
	String namespace() default "##default";

	/**
	 * Default value of this element.
	 *
	 * <p>
	 * The
	 * 
	 * <pre>
	 * '\u0000'
	 * </pre>
	 * 
	 * value specified as a default of this annotation element is used as a
	 * poor-man's substitute for null to allow implementations to recognize the 'no
	 * default value' state.
	 */
	String defaultValue() default "\u0000";

    /**
	 * Specifies if the XML Schema attribute is optional or required. If true, then
	 * the JavaBean property is mapped to a XML Schema attribute that is required.
	 * Otherwise it is mapped to a XML Schema attribute that is optional.
	 *
	 */
	boolean required() default false;
}
