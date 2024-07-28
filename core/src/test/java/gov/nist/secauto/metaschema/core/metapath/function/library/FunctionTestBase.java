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

package gov.nist.secauto.metaschema.core.metapath.function.library;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ExpressionTestBase;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class FunctionTestBase
    extends ExpressionTestBase {

  /**
   * Assert that the execution of the provided function and arguments produce the
   * desired results.
   *
   * @param function
   *          the function to test
   * @param expectedResult
   *          the expected result produced by the function
   * @param arguments
   *          the function arguments to use for evaluation
   */
  public static void assertFunctionResult(
      @NonNull IFunction function,
      @NonNull ISequence<?> expectedResult,
      List<ISequence<?>> arguments) {
    assertFunctionResult(function, null, expectedResult, arguments);
  }

  /**
   * Assert that the execution of the provided function and arguments produce the
   * desired results.
   *
   * @param function
   *          the function to test
   * @param focus
   *          the item focus to use for evaluation
   * @param expectedResult
   *          the expected result produced by the function
   * @param arguments
   *          the function arguments to use for evaluation
   */
  public static void assertFunctionResult(
      @NonNull IFunction function,
      @Nullable ISequence<?> focus,
      @NonNull ISequence<?> expectedResult,
      List<ISequence<?>> arguments) {

    List<ISequence<?>> usedArguments = arguments == null ? CollectionUtil.emptyList() : arguments;

    // QName functionName = function.getQName();
    //
    // IFunction resolvedFunction =
    // FunctionService.getInstance().getFunction(functionName,
    // usedArguments.size());
    //
    // assertNotNull(resolvedFunction, String.format("Function '%s' not found in
    // function service.", functionName));

    assertFunctionResultInternal(function, focus, expectedResult, usedArguments);
  }

  private static void assertFunctionResultInternal(
      @NonNull IFunction function,
      @Nullable ISequence<?> focus,
      @NonNull ISequence<?> expectedResult,
      List<ISequence<?>> arguments) {
    ISequence<INumericItem> result = FunctionTestBase.executeFunction(
        function,
        newDynamicContext(),
        focus,
        arguments);

    assertAll(
        () -> assertEquals(expectedResult, result),
        () -> assertEquals(
            FunctionUtils.getTypes(expectedResult.getValue()),
            FunctionUtils.getTypes(result.getValue())));

  }

  @SuppressWarnings("unchecked")
  private static <R extends IItem> ISequence<R> executeFunction(
      @NonNull IFunction function,
      @Nullable DynamicContext dynamicContext,
      @Nullable ISequence<?> focus,
      List<ISequence<?>> arguments) {

    DynamicContext context = dynamicContext == null ? new DynamicContext() : dynamicContext;
    ISequence<?> focusSeqence = function.isFocusDepenent()
        ? ObjectUtils.requireNonNull(focus, "Function call requires a focus")
        : ISequence.empty();
    return (ISequence<R>) function.execute(
        arguments == null ? CollectionUtil.emptyList() : arguments,
        context,
        focusSeqence);
  }
}
