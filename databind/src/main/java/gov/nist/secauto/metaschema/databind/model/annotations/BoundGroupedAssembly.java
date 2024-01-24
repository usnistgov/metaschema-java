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

package gov.nist.secauto.metaschema.databind.model.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Identifies that the annotation target is a bound property that references a
 * Module assembly.
 * <p>
 * For XML serialization, the {@link #useName()} identifies the name of the
 * element to use and the {@link #namespace()} identifies the namespace of this
 * element.
 * <p>
 * For JSON and YAML serializations, the {@link #useName()} identifies the
 * property/item name to use.
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
public @interface BoundGroupedAssembly {
  /**
   * Get the documentary formal name of the assembly.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a markdown string or {@code "##none"} if no formal name is provided
   */
  @NonNull
  String formalName() default ModelUtil.NO_STRING_VALUE;

  /**
   * Get the documentary description of the assembly.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a markdown string or {@code "##none"} if no description is provided
   */
  @NonNull
  String description() default ModelUtil.NO_STRING_VALUE;

  /**
   * The model name to use for singleton values. This name will be used for
   * associated XML elements.
   * <p>
   * If the value is "##none", then element name is derived from the JavaBean
   * property name.
   *
   * @return the name or {@code "##none"} if no use name is provided
   */
  @NonNull
  String useName() default ModelUtil.NO_STRING_VALUE;

  /**
   * The binary use name of the assembly.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no use name.
   *
   * @return the index value
   */
  int useIndex() default Integer.MIN_VALUE;

  /**
   * The namespace to use for associated XML elements.
   * <p>
   * If the value is "##default", then element name is derived from the namespace
   * provided in the package-info.
   *
   * @return the namespace
   */
  @NonNull
  String namespace() default ModelUtil.DEFAULT_STRING_VALUE;

  /**
   * Get any remarks for this field.
   *
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NonNull
  String remarks() default ModelUtil.NO_STRING_VALUE;

  /**
   * Get any remarks for this field.
   *
   * @return the discriminator string or {@code "##none"} if no discriminator is
   *         provided
   */
  @NonNull
  String discriminatorValue() default ModelUtil.NO_STRING_VALUE;

  /**
   * The bound class associated with this assembly.
   * <p>
   * This is optional when used on a field or method, and required when used with
   * an annotation type value, i.e. {@link BoundChoiceGroup}.
   *
   * @return the bound class
   */
  @NonNull
  Class<?> binding();
}
