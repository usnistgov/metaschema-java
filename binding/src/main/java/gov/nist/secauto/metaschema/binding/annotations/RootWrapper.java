package gov.nist.secauto.metaschema.binding.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RootWrapper {
    /**
     * The root's namespace for use in XML.
     */
    String namespace() default "##default";

    /**
     * The root's name.
     */
    String name() default "##default";

    /**
     * A list of other properties at the root level to ignore.
     */
    public String[] ignoreJsonProperties() default {}; 
}
