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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractCollectionJavaType extends AbstractJavaType {
  private final JavaType collectionClass;
  private final JavaType valueClass;

  public AbstractCollectionJavaType(Class<?> collectionClass, JavaType valueClass) {
    Objects.requireNonNull(collectionClass, "collectionClass");
    Objects.requireNonNull(valueClass, "itemClass");
    this.collectionClass = new ClassJavaType(collectionClass);
    this.valueClass = valueClass;
  }

  protected JavaType getCollectionClass() {
    return collectionClass;
  }

  protected JavaType getValueClass() {
    return valueClass;
  }

  @Override
  public String getType(Function<String, Boolean> clashEvaluator) {
    return String.format("%s<%s>", super.getType(clashEvaluator), getGenericArguments(clashEvaluator));
  }

  @Override
  public Set<JavaType> getImports(JavaType classType) {
    Set<JavaType> retval = new HashSet<>(super.getImports(classType));
    retval.addAll(getValueClass().getImports(classType));
    retval.addAll(getCollectionClass().getImports(classType));
    return Collections.unmodifiableSet(retval);
  }

  protected abstract Object getGenericArguments(Function<String, Boolean> clashEvaluator);

  @Override
  public String getClassName() {
    return getCollectionClass().getClassName();
  }

  @Override
  public String getPackageName() {
    return getCollectionClass().getPackageName();
  }

  @Override
  public String getQualifiedClassName() {
    return getCollectionClass().getQualifiedClassName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + collectionClass.hashCode();
    result = prime * result + valueClass.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AbstractCollectionJavaType)) {
      return false;
    }
    AbstractCollectionJavaType other = (AbstractCollectionJavaType) obj;
    if (!collectionClass.equals(other.collectionClass)) {
      return false;
    }
    if (!valueClass.equals(other.valueClass)) {
      return false;
    }
    return true;
  }

}
