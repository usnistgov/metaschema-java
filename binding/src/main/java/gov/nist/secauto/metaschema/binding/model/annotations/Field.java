package gov.nist.secauto.metaschema.binding.model.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Field {
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
	/**
	 * If the data type allows it, determines if the field's value must be wrapped with an element having the specified {@link #name()} and {@link #namespace()}.
	 * @return
	 */
	boolean inXmlWrapped() default true;
}
