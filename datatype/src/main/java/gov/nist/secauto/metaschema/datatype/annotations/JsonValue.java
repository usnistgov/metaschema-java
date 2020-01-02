package gov.nist.secauto.metaschema.datatype.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface JsonValue {
	/**
	 * The name of the JSON property that contains the field's value. If this value
	 * is provided, the the name will be used as the property name. Use of this
	 * value is mutually exclusive with the {@link JsonValueKey} annotation.
	 */
	String name() default "##default";
}
