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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IBoundObject;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Identifies that the annotation target is a bound property that references a
 * Module field.
 * <p>
 * For XML serialization, the {@link #useName()} identifies the name of the
 * element to use for this element.
 * <p>
 * For JSON and YAML serializations, the {@link #useName()} identifies the
 * property/item name to use.
 * <p>
 * The field must be either:
 * <ol>
 * <li>A Module data type or a collection whose item value is Module data type,
 * with a non-null {@link #typeAdapter()}.
 * <li>A type or a collection whose item value is a type based on a class with a
 * {@link MetaschemaField} annotation, with a property annotated with
 * {@link BoundFieldValue}.</li>
 * </ol>
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface BoundGroupedField {
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
   * The model name to use for JSON/YAML singleton values and associated XML
   * elements.
   * <p>
   * If the value is "##none", then the use name will be provided by the
   * definition or by the field name if the item value class is missing the
   * {@link MetaschemaField} annotation.
   *
   * @return the name
   */
  @NonNull
  String useName() default ModelUtil.NO_STRING_VALUE;

  /**
   * The binary use name of the field.
   * <p>
   * The value {@link Integer#MIN_VALUE} indicates that there is no use name.
   *
   * @return the index value
   */
  int useIndex() default Integer.MIN_VALUE;

  /**
   * The Metaschema data type adapter for the field's value.
   *
   * @return the data type adapter
   */
  @NonNull
  Class<? extends IDataTypeAdapter<?>> typeAdapter() default NullJavaTypeAdapter.class;

  /**
   * Get any remarks for this field.
   *
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NonNull
  String remarks() default ModelUtil.NO_STRING_VALUE;

  /**
   * Get the value constraints defined for this Metaschema field inline
   * definition.
   *
   * @return the value constraints
   */
  ValueConstraints valueConstraints() default @ValueConstraints;

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
  Class<? extends IBoundObject> binding();
}
