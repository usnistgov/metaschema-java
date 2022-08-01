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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.binding.model.AbstractBoundMetaschema;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This annotation indicates that the target class represents a Metaschema field.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetaschemaField {
  /**
   * Get the documentary formal name of the field.
   * <p>
   * If the value is "##none", then the description will be considered {@code null}.
   * 
   * @return a markdown string or {@code "##none"} if no formal name is provided
   */
  @NonNull
  String formalName() default "##none";

  /**
   * Get the documentary description of the field.
   * <p>
   * If the value is "##none", then the description will be considered {@code null}.
   * 
   * @return a markdown string or {@code "##none"} if no description is provided
   */
  @NonNull
  String description() default "##none";

  /**
   * Name of the field.
   * 
   * @return the name
   */
  @NonNull
  String name();

  /**
   * Get the metaschema class that "owns" this assembly, which is the concrete implementation of the
   * metaschema containing the assembly.
   * 
   * @return the metaschema class
   */
  @NonNull
  Class<? extends AbstractBoundMetaschema> metaschema();

  /**
   * Defines if the annotated class representing a Metaschema BoundField is collapsible, meaning
   * fields with the same set of flag/values can be combined together, with an array of values
   * containing the combination of corresponding field values.
   * <p>
   * For a Metaschema field to be collapsible, it must have flags, otherwise the collapsible status is
   * ignored.
   * 
   * @return {@code true} if the field is collapsible, or {@code false} otherwise
   */
  boolean isCollapsible() default false;

  /**
   * Get the allowed value constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NonNull
  AllowedValues[] allowedValues() default {};

  /**
   * Get the matches constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NonNull
  Matches[] matches() default {};

  /**
   * Get the index-has-key constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NonNull
  IndexHasKey[] indexHasKey() default {};

  /**
   * Get the expect constraints for this field.
   * 
   * @return the expected constraints or an empty array if no expected constraints are defined
   */
  @NonNull
  Expect[] expect() default {};

  /**
   * Get any remarks for this field.
   * 
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NonNull
  String remarks() default "##none";
}
