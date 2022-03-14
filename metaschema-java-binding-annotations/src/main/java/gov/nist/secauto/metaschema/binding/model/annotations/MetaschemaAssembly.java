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

import gov.nist.secauto.metaschema.binding.model.annotations.constraint.AllowedValues;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Expect;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.HasCardinality;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Index;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IndexHasKey;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.IsUnique;
import gov.nist.secauto.metaschema.binding.model.annotations.constraint.Matches;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the target class represents a Metaschema assembly.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetaschemaAssembly {
  /**
   * Name of the root XML element or the JSON/YAML property.
   * <p>
   * If the value is "##none", then there is no root name.
   * 
   * @return the name
   */
  String rootName() default "##none";

  /**
   * XML target namespace of the XML element.
   * <p>
   * If the value is "##default", then namespace is derived from the namespace provided in the
   * package-info.
   * 
   * @return the namespace
   */
  String rootNamespace() default "##default";

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
