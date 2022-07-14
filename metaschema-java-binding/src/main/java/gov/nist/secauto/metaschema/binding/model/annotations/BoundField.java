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
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies that the annotation target is a bound property that references a Metaschema field.
 * <p>
 * For XML serialization, the {@link #useName()} identifies the name of the element to use and the
 * {@link #namespace()} identifies the namespace of this element.
 * <p>
 * For JSON and YAML serializations, the {@link #useName()} identifies the property/item name to
 * use.
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD })
public @interface BoundField {
  /**
   * Get the documentary formal name of the field.
   * <p>
   * If the value is "##none", then the description will be considered {@code null}.
   * 
   * @return a markdown string or {@code "##none"} if no formal name is provided
   */
  @NotNull
  String formalName() default "##none";

  /**
   * Get the documentary description of the field.
   * <p>
   * If the value is "##none", then the description will be considered {@code null}.
   * 
   * @return a markdown string or {@code "##none"} if no description is provided
   */
  @NotNull
  String description() default "##none";

  /**
   * The model name to use for singleton values. This name will be used for associated XML elements.
   * <p>
   * If the value is "##default", then element name is derived from the JavaBean property name.
   * 
   * @return the name
   */
  @NotNull
  String useName() default "##default";

  /**
   */
  /**
   * The namespace to use for associated XML elements.
   * <p>
   * If the value is "##default", then element name is derived from the namespace provided in the
   * package-info.
   * 
   * @return the namespace
   */
  @NotNull
  String namespace() default "##default";

  /**
   * If the data type allows it, determines if the field's value must be wrapped with an element
   * having the specified {@link #useName()} and {@link #namespace()}.
   * 
   * @return {@code true} if the field must be wrapped, or {@code false} otherwise
   */
  boolean inXmlWrapped() default MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED;

  /**
   * The Metaschema data type adapter for the field's value.
   * 
   * @return the data type adapter
   */
  Class<? extends IDataTypeAdapter<?>> typeAdapter() default NullJavaTypeAdapter.class;
  //
  // /**
  // * The name of the JSON property that contains the field's value. If this value is provided, the
  // * name will be used as the property name. Use of this annotation is mutually exclusive with the
  // * {@link JsonFieldValueKeyFlag} annotation.
  // *
  // * @return the name
  // */
  // String valueName() default "##none";

  /**
   * The name to use for an XML element wrapper or a JSON/YAML property.
   * 
   * @return the name
   */
  @NotNull
  String groupName() default "##none";

  /**
   * XML target namespace of the XML Schema element.
   * <p>
   * If the value is "##default", then element name is derived from the namespace provided in the
   * package-info.
   * 
   * @return the namespace
   */
  @NotNull
  String groupNamespace() default "##default";

  /**
   * A non-negative number that indicates the minimum occurrence of the element.
   * 
   * @return a non-negative number
   */
  int minOccurs() default MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS;

  /**
   * A number that indicates the maximum occurrence of the element.
   * 
   * @return a positive number or {@code -1} to indicate "unbounded"
   */
  int maxOccurs() default MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS;

  /**
   * Describes how to handle collections in JSON/YAML.
   * 
   * @return the JSON collection strategy
   */
  @NotNull
  JsonGroupAsBehavior inJson() default JsonGroupAsBehavior.NONE;

  /**
   * Describes how to handle collections in XML.
   * 
   * @return the XML collection strategy
   */
  @NotNull
  XmlGroupAsBehavior inXml() default XmlGroupAsBehavior.UNGROUPED;

  /**
   * Get the allowed value constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NotNull
  AllowedValues[] allowedValues() default {};

  /**
   * Get the matches constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NotNull
  Matches[] matches() default {};

  /**
   * Get the index-has-key constraints for this field.
   * 
   * @return the allowed values or an empty array if no allowed values constraints are defined
   */
  @NotNull
  IndexHasKey[] indexHasKey() default {};

  /**
   * Get the expect constraints for this field.
   * 
   * @return the expected constraints or an empty array if no expected constraints are defined
   */
  @NotNull
  Expect[] expect() default {};

  /**
   * Get any remarks for this field.
   * 
   * @return a markdown string or {@code "##none"} if no remarks are provided
   */
  @NotNull
  String remarks() default "##none";
}
