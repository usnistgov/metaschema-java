/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.codegen.type;

import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;

import java.util.Objects;

public class SimpleJavaType extends AbstractJavaType {
  private final String className;
  private final String packageName;
  private final String qualifiedClassName;

  SimpleJavaType(String packageName, String className) {
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(className, "className");
    this.className = className;
    this.packageName = packageName;
    this.qualifiedClassName = packageName + "." + className;
  }

  SimpleJavaType(ManagedObject obj) {
    this(obj.getPackageName(), obj.getClassName());
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  @Override
  public String getQualifiedClassName() {
    return qualifiedClassName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + qualifiedClassName.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SimpleJavaType)) {
      return false;
    }
    SimpleJavaType other = (SimpleJavaType) obj;
    if (qualifiedClassName == null) {
      if (other.qualifiedClassName != null) {
        return false;
      }
    } else if (!qualifiedClassName.equals(other.qualifiedClassName)) {
      return false;
    }
    return true;
  }

}
