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

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodBuilder extends AbstractMethodBuilder<MethodBuilder> {
  private static final Visibility DEFAULT_VISIBILITY = Visibility.PUBLIC;
  private final String name;
  private JavaType returnType;
  private List<Class<? extends Throwable>> exceptionClasses = new LinkedList<>();

  public MethodBuilder(AbstractClassBuilder<?> classBuilder, String name) {
    super(classBuilder);
    Objects.requireNonNull(name, "name");
    this.name = name;
  }

  public MethodBuilder returnType(Class<?> clazz) {
    return returnType(JavaType.create(clazz));
  }

  public MethodBuilder returnType(JavaType type) {
    this.returnType = type;
    importEntries(type.getImports(getClassBuilder().getJavaType()));
    return this;
  }

  public MethodBuilder throwsDeclaration(Class<? extends Throwable> exceptionClass) {
    this.exceptionClasses.add(exceptionClass);
    return this;
  }

  protected JavaType getReturnType() {
    return returnType;
  }

  protected String getName() {
    return name;
  }

  @Override
  public void build(PrintWriter out) {
    buildJavadoc(out);
    buildAnnotations(out);

    JavaType returnType = getReturnType();
    String returnTypeValue;
    if (returnType == null) {
      returnTypeValue = "void";
    } else {
      returnTypeValue = returnType.getType(getClashEvaluator());
    }

    String arguments = getArguments();
    if (arguments == null) {
      arguments = "";
    }

    String throwsClause = null;
    if (!exceptionClasses.isEmpty()) {
      throwsClause = " throws " + exceptionClasses.stream().map(c -> c.getName()).collect(Collectors.joining(", "));
    } else {
      throwsClause = "";
    }

    out.printf("%s%s%s %s(%s)%s {%n", getPadding(), getVisibilityValue(DEFAULT_VISIBILITY), returnTypeValue, getName(),
        arguments, throwsClause);
    out.print(getBody());
    out.printf("%s}%n", getPadding());
  }

}
