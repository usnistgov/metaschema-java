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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnNot;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBase64BinaryItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A common base class for all comparison nodes, which consist of two expressions representing the
 * left and right sides of the comparison, and a comparison operator.
 */
abstract class AbstractComparison // NOPMD - unavoidable
    extends AbstractBinaryExpression<IExpression, IExpression>
    implements IComparison {

  @NotNull
  private final Operator operator;

  /**
   * Construct an expression that compares the result of the {@code right} expression with the result
   * of the {@code left} expression using the specified {@code operator}.
   * 
   * @param left
   *          the expression to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the expression to compare with
   */
  public AbstractComparison(@NotNull IExpression left, @NotNull Operator operator, @NotNull IExpression right) {
    super(left, right);
    this.operator = ObjectUtils.requireNonNull(operator, "operator");
  }

  /**
   * Get the comparison operator.
   * 
   * @return the operator
   */
  @NotNull
  public Operator getOperator() {
    return operator;
  }

  @SuppressWarnings("null")
  @Override
  public String toASTString() {
    return String.format("%s[operator=%s]", getClass().getName(), operator);
  }

  /**
   * Compare the {@code right} item with the {@code left} item using the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  protected IBooleanItem compare( // NOPMD - unavoidable
      @NotNull IAnyAtomicItem left,
      @NotNull Operator operator,
      @NotNull IAnyAtomicItem right) {
    @NotNull
    IBooleanItem retval;
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
   * Perform a string-based comparison of the {@code right} item against the {@code left} item using
   * the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem stringCompare(@NotNull IStringItem left, @NotNull Operator operator,
      @NotNull IStringItem right) {
    IBooleanItem retval;
    switch (operator) {
    case EQ:
      retval = OperationFunctions.opNumericEqual(left.compare(right), IIntegerItem.ZERO);
      break;
    case GE:
      retval = OperationFunctions.opNumericGreaterThan(left.compare(right), IIntegerItem.NEGATIVE_ONE);
      break;
    case GT:
      retval = OperationFunctions.opNumericGreaterThan(left.compare(right), IIntegerItem.ZERO);
      break;
    case LE:
      retval = OperationFunctions.opNumericLessThan(left.compare(right), IIntegerItem.ONE);
      break;
    case LT:
      retval = OperationFunctions.opNumericLessThan(left.compare(right), IIntegerItem.ZERO);
      break;
    case NE:
      retval = FnNot.fnNot(OperationFunctions.opNumericEqual(left.compare(right), IIntegerItem.ZERO));
      break;
    default:
      throw new IllegalArgumentException(
          String.format("Unsupported operator '%s'", operator.name())); // NOPMD
    }
    return retval;
  }

  /**
   * Perform a number-based comparison of the {@code right} item against the {@code left} item using
   * the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem numericCompare(@NotNull INumericItem left, @NotNull Operator operator,
      @NotNull INumericItem right) {
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
   * Perform a boolean-based comparison of the {@code right} item against the {@code left} item using
   * the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem booleanCompare(@NotNull IBooleanItem left, @NotNull Operator operator,
      @NotNull IBooleanItem right) {
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
   * Perform a date and time-based comparison of the {@code right} item against the {@code left} item
   * using the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem dateTimeCompare(@NotNull IDateTimeItem left, @NotNull Operator operator,
      @NotNull IDateTimeItem right) {
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
   * Perform a date-based comparison of the {@code right} item against the {@code left} item using the
   * specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem dateCompare(@NotNull IDateItem left, @NotNull Operator operator,
      @NotNull IDateItem right) {
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
   * Perform a duration-based comparison of the {@code right} item against the {@code left} item using
   * the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem durationCompare( // NOPMD - unavoidable
      @NotNull IDurationItem left,
      @NotNull Operator operator,
      @NotNull IDurationItem right) {
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
   * Perform a binary data-based comparison of the {@code right} item against the {@code left} item
   * using the specified {@code operator}.
   * 
   * @param left
   *          the value to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the value to compare with
   * @return the comparison result
   */
  @NotNull
  public static IBooleanItem binaryCompare(@NotNull IBase64BinaryItem left, @NotNull Operator operator,
      @NotNull IBase64BinaryItem right) {
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
}
