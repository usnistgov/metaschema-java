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
package gov.nist.secauto.metaschema.codegen.builder;

import gov.nist.secauto.metaschema.codegen.type.JavaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ClassBuilder extends AbstractClassBuilder<ClassBuilder> {
  private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;

  private Map<String, InnerClassBuilder> innerClasses = new LinkedHashMap<>();

  public ClassBuilder(JavaType classJavaType) {
    super(classJavaType);
  }

  public InnerClassBuilder newInnerClassBuilder(String name) {
    InnerClassBuilder retval = new InnerClassBuilder(this, name);
    innerClasses.put(retval.getClassName(), retval);
    return retval;
  }

  protected Map<String, InnerClassBuilder> getInnerClasses() {
    return Collections.unmodifiableMap(innerClasses);
  }

  protected void setInnerClasses(Map<String, InnerClassBuilder> innerClasses) {
    this.innerClasses = innerClasses;
  }

  private boolean needsImport(@SuppressWarnings("unused") JavaType javaType) {
    // String classPackageName = getJavaType().getPackageName();
    // String packageName = javaType.getPackageName();
    // return !"java.lang".equals(packageName) && !classPackageName.equals(packageName);
    return false;
  }

  @Override
  public void build(PrintWriter out) throws IOException {

    // package declaration
    out.format("package %s;%n", getJavaType().getPackageName());
    out.println();

    // Handle Imports
    Set<JavaType> imports = new HashSet<>(getImports());
    for (FieldBuilder field : getFields().values()) {
      imports.addAll(field.getImports());
    }
    for (ConstructorBuilder constructor : getConstructors()) {
      imports.addAll(constructor.getImports());
    }
    for (MethodBuilder method : getMethods().values()) {
      imports.addAll(method.getImports());
    }

    if (!imports.isEmpty()) {
      imports.stream().filter(this::needsImport).map(a -> a.getQualifiedClassName()).sorted().distinct()
          .forEachOrdered(a -> out.printf("import %s;%n", a));
      out.println();
      // // filter
      //
      //
      // // sort
      // imports.stream().filter(this::needsImport).map(a ->
      // a.getQualifiedClassName()).sorted().forEachOrdered(a -> out.printf("import %s;%n", a));
      //
      // JavaType classJavaType = getJavaType();
      // boolean hasImport = false;
      // for (JavaType importEntry : imports) {
      // String importValue = importEntry.getImportValue(classJavaType);
      // if (importValue != null && !importValue.startsWith("java.lang.")) {
      // out.printf("import %s;%n", importValue);
      // hasImport = true;
      // }
      // }
      // if (hasImport) {
      // out.println();
      // }
    }

    // class declaration
    buildJavadoc(out);
    buildAnnotations(out);

    out.printf("%sclass %s {%n", getVisibilityValue(DEFAULT_VISIBILITY), getJavaType().getClassName());

    for (FieldBuilder field : getFields().values()) {
      field.build(out);
      out.println();
    }

    for (ConstructorBuilder constructor : getConstructors()) {
      constructor.build(out);
      out.println();
    }

    for (MethodBuilder method : getMethods().values()) {
      method.build(out);
      out.println();
    }

    for (InnerClassBuilder innerClass : getInnerClasses().values()) {
      innerClass.build(out);
      out.println();
    }

    out.println("}");
    out.flush();
  }

  @Override
  public ClassBuilder getClassBuilder() {
    return this;
  }

  @Override
  public Function<String, Boolean> getClashEvaluator() {
    return this::evaluateClash;
  }

  private boolean evaluateClash(@SuppressWarnings("unused") String className) {
    // boolean retval = false;
    // if (getJavaType().getClassName().equals(className)) {
    // retval = true;
    // }
    // return retval;
    return true;
  }

  @Override
  public ClassBuilder getActualClassBuilder() {
    return this;
  }
}
