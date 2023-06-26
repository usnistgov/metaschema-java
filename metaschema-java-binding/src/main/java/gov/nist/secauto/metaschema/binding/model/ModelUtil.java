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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Constants;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlSchema;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ModelUtil {
  private ModelUtil() {
    // disable construction
  }

  /**
   * Resolves a provided local name value. If the value is {@code null} or "##default", then the
   * provided default value will be used instead. If the value is "##none", then the value will be
   * {@code null}. Otherwise, the value is returned.
   *
   * @param value
   *          the requested value
   * @param defaultValue
   *          the default value
   * @return the resolved value
   */
  public static String resolveLocalName(String value, String defaultValue) {
    String retval;
    if (value == null || Constants.DEFAULT_STRING_VALUE.equals(value)) {
      retval = defaultValue;
    } else if (Constants.NO_STRING_VALUE.equals(value)) {
      retval = null; // NOPMD - intentional
    } else {
      retval = value;
    }
    return retval;
  }

  @Nullable
  public static String resolveOptionalNamespace(String annotationValue, IClassBinding classBinding) {
    return resolveNamespace(annotationValue, classBinding, true);
  }

  @NonNull
  public static String resolveNamespace(String annotationValue, IClassBinding classBinding) {
    return ObjectUtils.notNull(resolveNamespace(annotationValue, classBinding, false));
  }

  /**
   * Resolves a provided namespace value. If the value is {@code null} or "##default", then the
   * provided default value will be used instead. If the value is {@code null} or "##none", then a
   * {@code null} value will be used if allowNone is {@code true}. Otherwise, the value is returned.
   *
   * @param value
   *          the requested value
   * @param classBinding
   *          a class with the {@link XmlSchema} annotation
   * @param allowNone
   *          if the "##none" value is honored
   * @return the resolved value or {@code null} if no namespace is defined
   */
  private static String resolveNamespace(String value, IClassBinding classBinding, boolean allowNone) {
    String retval;
    if (value == null || Constants.DEFAULT_STRING_VALUE.equals(value)) {
      // get namespace from the metaschema
      retval = classBinding.getContainingMetaschema().getXmlNamespace().toASCIIString();
    } else if (allowNone && Constants.NO_STRING_VALUE.equals(value)) {
      retval = null; // NOPMD - intentional
    } else {
      retval = value;
    }
    return retval;
  }

  /**
   * Get the markup value of a markdown string.
   *
   * @param annotationValue
   *          markdown text or {@code "##none"} if no text is provided
   * @return the markup line content or {@code null} if no markup content was provided
   */
  @Nullable
  public static MarkupLine resolveToMarkupLine(@NonNull String annotationValue) {
    return Constants.NO_STRING_VALUE.equals(annotationValue) ? null : MarkupLine.fromMarkdown(annotationValue);
  }

  /**
   * Get the markup value of a markdown string.
   *
   * @param annotationValue
   *          markdown text or {@code "##none"} if no text is provided
   * @return the markup line content or {@code null} if no markup content was provided
   */
  @Nullable
  public static MarkupMultiline resolveToMarkupMultiline(@NonNull String annotationValue) {
    return Constants.NO_STRING_VALUE.equals(annotationValue) ? null : MarkupMultiline.fromMarkdown(annotationValue);
  }

  /**
   * Get the string value of a string.
   *
   * @param annotationValue
   *          text or {@code "##none"} if no text is provided
   * @return the string content or {@code null} if no string content was provided
   */
  @Nullable
  public static String resolveToString(@NonNull String annotationValue) {
    return Constants.NO_STRING_VALUE.equals(annotationValue) ? null : annotationValue;
  }

  @NonNull
  public static IDataTypeAdapter<?> getDataTypeAdapter(@NonNull Class<? extends IDataTypeAdapter<?>> adapterClass,
      IBindingContext bindingContext) {
    IDataTypeAdapter<?> retval;
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      retval = ObjectUtils.requireNonNull(bindingContext.getJavaTypeAdapterInstance(adapterClass));
    }
    return retval;
  }

  @Nullable
  public static Object resolveDefaultValue(String defaultValue, IDataTypeAdapter<?> adapter) {
    Object retval = null;
    if (!Constants.NULL_VALUE.equals(defaultValue)) {
      retval = adapter.parse(defaultValue);
    }
    return retval;
  }
}
