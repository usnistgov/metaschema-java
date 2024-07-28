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
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IMetaschemaData;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.impl.DefaultGroupAs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;

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

  /**
   * Get the requested annotation from the provided Java class.
   *
   * @param <A>
   *          the annotation Java type
   * @param clazz
   *          the Java class to get the annotation from
   * @param annotationClass
   *          the annotation class instance
   * @return the annotation
   * @throws IllegalArgumentException
   *           if the annotation was not present on the class
   */
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

  /**
   * Get the requested annotation from the provided Java field.
   *
   * @param <A>
   *          the annotation Java type
   * @param javaField
   *          the Java field to get the annotation from
   * @param annotationClass
   *          the annotation class instance
   * @return the annotation
   * @throws IllegalArgumentException
   *           if the annotation was not present on the field
   */
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

  /**
   * Get the data type adapter instance of the provided adapter class.
   * <p>
   * If the provided adapter Java class is the {@link NullJavaTypeAdapter} class,
   * then the default data type adapter will be returned.
   *
   * @param adapterClass
   *          the data type adapter class to get the data type adapter instance
   *          for
   * @param bindingContext
   *          the Metaschema binding context used to lookup the data type adapter
   * @return the data type adapter
   */
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

  /**
   * Given a provided default value string, get the data type specific default
   * value using the provided data type adapter.
   * <p>
   * If the provided default value is {@link ModelUtil#NULL_VALUE}, then this
   * method will return a {@code null} value.
   *
   * @param defaultValue
   *          the string representation of the default value
   * @param adapter
   *          the data type adapter instance used to cast the default string value
   *          to a data type specific object
   * @return the data type specific object or {@code null} if the provided default
   *         value was {@link ModelUtil#NULL_VALUE}
   */
  @Nullable
  public static Object resolveDefaultValue(@NonNull String defaultValue, IDataTypeAdapter<?> adapter) {
    Object retval = null;
    if (!NULL_VALUE.equals(defaultValue)) {
      retval = adapter.parse(defaultValue);
    }
    return retval;
  }

  /**
   * Resolves an integer value by determining if an actual value is provided or
   * -2^31, which indicates that no actual value was provided.
   * <p>
   * The integer value -2^31 cannot be used, since this indicates no value.
   *
   * @param value
   *          the integer value to resolve
   * @return the integer value or {@code null} if the provided value was -2^31
   */
  public static Integer resolveDefaultInteger(int value) {
    return value == Integer.MIN_VALUE ? null : value;
  }

  /**
   * Resolves a {@link GroupAs} annotation determining if an actual value is
   * provided or if the value is the default, which indicates that no actual
   * GroupAs was provided.
   *
   * @param groupAs
   *          the GroupAs value to resolve
   * @param module
   *          the containing module instance
   * @return a new {@link IGroupAs} instance or a singleton group as if the
   *         provided value was the default value
   */
  @NonNull
  public static IGroupAs resolveDefaultGroupAs(
      @NonNull GroupAs groupAs,
      @NonNull IModule module) {
    return NULL_VALUE.equals(groupAs.name())
        ? IGroupAs.SINGLETON_GROUP_AS
        : new DefaultGroupAs(groupAs, module);
  }

  public static String toLocation(@NonNull IBoundObject obj) {
    IMetaschemaData data = obj.getMetaschemaData();

    String retval = "";
    if (data != null) {
      int line = data.getLine();
      if (line > -1) {
        retval = line + ":" + data.getColumn();
      }
    }
    return retval;
  }

  public static String toLocation(@NonNull IBoundObject obj, @Nullable URI uri) {
    String retval = uri == null ? "" : uri.toASCIIString();

    String location = toLocation(obj);
    if (!location.isEmpty()) {
      retval = retval.isEmpty() ? location : retval + "@" + location;
    }
    return retval;
  }
}
