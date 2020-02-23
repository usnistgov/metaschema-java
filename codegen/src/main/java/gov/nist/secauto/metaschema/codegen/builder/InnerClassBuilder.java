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
import java.util.Set;
import java.util.function.Function;

public class InnerClassBuilder extends AbstractClassBuilder<InnerClassBuilder> {
  private static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;

  private static JavaType makeInnerClassJavaType(ClassBuilder outerClassBuilder, String className) {
    JavaType outerType = outerClassBuilder.getJavaType();
    StringBuilder name = new StringBuilder();
    name.append(outerType.getClassName());
    name.append('.');
    name.append(className);
    return JavaType.create(outerType.getPackageName(), name.toString());

  }

  private final ClassBuilder outerClassBuilder;
  private final String className;
  private String extendsClass;

  public InnerClassBuilder(ClassBuilder outerClassBuilder, String className) {
    super(makeInnerClassJavaType(outerClassBuilder, className));
    this.outerClassBuilder = outerClassBuilder;
    this.className = className;
  }

  public InnerClassBuilder extendsClass(String value) {
    this.extendsClass = value;
    return this;
  }

  protected String getExtendsClass() {
    return extendsClass;
  }

  public String getClassName() {
    return className;
  }

  @Override
  protected String getPadding() {
    return "    ";
  }

  @Override
  public ClassBuilder getActualClassBuilder() {
    return getClassBuilder();
  }

  @Override
  public ClassBuilder getClassBuilder() {
    return outerClassBuilder;
  }

  @Override
  public Set<JavaType> getImports() {
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
    return Collections.unmodifiableSet(imports);
  }

  @Override
  public void build(PrintWriter out) throws IOException {
    // class declaration
    buildJavadoc(out);
    buildAnnotations(out);

    String extendsClass = getExtendsClass();
    if (extendsClass == null) {
      extendsClass = "";
    } else {
      extendsClass = "extends " + extendsClass + " ";
    }

    out.printf("%s%sstatic class %s %s{%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), getClassName(),
        extendsClass);

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

    out.printf("%s}%n", getPadding());
  }

  @Override
  public Function<String, Boolean> getClashEvaluator() {
    return getClassBuilder().getClashEvaluator();
  }
}
