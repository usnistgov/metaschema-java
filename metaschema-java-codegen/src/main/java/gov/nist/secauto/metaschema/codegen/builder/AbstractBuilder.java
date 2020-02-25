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
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
  private Visibility visibility;
  private Set<JavaType> imports = new HashSet<>();
  private List<String> annotations = new LinkedList<>();
  private StringBuilder javadoc;

  @SuppressWarnings("unchecked")
  public T visibility(Visibility visibility) {
    this.visibility = visibility;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T importEntry(Class<?> clazz) {
    this.imports.add(JavaType.create(clazz));
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T importEntry(JavaType javaType) {
    this.imports.add(javaType);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T importEntries(Set<JavaType> imports) {
    for (JavaType javaType : imports) {
      importEntry(javaType);
    }
    return (T) this;
  }

  public abstract AbstractClassBuilder<?> getClassBuilder();

  public abstract Function<String, Boolean> getClashEvaluator();

  public <A extends Annotation> T annotation(Class<A> annotation) {
    return annotation(annotation, null);
  }

  @SuppressWarnings("unchecked")
  public <A extends Annotation> T annotation(Class<A> annotation, String arguments) {
    JavaType annotationType = JavaType.create(annotation);

    StringBuilder builder = new StringBuilder();
    builder.append('@');
    builder.append(annotationType.getType());
    if (arguments != null) {
      arguments = arguments.trim();
      if (!arguments.isEmpty()) {
        builder.append('(');
        builder.append(arguments);
        builder.append(')');
      }
    }
    this.annotations.add(builder.toString());
    return (T) this;
  }

  public StringBuilder getJavadocBuilder() {
    if (javadoc == null) {
      javadoc = new StringBuilder();
    }
    return javadoc;
  }

  public abstract void build(PrintWriter writer) throws IOException;

  protected String getPadding() {
    return "";
  }

  protected Visibility getVisibility() {
    return visibility;
  }

  protected String getVisibilityValue(Visibility defaultVisibility) {
    Visibility visibility = getVisibility();
    if (visibility == null) {
      visibility = defaultVisibility;
    }
    StringBuilder builder = new StringBuilder();
    String visibilityValue = visibility.getModifier();
    if (visibilityValue != null) {
      builder.append(visibilityValue);
      builder.append(' ');
    }

    return builder.toString();
  }

  protected Set<JavaType> getImports() {
    return Collections.unmodifiableSet(imports);
  }

  protected List<String> getAnnotations() {
    return Collections.unmodifiableList(annotations);
  }

  protected void buildAnnotations(PrintWriter out) {
    for (String annotation : getAnnotations()) {
      out.format("%s%s%n", getPadding(), annotation);
    }
  }

  protected void buildJavadoc(PrintWriter out) {
    if (javadoc != null) {
      String javadocBody = javadoc.toString();
      out.format("%s/**%n", getPadding());
      for (String line : javadocBody.split(System.lineSeparator())) {
        out.format("%s * %s%n", getPadding(), line);
      }
      out.format("%s */%n", getPadding());
    }
  }

}
