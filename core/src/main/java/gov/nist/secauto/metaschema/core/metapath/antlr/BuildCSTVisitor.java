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

package gov.nist.secauto.metaschema.core.metapath.antlr;

import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.StaticMetapathException;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevforwardstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevreversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AdditiveexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AndexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArgumentlistContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrowexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AxisstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ComparisonexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ContextitemexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.EqnameContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ExprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ForwardstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.FunctioncallContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.GeneralcompContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.IntersectexceptexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.LetexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.LiteralContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MultiplicativeexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NumericliteralContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.OrexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PostfixexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PredicateContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.RelativepathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ReversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletbindingContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletclauseContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.StringconcatexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnaryexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnionexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ValuecompContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.VarrefContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.WildcardContext;
import gov.nist.secauto.metaschema.core.metapath.cst.Addition;
import gov.nist.secauto.metaschema.core.metapath.cst.And;
import gov.nist.secauto.metaschema.core.metapath.cst.Axis;
import gov.nist.secauto.metaschema.core.metapath.cst.ContextItem;
import gov.nist.secauto.metaschema.core.metapath.cst.DecimalLiteral;
import gov.nist.secauto.metaschema.core.metapath.cst.Division;
import gov.nist.secauto.metaschema.core.metapath.cst.Except;
import gov.nist.secauto.metaschema.core.metapath.cst.Flag;
import gov.nist.secauto.metaschema.core.metapath.cst.FunctionCall;
import gov.nist.secauto.metaschema.core.metapath.cst.GeneralComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.IBooleanLogicExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IntegerDivision;
import gov.nist.secauto.metaschema.core.metapath.cst.IntegerLiteral;
import gov.nist.secauto.metaschema.core.metapath.cst.Intersect;
import gov.nist.secauto.metaschema.core.metapath.cst.Let;
import gov.nist.secauto.metaschema.core.metapath.cst.Metapath;
import gov.nist.secauto.metaschema.core.metapath.cst.ModelInstance;
import gov.nist.secauto.metaschema.core.metapath.cst.Modulo;
import gov.nist.secauto.metaschema.core.metapath.cst.Multiplication;
import gov.nist.secauto.metaschema.core.metapath.cst.Name;
import gov.nist.secauto.metaschema.core.metapath.cst.Negate;
import gov.nist.secauto.metaschema.core.metapath.cst.Or;
import gov.nist.secauto.metaschema.core.metapath.cst.Predicate;
import gov.nist.secauto.metaschema.core.metapath.cst.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.RelativeSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.RootDoubleSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.core.metapath.cst.RootSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.Step;
import gov.nist.secauto.metaschema.core.metapath.cst.StringConcat;
import gov.nist.secauto.metaschema.core.metapath.cst.StringLiteral;
import gov.nist.secauto.metaschema.core.metapath.cst.Subtraction;
import gov.nist.secauto.metaschema.core.metapath.cst.Union;
import gov.nist.secauto.metaschema.core.metapath.cst.ValueComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.VariableReference;
import gov.nist.secauto.metaschema.core.metapath.cst.Wildcard;
import gov.nist.secauto.metaschema.core.metapath.function.ComparisonFunctions;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class BuildCSTVisitor
    extends AbstractAstVisitor<IExpression> {

  private static final Pattern QUALIFIED_NAME_PATTERN = Pattern.compile("^Q{([^}]*)}(.+)$");

  @SuppressWarnings("null")
  @Override
  @NonNull
  public IExpression visit(ParseTree tree) {
    return super.visit(tree);
  }

  // TODO: verify javadocs are accurate for the following n-ary functions.

  /**
   * Parse the provided context as a simple n-ary phrase, which will be one of the
   * following.
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left
   * and right members will be returned based on what is provided by the supplier.
   * </ol>
   *
   * @param <CONTEXT>
   *          the context type to parse
   * @param <NODE>
   *          the type of expression
   * @param context
   *          the context instance
   * @param supplier
   *          a supplier that will instantiate an expression based on the provided
   *          collection
   * @return the left expression or the supplied expression for a collection
   */
  @NonNull
  protected <CONTEXT extends ParserRuleContext, NODE extends IExpression> IExpression
      handleNAiryCollection(
          @NonNull CONTEXT context,
          @NonNull Function<List<NODE>, IExpression> supplier) {
    return handleNAiryCollection(context, 1, 2, (ctx, idx) -> {
      // skip operator, since we know what it is
      ParseTree tree = ctx.getChild(idx + 1);
      @SuppressWarnings({ "unchecked", "null" })
      @NonNull NODE node = (NODE) tree.accept(this);
      return node;
    }, supplier);
  }

  /**
   * Parse the provided context as a simple n-ary phrase, which will be one of the
   * following.
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left
   * and right members will be returned based on what is provided by the supplier.
   * </ol>
   *
   * @param <CONTEXT>
   *          the context type to parse
   * @param <EXPRESSION>
   *          the child expression type
   * @param context
   *          the context instance
   * @param startIndex
   *          the starting context child position
   * @param step
   *          the amount to advance the loop over the context children
   * @param parser
   *          a binary function used to parse the context children
   * @param supplier
   *          a supplier that will instantiate an expression based on the provided
   *          collection
   * @return the left expression or the supplied expression for a collection
   */
  @NonNull
  protected <CONTEXT extends ParserRuleContext, EXPRESSION extends IExpression> IExpression
      handleNAiryCollection(
          @NonNull CONTEXT context,
          int startIndex,
          int step,
          @NonNull BiFunction<CONTEXT, Integer, EXPRESSION> parser,
          @NonNull Function<List<EXPRESSION>, IExpression> supplier) {
    int numChildren = context.getChildCount();

    if (numChildren == 0) {
      throw new IllegalStateException("there should always be a child expression");
    } else if (startIndex > numChildren) {
      throw new IllegalStateException("Start index is out of bounds");
    }

    ParseTree leftTree = context.getChild(0);
    @SuppressWarnings({ "unchecked", "null" })
    @NonNull EXPRESSION leftResult = (EXPRESSION) leftTree.accept(this);

    IExpression retval;
    if (numChildren == 1) {
      retval = leftResult;
    } else {
      List<EXPRESSION> children = new ArrayList<>(numChildren - 1 / step);
      children.add(leftResult);
      for (int i = startIndex; i < numChildren; i = i + step) {
        EXPRESSION result = parser.apply(context, i);
        children.add(result);
      }
      IExpression result = ObjectUtils.notNull(supplier.apply(children));
      retval = result;
    }
    return retval;
  }

  /**
   * Parse the provided context as a simple n-ary phrase, which will be one of the
   * following.
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left
   * and right members will be returned based on what is provided by the supplier.
   * </ol>
   *
   * @param <CONTEXT>
   *          the context type to parse
   * @param context
   *          the context instance
   * @param startingIndex
   *          the index of the first child expression, which must be a
   *          non-negative value that is less than the number of children
   * @param step
   *          the amount to advance the loop over the context children
   * @param parser
   *          a trinary function used to parse the context children and supply a
   *          result
   * @return the left expression or the supplied expression
   */
  protected <CONTEXT extends ParserRuleContext> IExpression handleGroupedNAiry(
      @NonNull CONTEXT context,
      int startingIndex,
      int step,
      @NonNull ITriFunction<CONTEXT, Integer, IExpression, IExpression> parser) {
    int numChildren = context.getChildCount();
    if (startingIndex >= numChildren) {
      throw new IndexOutOfBoundsException(
          String.format("The starting index '%d' exceeds the child count '%d'",
              startingIndex,
              numChildren));
    }

    IExpression retval = null;
    if (numChildren > 0) {
      ParseTree leftTree = context.getChild(startingIndex);
      IExpression result = ObjectUtils.notNull(leftTree.accept(this));

      for (int i = startingIndex + 1; i < numChildren; i = i + step) {
        result = parser.apply(context, i, result);
      }
      retval = result;
    }
    return retval;
  }

  @Override
  protected IExpression handleExpr(ExprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new Metapath(children);
    });
  }

  @Override
  protected IExpression handleOrexpr(OrexprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new Or(children);
    });
  }

  @Override
  protected IExpression handleAndexpr(AndexprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new And(children);
    });
  }

  @Override
  protected IExpression handleComparisonexpr(ComparisonexprContext ctx) { // NOPMD - ok
    assert ctx.getChildCount() == 3;

    IExpression left = visit(ctx.getChild(0));
    IExpression right = visit(ctx.getChild(2));

    // the operator
    ParseTree operatorTree = ctx.getChild(1);
    Object payload = operatorTree.getPayload();

    ComparisonFunctions.Operator operator;
    IBooleanLogicExpression retval;
    if (payload instanceof GeneralcompContext) {
      GeneralcompContext compContext = (GeneralcompContext) payload;
      int type = ((TerminalNode) compContext.getChild(0)).getSymbol().getType();
      switch (type) {
      case Metapath10Lexer.EQ:
        operator = ComparisonFunctions.Operator.EQ;
        break;
      case Metapath10Lexer.NE:
        operator = ComparisonFunctions.Operator.NE;
        break;
      case Metapath10Lexer.LT:
        operator = ComparisonFunctions.Operator.LT;
        break;
      case Metapath10Lexer.LE:
        operator = ComparisonFunctions.Operator.LE;
        break;
      case Metapath10Lexer.GT:
        operator = ComparisonFunctions.Operator.GT;
        break;
      case Metapath10Lexer.GE:
        operator = ComparisonFunctions.Operator.GE;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) compContext.getChild(0)).getSymbol().getText());
      }
      retval = new GeneralComparison(left, operator, right);
    } else if (payload instanceof ValuecompContext) {
      ValuecompContext compContext = (ValuecompContext) payload;
      int type = ((TerminalNode) compContext.getChild(0)).getSymbol().getType();
      switch (type) {
      case Metapath10Lexer.KW_EQ:
        operator = ComparisonFunctions.Operator.EQ;
        break;
      case Metapath10Lexer.KW_NE:
        operator = ComparisonFunctions.Operator.NE;
        break;
      case Metapath10Lexer.KW_LT:
        operator = ComparisonFunctions.Operator.LT;
        break;
      case Metapath10Lexer.KW_LE:
        operator = ComparisonFunctions.Operator.LE;
        break;
      case Metapath10Lexer.KW_GT:
        operator = ComparisonFunctions.Operator.GT;
        break;
      case Metapath10Lexer.KW_GE:
        operator = ComparisonFunctions.Operator.GE;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) compContext.getChild(0)).getSymbol().getText());
      }
      retval = new ValueComparison(left, operator, right);
    } else {
      throw new UnsupportedOperationException();
    }
    return retval;
  }

  @Override
  protected IExpression handleStringconcatexpr(StringconcatexprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new StringConcat(children);
    });
  }

  @Override
  protected IExpression handleAdditiveexpr(AdditiveexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression right = rightTree.accept(this);

      assert left != null;
      assert right != null;

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case Metapath10Lexer.PLUS:
        retval = new Addition(left, right);
        break;
      case Metapath10Lexer.MINUS:
        retval = new Subtraction(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @Override
  protected IExpression handleMultiplicativeexpr(MultiplicativeexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression right = rightTree.accept(this);

      assert left != null;
      assert right != null;

      int type = ((TerminalNode) operatorTree).getSymbol().getType();
      IExpression retval;
      switch (type) {
      case Metapath10Lexer.STAR:
        retval = new Multiplication(left, right);
        break;
      case Metapath10Lexer.KW_DIV:
        retval = new Division(left, right);
        break;
      case Metapath10Lexer.KW_IDIV:
        retval = new IntegerDivision(left, right);
        break;
      case Metapath10Lexer.KW_MOD:
        retval = new Modulo(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @Override
  protected IExpression handleUnionexpr(UnionexprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new Union(children);
    });
  }

  @Override
  protected IExpression handleIntersectexceptexpr(IntersectexceptexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression right = rightTree.accept(this);

      assert left != null;
      assert right != null;

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case Metapath10Lexer.KW_INTERSECT:
        retval = new Intersect(left, right);
        break;
      case Metapath10Lexer.KW_EXCEPT:
        retval = new Except(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @SuppressWarnings("resource")
  @Override
  protected IExpression handleArrowexpr(ArrowexprContext context) {
    // TODO: handle new syntax

    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      // the next child is "=>"
      assert "=>".equals(ctx.getChild(idx).getText());

      FunctioncallContext fcCtx = ctx.getChild(FunctioncallContext.class, idx + 1);
      // QName name = toQName(
      String name = fcCtx.eqname().getText();
      assert name != null;

      Stream<IExpression> args = parseArgumentList(ObjectUtils.notNull(fcCtx.argumentlist()));
      args = Stream.concat(Stream.of(left), args);
      assert args != null;

      return new FunctionCall(name, ObjectUtils.notNull(args.collect(Collectors.toUnmodifiableList())));
    });
  }

  @Override
  protected IExpression handleUnaryexpr(UnaryexprContext ctx) {
    int numChildren = ctx.getChildCount();
    int negateCount = 0;

    int idx = 0;
    for (; idx < numChildren - 1; idx++) {
      ParseTree tree = ctx.getChild(idx);
      int type = ((TerminalNode) tree).getSymbol().getType();
      switch (type) {
      case Metapath10Lexer.PLUS:
        break;
      case Metapath10Lexer.MINUS:
        negateCount++;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) tree).getSymbol().getText());
      }
    }

    ParseTree expr = ctx.getChild(0);
    IExpression retval = expr.accept(this);
    assert retval != null;
    if (negateCount % 2 != 0) {
      retval = new Negate(retval);
    }
    return retval;
  }

  @Override
  protected IExpression handlePathexpr(PathexprContext ctx) {
    int numChildren = ctx.getChildCount();

    IExpression retval;
    ParseTree tree = ctx.getChild(0);
    if (tree instanceof TerminalNode) {
      int type = ((TerminalNode) tree).getSymbol().getType();
      switch (type) {
      case Metapath10Lexer.SLASH:
        // a slash expression with optional path
        if (numChildren == 2) {
          // the optional path
          ParseTree pathTree = ctx.getChild(1);
          retval = new RootSlashPath(ObjectUtils.notNull(pathTree.accept(this)));
        } else {
          retval = new RootSlashOnlyPath();
        }
        break;
      case Metapath10Lexer.SS:
        // a double slash expression with path
        ParseTree pathTree = ctx.getChild(1);
        IExpression node = pathTree.accept(this);
        assert node != null;
        retval = new RootDoubleSlashPath(node);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) tree).getSymbol().getText());
      }
    } else {
      // a relative expression or something else
      retval = tree.accept(this);
    }
    return retval;
  }

  @Override
  protected IExpression handleRelativepathexpr(RelativepathexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression rightResult = rightTree.accept(this);

      assert left != null;
      assert rightResult != null;

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case Metapath10Lexer.SLASH:
        retval = new RelativeSlashPath(left, rightResult);
        break;
      case Metapath10Lexer.SS:
        retval = new RelativeDoubleSlashPath(left, rightResult);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @SuppressWarnings("null")
  @NonNull
  protected IExpression parsePredicate(@NonNull PredicateContext context) {
    // the expression is always the second child
    ParseTree tree = context.getChild(1);
    return tree.accept(this);
  }

  @NonNull
  protected List<IExpression> parsePredicates(@NonNull ParseTree context, int staringChild) {
    int numChildren = context.getChildCount();
    int numPredicates = numChildren - staringChild;

    List<IExpression> predicates;
    if (numPredicates == 0) {
      // no predicates
      predicates = CollectionUtil.emptyList();
    } else if (numPredicates == 1) {
      // single predicate
      PredicateContext predicate = ObjectUtils.notNull((PredicateContext) context.getChild(staringChild));
      predicates = CollectionUtil.singletonList(parsePredicate(predicate));
    } else {
      // multiple predicates
      predicates = new ArrayList<>(numPredicates);
      for (int i = staringChild; i < numChildren; i++) {
        PredicateContext predicate = ObjectUtils.notNull((PredicateContext) context.getChild(i));
        predicates.add(parsePredicate(predicate));
      }
    }
    return predicates;
  }

  @Override
  protected IExpression handlePostfixexpr(PostfixexprContext ctx) {
    int numChildren = ctx.getChildCount();
    ParseTree primaryTree = ctx.getChild(0);
    IExpression retval = ObjectUtils.notNull(primaryTree.accept(this));

    List<IExpression> predicates = numChildren > 1 ? parsePredicates(ctx, 1) : CollectionUtil.emptyList();

    if (!predicates.isEmpty()) {
      retval = new Predicate(retval, predicates);
    }
    return retval;

  }

  @Override
  protected IExpression handleAxisstep(AxisstepContext ctx) {
    IExpression step = ctx.getChild(0).accept(this);
    assert step != null;

    ParseTree predicateTree = ctx.getChild(1);
    assert predicateTree != null;

    List<IExpression> predicates = parsePredicates(predicateTree, 0);

    return predicates.isEmpty() ? step : new Predicate(step, predicates);
  }

  @Override
  protected IExpression handleForwardstep(ForwardstepContext ctx) {
    assert ctx.getChildCount() == 2;

    Token token = (Token) ctx.forwardaxis().getChild(0).getPayload();

    Axis axis;
    switch (token.getType()) {
    case Metapath10Lexer.KW_SELF:
      axis = Axis.SELF;
      break;
    case Metapath10Lexer.KW_CHILD:
      axis = Axis.CHILDREN;
      break;
    case Metapath10Lexer.KW_DESCENDANT:
      axis = Axis.DESCENDANT;
      break;
    case Metapath10Lexer.KW_DESCENDANT_OR_SELF:
      axis = Axis.DESCENDANT_OR_SELF;
      break;
    default:
      throw new UnsupportedOperationException(token.getText());
    }
    return new Step(axis, ObjectUtils.notNull(ctx.nametest().accept(this)));
  }

  @Override
  protected IExpression handleAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    int numChildren = ctx.getChildCount();

    IExpression retval;
    if (numChildren == 1) {
      ParseTree tree = ctx.getChild(0);
      retval = new ModelInstance(ObjectUtils.notNull(tree.accept(this)));
    } else {
      // this is an AT test
      ParseTree tree = ctx.getChild(1);
      retval = new Flag(ObjectUtils.notNull(tree.accept(this)));

    }
    return retval;
  }

  @Override
  protected IExpression handleReversestep(ReversestepContext ctx) {
    assert ctx.getChildCount() == 2;

    Token token = (Token) ctx.reverseaxis().getChild(0).getPayload();

    Axis axis;
    switch (token.getType()) {
    case Metapath10Lexer.KW_PARENT:
      axis = Axis.PARENT;
      break;
    case Metapath10Lexer.KW_ANCESTOR:
      axis = Axis.ANCESTOR;
      break;
    case Metapath10Lexer.KW_ANCESTOR_OR_SELF:
      axis = Axis.ANCESTOR_OR_SELF;
      break;
    default:
      throw new UnsupportedOperationException(token.getText());
    }
    return new Step(axis, ObjectUtils.notNull(ctx.nametest().accept(this)));
  }

  @Override
  protected IExpression handleAbbrevreversestep(AbbrevreversestepContext ctx) {
    return Axis.PARENT;
  }

  @Override
  protected IExpression handleStringLiteral(LiteralContext ctx) {
    ParseTree tree = ctx.getChild(0);
    return new StringLiteral(ObjectUtils.notNull(tree.getText()));
  }

  @Override
  protected IExpression handleNumericLiteral(NumericliteralContext ctx) {
    ParseTree tree = ctx.getChild(0);
    Token token = (Token) tree.getPayload();
    IExpression retval;
    switch (token.getType()) {
    case Metapath10Lexer.IntegerLiteral:
      retval = new IntegerLiteral(new BigInteger(token.getText()));
      break;
    case Metapath10Lexer.DecimalLiteral:
    case Metapath10Lexer.DoubleLiteral:
      retval = new DecimalLiteral(new BigDecimal(token.getText()));
      break;
    default:
      throw new UnsupportedOperationException(token.getText());
    }
    return retval;
  }

  @Override
  protected IExpression handleContextitemexpr(ContextitemexprContext ctx) {
    return ContextItem.instance();
  }

  @NonNull
  protected Stream<IExpression> parseArgumentList(@NonNull ArgumentlistContext context) {
    int numChildren = context.getChildCount();

    Stream<IExpression> retval;
    if (numChildren == 2) {
      // just the OP CP tokens, which is an empty list
      retval = Stream.empty();
    } else {
      retval = context.argument().stream()
          .map(argument -> {
            return argument.exprsingle().accept(this);
          });
    }
    assert retval != null;

    return retval;
  }

  @Override
  protected IExpression handleFunctioncall(FunctioncallContext ctx) {
    EqnameContext nameCtx = ctx.eqname();
    String name = nameCtx.getText();

    assert name != null;

    return new FunctionCall(
        name,
        ObjectUtils.notNull(parseArgumentList(ObjectUtils.notNull(ctx.argumentlist()))
            .collect(Collectors.toUnmodifiableList())));
  }

  @Override
  protected IExpression handleEqname(EqnameContext ctx) {
    ParseTree tree = ctx.getChild(0);
    String name = ((TerminalNode) tree).getText();

    assert name != null;

    return new Name(name);
  }

  @Override
  protected IExpression handleWildcard(WildcardContext ctx) {
    return new Wildcard();
  }

  @FunctionalInterface
  interface ITriFunction<T, U, V, R> {

    R apply(T argT, U argU, V argV);

    default <W> ITriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
      Objects.requireNonNull(after);
      return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }
  }

  @Override
  protected IExpression handleLet(LetexprContext context) {
    @NonNull IExpression retval = ObjectUtils.notNull(context.exprsingle().accept(this));

    SimpleletclauseContext letClause = context.simpleletclause();
    List<SimpleletbindingContext> clauses = letClause.simpleletbinding();

    ListIterator<SimpleletbindingContext> reverseListIterator = clauses.listIterator(clauses.size());
    while (reverseListIterator.hasPrevious()) {
      SimpleletbindingContext simpleCtx = reverseListIterator.previous();

      Name varName = (Name) simpleCtx.varname().accept(this);
      IExpression boundExpression = simpleCtx.exprsingle().accept(this);

      assert varName != null;
      assert boundExpression != null;

      retval = new Let(varName, boundExpression, retval); // NOPMD intended
    }
    return retval;
  }

  @Override
  protected IExpression handleVarref(VarrefContext ctx) {
    Name varName = (Name) ctx.varname().accept(this);
    assert varName != null;
    return new VariableReference(varName);
  }

  /**
   * Get the QName for an
   * <a href="https://www.w3.org/TR/xpath-31/#dt-expanded-qname">expanded
   * QName</a>.
   *
   * @param eqname
   *          the expanded QName
   * @param context
   *          the Metapath evaluation static context
   * @param requireNamespace
   *          if {@code true} require the resulting QName to have a namespace, or
   *          {@code false} otherwise
   * @return the QName
   * @throws StaticMetapathException
   *           if the expanded QName prefix is not bound or if the resulting
   *           namespace is invalid
   */
  @NonNull
  static QName toQName(@NonNull EqnameContext eqname, @NonNull StaticContext context, boolean requireNamespace) {
    String namespaceUri;
    String localName;
    TerminalNode node;
    if ((node = eqname.URIQualifiedName()) != null) {
      // BracedURILiteral - Q{uri}name -
      // https://www.w3.org/TR/xpath-31/#doc-xpath31-BracedURILiteral
      Matcher matcher = QUALIFIED_NAME_PATTERN.matcher(node.getText());
      if (matcher.matches()) {
        namespaceUri = matcher.group(1);
        localName = matcher.group(2);
      } else {
        // the syntax should always match above, since ANTLR is parsing it
        throw new IllegalStateException();
      }
    } else {
      String prefix;
      String[] tokens = eqname.getText().split(":", 2);
      if (tokens.length == 2) {
        // lexical QName with prefix - prefix:name
        // https://www.w3.org/TR/xpath-31/#dt-qname
        prefix = ObjectUtils.notNull(tokens[0]);
        localName = tokens[1];
      } else {
        // lexical QName without prefix - name
        // https://www.w3.org/TR/xpath-31/#dt-qname
        prefix = "";
        localName = tokens[0];
      }
      namespaceUri = context.lookupNamespaceForPrefix(prefix);
      if (namespaceUri == null && requireNamespace) {
        throw new StaticMetapathException(
            StaticMetapathException.PREFIX_NOT_EXPANDABLE,
            String.format("The static context does not have a namespace URI configured for prefix '%s'.", prefix));
      }
    }

    QName retval;
    if (namespaceUri == null) {
      retval = new QName(localName);
    } else {
      if ("http://www.w3.org/2000/xmlns/".equals(namespaceUri)) {
        throw new StaticMetapathException(StaticMetapathException.NAMESPACE_MISUSE,
            "The namespace of an expanded QName cannot be: http://www.w3.org/2000/xmlns/");
      }
      retval = new QName(namespaceUri, localName);
    }
    return retval;
  }
}
