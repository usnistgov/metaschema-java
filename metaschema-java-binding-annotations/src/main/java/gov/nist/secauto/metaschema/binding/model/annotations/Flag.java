/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.model.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Matches;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies that the annotation target is a bound property that represents a Metaschema flag.
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Flag {
  /**
   * The model name to use for singleton values. This name will be used for associated XML attributes
   * and JSON properties.
   * <p>
   * If the value is "##default", then element name is derived from the JavaBean property name.
   * 
   * @return the name
   */
  String useName() default "##default";

  /**
   * XML target namespace of the XML Schema element.
   * <p>
   * If the value is "##default", then namespace is derived from the namespace provided in the
   * package-info. If the value is "##none", the namespace will be {@code null}.
   * 
   * @return the namespace
   */
  String namespace() default "##none";

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
   * value specified as a default of this annotation element is used as a poor-man's substitute for
   * null to allow implementations to recognize the 'no default value' state.
   * 
   * @return the default value
   */
  String defaultValue() default "\u0000";

  /**
   * Specifies if the XML Schema attribute is optional or required. If true, then the JavaBean
   * property is mapped to a XML Schema attribute that is required. Otherwise it is mapped to a XML
   * Schema attribute that is optional.
   * 
   * @return {@code true} if the flag must occur, or {@code false} otherwise
   */
  boolean required() default false;

  /**
   * The Metaschema data type adapter for the field's value.
   * 
   * @return the data type adapter
   */
  Class<? extends IJavaTypeAdapter<?>> typeAdapter();

  /**
   * Get the allowed value constraints for this flag.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  AllowedValues[] allowedValues() default {};

  /**
   * Get the matches constraints for this flag.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  Matches[] matches() default {};

  /**
   * Get the index-has-key constraints for this flag.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  IndexHasKey[] indexHasKey() default {};

  /**
   * Get the expect constraints for this flag.
   * 
   * @return the expected constraints or an empty array if no expected constraints are defined
   */
  Expect[] expect() default {};
}
