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
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractJavaType implements JavaType {

  public AbstractJavaType() {
  }

  @Override
  public abstract String getClassName();

  @Override
  public abstract String getPackageName();

  @Override
  public abstract String getQualifiedClassName();

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public String getType() {
    return getQualifiedClassName();
  }

  @Override
  public String getType(Function<String, Boolean> clashEvaluator) {
    String retval;
    if (clashEvaluator.apply(this.getClassName())) {
      // qualify the type
      retval = getQualifiedClassName();
    } else {
      // use import
      retval = getClassName();
    }
    return retval;
  }

  public String getType(JavaType classType) {
    String retval;
    if (getClassName().equals(classType.getClassName())) {
      // qualify the type
      retval = getQualifiedClassName();
    } else {
      // use import
      retval = getClassName();
    }
    return retval;
  }

  @Override
  public Set<JavaType> getImports(JavaType classType) {
    return Collections.singleton(this);
  }

  @Override
  public String getImportValue(JavaType classJavaType) {
    // check if the class name is the same as the containing class, if not add an import
    if (!getClassName().equals(classJavaType.getClassName())) {
      return getQualifiedClassName();
    }
    return null;
  }

}
