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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate;

import gov.nist.secauto.metaschema.model.common.datatype.adapter.IBase64BinaryItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDateItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDateTimeItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDurationItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.INumericItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IStringItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IYearMonthDurationItem;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Addition;
import gov.nist.secauto.metaschema.model.common.metapath.ast.And;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.DecimalLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Division;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.FunctionCall;
import gov.nist.secauto.metaschema.model.common.metapath.ast.GeneralComparison;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IComparison;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerDivision;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Mod;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Multiplication;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Negate;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Or;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ParenthesizedExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Step;
import gov.nist.secauto.metaschema.model.common.metapath.ast.StringConcat;
import gov.nist.secauto.metaschema.model.common.metapath.ast.StringLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Subtraction;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Union;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ValueComparison;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.function.XPathFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnNotFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.InvalidTypeMetapathException;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaschemaPathEvaluationVisitor
    extends AbstractExpressionEvaluationVisitor {

  private final DynamicContext dynamicContext;

  public MetaschemaPathEvaluationVisitor(DynamicContext context) {
    Objects.requireNonNull(context, "context");
    this.dynamicContext = context;
  }

  protected DynamicContext getDynamicContext() {
    return dynamicContext;
  }

  @NotNull
  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> resultOrEmptySequence(ITEM_TYPE item) {
    return item == null ? ISequence.empty() : ISequence.of(item);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitAnd(And expr, INodeContext context) {
    boolean retval = true;
    for (IExpression child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      if (!XPathFunctions.fnBooleanAsPrimative(result)) {
        retval = false;
        break;
      }
    }
    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public ISequence<? extends IBooleanItem> visitOr(Or expr, INodeContext context) {
    boolean retval = false;
    for (IExpression child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      if (XPathFunctions.fnBooleanAsPrimative(result)) {
        retval = true;
        break;
      }
    }
    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public ISequence<? extends IBooleanItem> visitValueComparison(ValueComparison expr, INodeContext context) {
    IItem leftItem = FunctionUtils.getFirstItem(expr.getLeft().accept(this, context), false);
    if (leftItem == null) {
      return ISequence.empty();
    }
    IItem rightItem = FunctionUtils.getFirstItem(expr.getRight().accept(this, context), false);
    if (rightItem == null) {
      return ISequence.empty();
    }

    IAnyAtomicItem left = XPathFunctions.fnDataItem(leftItem);
    IAnyAtomicItem right = XPathFunctions.fnDataItem(rightItem);
    IComparison.Operator operator = expr.getOperator();

    try {
      return resultOrEmptySequence(compare(left, operator, right));
    } catch (IllegalArgumentException ex) {
      throw new UnsupportedOperationException(String.format("The value expression '%s %s %s' is not supported",
          leftItem.getItemName(), operator.name().toLowerCase(), rightItem.getItemName()));
    }
  }

  protected IBooleanItem compare(@NotNull IAnyAtomicItem left, @NotNull IComparison.Operator operator,
      @NotNull IAnyAtomicItem right) {
    IBooleanItem retval = null;
    if (left instanceof IStringItem || right instanceof IStringItem) {
      switch (operator) {
      case EQ:
        retval = OperationFunctions.opNumericEqual(XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case GE:
        retval = OperationFunctions.opNumericGreaterThan(
            XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right), IIntegerItem.NEGATIVE_ONE);
        break;
      case GT:
        retval = OperationFunctions
            .opNumericGreaterThan(XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right), IIntegerItem.ZERO);
        break;
      case LE:
        retval = OperationFunctions.opNumericLessThan(XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ONE);
        break;
      case LT:
        retval = OperationFunctions.opNumericLessThan(XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case NE:
        retval = FnNotFunction.fnNot(OperationFunctions
            .opNumericEqual(XPathFunctions.fnCompare((IStringItem) left, (IStringItem) right), IIntegerItem.ZERO));
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
        retval = FnNotFunction.fnNot(OperationFunctions.opNumericEqual((INumericItem) left, (INumericItem) right));
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
        retval = FnNotFunction.fnNot(OperationFunctions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right));
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
        retval = FnNotFunction.fnNot(OperationFunctions.opDateTimeEqual((IDateTimeItem) left, (IDateTimeItem) right));
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
        retval = FnNotFunction.fnNot(OperationFunctions.opDateEqual((IDateItem) left, (IDateItem) right));
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
        retval = FnNotFunction.fnNot(OperationFunctions.opDurationEqual((IDurationItem) left, (IDurationItem) right));
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
        retval = FnNotFunction
            .fnNot(OperationFunctions.opBase64BinaryEqual((IBase64BinaryItem) left, (IBase64BinaryItem) right));
        break;
      default:
        throw new IllegalArgumentException(String.format("Unsupported operator '%s'", operator.name()));
      }
    } else {
      throw new InvalidTypeMetapathException(String.format("invalid types for comparison: %s %s %s", left.getItemName(),
          operator.name().toLowerCase(), right.getItemName()));
    }

    return retval;
  }

  @Override
  public @NotNull ISequence<? extends IBooleanItem> visitGeneralComparison(@NotNull GeneralComparison expr,
      @NotNull INodeContext context) {
    ISequence<? extends IAnyAtomicItem> leftItems = XPathFunctions.fnData(expr.getLeft().accept(this, context));
    ISequence<? extends IAnyAtomicItem> rightItems = XPathFunctions.fnData(expr.getRight().accept(this, context));
    IComparison.Operator operator = expr.getOperator();

    IBooleanItem retval = IBooleanItem.FALSE;
    // TODO: apply data on each iteration
    for (IAnyAtomicItem left : leftItems.asList()) {
      for (IAnyAtomicItem right : rightItems.asList()) {
        @NotNull
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

        try {
          IBooleanItem result = compare(leftCast, operator, rightCast);
          if (IBooleanItem.TRUE.equals(result)) {
            retval = IBooleanItem.TRUE;
          }
        } catch (IllegalArgumentException ex) {
          throw new UnsupportedOperationException(String.format("The value expression '%s %s %s' is not supported",
              left.getItemName(), operator.name().toLowerCase(), right.getItemName()));
        }
      }
    }
    return ISequence.of(retval);
  }

  @NotNull
  protected IAnyAtomicItem applyGeneralComparisonCast(@NotNull IAnyAtomicItem item, @NotNull IAnyAtomicItem other) {
    IAnyAtomicItem retval = other;
    if (item instanceof INumericItem) {
      retval = IDecimalItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IDayTimeDurationItem.cast(other);
    } else if (item instanceof IDayTimeDurationItem) {
      retval = IYearMonthDurationItem.cast(other);
    } else {
      retval = item.getItemType().cast(other);
    }
    return retval;
  }

  @Override
  public ISequence<? extends IDocumentNodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath expr, INodeContext context) {
    return context instanceof IDocumentNodeItem ? ISequence.of((IDocumentNodeItem) context) : ISequence.empty();
  }

  @Override
  public ISequence<?> visitRootSlashPath(RootSlashPath expr, INodeContext context) {
    if (context.getContextNodeItem() instanceof IDocumentNodeItem) {

      return expr.getNode().accept(this, context);
    } else {
      throw new UnsupportedOperationException("root searching is not supported on non-document nodes");
    }
  }

  @Override
  public ISequence<? extends INodeItem> visitContextItem(ContextItem expr, INodeContext context) {
    return ISequence.of(context.getContextNodeItem());
  }

  @Override
  public ISequence<?> visitRelativeSlashPath(RelativeSlashPath expr, INodeContext context) {
    IExpression left = expr.getLeft();

    @SuppressWarnings("unchecked")
    @NotNull
    ISequence<? extends INodeItem> leftResult = (ISequence<? extends INodeItem>) left.accept(this, context);

    return evaluateInNodeContext(leftResult, expr.getRight());
  }

  @SuppressWarnings("null")
  @NotNull
  protected ISequence<?> evaluateInNodeContext(@NotNull ISequence<? extends INodeItem> contextItems,
      @NotNull IExpression expr) {
    ISequence<?> retval;
    if (contextItems.isEmpty()) {
      retval = ISequence.empty();
    } else {
      // evaluate the right path in the context of the left's children
      Stream<? extends IItem> result = contextItems.asStream()
          // .flatMap(node -> Stream.concat(node.flags(), node.modelItems()))
          .flatMap(node -> expr.accept(this, node).asStream());
      retval = ISequence.of(result);
    }
    return retval;
  }

  @Override
  public ISequence<?> visitStep(Step expr, INodeContext context) {

    ISequence<?> stepResult = expr.getStep().accept(this, context);

    // evaluate the predicates for this step
    AtomicInteger index = new AtomicInteger();

    Stream<? extends IItem> stream = stepResult.asStream().map(item -> {
      // build a positional index of the items
      return Map.entry(BigInteger.valueOf(index.incrementAndGet()), item);
    }).filter(entry -> {
      @SuppressWarnings("null")
      @NotNull
      IItem item = entry.getValue();

      // return false if any predicate evaluates to false
      boolean result = !expr.getPredicates().stream().map(predicateExpr -> {
        boolean bool;
        if (predicateExpr instanceof IntegerLiteral) {
          // reduce the result to the matching item
          BigInteger predicateIndex = ((IntegerLiteral) predicateExpr).getValue();

          // get the position of the item
          final BigInteger position = entry.getKey();

          // it is a match if the position matches
          bool = position.equals(predicateIndex);
        } else {
          INodeContext childContext = (INodeContext) item;
          ISequence<?> predicateResult = predicateExpr.accept(this, childContext);
          bool = XPathFunctions.fnBoolean(predicateResult).toBoolean();
        }
        return bool;
      }).anyMatch(x -> !x);
      return result;
    }).map(entry -> entry.getValue());
    @SuppressWarnings("null")
    ISequence<?> retval = ISequence.of(stream);
    return retval;
  }

  @SuppressWarnings("null")
  @NotNull
  private static Stream<? extends IFlagNodeItem> matchFlags(@NotNull Flag expr, @NotNull INodeContext context) {
    Stream<? extends IFlagNodeItem> retval;
    if (expr.isName()) {
      String name = ((Name) expr.getNode()).getValue();
      IFlagNodeItem item = context.getFlagByName(name);
      retval = item == null ? Stream.empty() : Stream.of(item);
    } else {
      // wildcard
      retval = context.flags();
    }
    return retval;
  }

  @Override
  public ISequence<? extends IFlagNodeItem> visitFlag(@NotNull Flag expr, @NotNull INodeContext context) {
    return ISequence.of(matchFlags(expr, context));
  }

  @SuppressWarnings("null")
  @NotNull
  private static Stream<? extends IModelNodeItem> matchModelInstance(@NotNull ModelInstance expr,
      @NotNull INodeContext context) {
    Stream<? extends IModelNodeItem> retval;
    if (expr.isName()) {
      String name = ((Name) expr.getNode()).getValue();
      List<? extends IModelNodeItem> items = context.getModelItemsByName(name);
      retval = items.stream();
    } else {
      // wildcard
      retval = context.modelItems();
    }
    return retval;
  }

  @Override
  public ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance expr, INodeContext context) {
    return ISequence.of(matchModelInstance(expr, context));
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr,
      INodeContext context) {
    IExpression left = expr.getLeft();
    @SuppressWarnings("unchecked")
    ISequence<? extends INodeItem> leftResult
        = (ISequence<? extends INodeItem>) left.accept(this, context);

    Stream<? extends INodeItem> result = (Stream<? extends INodeItem>) leftResult.asStream()
        .flatMap(item -> {
          // evaluate the right path in the context of the left
          return search(expr.getRight(), item);
        });

    @SuppressWarnings("null")
    ISequence<? extends INodeItem> retval = ISequence.of(result);
    return retval;
  }

  @Override
  public ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath expr, INodeContext context) {
    return ISequence.of(search(expr.getNode(), context));
  }

  @NotNull
  protected Stream<? extends INodeItem> search(@NotNull IExpression expr, @NotNull INodeContext context) {
    Stream<? extends INodeItem> retval;
    // if (expr instanceof Flag) {
    // // check instances as a flag
    // retval = searchFlags((Flag) expr, context);
    // } else if (expr instanceof ModelInstance) {
    // // check instances as a ModelInstance
    // retval = searchModelInstances((ModelInstance) expr, context);
    // } else {
    // recurse tree
    // searchExpression(expr, context);
    retval = searchExpression(expr, context);
    ;
    // }
    return retval;
  }

  @NotNull
  protected Stream<? extends INodeItem> searchExpression(@NotNull IExpression expr, @NotNull INodeContext context) {

    // check the current node
    @SuppressWarnings("unchecked")
    Stream<? extends INodeItem> nodeMatches
        = (Stream<? extends INodeItem>) expr.accept(this, context).asStream();

    // create a stream of flags and model elements to check
    Stream<? extends IFlagNodeItem> flags = context.flags();
    Stream<? extends INodeItem> modelItems = context.modelItems();

    @SuppressWarnings("null")
    Stream<? extends INodeItem> childMatches = Stream.concat(flags, modelItems)
        .flatMap(instance -> {
          return searchExpression(expr, instance);
        });

    @SuppressWarnings("null")
    @NotNull
    Stream<? extends INodeItem> result = Stream.concat(nodeMatches, childMatches);
    return result;
  }

  /**
   * Recursively searches the node graph for {@link IModelNodeItem} instances that match the provided
   * {@link ModelInstance} expression. The resulting nodes are returned in document order.
   * 
   * @param modelInstance
   *          the search expression
   * @param context
   *          the current node context
   * @return a stream of matching model node items
   */
  @NotNull
  protected Stream<? extends IModelNodeItem> searchModelInstances(@NotNull ModelInstance modelInstance,
      @NotNull INodeContext context) {

    // check if the current node context matches the expression
    Stream<? extends IModelNodeItem> nodeMatches = matchModelInstance(modelInstance, context);

    // next iterate over the child model instances, if the context item is an assembly
    @SuppressWarnings("null")
    Stream<? extends IModelNodeItem> childMatches
        = context.modelItems().flatMap(modelItem -> {
          // apply the search criteria to these node items
          return searchModelInstances(modelInstance, modelItem);
        });

    // combine the results
    @SuppressWarnings("null")
    @NotNull
    Stream<? extends IModelNodeItem> retval = Stream.concat(nodeMatches, childMatches);
    return retval;
  }

  // /**
  // * Recursively searches the node graph for {@link IFlagNodeItem} instances that match the provided
  // * {@link Flag} expression. The resulting nodes are returned in document order.
  // *
  // * @param expr
  // * the search expression
  // * @param context
  // * the current node context
  // * @return a stream of matching flag node items
  // */
  // @NotNull
  // private Stream<? extends IFlagNodeItem> searchFlags(Flag expr, INodeContext context) {
  //
  // // check if any flags on the the current node context matches the expression
  // Stream<? extends IFlagNodeItem> retval = context.getMatchingChildFlags(expr);
  //
  // // next iterate over the child model instances, if the context item is an assembly
  // INodeItem contextItem = context.getContextNodeItem();
  //
  // if (contextItem instanceof IAssemblyNodeItem) {
  // IAssemblyNodeItem assemblyContextItem = (IAssemblyNodeItem) contextItem;
  //
  // Stream<? extends IFlagNodeItem> childFlagInstances =
  // assemblyContextItem.modelItems().flatMap(modelItem -> {
  // // apply the search criteria to these node items
  // return searchFlags(expr, modelItem);
  // });
  // retval = Stream.concat(retval, childFlagInstances);
  // }
  // return retval;
  // return Stream.empty();
  // }

  @Override
  public ISequence<? extends INumericItem> visitNegate(Negate expr, INodeContext context) {
    INumericItem item = FunctionUtils.toNumeric(expr.getChild().accept(this, context), true);
    if (item == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(OperationFunctions.opNumericUnaryMinus(item));
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitAddition(Addition expr, INodeContext context) {
    ISequence<?> leftSequence = expr.getLeft().accept(this, context);
    IAnyAtomicItem leftItem;
    {
      IItem item = FunctionUtils.getFirstItem(leftSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      leftItem = XPathFunctions.fnDataItem(item);
    }

    ISequence<?> rightSequence = expr.getRight().accept(this, context);
    IAnyAtomicItem rightItem;
    {
      IItem item = FunctionUtils.getFirstItem(rightSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      rightItem = XPathFunctions.fnDataItem(item);
    }

    IAnyAtomicItem retval = null;
    boolean supported = true;
    if (leftItem instanceof IDateItem) {
      IDateItem left = (IDateItem) leftItem;
      if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDate(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDate(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDateTimeItem) {
      IDateTimeItem left = (IDateTimeItem) leftItem;
      if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDateTime(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDateTime(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) leftItem;
      if (rightItem instanceof IDateItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDate((IDateItem) rightItem, left);
      } else if (rightItem instanceof IDateTimeItem) {
        retval = OperationFunctions.opAddYearMonthDurationToDateTime((IDateTimeItem) rightItem, left);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurations(left, (IYearMonthDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) leftItem;
      if (rightItem instanceof IDateItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDate((IDateItem) rightItem, left);
      } else if (rightItem instanceof IDateTimeItem) {
        retval = OperationFunctions.opAddDayTimeDurationToDateTime((IDateTimeItem) rightItem, left);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opAddDayTimeDurations(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(leftItem);
      INumericItem right = FunctionUtils.toNumeric(rightItem);
      retval = OperationFunctions.opNumericAdd(left, right);
    }
    if (!supported) {
      throw new UnsupportedOperationException(
          String.format("The expression '%s + %s' is not supported", leftItem.getItemName(), rightItem.getItemName()));
    }
    return resultOrEmptySequence(retval);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitSubtraction(Subtraction expr, INodeContext context) {
    ISequence<?> leftSequence = expr.getLeft().accept(this, context);
    IAnyAtomicItem leftItem;
    {
      IItem item = FunctionUtils.getFirstItem(leftSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      leftItem = XPathFunctions.fnDataItem(item);
    }

    ISequence<?> rightSequence = expr.getRight().accept(this, context);
    IAnyAtomicItem rightItem;
    {
      IItem item = FunctionUtils.getFirstItem(rightSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      rightItem = XPathFunctions.fnDataItem(item);
    }

    IAnyAtomicItem retval = null;
    boolean supported = true;
    if (leftItem instanceof IDateItem) {
      IDateItem left = (IDateItem) leftItem;

      if (rightItem instanceof IDateItem) {
        retval = OperationFunctions.opSubtractDates(left, (IDateItem) rightItem);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurationFromDate(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurationFromDate(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDateTimeItem) {
      IDateTimeItem left = (IDateTimeItem) leftItem;
      if (rightItem instanceof IDateTimeItem) {
        retval = OperationFunctions.opSubtractDateTimes(left, (IDateTimeItem) rightItem);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurationFromDateTime(left, (IYearMonthDurationItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurationFromDateTime(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) leftItem;
      if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opSubtractYearMonthDurations(left, (IYearMonthDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) leftItem;
      if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opSubtractDayTimeDurations(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(leftItem);
      INumericItem right = FunctionUtils.toNumeric(rightItem);
      retval = OperationFunctions.opNumericSubtract(left, right);
    }
    if (!supported) {
      throw new UnsupportedOperationException(
          String.format("The expression '%s - %s' is not supported", leftItem.getItemName(), rightItem.getItemName()));
    }
    return resultOrEmptySequence(retval);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitMultiplication(Multiplication expr, INodeContext context) {
    ISequence<?> leftSequence = expr.getLeft().accept(this, context);
    IAnyAtomicItem leftItem;
    {
      IItem item = FunctionUtils.getFirstItem(leftSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      leftItem = XPathFunctions.fnDataItem(item);
    }

    ISequence<?> rightSequence = expr.getRight().accept(this, context);
    IAnyAtomicItem rightItem;
    {
      IItem item = FunctionUtils.getFirstItem(rightSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      rightItem = XPathFunctions.fnDataItem(item);
    }

    IAnyAtomicItem retval = null;
    boolean supported = true;
    if (leftItem instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) leftItem;
      if (rightItem instanceof INumericItem) {
        retval = OperationFunctions.opMultiplyYearMonthDuration(left, (INumericItem) rightItem);
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) leftItem;
      if (rightItem instanceof INumericItem) {
        retval = OperationFunctions.opMultiplyDayTimeDuration(left, (INumericItem) rightItem);
      } else {
        supported = false;
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(leftItem);
      if (rightItem instanceof INumericItem) {
        INumericItem right = FunctionUtils.toNumeric(rightItem);
        retval = OperationFunctions.opNumericMultiply(left, right);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opMultiplyYearMonthDuration((IYearMonthDurationItem) rightItem, left);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opMultiplyDayTimeDuration((IDayTimeDurationItem) rightItem, left);
      } else {
        supported = false;
      }
    }
    if (!supported) {
      throw new UnsupportedOperationException(
          String.format("The expression '%s - %s' is not supported", leftItem.getItemName(), rightItem.getItemName()));
    }
    return resultOrEmptySequence(retval);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitDivision(Division expr, INodeContext context) {
    ISequence<?> leftSequence = expr.getLeft().accept(this, context);
    IAnyAtomicItem leftItem;
    {
      IItem item = FunctionUtils.getFirstItem(leftSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      leftItem = XPathFunctions.fnDataItem(item);
    }

    ISequence<?> rightSequence = expr.getRight().accept(this, context);
    IAnyAtomicItem rightItem;
    {
      IItem item = FunctionUtils.getFirstItem(rightSequence, true);
      if (item == null) {
        return ISequence.empty();
      }
      rightItem = XPathFunctions.fnDataItem(item);
    }

    IAnyAtomicItem retval = null;
    boolean supported = true;
    if (leftItem instanceof IYearMonthDurationItem) {
      IYearMonthDurationItem left = (IYearMonthDurationItem) leftItem;
      if (rightItem instanceof INumericItem) {
        retval = OperationFunctions.opDivideYearMonthDuration(left, (INumericItem) rightItem);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        // TODO: find a way to support this
        supported = false;
      } else {
        supported = false;
      }
    } else if (leftItem instanceof IDayTimeDurationItem) {
      IDayTimeDurationItem left = (IDayTimeDurationItem) leftItem;
      if (rightItem instanceof INumericItem) {
        retval = OperationFunctions.opDivideDayTimeDuration(left, (INumericItem) rightItem);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opDivideDayTimeDurationByDayTimeDuration(left, (IDayTimeDurationItem) rightItem);
      } else {
        supported = false;
      }
    } else {
      // handle as numeric
      INumericItem left = FunctionUtils.toNumeric(leftItem);
      if (rightItem instanceof INumericItem) {
        INumericItem right = FunctionUtils.toNumeric(rightItem);
        retval = OperationFunctions.opNumericDivide(left, right);
      } else if (rightItem instanceof IYearMonthDurationItem) {
        retval = OperationFunctions.opMultiplyYearMonthDuration((IYearMonthDurationItem) rightItem, left);
      } else if (rightItem instanceof IDayTimeDurationItem) {
        retval = OperationFunctions.opMultiplyDayTimeDuration((IDayTimeDurationItem) rightItem, left);
      } else {
        supported = false;
      }
    }
    if (!supported) {
      throw new UnsupportedOperationException(
          String.format("The expression '%s - %s' is not supported", leftItem.getItemName(), rightItem.getItemName()));
    }
    return resultOrEmptySequence(retval);
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, INodeContext context) {
    INumericItem left = FunctionUtils.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = FunctionUtils.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(OperationFunctions.opNumericIntegerDivide(left, right));
  }

  @Override
  public ISequence<? extends INumericItem> visitMod(Mod expr, INodeContext context) {
    INumericItem left = FunctionUtils.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = FunctionUtils.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(OperationFunctions.opNumericMod(left, right));
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, INodeContext context) {
    return ISequence.of(IIntegerItem.valueOf(expr.getValue()));
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, INodeContext context) {
    return ISequence.of(IDecimalItem.valueOf(expr.getValue()));
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<IStringItem> visitStringConcat(StringConcat expr, INodeContext context) {
    StringBuilder builder = new StringBuilder();
    for (IExpression child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      XPathFunctions.fnData(result).asStream().forEachOrdered(item -> {
        // TODO: is this right to concat all sequence members?
        builder.append(((IAnyAtomicItem) item).asString());
      });
    }
    return ISequence.of(IStringItem.valueOf(builder.toString()));
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<IStringItem> visitStringLiteral(StringLiteral expr, INodeContext context) {
    return ISequence.of(IStringItem.valueOf(expr.getValue()));
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<?> visitFunctionCall(FunctionCall expr, INodeContext context) {
    List<ISequence<?>> arguments = expr.getChildren().stream().map(expression -> {
      ISequence<?> result = expression.accept(this, context);
      return result;
    }).collect(Collectors.toList());

    IFunction function = expr.getFunction();
    return function.execute(arguments, getDynamicContext(), context);
  }

  @Override
  public ISequence<?> visitMetapath(Metapath expr, INodeContext context) {
    return ISequence.of(expr.getChildren().stream().flatMap(child -> {
      ISequence<?> result = child.accept(this, context);
      return result.asStream();
    }));
  }

  @Override
  public ISequence<?> visitParenthesizedExpression(ParenthesizedExpression expr, INodeContext context) {
    IExpression childExpr = expr.getChild();
    return childExpr.accept(this, context);
  }

  @Override
  public ISequence<?> visitUnion(Union expr, INodeContext context) {
    return ISequence.of(expr.getChildren().stream().flatMap(child -> {
      ISequence<?> result = child.accept(this, context);
      return result.asStream();
    }).distinct());
  }

}
