package gov.nist.secauto.metaschema.binding.model.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies that the target is a bound property that references an assembly.
 * <p>
 * For XML serialization, the {@link #name()} identifies the name of the element to use and the {@link #namespace()} identifies the namespace of this element.
 * <p>
 * For JSON and YAML serialization, the {@link #name()} identifies the field name to use.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Assembly {
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
	 * Specifies if the field must occur, {@code true}, or {@code false} otherwise.
	 */
	boolean required() default false;
}
