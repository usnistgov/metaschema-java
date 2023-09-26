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

package gov.nist.secauto.metaschema.databind.codegen.typeinfo;

import com.squareup.javapoet.TypeName;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ITypeInfo {

  /**
   * Get the name to use for the property. If the property is a collection type,
   * then this will be the group-as name, else this will be the use name or the
   * name if not use name is set.
   *
   * @return the name
   */
  @NonNull
  String getBaseName();

  /**
   * The name to use for Java constructs that refer to the item. This is used for
   * when a field is collection-based and there is a need to refer to a single
   * item, such as in an add/remove method name.
   *
   * @return the item base name
   */
  @NonNull
  default String getItemBaseName() {
    return getBaseName();
  }

  /**
   * Get the Java property name for the property.
   *
   * @return the Java property name
   */
  @NonNull
  String getPropertyName();

  /**
   * Gets the name of the Java field for this property.
   *
   * @return the Java field name
   */
  @NonNull
  String getJavaFieldName();

  /**
   * Gets the type of the associated Java field for the property.
   *
   * @return the Java type for the field
   */
  @NonNull
  TypeName getJavaFieldType();

  /**
   * Gets the type of the property's item.
   *
   * @return the Java type for the item
   */
  @NonNull
  default TypeName getJavaItemType() {
    return getJavaFieldType();
  }
}
