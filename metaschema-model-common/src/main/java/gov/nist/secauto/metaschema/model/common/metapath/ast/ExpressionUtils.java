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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class ExpressionUtils {
  private ExpressionUtils() {
    // disable
  }

  @NotNull
  public static <RESULT_TYPE> Class<@NotNull ? extends RESULT_TYPE> analyzeStaticResultType(
      @NotNull Class<@NotNull RESULT_TYPE> baseType,
      @NotNull List<@NotNull IExpression> expressions) {

    Class<@NotNull ? extends RESULT_TYPE> retval;
    if (expressions.isEmpty()) {
      // no expressions, so use the base type
      retval = baseType;
    } else {
      List<@NotNull Class<@NotNull ?>> expressionClasses = ObjectUtils.notNull(expressions.stream()
          .map(expr -> expr.getStaticResultType()).collect(Collectors.toList()));

      // check if the expression classes, are derived from the base type
      if (!checkDerivedFrom(baseType, expressionClasses)) {
        retval = baseType;
      } else {
        retval = findCommonBase(baseType, expressionClasses);
      }
    }
    return retval;
  }

  @NotNull
  private static <RESULT_TYPE> Class<@NotNull ? extends RESULT_TYPE> findCommonBase(
      @NotNull Class<@NotNull RESULT_TYPE> baseType,
      @NotNull List<@NotNull Class<@NotNull ?>> expressionClasses) {
    Class<? extends RESULT_TYPE> retval;
    if (expressionClasses.size() == 1) {
      @SuppressWarnings("unchecked")
      Class<? extends RESULT_TYPE> result
          = (Class<? extends RESULT_TYPE>) expressionClasses.iterator().next();
      retval = result;
    } else {
      @SuppressWarnings("unchecked")
      Class<? extends RESULT_TYPE> first
          = (Class<? extends RESULT_TYPE>) expressionClasses.iterator().next();
      if (baseType.equals(first)) {
        // the first type is the same as the base, which is the least common type
        retval = baseType;
      } else {
        // search for the least common type
        @SuppressWarnings("unchecked")
        Class<? extends RESULT_TYPE> newBase
            = (Class<? extends RESULT_TYPE>) getCommonBaseClass(baseType, first,
                expressionClasses.subList(1, expressionClasses.size()));
        if (newBase != null) {
          retval = newBase;
        } else {
          retval = baseType;
        }
      }
    }
    return retval;
  }

  @Nullable
  private static Class<@NotNull ?> getCommonBaseClass(@NotNull Class<@NotNull ?> baseType,
      @NotNull Class<@NotNull ?> first, @NotNull List<@NotNull Class<@NotNull ?>> expressionClasses) {
    boolean match = true;
    for (Class<@NotNull ?> clazz : expressionClasses) {
      if (!first.isAssignableFrom(clazz)) {
        match = false;
        break;
      }
    }

    Class<@NotNull ?> retval;
    if (match) {
      retval = first;
    } else {
      retval = null;
      for (Class<?> clazz : first.getInterfaces()) {
        // ensure the new interface is a sublass of the baseType
        if (baseType.isAssignableFrom(clazz)) {
          Class<@NotNull ?> newBase = getCommonBaseClass(baseType, clazz, expressionClasses);
          if (newBase != null) {
            retval = newBase;
            break;
          }
        }
      }
    }
    return retval;
  }

  private static boolean checkDerivedFrom(@NotNull Class<@NotNull ?> baseType,
      @NotNull List<@NotNull Class<@NotNull ?>> expressionClasses) {
    boolean retval = true;
    for (Class<?> clazz : expressionClasses) {
      if (!baseType.isAssignableFrom(clazz)) {
        retval = false;
        break;
      }
    }
    return retval;
  }

}
