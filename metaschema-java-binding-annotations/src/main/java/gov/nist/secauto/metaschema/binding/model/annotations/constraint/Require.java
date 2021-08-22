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

package gov.nist.secauto.metaschema.binding.model.annotations.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface Require {
  /**
   * An optional identifier for the constraint, which must be unique to only this constraint.
   * 
   * @return the identifier if provided or an empty string otherwise
   */
  String id() default "";

  /**
   * A metapath that is expected to evaluate to {@code true} in this context for the other constraints
   * to apply.
   * 
   * @return a metapath expression
   */
  String when();

  /**
   * Any remarks about the constraint, encoded as an escaped Markdown string.
   * 
   * @return an encoded markdown string or an empty string if no remarks are provided
   */
  String remarks() default "";

  /**
   * Get the allowed value constraints for this assembly.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  AllowedValues[] allowedValues() default {};

  /**
   * Get the matches constraints for this assembly.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  Matches[] matches() default {};

  /**
   * Get the index-has-key constraints for this assembly.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  IndexHasKey[] indexHasKey() default {};

  /**
   * Get the expect constraints for this assembly.
   * 
   * @return the expected constraints or an empty array if no expected constraints are defined
   */
  Expect[] expect() default {};

  /**
   * Get the index constraints for this assembly.
   * 
   * @return the index constraints or an empty array if no index constraints are defined
   */
  Index[] index() default {};

  /**
   * Get the unique constraints for this assembly.
   * 
   * @return the unique constraints or an empty array if no unique constraints are defined
   */
  IsUnique[] isUnique() default {};

  /**
   * Get the cardinality constraints for this assembly.
   * 
   * @return the cardinality constraints or an empty array if no cardinality constraints are defined
   */
  HasCardinality[] hasCardinality() default {};
}
