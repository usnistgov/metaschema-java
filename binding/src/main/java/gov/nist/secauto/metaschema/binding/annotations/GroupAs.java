package gov.nist.secauto.metaschema.binding.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface GroupAs {
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
	 * A non-negative number that indicates the minimum occurrence of the element.
	 */
	int minOccurs() default 0;

	/**
	 * A number that indicates the maximum occurrence of the element. The value must be a positive number or {@code -1} to indicate "unbounded".
	 * @return
	 */
	int maxOccurs() default 1;

	JsonGroupAsBehavior inJson() default JsonGroupAsBehavior.SINGLETON_OR_LIST;
	XmlGroupAsBehavior inXml() default XmlGroupAsBehavior.UNGROUPED;
}
