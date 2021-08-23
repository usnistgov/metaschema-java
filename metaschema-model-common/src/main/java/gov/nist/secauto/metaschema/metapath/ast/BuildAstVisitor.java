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

package gov.nist.secauto.metaschema.metapath.ast;

import gov.nist.secauto.metaschema.metapath.ast.Comparison.Operator;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import src.main.antlr4.metapath10BaseVisitor;
import src.main.antlr4.metapath10Lexer;
import src.main.antlr4.metapath10Parser.AdditiveexprContext;
import src.main.antlr4.metapath10Parser.AndexprContext;
import src.main.antlr4.metapath10Parser.ArgumentlistContext;
import src.main.antlr4.metapath10Parser.ArrowexprContext;
import src.main.antlr4.metapath10Parser.ArrowfunctionspecifierContext;
import src.main.antlr4.metapath10Parser.AxisstepContext;
import src.main.antlr4.metapath10Parser.ComparisonexprContext;
import src.main.antlr4.metapath10Parser.ContextitemexprContext;
import src.main.antlr4.metapath10Parser.EqnameContext;
import src.main.antlr4.metapath10Parser.ExprContext;
import src.main.antlr4.metapath10Parser.ForwardstepContext;
import src.main.antlr4.metapath10Parser.FunctioncallContext;
import src.main.antlr4.metapath10Parser.GeneralcompContext;
import src.main.antlr4.metapath10Parser.IntersectexceptexprContext;
import src.main.antlr4.metapath10Parser.LiteralContext;
import src.main.antlr4.metapath10Parser.MultiplicativeexprContext;
import src.main.antlr4.metapath10Parser.NumericliteralContext;
import src.main.antlr4.metapath10Parser.OrexprContext;
import src.main.antlr4.metapath10Parser.ParenthesizedexprContext;
import src.main.antlr4.metapath10Parser.PathexprContext;
import src.main.antlr4.metapath10Parser.PostfixexprContext;
import src.main.antlr4.metapath10Parser.PredicateContext;
import src.main.antlr4.metapath10Parser.PredicatelistContext;
import src.main.antlr4.metapath10Parser.RelativepathexprContext;
import src.main.antlr4.metapath10Parser.StringconcatexprContext;
import src.main.antlr4.metapath10Parser.UnaryexprContext;
import src.main.antlr4.metapath10Parser.UnionexprContext;
import src.main.antlr4.metapath10Parser.ValuecompContext;
import src.main.antlr4.metapath10Parser.WildcardContext;

public class BuildAstVisitor extends metapath10BaseVisitor<IExpression> {

  /**
   * Parse the provided context as a simple trinary phrase, which will be one of the following.
   * <p>
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left and right members will
   * be returned based on what is provided by the supplier.
   * </ol>
   * 
   * @param <CONTEXT>
   *          the context type to parse
   * @param context
   *          the context instance
   * @param supplier
   *          a supplier that will instantiate an expression based on the provided collection
   * @return the left expression or the supplied expression for a collection
   */
  protected <CONTEXT extends ParserRuleContext, NODE extends IExpression> IExpression
      handleNAiryCollection(CONTEXT context, java.util.function.Function<List<NODE>, IExpression> supplier) {
    return handleNAiryCollection(context, 2, (ctx, idx) -> {
      // skip operator, since we know what it is
      ParseTree tree = ctx.getChild(idx + 1);
      @SuppressWarnings("unchecked")
      NODE node = (NODE) tree.accept(this);
      return node;
    }, supplier);
  }

  /**
   * Parse the provided context as a simple trinary phrase, which will be one of the following.
   * <p>
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left and right members will
   * be returned based on what is provided by the supplier.
   * </ol>
   * 
   * @param <CONTEXT>
   *          the context type to parse
   * @param context
   *          the context instance
   * @param step
   *          the amount to advance the loop over the context children
   * @param parser
   *          a binary function used to parse the context children
   * @param supplier
   *          a supplier that will instantiate an expression based on the provided collection
   * @return the left expression or the supplied expression for a collection
   */
  protected <CONTEXT extends ParserRuleContext, NODE extends IExpression> IExpression handleNAiryCollection(
      CONTEXT context, int step, BiFunction<CONTEXT, Integer, NODE> parser,
      java.util.function.Function<List<NODE>, IExpression> supplier) {
    int numChildren = context.getChildCount();

    IExpression retval;
    if (numChildren == 0) {
      retval = null;
    } else {
      ParseTree leftTree = context.getChild(0);
      @SuppressWarnings("unchecked")
      NODE leftResult = (NODE) leftTree.accept(this);

      if (numChildren == 1) {
        retval = leftResult;
      } else {
        List<NODE> children = new ArrayList<>(numChildren - 1 / step);
        children.add(leftResult);
        for (int i = 1; i < numChildren; i = i + step) {
          NODE result = parser.apply(context, i);
          children.add(result);
        }
        retval = supplier.apply(children);
      }
    }
    return retval;
  }

  @Override
  public IExpression visitExpr(ExprContext context) {
    return handleNAiryCollection(context, children -> new Metapath(children));
  }

  @Override
  public IExpression visitOrexpr(OrexprContext context) {
    return handleNAiryCollection(context, children -> new OrNode(children));
  }

  @Override
  public IExpression visitAndexpr(AndexprContext context) {
    return handleNAiryCollection(context, children -> new And(children));
  }

  @Override
  public IExpression visitComparisonexpr(ComparisonexprContext ctx) {
    int numChildren = ctx.getChildCount();
    if (numChildren == 1) {
      return (IExpression) super.visitComparisonexpr(ctx);
    } else if (numChildren != 3) {
      throw new UnsupportedOperationException();
    }

    IExpression left = (IExpression) visit(ctx.getChild(0));
    IExpression right = (IExpression) visit(ctx.getChild(2));

    // the operator
    ParseTree operatorTree = ctx.getChild(1);
    Object payload = operatorTree.getPayload();
    Operator operator;
    if (payload instanceof GeneralcompContext) {
      GeneralcompContext compContext = (GeneralcompContext) payload;
      int type = ((TerminalNode) compContext.getChild(0)).getSymbol().getType();
      switch (type) {
      case metapath10Lexer.EQ:
        operator = Operator.EQ;
        break;
      case metapath10Lexer.NE:
        operator = Operator.NE;
        break;
      case metapath10Lexer.LT:
        operator = Operator.LT;
        break;
      case metapath10Lexer.LE:
        operator = Operator.LE;
        break;
      case metapath10Lexer.GT:
        operator = Operator.GT;
        break;
      case metapath10Lexer.GE:
        operator = Operator.GE;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) compContext.getChild(0)).getSymbol().getText());
      }
    } else if (payload instanceof ValuecompContext) {
      ValuecompContext compContext = (ValuecompContext) payload;
      int type = ((TerminalNode) compContext.getChild(0)).getSymbol().getType();
      switch (type) {
      case metapath10Lexer.KW_EQ:
        operator = Comparison.Operator.EQ;
        break;
      case metapath10Lexer.KW_NE:
        operator = Comparison.Operator.NE;
        break;
      case metapath10Lexer.KW_LT:
        operator = Comparison.Operator.LT;
        break;
      case metapath10Lexer.KW_LE:
        operator = Comparison.Operator.LE;
        break;
      case metapath10Lexer.KW_GT:
        operator = Comparison.Operator.GT;
        break;
      case metapath10Lexer.KW_GE:
        operator = Comparison.Operator.GE;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) compContext.getChild(0)).getSymbol().getText());
      }
    } else {
      throw new UnsupportedOperationException();
    }
    return new Comparison(left, operator, right);
  }

  @Override
  public IExpression visitStringconcatexpr(StringconcatexprContext context) {
    return handleNAiryCollection(context, children -> new StringConcat(children));
  }

  /**
   * Parse the provided context as a simple trinary phrase, which will be one of the following.
   * <p>
   * <ol>
   * <li><code>expr</code> for which the expr will be returned</li>
   * <li><code>left (operator right)*</code> for which a collection of the left and right members will
   * be returned based on what is provided by the supplier.
   * </ol>
   * 
   * @param <CONTEXT>
   *          the context type to parse
   * @param context
   *          the context instance
   * @param step
   *          the amount to advance the loop over the context children
   * @param parser
   *          a trinary function used to parse the context children and supply a result
   * @return the left expression or the supplied expression
   */
  protected <CONTEXT extends ParserRuleContext> IExpression handleGroupedNAiry(CONTEXT context, int step,
      TriFunction<CONTEXT, Integer, IExpression, IExpression> parser) {
    int numChildren = context.getChildCount();

    IExpression retval;
    if (numChildren == 0) {
      retval = null;
    } else {
      ParseTree leftTree = context.getChild(0);
      retval = (IExpression) leftTree.accept(this);

      for (int i = 1; i < numChildren; i = i + step) {
        retval = parser.apply(context, i, retval);
      }
    }
    return retval;
  }

  @Override
  public IExpression visitAdditiveexpr(AdditiveexprContext context) {
    return handleGroupedNAiry(context, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression right = (IExpression) rightTree.accept(this);

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case metapath10Lexer.PLUS:
        retval = new Addition(left, right);
        break;
      case metapath10Lexer.MINUS:
        retval = new Subtraction(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @Override
  public IExpression visitMultiplicativeexpr(MultiplicativeexprContext context) {
    return handleGroupedNAiry(context, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression right = (IExpression) rightTree.accept(this);

      int type = ((TerminalNode) operatorTree).getSymbol().getType();
      IExpression retval;
      switch (type) {
      case metapath10Lexer.STAR:
        retval = new Multiplication(left, right);
        break;
      case metapath10Lexer.KW_DIV:
        retval = new Division(left, right);
        break;
      case metapath10Lexer.KW_IDIV:
        retval = new IntegerDivision(left, right);
        break;
      case metapath10Lexer.KW_MOD:
        retval = new Mod(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @Override
  public IExpression visitUnionexpr(UnionexprContext context) {
    return handleNAiryCollection(context, children -> new Union(children));
  }

  @Override
  public IExpression visitUnaryexpr(UnaryexprContext ctx) {
    int numChildren = ctx.getChildCount();
    int negateCount = 0;

    int idx = 0;
    for (; idx < numChildren - 1; idx++) {
      ParseTree tree = ctx.getChild(idx);
      int type = ((TerminalNode) tree).getSymbol().getType();
      switch (type) {
      case metapath10Lexer.PLUS:
        break;
      case metapath10Lexer.MINUS:
        negateCount++;
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) tree).getSymbol().getText());
      }
    }

    ParseTree expr = ctx.getChild(0);
    IExpression retval = (IExpression) expr.accept(this);
    if (negateCount % 2 == 1) {
      retval = new Negate(retval);
    }
    return retval;
  }

  @Override
  public IExpression visitPathexpr(PathexprContext ctx) {
    int numChildren = ctx.getChildCount();

    IExpression retval;
    ParseTree tree = ctx.getChild(0);
    if (tree instanceof TerminalNode) {
      int type = ((TerminalNode) tree).getSymbol().getType();
      switch (type) {
      case metapath10Lexer.SLASH:
        // a slash expression with optional path
        if (numChildren == 2) {
          // the optional path
          ParseTree pathTree = ctx.getChild(1);
          IExpression relativeExpr = (IExpression) pathTree.accept(this);
          retval = new RootSlashPath(relativeExpr);
        } else {
          retval = new RootSlashOnlyPath();
        }
        break;
      case metapath10Lexer.SS:
        // a double slash expression with path
        ParseTree pathTree = ctx.getChild(1);
        IExpression node = (IExpression) pathTree.accept(this);
        retval = new RootDoubleSlashPath(node);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) tree).getSymbol().getText());
      }
    } else {
      // a relative expression or something else
      retval = (IExpression) tree.accept(this);
    }
    return retval;
  }

  @Override
  public IExpression visitRelativepathexpr(RelativepathexprContext context) {
    return handleGroupedNAiry(context, 2, (ctx, idx, left) -> {
      ParseTree operatorTree = ctx.getChild(idx);
      ParseTree rightTree = ctx.getChild(idx + 1);
      IExpression rightResult = (IExpression) rightTree.accept(this);

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case metapath10Lexer.SLASH:
        retval = new RelativeSlashPath(left, rightResult);
        break;
      case metapath10Lexer.SS:
        retval = new RelativeDoubleSlashPath(left, rightResult);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  @Override
  public IExpression visitLiteral(LiteralContext ctx) {
    ParseTree tree = ctx.getChild(0);
    IExpression retval;
    if (tree instanceof NumericliteralContext) {
      retval = (IExpression) tree.accept(this);
    } else {
      // String literal
      retval = new StringLiteral(tree.getText());
    }
    return retval;
  }

  @Override
  public IExpression visitNumericliteral(NumericliteralContext ctx) {
    ParseTree tree = ctx.getChild(0);
    Token token = (Token) tree.getPayload();
    IExpression retval;
    switch (token.getType()) {
    case metapath10Lexer.IntegerLiteral:
      retval = new IntegerLiteral(new BigInteger(token.getText()));
      break;
    case metapath10Lexer.DecimalLiteral:
    case metapath10Lexer.DoubleLiteral:
      retval = new DecimalLiteral(new BigDecimal(token.getText()));
      break;
    default:
      throw new UnsupportedOperationException(token.getText());
    }
    return retval;
  }

  @Override
  public IExpression visitParenthesizedexpr(ParenthesizedexprContext context) {
    IExpression expr = null;
    int numChildren = context.getChildCount();
    // if there is an expression, it will be the second node
    if (numChildren == 3) {
      ParseTree tree = context.getChild(1);
      expr = (IExpression) tree.accept(this);
    }
    return new ParenthesizedExpression(expr);
  }

  @Override
  public IExpression visitContextitemexpr(ContextitemexprContext ctx) {
    return new ContextItem();
  }

  protected List<IExpression> parseArgumentList(ArgumentlistContext context) {
    int numChildren = context.getChildCount();

    List<IExpression> arguments;
    if (numChildren == 2) {
      // just the OP CP tokens, which is an empty list
      arguments = Collections.emptyList();
    } else if (numChildren == 3) {
      // single argument
      IExpression argument = context.getChild(1).accept(this);
      arguments = Collections.singletonList(argument);
    } else {
      // more children than the OP CP tokens
      arguments = new ArrayList<>((numChildren - 3 / 2) + 1);
      for (int i = 1; i < numChildren - 1; i = i + 2) {
        IExpression argument = context.getChild(1).accept(this);
        arguments.add(argument);
      }
    }
    return arguments;
  }

  @Override
  public IExpression visitFunctioncall(FunctioncallContext context) {
    ParseTree nameTree = context.getChild(0);
    String name = nameTree.getText();
    ParseTree argumentListTree = context.getChild(1);

    return new Function(name, parseArgumentList((ArgumentlistContext) argumentListTree));
  }

  @Override
  public IExpression visitArgumentlist(ArgumentlistContext context) {
    throw new UnsupportedOperationException();
  }

  protected IExpression parsePredicate(PredicateContext context) {
    // the expression is always the second child
    ParseTree tree = context.getChild(1);
    IExpression expr = (IExpression) tree.accept(this);
    return expr;
  }

  @Override
  public IExpression visitPredicate(PredicateContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IExpression visitPostfixexpr(PostfixexprContext context) {
    int numChildren = context.getChildCount();
    ParseTree primaryTree = context.getChild(0);
    IExpression retval = primaryTree.accept(this);

    if (numChildren > 1) {
      List<IExpression> predicates = parsePredicates(context, 1);
      if (!predicates.isEmpty()) {
        retval = new Step(retval, predicates); 
      }
    }
    return retval;
  }

  protected List<IExpression> parsePredicates(ParseTree context, int staringChild) {
    int numChildren = context.getChildCount();
    int numPredicates = numChildren - staringChild;

    List<IExpression> predicates;
    if (numPredicates == 0) {
      // just the OP CP tokens, which is an empty list
      predicates = Collections.emptyList();
    } else if (numPredicates == 1) {
      // single argument
      predicates = Collections.singletonList(parsePredicate((PredicateContext) context.getChild(staringChild)));
    } else {
      // more children than the OP CP tokens
      predicates = new ArrayList<>(numChildren);
      for (int i = staringChild; i < numChildren; i++) {
        PredicateContext predicate = (PredicateContext) context.getChild(i);
        predicates.add(parsePredicate(predicate));
      }
    }
    return predicates;
  }

  @Override
  public IExpression visitAxisstep(AxisstepContext context) {
    ParseTree stepTree = context.getChild(0);
    IExpression retval = (IExpression) stepTree.accept(this);

    ParseTree predicateTree = context.getChild(1);
    List<IExpression> predicates = parsePredicates(predicateTree, 0);
    if (!predicates.isEmpty()) {
      retval = new Step(retval, predicates); 
    }
    return retval;
  }

  @Override
  public IExpression visitForwardstep(ForwardstepContext context) {
    int numChildren = context.getChildCount();
    IExpression retval;
    if (numChildren == 1) {
      ParseTree tree = context.getChild(0);
      retval = tree.accept(this);
      retval = new ModelInstance((IExpression) tree.accept(this));
    } else {
      // this is an AT test
      ParseTree tree = context.getChild(1);
      retval = new Flag((IExpression) tree.accept(this));

    }
    return retval;
  }

  @Override
  public IExpression visitEqname(EqnameContext ctx) {
    ParseTree tree = ctx.getChild(0);
    String name = ((TerminalNode) tree).getText();
    return new Name(name);
  }

  @Override
  public IExpression visitWildcard(WildcardContext ctx) {
    return new Wildcard();
  }

  @Override
  public IExpression visitPredicatelist(PredicatelistContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IExpression visitIntersectexceptexpr(IntersectexceptexprContext ctx) {
    int numChildren = ctx.getChildCount();
    if (numChildren > 1) {
      // TODO: implement
      throw new UnsupportedOperationException();
    }
    return super.visitIntersectexceptexpr(ctx);
  }

  @Override
  public IExpression visitArrowexpr(ArrowexprContext ctx) {
    int numChildren = ctx.getChildCount();
    if (numChildren > 1) {
      // TODO: implement
      throw new UnsupportedOperationException();
    }
    return super.visitArrowexpr(ctx);
  }

  @Override
  public IExpression visitArrowfunctionspecifier(ArrowfunctionspecifierContext ctx) {
    // TODO: implement
    throw new UnsupportedOperationException();
  }
}
