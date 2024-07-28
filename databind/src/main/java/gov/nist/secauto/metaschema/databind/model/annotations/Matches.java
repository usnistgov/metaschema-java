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
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This annotation defines a rule that requires matching patterns and/or data
 * types among the target contents of the assembly represented by the containing
 * {@link MetaschemaAssembly} annotation.
 */
@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface Matches {
  /**
   * An optional identifier for the constraint, which must be unique to only this
   * constraint.
   *
   * @return the identifier if provided or an empty string otherwise
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  String id() default "";

  /**
   * An optional formal name for the constraint.
   *
   * @return the formal name if provided or an empty string otherwise
   */
  @NonNull
  String formalName() default "";

  /**
   * An optional description of the constraint.
   *
   * @return the description if provided or an empty string otherwise
   */
  @NonNull
  String description() default "";

  /**
   * The significance of a violation of this constraint.
   *
   * @return the level
   */
  @NonNull
  Level level() default IConstraint.Level.ERROR;

  /**
   * An optional Metapath that points to the target flag or field value that the
   * constraint applies to. If omitted the target will be ".", which means the
   * target is the value of the {@link BoundFlag}, {@link BoundField} or
   * {@link BoundFieldValue} annotation the constraint appears on. In the prior
   * case, this annotation may only appear on a {@link BoundField} if the field
   * has no flags, which results in a {@link BoundField} annotation on a field
   * instance with a scalar, data type value.
   *
   * @return the target metapath
   */
  @NonNull
  String target() default ".";

  /**
   * An optional set of properties associated with these allowed values.
   *
   * @return the properties or an empty array with no properties
   */
  Property[] properties() default {};

  /**
   * Retrieve an optional pattern that the associated value must match. This must
   * be a pattern that can compile using {@link Pattern#compile(String)}.
   *
   * @return a pattern string or an empty string if no pattern is provided
   */
  @NonNull
  String pattern() default "";

  /**
   * The Module data type adapter for the data type that the associated value must
   * conform to.
   *
   * @return the data type adapter or a {@link NullJavaTypeAdapter} if none is
   *         provided
   */
  @NonNull
  Class<? extends IDataTypeAdapter<?>> typeAdapter() default NullJavaTypeAdapter.class;

  /**
   * The message to emit when the constraint is violated.
   *
   * @return the message or an empty string otherwise
   */
  @NonNull
  String message() default "";

  /**
   * Any remarks about the constraint, encoded as an escaped Markdown string.
   *
   * @return an encoded markdown string or an empty string if no remarks are
   *         provided
   */
  @NonNull
  String remarks() default "";
}
