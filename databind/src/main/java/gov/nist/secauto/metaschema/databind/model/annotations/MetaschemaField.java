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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This annotation indicates that the target class represents a Module field.
 * <p>
 * Classes with this annotation must have a field with the
 * {@link BoundFieldValue} annotation.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetaschemaField {
  /**
   * Get the documentary formal name of the field.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a markdown string or {@code "##none"} if no formal name is provided
   */
  @NonNull
  String formalName() default ModelUtil.NO_STRING_VALUE;

  /**
   * Get the documentary description of the field.
   * <p>
   * If the value is "##none", then the description will be considered
   * {@code null}.
   *
   * @return a markdown string or {@code "##none"} if no description is provided
   */
  @NonNull
  String description() default ModelUtil.NO_STRING_VALUE;

  /**
   * Name of the field.
   *
   * @return the name
   */
  @NonNull
  String name();

  /**
   * The binary name of the assembly.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no index.
   *
   * @return the index value
   */
  int index() default Integer.MIN_VALUE;

  /**
   * Get the name to use for data instances of this field.
   * <p>
   * This overrides the name provided by {@link #name()}.
   * <p>
   * The value {@link ModelUtil#NO_STRING_VALUE} indicates that there is no use
   * name.
   *
   *
   * @return the use name or {@link ModelUtil#NO_STRING_VALUE} if no use name is
   *         provided
   */
  @NonNull
  String useName() default ModelUtil.NO_STRING_VALUE;

  /**
   * The binary use name of the assembly.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no index.
   *
   * @return the index value or {@link Integer#MIN_VALUE} if there is no index
   *         value
   */
  int useIndex() default Integer.MIN_VALUE;

  /**
   * Get the metaschema class that "owns" this assembly, which is the concrete
   * implementation of the metaschema containing the assembly.
   *
   * @return the class that extends {@link IBoundModule}
   */
  @NonNull
  Class<? extends IBoundModule> moduleClass();

  /**
   * If the data type allows it, determines if the field's value must be wrapped
   * with an XML element whose name is the specified {@link #name()} and namespace
   * is derived from the namespace of the instance.
   *
   * @return {@code true} if the field must be wrapped, or {@code false} otherwise
   */
  boolean inXmlWrapped() default IFieldInstance.DEFAULT_FIELD_IN_XML_WRAPPED;

  /**
   * Get any remarks for this field.
   *
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NonNull
  String remarks() default ModelUtil.NO_STRING_VALUE;

  /**
   * Get the value constraints defined for this Metaschema field definition.
   *
   * @return the value constraints
   */
  ValueConstraints valueConstraints() default @ValueConstraints;
}
