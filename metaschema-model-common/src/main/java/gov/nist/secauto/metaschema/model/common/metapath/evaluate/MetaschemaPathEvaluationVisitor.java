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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Comparison;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.DecimalLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Division;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.FunctionCall;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerDivision;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Mod;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Multiplication;
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
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.OperationFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.function.XPathFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnNotFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.InvalidTypeMetapathException;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaschemaPathEvaluationVisitor extends AbstractExpressionEvaluationVisitor {

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
    for (IExpression<?> child : expr.getChildren()) {
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
    for (IExpression<?> child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      if (XPathFunctions.fnBooleanAsPrimative(result)) {
        retval = true;
        break;
      }
    }
    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public ISequence<? extends IBooleanItem> visitComparison(Comparison expr, INodeContext context) {
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

    Comparison.Operator operator = expr.getOperator();
    IBooleanItem retval = null;
    boolean supported = true;
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
        supported = false;
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
        supported = false;
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
        supported = false;
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
        supported = false;
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
        supported = false;
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
          supported = false;
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
          supported = false;
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
          supported = false;
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
          supported = false;
        }
        break;
      }
      case NE:
        retval = FnNotFunction.fnNot(OperationFunctions.opDurationEqual((IDurationItem) left, (IDurationItem) right));
        break;
      default:
        supported = false;
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
        supported = false;
      }
    } else {
      throw new InvalidTypeMetapathException(String.format("invalid types for comparison: %s %s %s", left.getItemName(),
          operator.name().toLowerCase(), right.getItemName()));
    }

    if (!supported) {
      throw new UnsupportedOperationException(String.format("The expression '%s %s %s' is not supported",
          leftItem.getItemName(), operator.name().toLowerCase(), rightItem.getItemName()));
    }

    return resultOrEmptySequence(retval);
  }

  @Override
  public ISequence<? extends INodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath expr, INodeContext context) {
    return ISequence.empty();
  }

  @Override
  public ISequence<? extends INodeItem> visitRootSlashPath(RootSlashPath expr, INodeContext context) {
    if (context.getNodeItem().isRootNode()) {
      @SuppressWarnings("unchecked") ISequence<? extends INodeItem> retval
          = (ISequence<? extends INodeItem>) expr.getNode().accept(this, context);
      return retval;
    } else {
      throw new UnsupportedOperationException("root searching is not supported on non-root nodes");
    }
  }

  @Override
  public ISequence<? extends INodeItem> visitContextItem(ContextItem expr, INodeContext context) {
    return ISequence.of(context.getNodeItem());
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeSlashPath(RelativeSlashPath expr, INodeContext context) {
    IExpression<?> left = expr.getLeft();
    @SuppressWarnings("unchecked") ISequence<? extends INodeItem> leftResult
        = (ISequence<? extends INodeItem>) left.accept(this, context);
    IExpression<?> right = expr.getRight();

    List<gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem> result = new LinkedList<>();
    leftResult.asStream().forEachOrdered(item -> {
      INodeItem node = (INodeItem) item;

      // evaluate the right path in the context of the left
      @SuppressWarnings("unchecked") ISequence<? extends INodeItem> otherResult
          = (ISequence<? extends INodeItem>) right.accept(this, node);
      otherResult.asStream().forEachOrdered(otherItem -> {
        result.add(otherItem);
      });
    });
    return ISequence.of(result);
  }

  @Override
  public ISequence<? extends INodeItem> visitStep(Step expr, INodeContext context) {
    @SuppressWarnings("unchecked") ISequence<? extends INodeItem> stepResult
        = (ISequence<? extends INodeItem>) expr.getStep().accept(this, context);

    // evaluate the predicates for this step
    AtomicInteger index = new AtomicInteger();

    Stream<? extends INodeItem> stream = stepResult.asStream().map(item -> {
      // build a positional index of the items
      return Map.entry(BigInteger.valueOf(index.incrementAndGet()), item);
    }).filter(entry -> {
      @SuppressWarnings("null")
      @NotNull INodeItem item = entry.getValue();
      INodeContext childContext = item;

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
          ISequence<?> predicateResult = predicateExpr.accept(this, childContext);
          bool = XPathFunctions.fnBoolean(predicateResult).toBoolean();
        }
        return bool;
      }).anyMatch(x -> !x);
      return result;
    }).map(entry -> entry.getValue());
    @SuppressWarnings("null") ISequence<? extends INodeItem> retval = ISequence.of(stream);
    return retval;
  }

  @Override
  public ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, INodeContext context) {
    return ISequence.of(context.getMatchingChildFlags(expr));
  }

  @Override
  public ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance expr, INodeContext context) {
    return ISequence.of(context.getMatchingChildModelInstances(expr));
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr,
      INodeContext context) {
    IExpression<?> left = expr.getLeft();
    @SuppressWarnings("unchecked") ISequence<? extends INodeItem> leftResult
        = (ISequence<? extends INodeItem>) left.accept(this, context);

    Stream<? extends INodeItem> result = leftResult.asStream().flatMap(item -> {
      // evaluate the right path in the context of the left
      return search(expr.getRight(), item);
    });

    @SuppressWarnings("null") ISequence<? extends INodeItem> retval = ISequence.of(result);
    return retval;
  }

  @Override
  public ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath expr, INodeContext context) {
    return ISequence.of(search(expr.getNode(), context));
  }

  @NotNull
  protected Stream<? extends INodeItem> search(@NotNull IExpression<?> expr, @NotNull INodeContext context) {
    Stream<? extends INodeItem> retval;
    if (expr instanceof Flag) {
      // check instances as a flag
      retval = searchFlags((Flag) expr, context);
    } else if (expr instanceof ModelInstance) {
      // check instances as a ModelInstance
      retval = searchModelInstances((ModelInstance) expr, context);
    } else {
      // recurse tree
      retval = context.getMatchingChildInstances(this, expr, true);
    }
    return retval;
  }

  /**
   * Recursively searches the node graph for {@link IModelNodeItem} instances that match the provided
   * {@link ModelInstance} expression. The resulting nodes are returned in document order.
   * 
   * @param expr
   *          the search expression
   * @param context
   *          the current node context
   * @return a stream of matching model node items
   */
  @NotNull
  protected Stream<? extends IModelNodeItem> searchModelInstances(@NotNull ModelInstance expr,
      @NotNull INodeContext context) {

    // check if the current node context matches the expression
    Stream<? extends IModelNodeItem> retval = context.getMatchingChildModelInstances(expr);

    // next iterate over the child model instances, if the context item is an assembly
    INodeItem contextItem = context.getNodeItem();

    if (contextItem instanceof IAssemblyNodeItem) {
      IAssemblyNodeItem assemblyContextItem = (IAssemblyNodeItem) contextItem;

      @SuppressWarnings("null") Stream<? extends IModelNodeItem> childModelInstances
          = assemblyContextItem.modelItems().flatMap(modelItem -> {
            // apply the search criteria to these node items
            return searchModelInstances(expr, modelItem);
          });
      retval = Stream.concat(retval, childModelInstances);
    }
    return retval;
  }

  /**
   * Recursively searches the node graph for {@link IFlagNodeItem} instances that match the provided
   * {@link Flag} expression. The resulting nodes are returned in document order.
   * 
   * @param expr
   *          the search expression
   * @param context
   *          the current node context
   * @return a stream of matching flag node items
   */
  private Stream<? extends IFlagNodeItem> searchFlags(Flag expr, INodeContext context) {

    // check if any flags on the the current node context matches the expression
    Stream<? extends IFlagNodeItem> retval = context.getMatchingChildFlags(expr);

    // next iterate over the child model instances, if the context item is an assembly
    INodeItem contextItem = context.getNodeItem();

    if (contextItem instanceof IAssemblyNodeItem) {
      IAssemblyNodeItem assemblyContextItem = (IAssemblyNodeItem) contextItem;

      Stream<? extends IFlagNodeItem> childFlagInstances = assemblyContextItem.modelItems().flatMap(modelItem -> {
        // apply the search criteria to these node items
        return searchFlags(expr, modelItem);
      });
      retval = Stream.concat(retval, childFlagInstances);
    }
    return retval;
  }

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

  @Override
  public ISequence<IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, INodeContext context) {
    return ISequence.of(IIntegerItem.valueOf(expr.getValue()));
  }

  @Override
  public ISequence<IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, INodeContext context) {
    return ISequence.of(IDecimalItem.valueOf(expr.getValue()));
  }

  @Override
  public ISequence<IStringItem> visitStringConcat(StringConcat expr, INodeContext context) {
    StringBuilder builder = new StringBuilder();
    for (IExpression<?> child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      XPathFunctions.fnData(result).asStream().forEachOrdered(item -> {
        // TODO: is this right to concat all sequence members?
        builder.append(((IAnyAtomicItem) item).asString());
      });
    }
    return ISequence.of(IStringItem.valueOf(builder.toString()));
  }

  @Override
  public ISequence<IStringItem> visitStringLiteral(StringLiteral expr, INodeContext context) {
    return ISequence.of(IStringItem.valueOf(expr.getValue()));
  }

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
    IExpression<?> childExpr = expr.getChild();
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