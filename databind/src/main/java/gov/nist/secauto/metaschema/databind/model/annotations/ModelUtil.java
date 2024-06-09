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

package gov.nist.secauto.metaschema.databind.model.annotations;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.impl.DefaultGroupAs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ModelUtil {
  // TODO: replace NO_STRING_VALUE with NULL_VALUE where possible. URIs will not
  // allow NULL_VALUE.
  public static final String NO_STRING_VALUE = "##none";
  public static final String DEFAULT_STRING_VALUE = "##default";
  /**
   * A placeholder for a {@code null} value for use in annotations, which cannot
   * be null by default.
   * <p>
   * Use of {@code "\u0000"} simple substitute for {@code null} to allow
   * implementations to recognize the "no default value" state.
   */
  public static final String NULL_VALUE = "\u0000";

  private ModelUtil() {
    // disable construction
  }

  @NonNull
  public static <A extends Annotation> A getAnnotation(
      @NonNull Class<?> clazz,
      Class<A> annotationClass) {
    A annotation = clazz.getAnnotation(annotationClass);
    if (annotation == null) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              clazz.getName(),
              annotationClass.getName()));
    }
    return annotation;
  }

  @NonNull
  public static <A extends Annotation> A getAnnotation(
      @NonNull Field javaField,
      Class<A> annotationClass) {
    A annotation = javaField.getAnnotation(annotationClass);
    if (annotation == null) {
      throw new IllegalArgumentException(
          String.format("Field '%s' is missing the '%s' annotation.",
              javaField.toGenericString(),
              annotationClass.getName()));
    }
    return annotation;
  }

  /**
   * Resolves a string value. If the value is {@code null} or "##default", then
   * the provided default value will be used instead. If the value is "##none",
   * then the value will be {@code null}. Otherwise, the value is returned.
   *
   * @param value
   *          the requested value
   * @param defaultValue
   *          the default value
   * @return the resolved value or {@code null}
   */
  @Nullable
  public static String resolveNoneOrDefault(@Nullable String value, @Nullable String defaultValue) {
    String retval;
    if (value == null || DEFAULT_STRING_VALUE.equals(value)) {
      retval = defaultValue;
    } else if (NO_STRING_VALUE.equals(value)) {
      retval = null; // NOPMD - intentional
    } else {
      retval = value;
    }
    return retval;
  }

  /**
   * Get the processed value of a string. If the value is "##none", then the value
   * will be {@code null}. Otherwise the value is returned.
   *
   * @param value
   *          text or {@code "##none"} if no text is provided
   * @return the resolved value or {@code null}
   */
  @Nullable
  public static String resolveNoneOrValue(@NonNull String value) {
    return NO_STRING_VALUE.equals(value) ? null : value;
  }

  /**
   * Get the markup value of a markdown string.
   *
   * @param value
   *          markdown text or {@code "##none"} if no text is provided
   * @return the markup line content or {@code null} if no markup content was
   *         provided
   */
  @Nullable
  public static MarkupLine resolveToMarkupLine(@NonNull String value) {
    return resolveNoneOrValue(value) == null ? null : MarkupLine.fromMarkdown(value);
  }

  /**
   * Get the markup value of a markdown string.
   *
   * @param value
   *          markdown text or {@code "##none"} if no text is provided
   * @return the markup line content or {@code null} if no markup content was
   *         provided
   */
  @Nullable
  public static MarkupMultiline resolveToMarkupMultiline(@NonNull String value) {
    return resolveNoneOrValue(value) == null ? null : MarkupMultiline.fromMarkdown(value);
  }

  @NonNull
  public static IDataTypeAdapter<?> getDataTypeAdapter(
      @NonNull Class<? extends IDataTypeAdapter<?>> adapterClass,
      @NonNull IBindingContext bindingContext) {
    IDataTypeAdapter<?> retval;
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      retval = ObjectUtils.requireNonNull(bindingContext.getJavaTypeAdapterInstance(adapterClass));
    }
    return retval;
  }

  @Nullable
  public static Object resolveDefaultValue(@NonNull String defaultValue, IDataTypeAdapter<?> adapter) {
    Object retval = null;
    if (!NULL_VALUE.equals(defaultValue)) {
      retval = adapter.parse(defaultValue);
    }
    return retval;
  }

  public static Integer resolveNullOrInteger(int value) {
    return value == Integer.MIN_VALUE ? null : value;
  }

  public static Object resolveNullOrValue(
      @NonNull String defaultValue,
      @NonNull IDataTypeAdapter<?> javaTypeAdapter) {
    return NULL_VALUE.equals(defaultValue)
        ? null
        : javaTypeAdapter.parse(defaultValue);
  }

  @NonNull
  public static IGroupAs groupAs(
      @NonNull GroupAs groupAs,
      @NonNull IModule module) {
    return NULL_VALUE.equals(groupAs.name())
        ? IGroupAs.SINGLETON_GROUP_AS
        : new DefaultGroupAs(groupAs, module);
  }
}
