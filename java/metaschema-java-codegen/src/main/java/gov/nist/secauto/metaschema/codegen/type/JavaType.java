/**
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

package gov.nist.secauto.metaschema.codegen.type;

import java.util.Set;
import java.util.function.Function;

public interface JavaType {
  /**
   * Gets the simple, unqualified class name for the Java type.
   * 
   * @return the class name
   */
  String getClassName();

  /**
   * Get the package name for the Java type.
   * 
   * @return the package name
   */
  String getPackageName();

  /**
   * Gets the qualified class name.
   * 
   * @return the qualified class name
   */
  String getQualifiedClassName();

  /**
   * Get the actual Java type. This will qualify the Java type if it has the same name as the
   * containing class, or will add an import otherwise.
   * 
   * @param clashEvaluator
   *          a functional interface used to determine if this Java type clashes with another in the
   *          this types context
   * @return the type name
   */
  /**
   * Gets the name of the type to use in generated code.
   * <p>
   * The clashEvaluator can be used to detect naming clashes between the unqualified type name of the
   * containing class and this type's unqualified name. When a clash is detected, then the qualified
   * name will be used.
   * 
   * @param clashEvaluator
   *          used to detect naming clashes
   * @return the Java type name
   */
  String getType(Function<String, Boolean> clashEvaluator);

  String getType();

  /**
   * Get the set of imports needed for this Java type.
   * 
   * @param classType
   *          the Java type of the containing class
   * @return the set of Java types to be imported
   */
  Set<JavaType> getImports(JavaType classType);

  /**
   * Get the import value for this Java type.
   * 
   * @param classJavaType
   *          the Java type of the containing class
   * @return the import value or {@code null} if no import is needed
   */
  String getImportValue(JavaType classJavaType);

  @Override
  int hashCode();

  @Override
  boolean equals(Object obj);

  public static JavaType create(Class<?> clazz) {
    return new ClassJavaType(clazz);
  }

  public static JavaType create(String packageName, String className) {
    return new SimpleJavaType(packageName, className);
  }

  public static ListJavaType createGenericList(Class<?> valueClass) {
    return new ListJavaType(create(valueClass));
  }

  public static ListJavaType createGenericList(JavaType valueType) {
    return new ListJavaType(valueType);
  }

  public static MapJavaType createGenericMap(Class<?> keyClass, Class<?> valueClass) {
    return new MapJavaType(create(keyClass), create(valueClass));
  }

  public static MapJavaType createGenericMap(JavaType keyClass, JavaType valueClass) {
    return new MapJavaType(keyClass, valueClass);
  }
}
