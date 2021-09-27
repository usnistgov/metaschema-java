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

package gov.nist.secauto.metaschema.binding.metapath;

import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.ast.AbstractExpressionEvaluationVisitor;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.OrNode;
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
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.impl.Functions;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.InvalidTypeException;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaschemaPathEvaluationVisitor
    extends AbstractExpressionEvaluationVisitor<INodeContext> {

  public <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> visit(IExpression<?> expr, INodeContext context) {
    @SuppressWarnings("unchecked")
    ISequence<ITEM_TYPE> retval = (ISequence<ITEM_TYPE>) expr.accept(this, context);
    return retval;
  }

  @Override
  public ISequence<? extends IBooleanItem> visitAnd(And expr, INodeContext context) {
    boolean retval = true;
    for (IExpression<?> child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      if (!Functions.fnBooleanAsPrimative(result)) {
        retval = false;
        break;
      }
    }
    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public ISequence<? extends IBooleanItem> visitOr(OrNode expr, INodeContext context) {
    boolean retval = false;
    for (IExpression<?> child : expr.getChildren()) {
      ISequence<?> result = child.accept(this, context);
      if (Functions.fnBooleanAsPrimative(result)) {
        retval = true;
        break;
      }
    }
    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public ISequence<? extends IBooleanItem> visitComparison(Comparison expr, INodeContext context) {
    IItem leftItem = Functions.getFirstItem(expr.getLeft().accept(this, context), false);
    if (leftItem == null) {
      return ISequence.empty();
    }
    IItem rightItem = Functions.getFirstItem(expr.getRight().accept(this, context), false);
    if (rightItem == null) {
      return ISequence.empty();
    }

    IAnyAtomicItem left = Functions.fnDataItem(leftItem);
    IAnyAtomicItem right = Functions.fnDataItem(rightItem);

    Comparison.Operator operator = expr.getOperator();
    IBooleanItem retval;
    if (left instanceof IStringItem || right instanceof IStringItem) {
      switch (operator) {
      case EQ:
        retval
            = Functions.opNumericEqual(Functions.fnCompare((IStringItem) left, (IStringItem) right), IIntegerItem.ZERO);
        break;
      case GE:
        retval = Functions.opNumericGreaterThan(Functions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.NEGATIVE_ONE);
        break;
      case GT:
        retval = Functions.opNumericGreaterThan(Functions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case LE:
        retval = Functions.opNumericLessThan(Functions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ONE);
        break;
      case LT:
        retval = Functions.opNumericLessThan(Functions.fnCompare((IStringItem) left, (IStringItem) right),
            IIntegerItem.ZERO);
        break;
      case NE:
        retval = Functions.fnNot(
            Functions.opNumericEqual(Functions.fnCompare((IStringItem) left, (IStringItem) right), IIntegerItem.ZERO));
        break;
      default:
        throw new UnsupportedOperationException(operator.name());
      }
    } else if (left instanceof INumericItem && right instanceof INumericItem) {
      switch (operator) {
      case EQ:
        retval = Functions.opNumericEqual((INumericItem) left, (INumericItem) right);
        break;
      case GE: {
        IBooleanItem gt = Functions.opNumericGreaterThan((INumericItem) left, (INumericItem) right);
        IBooleanItem eq = Functions.opNumericEqual((INumericItem) left, (INumericItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = Functions.opNumericGreaterThan((INumericItem) left, (INumericItem) right);
        break;
      case LE: {
        IBooleanItem lt = Functions.opNumericLessThan((INumericItem) left, (INumericItem) right);
        IBooleanItem eq = Functions.opNumericEqual((INumericItem) left, (INumericItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = Functions.opNumericLessThan((INumericItem) left, (INumericItem) right);
        break;
      case NE:
        retval = Functions.fnNot(Functions.opNumericEqual((INumericItem) left, (INumericItem) right));
        break;
      default:
        throw new UnsupportedOperationException(operator.name());
      }
    } else if (left instanceof IBooleanItem && right instanceof IBooleanItem) {
      switch (operator) {
      case EQ:
        retval = Functions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        break;
      case GE: {
        IBooleanItem gt = Functions.opBooleanGreaterThan((IBooleanItem) left, (IBooleanItem) right);
        IBooleanItem eq = Functions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        retval = IBooleanItem.valueOf(gt.toBoolean() || eq.toBoolean());
        break;
      }
      case GT:
        retval = Functions.opBooleanGreaterThan((IBooleanItem) left, (IBooleanItem) right);
        break;
      case LE: {
        IBooleanItem lt = Functions.opBooleanLessThan((IBooleanItem) left, (IBooleanItem) right);
        IBooleanItem eq = Functions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right);
        retval = IBooleanItem.valueOf(lt.toBoolean() || eq.toBoolean());
        break;
      }
      case LT:
        retval = Functions.opBooleanLessThan((IBooleanItem) left, (IBooleanItem) right);
        break;
      case NE:
        retval = Functions.fnNot(Functions.opBooleanEqual((IBooleanItem) left, (IBooleanItem) right));
        break;
      default:
        throw new UnsupportedOperationException(operator.name());
      }
    } else {
      throw new InvalidTypeException(String.format("invalid types for comparison: %s %s %s", left.getClass(),
          operator.name().toLowerCase(), right.getClass()));
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
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> retval = (ISequence<? extends INodeItem>) expr.getNode().accept(this, context);
      return retval;
    } else {
      throw new UnsupportedOperationException("root searching is not supported");
    }
  }

  @Override
  public ISequence<? extends INodeItem> visitContextItem(ContextItem expr, INodeContext context) {
    return ISequence.of(context.getNodeItem());
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeSlashPath(RelativeSlashPath expr, INodeContext context) {
    IExpression<?> left = expr.getLeft();
    @SuppressWarnings("unchecked")
    ISequence<? extends INodeItem> leftResult = (ISequence<? extends INodeItem>) left.accept(this, context);
    IExpression<?> right = expr.getRight();

    List<INodeItem> result = new LinkedList<INodeItem>();
    leftResult.asStream().forEachOrdered(item -> {
      INodeItem node = (INodeItem) item;

      // evaluate the right path in the context of the left
      @SuppressWarnings("unchecked")
      ISequence<? extends INodeItem> otherResult = (ISequence<? extends INodeItem>) right.accept(this, node);
      otherResult.asStream().forEachOrdered(otherItem -> {
        result.add(otherItem);
      });
    });
    return ISequence.of(result);
  }

  @Override
  public ISequence<? extends INodeItem> visitStep(Step expr, INodeContext context) {
    @SuppressWarnings("unchecked")
    ISequence<? extends INodeItem> retval = (ISequence<? extends INodeItem>) expr.getStep().accept(this, context);

    // evaluate the predicates for this step
    AtomicInteger index = new AtomicInteger();
    Stream<? extends INodeItem> stream = retval.asStream().map(item -> {
      // build a positional index of the items
      return Map.entry(BigInteger.valueOf(index.incrementAndGet()), item);
    }).filter(entry -> {
      IItem item = entry.getValue();
      INodeContext childContext;
      if (item instanceof INodeItem) {
        childContext = (INodeItem) item;
      } else {
        childContext = context;
      }

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
          bool = Functions.fnBoolean(predicateResult).toBoolean();
        }
        return bool;
      }).anyMatch(x -> !x);
      return result;
    }).map(entry -> entry.getValue());
    return ISequence.of(stream);
  }

  @Override
  public ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, INodeContext context) {
    return ISequence.of(context.getChildFlags(expr));
  }

  @Override
  public ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance expr, INodeContext context) {
    return ISequence.of(context.getChildModelInstances(expr));
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr,
      INodeContext context) {
    IExpression<?> left = expr.getLeft();
    @SuppressWarnings("unchecked")
    ISequence<? extends INodeItem> leftResult = (ISequence<? extends INodeItem>) left.accept(this, context);

    Stream<? extends INodeItem> result = leftResult.asStream().flatMap(item -> {
      // evaluate the right path in the context of the left
      return search(expr.getRight(), item);
    });
    return ISequence.of(result);
  }

  @Override
  public ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath expr, INodeContext context) {
    return ISequence.of(search(expr.getNode(), context));
  }

  private Stream<? extends INodeItem> search(IExpression<?> expr, INodeContext context) {
    Stream<? extends INodeItem> retval;
    if (expr instanceof Flag) {
      // check instances as a flag
      retval = searchFlags((Flag) expr, context);
    } else if (expr instanceof ModelInstance) {
      // check instances as a ModelInstance
      retval = searchModelInstances((ModelInstance) expr, context);
    } else {
      // recurse tree
      retval = context.getChildInstances(this, expr, true);
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
  private Stream<? extends IModelNodeItem> searchModelInstances(ModelInstance expr, INodeContext context) {

    // check if the current node context matches the expression
    Stream<? extends IModelNodeItem> retval = context.getChildModelInstances(expr);

    // next iterate over the child model instances, if they are an assembly
    final INodeItem contextItem = context.getNodeItem();
    IDefinition definition = contextItem.getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) definition;
      Collection<? extends NamedModelProperty> instances = assembly.getNamedModelInstances().values();

      Stream<? extends IModelNodeItem> childModelInstances = instances.stream().flatMap(property -> {
        // get the node items for the children, using their associated values
        return property.getNodeItemsFromParentInstance((IAssemblyNodeItem) contextItem);
      }).flatMap(item -> {
        // apply the search criteria to these node items
        return searchModelInstances(expr, item);
      }).sequential();

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
  private Stream<IFlagNodeItem> searchFlags(Flag expr, INodeContext context) {

    // check if any flags on the the current node context matches the expression
    Stream<IFlagNodeItem> retval = context.getChildFlags(expr);
    // List<IFlagNodeItem> flags = retval.collect(Collectors.toList());
    // retval = flags.stream();

    // next iterate over the child model instances, if they are an assembly
    final INodeItem contextItem = context.getNodeItem();
    IDefinition definition = contextItem.getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) definition;
      Collection<? extends NamedModelProperty> instances = assembly.getNamedModelInstances().values();

      Stream<IFlagNodeItem> childFlags = instances.stream().flatMap(property -> {
        // get the node items for the children, using their associated values
        return property.getNodeItemsFromParentInstance((IAssemblyNodeItem) contextItem);
      }).flatMap(item -> {
        // apply the search criteria to these node items
        return searchFlags(expr, item);
      });

      retval = Stream.concat(retval, childFlags).sequential();
    }
    return retval;
  }

  @Override
  public ISequence<? extends INumericItem> visitNegate(Negate expr, INodeContext context) {
    INumericItem item = Functions.toNumeric(expr.getChild().accept(this, context), true);
    if (item == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericUnaryMinus(item));
  }

  @Override
  public ISequence<? extends INumericItem> visitAddition(Addition expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericAdd(left, right));
  }

  @Override
  public ISequence<? extends INumericItem> visitSubtraction(Subtraction expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericSubtract(left, right));
  }

  @Override
  public ISequence<? extends INumericItem> visitMultiplication(Multiplication expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericMultiply(left, right));
  }

  @Override
  public ISequence<? extends INumericItem> visitDivision(Division expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericDivide(left, right));
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericIntegerDivide(left, right));
  }

  private <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> resultOrEmptySequence(ITEM_TYPE item) {
    return item == null ? ISequence.empty() : ISequence.of(item);
  }

  @Override
  public ISequence<? extends INumericItem> visitMod(Mod expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.empty();
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.empty();
    }
    return resultOrEmptySequence(Functions.opNumericMod(left, right));
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
      Functions.fnData(result).asStream().forEachOrdered(item -> {
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
    arguments = function.convertArguments(function, arguments);
    return function.execute(arguments);
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
