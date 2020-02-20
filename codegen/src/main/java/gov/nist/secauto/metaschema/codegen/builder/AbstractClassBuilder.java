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

package gov.nist.secauto.metaschema.codegen.builder;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractClassBuilder<T extends AbstractClassBuilder<T>> extends AbstractBuilder<T> {
  private final JavaType javaType;
  private Map<String, FieldBuilder> fields = new LinkedHashMap<>();
  private List<ConstructorBuilder> constructors = new LinkedList<>();
  private Map<String, MethodBuilder> methods = new LinkedHashMap<>();

  public AbstractClassBuilder(JavaType classJavaType) {
    this.javaType = classJavaType;
  }

  public abstract ClassBuilder getActualClassBuilder();

  public JavaType getJavaType() {
    return javaType;
  }

  public FieldBuilder newFieldBuilder(JavaType javaType, String name) {
    FieldBuilder retval = new FieldBuilder(this, javaType, name);
    fields.put(retval.getName(), retval);
    return retval;
  }

  public ConstructorBuilder newConstructorBuilder() {
    ConstructorBuilder retval = new ConstructorBuilder(this);
    constructors.add(retval);
    return retval;
  }

  public MethodBuilder newMethodBuilder(String name) {
    MethodBuilder retval = new MethodBuilder(this, name);
    methods.put(retval.getName(), retval);
    return retval;
  }

  protected Map<String, FieldBuilder> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  protected List<ConstructorBuilder> getConstructors() {
    return Collections.unmodifiableList(constructors);
  }

  protected Map<String, MethodBuilder> getMethods() {
    return Collections.unmodifiableMap(methods);
  }

}
