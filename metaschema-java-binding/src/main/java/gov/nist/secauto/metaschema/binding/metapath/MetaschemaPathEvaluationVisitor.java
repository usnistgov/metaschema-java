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

import gov.nist.secauto.metaschema.binding.metapath.type.INodeItem;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;
import gov.nist.secauto.metaschema.datatypes.metaschema.IAtomicItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IBooleanItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IDecimalItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IIntegerItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.IMetapathResult;
import gov.nist.secauto.metaschema.datatypes.metaschema.INumericItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.ISequence;
import gov.nist.secauto.metaschema.datatypes.metaschema.IStringItem;
import gov.nist.secauto.metaschema.datatypes.metaschema.InvalidTypeException;
import gov.nist.secauto.metaschema.datatypes.metaschema.ListSequence;
import gov.nist.secauto.metaschema.datatypes.metaschema.SingletonSequence;
import gov.nist.secauto.metaschema.datatypes.metaschema.StringItem;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.ast.AbstractExpressionVisitor;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Wildcard;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaschemaPathEvaluationVisitor extends AbstractExpressionVisitor<IMetapathResult, INodeContext> {

  public IMetapathResult visit(IExpression expr, INodeContext context) {
    return expr.accept(this, context);
  }

  @Override
  public IBooleanItem visitAnd(And expr, INodeContext context) {
    boolean retval = true;
    for (IExpression child : expr.getChildren()) {
      IMetapathResult result = child.accept(this, context);
      IBooleanItem booleanResult = Functions.fnBoolean(result.toSequence());
      if (IBooleanItem.FALSE.equals(booleanResult)) {
        retval = false;
        break;
      }
    }
    return IBooleanItem.valueOf(retval);
  }

  @Override
  public IBooleanItem visitOr(OrNode expr, INodeContext context) {
    boolean retval = false;
    for (IExpression child : expr.getChildren()) {
      IMetapathResult result = child.accept(this, context);
      IBooleanItem booleanResult = Functions.fnBoolean(result.toSequence());
      if (IBooleanItem.TRUE.equals(booleanResult)) {
        retval = true;
        break;
      }
    }
    return IBooleanItem.valueOf(retval);
  }

  @Override
  public IMetapathResult visitComparison(Comparison expr, INodeContext context) {
    IItem leftItem = Functions.getFirstItem(expr.getLeft().accept(this, context).toSequence(), false);
    if (leftItem == null) {
      return ISequence.EMPTY;
    }
    IItem rightItem = Functions.getFirstItem(expr.getRight().accept(this, context).toSequence(), false);
    if (rightItem == null) {
      return ISequence.EMPTY;
    }

    IAtomicItem left = Functions.fnDataItem(leftItem);
    IAtomicItem right = Functions.fnDataItem(rightItem);

    Comparison.Operator operator = expr.getOperator();
    IBooleanItem retval;
    if (left instanceof IStringItem || right instanceof IStringItem) {
      left = left.toStringItem();
      right = right.toStringItem();

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
  public ISequence visitRootSlashOnlyPath(RootSlashOnlyPath expr, INodeContext context) {
    return ISequence.EMPTY;
  }

  @Override
  public IMetapathResult visitRootSlashPath(RootSlashPath expr, INodeContext context) {
    if (context.getNodeItem().isRootNode()) {
      return expr.getNode().accept(this, context);
    } else {
      throw new UnsupportedOperationException("root searching is not supported");
    }
  }

  @Override
  public IMetapathResult visitContextItem(ContextItem expr, INodeContext context) {
    return context.getNodeItem();
  }

  @Override
  public IMetapathResult visitRelativeSlashPath(RelativeSlashPath expr, INodeContext context) {
    IExpression left = expr.getLeft();
    IMetapathResult leftResult = left.accept(this, context);
    IExpression right = expr.getRight();

    List<INodeItem> result = new LinkedList<INodeItem>();
    leftResult.toSequence().asStream().forEachOrdered(item -> {
      INodeItem node = (INodeItem) item;

      // evaluate the right path in the context of the left
      IMetapathResult otherResult = right.accept(this, node);
      otherResult.toSequence().asStream().map(x -> (INodeItem) x).forEachOrdered(otherItem -> {
        result.add(otherItem);
      });
    });
    return ISequence.of(result);
  }

  @Override
  public ISequence visitStep(Step expr, INodeContext context) {
    IMetapathResult retval = expr.getStep().accept(this, context);

    // evaluate the predicates for this step
    AtomicInteger index = new AtomicInteger();
    Stream<? extends IItem> stream = retval.toSequence().asStream().map(item -> {
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
        IMetapathResult predicateResult = predicateExpr.accept(this, childContext);
        boolean bool;
        if (predicateExpr instanceof IntegerLiteral) {
          // reduce the result to the matching item
          BigInteger predicateIndex = ((IntegerLiteral) predicateExpr).getValue();

          // get the position of the item
          final BigInteger position = entry.getKey();

          // it is a match if the position matches
          bool = position.equals(predicateIndex);
        } else {
          bool = Functions.fnBoolean(predicateResult).toBoolean();
        }
        return bool;
      }).anyMatch(x -> !x);
      return result;
    }).map(entry -> entry.getValue());
    return ISequence.of(stream);
  }

  @Override
  public ISequence visitFlag(Flag expr, INodeContext context) {
    return ISequence.of(context.getChildFlags(expr));
  }

  @Override
  public ISequence visitModelInstance(ModelInstance expr, INodeContext context) {
    return ISequence.of(context.getChildModelInstances(expr));
  }

  @Override
  public ISequence visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, INodeContext context) {
    IExpression left = expr.getLeft();
    IMetapathResult leftResult = left.accept(this, context);

    Stream<INodeItem> result = leftResult.toSequence().asStream().map(x -> (INodeItem) x).flatMap(item -> {
      // evaluate the right path in the context of the left
      return search(expr.getRight(), item);
    });
    return ISequence.of(result);
  }

  @Override
  public ISequence visitRootDoubleSlashPath(RootDoubleSlashPath expr, INodeContext context) {
    return ISequence.of(search(expr.getNode(), context));
  }

  private Stream<INodeItem> search(IExpression expr, INodeContext context) {
    Stream<INodeItem> retval;
    if (expr instanceof Flag) {
      // check instances as a flag
      retval = searchFlags((Flag) expr, context);
    } else if (expr instanceof ModelInstance) {
      // check instances as a ModelInstance
      retval = searchModelInstances((ModelInstance) expr, context);
    } else {
      // recurse tree
      retval = searchExpression(expr, context);
    }
    return retval;
  }

  // TODO: fix this, not in document order
  private Stream<INodeItem> searchModelInstances(ModelInstance expr, INodeContext context) {

    Stream<INodeItem> retval = context.getChildModelInstances(expr);

    final INodeItem contextItem = context.getNodeItem();

    IDefinition definition = contextItem.getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) definition;
      Collection<? extends NamedModelProperty> instances = assembly.getNamedModelInstances().values();

      Stream<INodeItem> childModelInstances = instances.stream().flatMap(property -> {
        return contextItem.newChildNodeItems(property, property.getItemsFromParentInstance(contextItem.getValue()));
      }).flatMap(item -> {
        return searchModelInstances(expr, item);
      }).sequential();

      retval = Stream.concat(retval, childModelInstances);
    }
    return retval;
  }

  /**
   * Recursively search the model of each ancestor instance for matching flags.
   * 
   * @param expr
   *          the flag expresion to search using
   * @param context
   *          the current node context
   * @return a stream of matching flags
   */
  private Stream<INodeItem> searchFlags(Flag expr, INodeContext context) {

    Stream<INodeItem> retval = context.getChildFlags(expr);
    List<INodeItem> flags = retval.collect(Collectors.toList());
    retval = flags.stream();

    final INodeItem contextItem = context.getNodeItem();

    IDefinition definition = contextItem.getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) definition;
      Collection<? extends NamedModelProperty> instances = assembly.getNamedModelInstances().values();

      Stream<INodeItem> childFlags = instances.stream().flatMap(property -> {
        return contextItem.newChildNodeItems(property, property.getItemsFromParentInstance(contextItem.getValue()));
      }).flatMap(item -> {
        return searchFlags(expr, item);
      }).sequential();

      retval = Stream.concat(retval, childFlags).sequential();
    }
    return retval;
  }

  /**
   * Recursively search the model of each ancestor instance for nodes matching the expression.
   * 
   * @param expr
   *          the flag expresion to search using
   * @param context
   *          the current node context
   * @return a stream of matching flags
   */
  // TODO: fix this, not in document order
  private Stream<INodeItem> searchExpression(IExpression expr, INodeContext context) {

    @SuppressWarnings("unchecked")
    Stream<INodeItem> retval = (Stream<INodeItem>) expr.accept(this, context).toSequence().asStream();
    List<INodeItem> flags = retval.collect(Collectors.toList());
    retval = flags.stream();

    final INodeItem contextItem = context.getNodeItem();

    IDefinition definition = contextItem.getPathSegment().getDefinition();
    if (definition instanceof AssemblyClassBinding) {
      AssemblyClassBinding assembly = (AssemblyClassBinding) definition;
      Collection<? extends NamedModelProperty> instances = assembly.getNamedModelInstances().values();

      Stream<INodeItem> children = instances.stream().flatMap(property -> {
        return contextItem.newChildNodeItems(property, property.getItemsFromParentInstance(contextItem.getValue()));
      }).flatMap(item -> {
        return searchExpression(expr, item);
      }).sequential();

      retval = Stream.concat(retval, children);
    }
    return retval;
  }

  @Override
  public IStringItem visitName(Name expr, INodeContext context) {
    // this should never be reached
    throw new UnsupportedOperationException();
  }

  @Override
  public IMetapathResult visitWildcard(Wildcard expr, INodeContext context) {
    // this should never be reached
    throw new UnsupportedOperationException();
  }

  @Override
  public IMetapathResult visitNegate(Negate expr, INodeContext context) {
    INumericItem item = Functions.toNumeric(expr.getChild().accept(this, context), true);
    if (item == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericUnaryMinus(item));
  }

  @Override
  public IMetapathResult visitAddition(Addition expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericAdd(left, right));
  }

  @Override
  public IMetapathResult visitSubtraction(Subtraction expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericSubtract(left, right));
  }

  @Override
  public IMetapathResult visitMultiplication(Multiplication expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericMultiply(left, right));
  }

  @Override
  public IMetapathResult visitDivision(Division expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericDivide(left, right));
  }

  @Override
  public IMetapathResult visitIntegerDivision(IntegerDivision expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericIntegerDivide(left, right));
  }

  private IMetapathResult resultOrEmptySequence(IItem item) {
    return item == null ? ISequence.EMPTY : item;
  }

  @Override
  public IMetapathResult visitMod(Mod expr, INodeContext context) {
    INumericItem left = Functions.toNumeric(expr.getLeft().accept(this, context), true);
    if (left == null) {
      return ISequence.EMPTY;
    }
    INumericItem right = Functions.toNumeric(expr.getRight().accept(this, context), true);
    if (right == null) {
      return ISequence.EMPTY;
    }
    return resultOrEmptySequence(Functions.opNumericMod(left, right));
  }

  @Override
  public IIntegerItem visitIntegerLiteral(IntegerLiteral expr, INodeContext context) {
    return IIntegerItem.valueOf(expr.getValue());
  }

  @Override
  public IDecimalItem visitDecimalLiteral(DecimalLiteral expr, INodeContext context) {
    return IDecimalItem.valueOf(expr.getValue());
  }

  @Override
  public IStringItem visitStringConcat(StringConcat expr, INodeContext context) {
    StringBuilder builder = new StringBuilder();
    for (IExpression child : expr.getChildren()) {
      IMetapathResult result = child.accept(this, context);
      result.toSequence().asStream().forEachOrdered(item -> {
        builder.append(item.toStringItem().toString());
      });
    }
    return new StringItem(builder.toString());
  }

  @Override
  public IStringItem visitStringLiteral(StringLiteral expr, INodeContext context) {
    return new StringItem(expr.getValue());
  }

  @Override
  public IMetapathResult visitFunctionCall(FunctionCall expr, INodeContext context) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public IMetapathResult visitMetapath(Metapath expr, INodeContext context) {
    List<IItem> items = new LinkedList<>();
    for (IExpression childExpr : expr.getChildren()) {
      IMetapathResult result = childExpr.accept(this, context);
      result.toSequence().asStream().forEachOrdered(item -> {
        items.add(item);
      });
    }

    IMetapathResult retval;
    if (items.isEmpty()) {
      retval = ISequence.EMPTY;
    } else if (items.size() == 1) {
      retval = new SingletonSequence(items.iterator().next());
    } else {
      retval = new ListSequence(items);
    }
    return retval;
  }

  @Override
  public IMetapathResult visitParenthesizedExpression(ParenthesizedExpression expr, INodeContext context) {
    IExpression childExpr = expr.getNode();
    IMetapathResult result = childExpr.accept(this, context);
    return result;
  }

  @Override
  public IMetapathResult visitUnion(Union expr, INodeContext context) {
    Set<IItem> items = new LinkedHashSet<>();
    for (IExpression childExpr : expr.getChildren()) {
      IMetapathResult result = childExpr.accept(this, context);
      result.toSequence().asStream().forEachOrdered(item -> {
        if (!items.contains(item)) {
          items.add(item);
        }
      });
    }

    IMetapathResult retval;
    if (items.isEmpty()) {
      retval = ISequence.EMPTY;
    } else if (items.size() == 1) {
      retval = items.iterator().next();
    } else {
      retval = new ListSequence(items);
    }
    return retval;
  }

}
