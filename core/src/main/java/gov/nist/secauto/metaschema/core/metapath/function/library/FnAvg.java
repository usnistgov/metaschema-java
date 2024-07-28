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

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidArgumentFunctionException;
import gov.nist.secauto.metaschema.core.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * An implementation of XPath 3.1
 * <a href="https://www.w3.org/TR/xpath-functions-31/#func-avg">fn:avg</a>.
 */
@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class FnAvg {
  private static final String NAME = "avg";

  @NonNull
  static final IFunction SIGNATURE = IFunction.builder()
      .name(NAME)
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("arg")
          .type(IAnyAtomicItem.class)
          .zeroOrMore()
          .build())
      .returnType(IAnyAtomicItem.class)
      .returnZeroOrOne()
      .functionHandler(FnAvg::execute)
      .build();

  private FnAvg() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IAnyAtomicItem> execute(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    ISequence<? extends IAnyAtomicItem> sequence = FunctionUtils.asType(
        ObjectUtils.requireNonNull(arguments.get(0)));

    List<? extends IAnyAtomicItem> items = sequence.getValue();

    return ISequence.of(average(items));
  }

  /**
   * An implementation of XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-avg">fn:avg</a>.
   *
   * @param items
   *          the items to average
   * @return the average
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  @Nullable
  public static IAnyAtomicItem average(@NonNull Collection<? extends IAnyAtomicItem> items) {
    if (items.isEmpty()) {
      return null; // NOPMD - readability
    }

    // tell cpd to start ignoring code - CPD-OFF

    Map<Class<? extends IAnyAtomicItem>, Integer> typeCounts = FunctionUtils.countTypes(
        OperationFunctions.AGGREGATE_MATH_TYPES,
        ObjectUtils.notNull(items));

    int count = items.size();
    int dayTimeCount = typeCounts.getOrDefault(IDayTimeDurationItem.class, 0);
    int yearMonthCount = typeCounts.getOrDefault(IYearMonthDurationItem.class, 0);
    int numericCount = typeCounts.getOrDefault(INumericItem.class, 0);

    IAnyAtomicItem retval;
    if (dayTimeCount > 0) {
      if (dayTimeCount != count) {
        throw new InvalidArgumentFunctionException(
            InvalidArgumentFunctionException.INVALID_ARGUMENT_TYPE,
            String.format("Values must all be of type '%s'.", IDayTimeDurationItem.class.getName()));
      }

      List<IDayTimeDurationItem> values = items.stream()
          .map(item -> (IDayTimeDurationItem) item)
          .collect(Collectors.toList());
      retval = averageDayTimeDurations(ObjectUtils.notNull(values));
    } else if (yearMonthCount > 0) {
      if (yearMonthCount != count) {
        throw new InvalidArgumentFunctionException(
            InvalidArgumentFunctionException.INVALID_ARGUMENT_TYPE,
            String.format("Values must all be of type '%s'.", IYearMonthDurationItem.class.getName()));
      }

      List<IYearMonthDurationItem> values = items.stream()
          .map(item -> (IYearMonthDurationItem) item)
          .collect(Collectors.toList());
      retval = averageYearMonthDurations(ObjectUtils.notNull(values));
    } else if (numericCount > 0) {
      if (numericCount != count) {
        throw new InvalidArgumentFunctionException(
            InvalidArgumentFunctionException.INVALID_ARGUMENT_TYPE,
            String.format("Values must all be of type '%s'.", INumericItem.class.getName()));
      }

      List<INumericItem> values = items.stream()
          .map(item -> IDecimalItem.cast(ObjectUtils.notNull(item)))
          .collect(Collectors.toList());
      retval = averageNumeric(ObjectUtils.notNull(values));
    } else {
      throw new InvalidArgumentFunctionException(
          InvalidArgumentFunctionException.INVALID_ARGUMENT_TYPE,
          String.format("Values must all be of type '%s'.",
              OperationFunctions.AGGREGATE_MATH_TYPES.stream()
                  .map(Class::getName)
                  .collect(CustomCollectors.joiningWithOxfordComma(","))));
    }

    // resume CPD analysis - CPD-ON

    return retval;
  }

  @SuppressWarnings("PMD.UnnecessaryCast")
  @NonNull
  private static <T, R extends T> R average(
      @NonNull Collection<? extends T> items,
      @NonNull BinaryOperator<T> adder,
      @NonNull BiFunction<T, IIntegerItem, R> divider) {
    T sum = items.stream()
        .map(item -> (T) item)
        .reduce(adder)
        .get();
    return ObjectUtils.notNull(divider.apply(sum, IIntegerItem.valueOf(items.size())));
  }

  /**
   * Get the average of a collection of day/time duration-based items.
   *
   * @param items
   *          the Metapath items to average
   * @return the average
   */
  @NonNull
  public static IDayTimeDurationItem
      averageDayTimeDurations(@NonNull Collection<? extends IDayTimeDurationItem> items) {
    return average(
        items,
        (BinaryOperator<IDayTimeDurationItem>) OperationFunctions::opAddDayTimeDurations,
        (BiFunction<IDayTimeDurationItem, IIntegerItem,
            IDayTimeDurationItem>) OperationFunctions::opDivideDayTimeDuration);
  }

  /**
   * Get the average of a collection of year/month duration-based items.
   *
   * @param items
   *          the Metapath items to average
   * @return the average
   */
  @NonNull
  public static IYearMonthDurationItem
      averageYearMonthDurations(@NonNull Collection<? extends IYearMonthDurationItem> items) {
    return average(
        items,
        (BinaryOperator<IYearMonthDurationItem>) OperationFunctions::opAddYearMonthDurations,
        (BiFunction<IYearMonthDurationItem, IIntegerItem,
            IYearMonthDurationItem>) OperationFunctions::opDivideYearMonthDuration);
  }

  /**
   * Get the average of a collection of numeric items.
   *
   * @param items
   *          the Metapath items to average
   * @return the average
   */
  @NonNull
  public static IDecimalItem averageNumeric(@NonNull Collection<? extends INumericItem> items) {
    return average(
        items,
        (BinaryOperator<INumericItem>) OperationFunctions::opNumericAdd,
        (BiFunction<INumericItem, IIntegerItem, IDecimalItem>) OperationFunctions::opNumericDivide);
  }
}
