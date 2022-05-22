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

import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidTypeMetapathException;
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

public abstract class AbstractComparison
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
   * Compare the {@code right} item with the  {@code left} item using the specified {@code operator}.
   * 
   * @param left
   *          the expression to compare against
   * @param operator
   *          the comparison operator
   * @param right
   *          the expression to compare with
   * @return the comparison result
   */
  @NotNull
  protected IBooleanItem compare(@NotNull IAnyAtomicItem left, @NotNull Operator operator,
      @NotNull IAnyAtomicItem right) {
    IBooleanItem retval;
    if (left instanceof IStringItem || right instanceof IStringItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opNumericEqual(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case GE:
        retval = OperationFunctions.opNumericGreaterThan(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.NEGATIVE_ONE);
        break;
      case GT:
        retval = OperationFunctions.opNumericGreaterThan(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case LE:
        retval = OperationFunctions.opNumericLessThan(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.ONE);
        break;
      case LT:
        retval = OperationFunctions.opNumericLessThan(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opNumericEqual(
            ((IStringItem) left).compare((IStringItem) right),
            IIntegerItem.ZERO));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof INumericItem && right instanceof INumericItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opNumericEqual((INumericItem) left, (INumericItem) right);
        break;
      case GE: {
        IBooleanItem gt = OperationFunctions.opNumericGreaterThan((INumericItem) left, (INumericItem) right);
        IBooleanItem eq = OperationFunctions.opNumericEqual((INumericItem) left, (INumericItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = OperationFunctions.opNumericGreaterThan((INumericItem) left, (INumericItem) right);
        break;
      case LE: {
        IBooleanItem lt = OperationFunctions.opNumericLessThan((INumericItem) left, (INumericItem) right);
        IBooleanItem eq = OperationFunctions.opNumericEqual((INumericItem) left, (INumericItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = OperationFunctions.opNumericLessThan((INumericItem) left, (INumericItem) right);
        break;
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opNumericEqual((INumericItem) left, (INumericItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof IBooleanItem && right instanceof IBooleanItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        break;
      case GE: {
        IBooleanItem gt = OperationFunctions.opBooleanGreaterThan((IBooleanItem) left, (IBooleanItem) right);
        IBooleanItem eq = OperationFunctions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = OperationFunctions.opBooleanGreaterThan((IBooleanItem) left, (IBooleanItem) right);
        break;
      case LE: {
        IBooleanItem lt = OperationFunctions.opBooleanLessThan((IBooleanItem) left, (IBooleanItem) right);
        IBooleanItem eq = OperationFunctions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = OperationFunctions.opBooleanLessThan((IBooleanItem) left, (IBooleanItem) right);
        break;
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof IDateTimeItem && right instanceof IDateTimeItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opDateTimeEqual((IDateTimeItem) left, (IDateTimeItem) right);
        break;
      case GE: {
        IBooleanItem gt = OperationFunctions.opDateTimeGreaterThan((IDateTimeItem) left, (IDateTimeItem) right);
        IBooleanItem eq = OperationFunctions.opDateTimeEqual((IDateTimeItem) left, (IDateTimeItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = OperationFunctions.opDateTimeGreaterThan((IDateTimeItem) left, (IDateTimeItem) right);
        break;
      case LE: {
        IBooleanItem lt = OperationFunctions.opDateTimeLessThan((IDateTimeItem) left, (IDateTimeItem) right);
        IBooleanItem eq = OperationFunctions.opDateTimeEqual((IDateTimeItem) left, (IDateTimeItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = OperationFunctions.opDateTimeLessThan((IDateTimeItem) left, (IDateTimeItem) right);
        break;
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opDateTimeEqual((IDateTimeItem) left, (IDateTimeItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof IDateItem && right instanceof IDateItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opDateEqual((IDateItem) left, (IDateItem) right);
        break;
      case GE: {
        IBooleanItem gt = OperationFunctions.opDateGreaterThan((IDateItem) left, (IDateItem) right);
        IBooleanItem eq = OperationFunctions.opDateEqual((IDateItem) left, (IDateItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = OperationFunctions.opDateGreaterThan((IDateItem) left, (IDateItem) right);
        break;
      case LE: {
        IBooleanItem lt = OperationFunctions.opDateLessThan((IDateItem) left, (IDateItem) right);
        IBooleanItem eq = OperationFunctions.opDateEqual((IDateItem) left, (IDateItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = OperationFunctions.opDateLessThan((IDateItem) left, (IDateItem) right);
        break;
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opDateEqual((IDateItem) left, (IDateItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof IDurationItem && right instanceof IDurationItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opDurationEqual((IDurationItem) left, (IDurationItem) right);
        break;
      case GE: {
        if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
          IBooleanItem gt = OperationFunctions.opYearMonthDurationGreaterThan((IYearMonthDurationItem) left,
              (IYearMonthDurationItem) right);
          IBooleanItem eq
              = OperationFunctions.opDurationEqual((IYearMonthDurationItem) left, (IYearMonthDurationItem) right);
          retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
          IBooleanItem gt = OperationFunctions.opDayTimeDurationGreaterThan((IDayTimeDurationItem) left,
              (IDayTimeDurationItem) right);
          IBooleanItem eq
              = OperationFunctions.opDurationEqual((IDayTimeDurationItem) left, (IDayTimeDurationItem) right);
          retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        } else {
          throw new IllegalArgumentException("the item types are not comparable");
        }
        break;
      }
      case GT:
        if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
          retval = OperationFunctions.opYearMonthDurationGreaterThan((IYearMonthDurationItem) left,
              (IYearMonthDurationItem) right);
        } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
          retval = OperationFunctions.opDayTimeDurationGreaterThan((IDayTimeDurationItem) left,
              (IDayTimeDurationItem) right);
        } else {
          throw new IllegalArgumentException("the item types are not comparable");
        }
        break;
      case LE: {
        if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
          IBooleanItem lt = OperationFunctions.opYearMonthDurationLessThan((IYearMonthDurationItem) left,
              (IYearMonthDurationItem) right);
          IBooleanItem eq
              = OperationFunctions.opDurationEqual((IYearMonthDurationItem) left, (IYearMonthDurationItem) right);
          retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
          IBooleanItem lt
              = OperationFunctions.opDayTimeDurationLessThan((IDayTimeDurationItem) left, (IDayTimeDurationItem) right);
          IBooleanItem eq
              = OperationFunctions.opDurationEqual((IDayTimeDurationItem) left, (IDayTimeDurationItem) right);
          retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        } else {
          throw new IllegalArgumentException("the item types are not comparable");
        }
        break;
      }
      case LT: {
        if (left instanceof IYearMonthDurationItem && right instanceof IYearMonthDurationItem) {
          retval = OperationFunctions.opYearMonthDurationLessThan((IYearMonthDurationItem) left,
              (IYearMonthDurationItem) right);
        } else if (left instanceof IDayTimeDurationItem && right instanceof IDayTimeDurationItem) {
          retval
              = OperationFunctions.opDayTimeDurationLessThan((IDayTimeDurationItem) left, (IDayTimeDurationItem) right);
        } else {
          throw new IllegalArgumentException("the item types are not comparable");
        }
        break;
      }
      case NE:
        retval = FnNot.fnNot(OperationFunctions.opDurationEqual((IDurationItem) left, (IDurationItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else if (left instanceof IBase64BinaryItem && right instanceof IBase64BinaryItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opBase64BinaryEqual((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        break;
      case GE: {
        IBooleanItem gt
            = OperationFunctions.opBase64BinaryGreaterThan((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        IBooleanItem eq = OperationFunctions.opBase64BinaryEqual((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = OperationFunctions.opBase64BinaryGreaterThan((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        break;
      case LE: {
        IBooleanItem lt
            = OperationFunctions.opBase64BinaryLessThan((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        IBooleanItem eq = OperationFunctions.opBase64BinaryEqual((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = OperationFunctions.opBase64BinaryLessThan((IBase64BinaryItem) left, (IBase64BinaryItem) right);
        break;
      case NE:
        retval = FnNot
            .fnNot(OperationFunctions.opBase64BinaryEqual((IBase64BinaryItem) left, (IBase64BinaryItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else {
      throw new InvalidTypeMetapathException(String.format("invalid types for comparison: %s %s %s", left.getClass().getName(),
          operator.name().toLowerCase(), right.getClass().getName()));
    }
    return retval;
  }
}
