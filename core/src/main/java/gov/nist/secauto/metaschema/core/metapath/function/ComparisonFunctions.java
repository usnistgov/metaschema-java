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

package gov.nist.secauto.metaschema.core.metapath.function;

import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnNot;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBase64BinaryItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDateTimeItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;

import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class ComparisonFunctions {
  /**
   * Comparison operators.
   */
  public enum Operator {
    /**
     * An equal comparison.
     */
    EQ,
    /**
     * A not equal comparison.
     */
    NE,
    /**
     * A less than comparison.
     */
    LT,
    /**
     * A less than or equal comparison.
     */
    LE,
    /**
     * A greater than comparison.
     */
    GT,
    /**
     * A greater than or equal comparison.
     */
    GE;
  }

  private ComparisonFunctions() {
    // disable construction
  }

  /**
   * Compare the two items using the provided {@code operator}.
   *
   * @param leftItem
   *          the first item to compare
   * @param operator
   *          the comparison operator
   * @param rightItem
   *          the second item to compare
   * @return the result of the comparison
   */
  @NonNull
  public static IBooleanItem valueCompairison(@NonNull IAnyAtomicItem leftItem, @NonNull Operator operator,
      @NonNull IAnyAtomicItem rightItem) {
    return compare(leftItem, operator, rightItem);
  }

  /**
   * Compare the sets of atomic items.
   *
   * @param leftItems
   *          the first set of items to compare
   * @param operator
   *          the comparison operator
   * @param rightItems
   *          the second set of items to compare
   * @return a or an empty {@link ISequence} if either item is {@code null}
   */
  @NonNull
  public static IBooleanItem generalCompairison( // NOPMD - acceptable complexity
      @NonNull ISequence<? extends IAnyAtomicItem> leftItems,
      @NonNull Operator operator,
      @NonNull ISequence<? extends IAnyAtomicItem> rightItems) {

    IBooleanItem retval = IBooleanItem.FALSE;
    for (IAnyAtomicItem left : leftItems.getValue()) {
      assert left != null;
      for (IAnyAtomicItem right : rightItems.getValue()) {
        assert right != null;
        IAnyAtomicItem leftCast;
        IAnyAtomicItem rightCast;
        if (left instanceof IUntypedAtomicItem) {
          if (right instanceof IUntypedAtomicItem) {
            leftCast = IStringItem.cast(left);
            rightCast = IStringItem.cast(right);
          } else {
            leftCast = applyGeneralComparisonCast(right, left);
            rightCast = right;
          }
        } else if (right instanceof IUntypedAtomicItem) {
          leftCast = left;
          rightCast = applyGeneralComparisonCast(left, right);
        } else {
          leftCast = left;
          rightCast = right;
        }

        assert leftCast != null;
        IBooleanItem result = compare(leftCast, operator, rightCast);
        if (IBooleanItem.TRUE.equals(result)) {
          retval = IBooleanItem.TRUE;
        }
      }
    }
    return retval;
  }

  /**
   * Attempts to cast the provided {@code other} item to the type of the
   * {@code item}.
   *
   * @param item
   *          the item whose type the other item is to be cast to
   * @param other
   *          the item to cast
   * @return the casted item
   */
  @NonNull
  private static IAnyAtomicItem applyGeneralComparisonCast(@NonNull IAnyAtomicItem item,
      @NonNull IAnyAtomicItem other) {
    IAnyAtomicItem retval;
    if (item instanceof INumericItem) {
      retval = IDecimalItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IDayTimeDurationItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IYearMonthDurationItem.cast(other);
    } else {
      retval = item.getJavaTypeAdapter().cast(other);
    }
    return retval;
  }

  /**
   * Compare the {@code right} item with the {@code left} item using the specified
   * {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem compare( // NOPMD - unavoidable
      @NonNull IAnyAtomicItem left,
      @NonNull Operator operator,
      @NonNull IAnyAtomicItem right) {
    @NonNull IBooleanItem retval;
    if (left instanceof IStringItem || right instanceof IStringItem) {
      retval = stringCompare(IStringItem.cast(left), operator, IStringItem.cast(right));
    } else if (left instanceof INumericItem && right instanceof INumericItem) {
      retval = numericCompare((INumericItem) left, operator, (INumericItem) right);
    } else if (left instanceof IBooleanItem && right instanceof IBooleanItem) {
      retval = booleanCompare((IBooleanItem) left, operator, (IBooleanItem) right);
    } else if (left instanceof IDateTimeItem && right instanceof IDateTimeItem) {
      retval = dateTimeCompare((IDateTimeItem) left, operator, (IDateTimeItem) right);
    } else if (left instanceof IDateItem && right instanceof IDateItem) {
      retval = dateCompare((IDateItem) left, operator, (IDateItem) right);
    } else if (left instanceof IDurationItem && right instanceof IDurationItem) {
      retval = durationCompare((IDurationItem) left, operator, (IDurationItem) right);
    } else if (left instanceof IBase64BinaryItem && right instanceof IBase64BinaryItem) {
      retval = binaryCompare((IBase64BinaryItem) left, operator, (IBase64BinaryItem) right);
    } else {
      throw new InvalidTypeMetapathException(
          null,
          String.format("invalid types for comparison: %s %s %s", left.getClass().getName(),
              operator.name().toLowerCase(Locale.ROOT), right.getClass().getName()));
    }
    return retval;
  }

  /**
   * Perform a string-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem stringCompare(@NonNull IStringItem left, @NonNull Operator operator,
      @NonNull IStringItem right) {
    int result = left.compareTo(right);
    boolean retval;
    switch (operator) {
    case EQ:
      // retval = OperationFunctions.opNumericEqual(left.compare(right),
      // IIntegerItem.ZERO);
      retval = result == 0;
      break;
    case GE:
      // retval = OperationFunctions.opNumericGreaterThan(left.compare(right),
      // IIntegerItem.NEGATIVE_ONE);
      retval = result >= 0;
      break;
    case GT:
      // retval = OperationFunctions.opNumericGreaterThan(left.compare(right),
      // IIntegerItem.ZERO);
      retval = result > 0;
      break;
    case LE:
      // retval = OperationFunctions.opNumericLessThan(left.compare(right),
      // IIntegerItem.ONE);
      retval = result <= 0;
      break;
    case LT:
      // retval = OperationFunctions.opNumericLessThan(left.compare(right),
      // IIntegerItem.ZERO);
      retval = result < 0;
      break;
    case NE:
      // retval = FnNot.fnNot(OperationFunctions.opNumericEqual(left.compare(right),
      // IIntegerItem.ZERO));
      retval = result != 0;
      break;
    default:
      throw new IllegalArgumentException(
          String.format("Unsupported operator '%s'", operator.name())); // NOPMD
    }
    return IBooleanItem.valueOf(retval);
  }

  /**
   * Perform a number-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem numericCompare(@NonNull INumericItem left, @NonNull Operator operator,
      @NonNull INumericItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opNumericEqual(left, right);
      break;
    case GE: {
      IBooleanItem gt = OperationFunctions.opNumericGreaterThan(left, right);
      IBooleanItem eq = OperationFunctions.opNumericEqual(left, right);
      retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      break;
    }
    case GT:
      retval = OperationFunctions.opNumericGreaterThan(left, right);
      break;
    case LE: {
      IBooleanItem lt = OperationFunctions.opNumericLessThan(left, right);
      IBooleanItem eq = OperationFunctions.opNumericEqual(left, right);
      retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      break;
    }
    case LT:
      retval = OperationFunctions.opNumericLessThan(left, right);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opNumericEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }
    return retval;
  }

  /**
   * Perform a boolean-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem booleanCompare(@NonNull IBooleanItem left, @NonNull Operator operator,
      @NonNull IBooleanItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opBooleanEqual(left, right);
      break;
    case GE: {
      IBooleanItem gt = OperationFunctions.opBooleanGreaterThan(left, right);
      IBooleanItem eq = OperationFunctions.opBooleanEqual(left, right);
      retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      break;
    }
    case GT:
      retval = OperationFunctions.opBooleanGreaterThan(left, right);
      break;
    case LE: {
      IBooleanItem lt = OperationFunctions.opBooleanLessThan(left, right);
      IBooleanItem eq = OperationFunctions.opBooleanEqual(left, right);
      retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      break;
    }
    case LT:
      retval = OperationFunctions.opBooleanLessThan(left, right);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opBooleanEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }
    return retval;
  }

  /**
   * Perform a date and time-based comparison of the {@code right} item against
   * the {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem dateTimeCompare(@NonNull IDateTimeItem left, @NonNull Operator operator,
      @NonNull IDateTimeItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opDateTimeEqual(left, right);
      break;
    case GE: {
      IBooleanItem gt = OperationFunctions.opDateTimeGreaterThan(left, right);
      IBooleanItem eq = OperationFunctions.opDateTimeEqual(left, right);
      retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      break;
    }
    case GT:
      retval = OperationFunctions.opDateTimeGreaterThan(left, right);
      break;
    case LE: {
      IBooleanItem lt = OperationFunctions.opDateTimeLessThan(left, right);
      IBooleanItem eq = OperationFunctions.opDateTimeEqual(left, right);
      retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      break;
    }
    case LT:
      retval = OperationFunctions.opDateTimeLessThan(left, right);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opDateTimeEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }
    return retval;
  }

  /**
   * Perform a date-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem dateCompare(@NonNull IDateItem left, @NonNull Operator operator,
      @NonNull IDateItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opDateEqual(left, right);
      break;
    case GE: {
      IBooleanItem gt = OperationFunctions.opDateGreaterThan(left, right);
      IBooleanItem eq = OperationFunctions.opDateEqual(left, right);
      retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      break;
    }
    case GT:
      retval = OperationFunctions.opDateGreaterThan(left, right);
      break;
    case LE: {
      IBooleanItem lt = OperationFunctions.opDateLessThan(left, right);
      IBooleanItem eq = OperationFunctions.opDateEqual(left, right);
      retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      break;
    }
    case LT:
      retval = OperationFunctions.opDateLessThan(left, right);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opDateEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }
    return retval;
  }

  /**
   * Perform a duration-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem durationCompare( // NOPMD - unavoidable
      @NonNull IDurationItem left,
      @NonNull Operator operator,
      @NonNull IDurationItem right) {
    IBooleanItem retval = null;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opDurationEqual(left, right);
      break;
    case GE:
      if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
        IBooleanItem gt = OperationFunctions.opYearMonthDurationGreaterThan(
            (IYearMonthDurationItem) left,
            (IYearMonthDurationItem) right);
        IBooleanItem eq = OperationFunctions.opDurationEqual(left, right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
        IBooleanItem gt = OperationFunctions.opDayTimeDurationGreaterThan(
            (IDayTimeDurationItem) left,
            (IDayTimeDurationItem) right);
        IBooleanItem eq = OperationFunctions.opDurationEqual(left, right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      }
      break;
    case GT:
      if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opYearMonthDurationGreaterThan(
            (IYearMonthDurationItem) left,
            (IYearMonthDurationItem) right);
      } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opDayTimeDurationGreaterThan(
            (IDayTimeDurationItem) left,
            (IDayTimeDurationItem) right);
      }
      break;
    case LE:
      if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
        IBooleanItem lt = OperationFunctions.opYearMonthDurationLessThan(
            (IYearMonthDurationItem) left,
            (IYearMonthDurationItem) right);
        IBooleanItem eq = OperationFunctions.opDurationEqual(left, right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
        IBooleanItem lt = OperationFunctions.opDayTimeDurationLessThan(
            (IDayTimeDurationItem) left,
            (IDayTimeDurationItem) right);
        IBooleanItem eq = OperationFunctions.opDurationEqual(left, right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      }
      break;
    case LT:
      if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opYearMonthDurationLessThan(
            (IYearMonthDurationItem) left,
            (IYearMonthDurationItem) right);
      } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opDayTimeDurationLessThan(
            (IDayTimeDurationItem) left,
            (IDayTimeDurationItem) right);
      }
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opDurationEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }

    if (retval == null) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("The item types '%s' and '%s' are not comparable",
              left.getClass().getName(),
              right.getClass().getName()));
    }
    return retval;
  }

  /**
   * Perform a binary data-based comparison of the {@code right} item against the
   * {@code left} item using the specified {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IBooleanItem binaryCompare(@NonNull IBase64BinaryItem left, @NonNull Operator operator,
      @NonNull IBase64BinaryItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opBase64BinaryEqual(left, right);
      break;
    case GE: {
      IBooleanItem gt = OperationFunctions.opBase64BinaryGreaterThan(left, right);
      IBooleanItem eq = OperationFunctions.opBase64BinaryEqual(left, right);
      retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
      break;
    }
    case GT:
      retval = OperationFunctions.opBase64BinaryGreaterThan(left, right);
      break;
    case LE: {
      IBooleanItem lt = OperationFunctions.opBase64BinaryLessThan(left, right);
      IBooleanItem eq = OperationFunctions.opBase64BinaryEqual(left, right);
      retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
      break;
    }
    case LT:
      retval = OperationFunctions.opBase64BinaryLessThan(left, right);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opBase64BinaryEqual(left, right));
      break;
    default:
      throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
    }
    return retval;
  }

  /**
   * Compare the {@code right} item with the {@code left} item using the specified
   * {@code operator}.
   *
   * @param left
   *          the value to compare against
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NonNull
  public static IIntegerItem compareTo(
      @NonNull IAnyAtomicItem left,
      @NonNull IAnyAtomicItem right) {
    return IIntegerItem.valueOf(left.compareTo(right));
  }
}
