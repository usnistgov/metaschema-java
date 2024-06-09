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

package gov.nist.secauto.metaschema.core.metapath.cst;

import gov.nist.secauto.metaschema.core.metapath.EQNameUtils;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevforwardstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevreversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AdditiveexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AndexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArgumentlistContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrowexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrowfunctionspecifierContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AxisstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ComparisonexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ContextitemexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.CurlyarrayconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.EqnameContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ExprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ExprsingleContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ForexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ForwardstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.FunctioncallContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.GeneralcompContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.IfexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.IntersectexceptexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.KeyspecifierContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.LetexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.LiteralContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.LookupContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MapconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MapconstructorentryContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MultiplicativeexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NametestContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NodetestContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NumericliteralContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.OrexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ParenthesizedexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PostfixexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PredicateContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.QuantifiedexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.RangeexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.RelativepathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ReversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleforbindingContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleforclauseContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletbindingContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletclauseContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimplemapexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SquarearrayconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.StringconcatexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnaryexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnarylookupContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnionexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ValuecompContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.VarnameContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.VarrefContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.WildcardContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10Lexer;
import gov.nist.secauto.metaschema.core.metapath.cst.AbstractLookup.IKeySpecifier;
import gov.nist.secauto.metaschema.core.metapath.cst.comparison.GeneralComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.comparison.ValueComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.math.Addition;
import gov.nist.secauto.metaschema.core.metapath.cst.math.Division;
import gov.nist.secauto.metaschema.core.metapath.cst.math.IntegerDivision;
import gov.nist.secauto.metaschema.core.metapath.cst.math.Modulo;
import gov.nist.secauto.metaschema.core.metapath.cst.math.Multiplication;
import gov.nist.secauto.metaschema.core.metapath.cst.math.Subtraction;
import gov.nist.secauto.metaschema.core.metapath.cst.path.Axis;
import gov.nist.secauto.metaschema.core.metapath.cst.path.Flag;
import gov.nist.secauto.metaschema.core.metapath.cst.path.INameTestExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.path.INodeTestExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.path.ModelInstance;
import gov.nist.secauto.metaschema.core.metapath.cst.path.NameTest;
import gov.nist.secauto.metaschema.core.metapath.cst.path.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.path.RelativeSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.path.RootDoubleSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.path.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.core.metapath.cst.path.RootSlashPath;
import gov.nist.secauto.metaschema.core.metapath.cst.path.Step;
import gov.nist.secauto.metaschema.core.metapath.cst.path.Wildcard;
import gov.nist.secauto.metaschema.core.metapath.function.ComparisonFunctions;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Supports converting a Metapath abstract syntax tree (AST) generated by
 * <a href="https://www.antlr.org/">ANTLRv4</a> into a compact syntax tree
 * (CST).
 */
@SuppressWarnings({
    "PMD.GodClass", "PMD.CyclomaticComplexity", // acceptable complexity
    "PMD.CouplingBetweenObjects" // needed
})
public class BuildCSTVisitor
    extends AbstractCSTVisitorBase {
  @NonNull
  private final StaticContext context;

  public BuildCSTVisitor(@NonNull StaticContext context) {
    this.context = context;
  }

  // ============================================================
  // Expressions - https://www.w3.org/TR/xpath-31/#id-expressions
  // ============================================================

  @NonNull
  protected StaticContext getContext() {
    return context;
  }

  @Override
  protected IExpression handleExpr(ExprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new Metapath(children);
    });
  }

  // =================================================================
  // Literal Expressions - https://www.w3.org/TR/xpath-31/#id-literals
  // =================================================================

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

  // ==================================================================
  // Variable References - https://www.w3.org/TR/xpath-31/#id-variables
  // ==================================================================

  @Override
  protected IExpression handleVarref(VarrefContext ctx) {
    return new VariableReference(
        EQNameUtils.parseName(
            ctx.varname().eqname().getText(),
            getContext().getVariablePrefixResolver()));
  }

  // ====================================================================
  // For Expressions - https://www.w3.org/TR/xpath-31/#id-for-expressions
  // ====================================================================

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  @Override
  protected IExpression handleForexpr(ForexprContext ctx) {
    SimpleforclauseContext simpleForClause = ctx.simpleforclause();

    // for SimpleForBinding ("," SimpleForBinding)*
    int bindingCount = simpleForClause.getChildCount() / 2;

    @NonNull IExpression retval = ObjectUtils.notNull(ctx.exprsingle().accept(this));

    // step through in reverse
    for (int idx = bindingCount - 1; idx >= 0; idx--) {
      SimpleforbindingContext simpleForBinding = simpleForClause.simpleforbinding(idx);

      VarnameContext varName = simpleForBinding.varname();
      ExprsingleContext exprSingle = simpleForBinding.exprsingle();

      IExpression boundExpression = exprSingle.accept(this);
      assert boundExpression != null;

      QName qname = EQNameUtils.parseName(
          varName.eqname().getText(),
          getContext().getVariablePrefixResolver());

      Let.VariableDeclaration variable = new Let.VariableDeclaration(qname, boundExpression);

      retval = new For(variable, retval);
    }
    return retval;
  }

  // ====================================================================
  // Let Expressions - https://www.w3.org/TR/xpath-31/#id-let-expressions
  // ====================================================================

  @Override
  protected IExpression handleLet(LetexprContext context) {
    @NonNull IExpression retval = ObjectUtils.notNull(context.exprsingle().accept(this));

    SimpleletclauseContext letClause = context.simpleletclause();
    List<SimpleletbindingContext> clauses = letClause.simpleletbinding();

    ListIterator<SimpleletbindingContext> reverseListIterator = clauses.listIterator(clauses.size());
    while (reverseListIterator.hasPrevious()) {
      SimpleletbindingContext simpleCtx = reverseListIterator.previous();

      IExpression boundExpression = simpleCtx.exprsingle().accept(this);
      assert boundExpression != null;

      QName varName = EQNameUtils.parseName(
          simpleCtx.varname().eqname().getText(),
          getContext().getVariablePrefixResolver());

      retval = new Let(varName, boundExpression, retval); // NOPMD intended
    }
    return retval;
  }

  // ======================================================================
  // Map Constructors - https://www.w3.org/TR/xpath-31/#id-map-constructors
  // ======================================================================

  @Override
  protected MapConstructor handleMapConstructor(MapconstructorContext context) {
    return context.getChildCount() == 3
        // empty
        ? new MapConstructor(CollectionUtil.emptyList())
        // with members
        : nairyToCollection(context, 3, 2,
            (ctx, idx) -> {
              int pos = (idx - 3) / 2;
              MapconstructorentryContext entry = ctx.mapconstructorentry(pos);
              assert entry != null;
              return new MapConstructor.Entry(entry.mapkeyexpr().accept(this), entry.mapvalueexpr().accept(this));
            },
            children -> {
              assert children != null;
              return new MapConstructor(children);
            });
  }

  // ==============================================================
  // Array Constructors - https://www.w3.org/TR/xpath-31/#id-arrays
  // ==============================================================

  @Override
  protected IExpression handleArrayConstructor(SquarearrayconstructorContext context) {
    return context.getChildCount() == 2
        // empty
        ? new ArraySquareConstructor(CollectionUtil.emptyList())
        // with members
        : nairyToCollection(context, 1, 2,
            (ctx, idx) -> {
              int pos = (idx - 1) / 2;
              ParseTree tree = ctx.exprsingle(pos);
              return visit(tree);
            },
            children -> {
              assert children != null;
              return new ArraySquareConstructor(children);
            });
  }

  @Override
  protected IExpression handleArrayConstructor(CurlyarrayconstructorContext ctx) {
    return new ArraySequenceConstructor(visit(ctx.enclosedexpr()));
  }

  // ===============================================
  // Unary Lookup -
  // https://www.w3.org/TR/xpath-31/#id-unary-lookup
  // ===============================================

  @Override
  protected IExpression handleUnarylookup(UnarylookupContext ctx) {
    KeyspecifierContext specifier = ctx.keyspecifier();

    IKeySpecifier keySpecifier;
    if (specifier.parenthesizedexpr() != null) {
      keySpecifier
          = new UnaryLookup.ParenthesizedExprKeySpecifier(
              ObjectUtils.requireNonNull(specifier.parenthesizedexpr().accept(this)));
    } else if (specifier.NCName() != null) {
      keySpecifier
          = new UnaryLookup.NCNameKeySpecifier(ObjectUtils.requireNonNull(specifier.NCName().getText()));
    } else if (specifier.IntegerLiteral() != null) {
      keySpecifier = new UnaryLookup.IntegerLiteralKeySpecifier(
          IIntegerItem.valueOf(ObjectUtils.requireNonNull(specifier.IntegerLiteral().getText())));
    } else if (specifier.STAR() != null) {
      keySpecifier = new UnaryLookup.WildcardKeySpecifier();
    } else {
      throw new UnsupportedOperationException("unknown key specifier");
    }
    return new UnaryLookup(keySpecifier);
  }

  // =========================================================
  // Quantified Expressions -
  // https://www.w3.org/TR/xpath-31/#id-quantified-expressions
  // =========================================================

  @Override
  protected IExpression handleQuantifiedexpr(QuantifiedexprContext ctx) {
    Quantified.Quantifier quantifier;
    int type = ((TerminalNode) ctx.getChild(0)).getSymbol().getType();
    switch (type) {
    case Metapath10Lexer.KW_SOME:
      quantifier = Quantified.Quantifier.SOME;
      break;
    case Metapath10Lexer.KW_EVERY:
      quantifier = Quantified.Quantifier.EVERY;
      break;
    default:
      throw new UnsupportedOperationException(((TerminalNode) ctx.getChild(0)).getSymbol().getText());
    }

    int numVars = (ctx.getChildCount() - 2) / 5; // children - "satisfies expr" / ", $ varName in expr"
    Map<QName, IExpression> vars = new LinkedHashMap<>(); // NOPMD ordering needed
    int offset = 0;
    for (; offset < numVars; offset++) {
      // $
      QName varName = EQNameUtils.parseName(
          ctx.varname(offset).eqname().getText(),
          getContext().getVariablePrefixResolver());

      // in
      IExpression varExpr = visit(ctx.exprsingle(offset));

      vars.put(varName, varExpr);
    }

    IExpression satisfies = visit(ctx.exprsingle(offset));

    return new Quantified(quantifier, vars, satisfies);
  }

  // =======================================================================
  // Arrow operator (=>) - https://www.w3.org/TR/xpath-31/#id-arrow-operator
  // =======================================================================

  @Override
  protected IExpression handleArrowexpr(ArrowexprContext context) {
    // TODO: handle additional syntax for varef and parenthesized
    return handleGroupedNAiry(context, 0, 3, (ctx, idx, left) -> {
      // the next child is "=>"
      assert "=>".equals(ctx.getChild(idx).getText());

      int offset = (idx - 1) / 3;

      ArrowfunctionspecifierContext fcCtx = ctx.getChild(ArrowfunctionspecifierContext.class, offset);
      ArgumentlistContext argumentCtx = ctx.getChild(ArgumentlistContext.class, offset);

      QName name = EQNameUtils.parseName(
          fcCtx.eqname().getText(),
          getContext().getFunctionPrefixResolver());

      try (Stream<IExpression> args = Stream.concat(
          Stream.of(left),
          parseArgumentList(ObjectUtils.notNull(argumentCtx)))) {
        assert args != null;

        return new StaticFunctionCall(name, ObjectUtils.notNull(args.collect(Collectors.toUnmodifiableList())));
      }
    });
  }

  // ====================================================
  // Parenthesized Expressions -
  // https://www.w3.org/TR/xpath-31/#id-paren-expressions
  // ====================================================

  @Override
  protected IExpression handleEmptyParenthesizedexpr(ParenthesizedexprContext ctx) {
    return EmptySequence.instance();
  }

  // ==========================================================
  // Context Item Expression -
  // https://www.w3.org/TR/xpath-31/#id-context-item-expression
  // ==========================================================

  @Override
  protected IExpression handleContextitemexpr(ContextitemexprContext ctx) {
    return ContextItem.instance();
  }

  // =========================================================================
  // Static Function Calls - https://www.w3.org/TR/xpath-31/#id-function-calls
  // =========================================================================

  /**
   * Parse a list of arguments.
   *
   * @param context
   *          the argument list AST
   * @return a stream of CST expressions for each argument, in the original
   *         argument order
   */
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
    QName qname = EQNameUtils.parseName(
        ctx.eqname().getText(),
        getContext().getFunctionPrefixResolver());
    return new StaticFunctionCall(
        qname,
        ObjectUtils.notNull(parseArgumentList(ObjectUtils.notNull(ctx.argumentlist()))
            .collect(Collectors.toUnmodifiableList())));
  }

  // =========================================================================
  // Filter Expressions - https://www.w3.org/TR/xpath-31/#id-filter-expression
  // =========================================================================

  /**
   * Parse a predicate AST.
   *
   * @param predicate
   *          the predicate expression
   * @return the CST expression generated for the predicate
   */
  @NonNull
  protected IExpression parsePredicate(@NonNull PredicateContext predicate) {
    // the expression is always the second child
    return visit(predicate.getChild(1));
  }

  /**
   * Parse a series of predicate ASTs.
   *
   * @param context
   *          the parse tree node containing the predicates
   * @param staringChild
   *          the first child node corresponding to a predicate
   * @return the list of CST predicate expressions in the same order as the
   *         original predicate list
   */
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
  protected IExpression handlePostfixexpr(PostfixexprContext context) {
    return handleGroupedNAiry(
        context,
        0,
        1,
        (ctx, idx, left) -> {
          ParseTree tree = ctx.getChild(idx);
          IExpression result;
          if (tree instanceof ArgumentlistContext) {
            // map or array access using function call syntax
            result = new FunctionCallAccessor(
                left,
                parseArgumentList((ArgumentlistContext) tree).findFirst().get());
          } else if (tree instanceof PredicateContext) {
            result = new PredicateExpression(
                left,
                CollectionUtil.singletonList(parsePredicate((PredicateContext) tree)));
          } else if (tree instanceof LookupContext) {
            KeyspecifierContext specifier = ((LookupContext) tree).keyspecifier();

            IKeySpecifier keySpecifier;
            if (specifier.parenthesizedexpr() != null) {
              keySpecifier
                  = new PostfixLookup.ParenthesizedExprKeySpecifier(
                      ObjectUtils.requireNonNull(specifier.parenthesizedexpr().accept(this)));
            } else if (specifier.NCName() != null) {
              keySpecifier
                  = new PostfixLookup.NCNameKeySpecifier(ObjectUtils.requireNonNull(specifier.NCName().getText()));
            } else if (specifier.IntegerLiteral() != null) {
              keySpecifier = new PostfixLookup.IntegerLiteralKeySpecifier(
                  IIntegerItem.valueOf(ObjectUtils.requireNonNull(specifier.IntegerLiteral().getText())));
            } else if (specifier.STAR() != null) {
              keySpecifier = new PostfixLookup.WildcardKeySpecifier();
            } else {
              throw new UnsupportedOperationException("unknown key specifier");
            }
            result = new PostfixLookup(left, keySpecifier);
          } else {
            result = visit(tree);
          }
          return result;
        });
  }

  // ======================================================================
  // Path Expressions - https://www.w3.org/TR/xpath-31/#id-path-expressions
  // ======================================================================

  @Override
  protected IExpression handlePredicate(PredicateContext ctx) {
    parsePredicate(ctx);
    return null;
  }

  @Override
  protected IExpression handleLookup(LookupContext ctx) {
    // TODO Auto-generated method stub
    return null;
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
          retval = new RootSlashPath(visit(ctx.getChild(1)));
        } else {
          retval = new RootSlashOnlyPath();
        }
        break;
      case Metapath10Lexer.SS:
        // a double slash expression with path
        retval = new RootDoubleSlashPath(visit(ctx.getChild(1)));
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) tree).getSymbol().getText());
      }
    } else {
      // a relative expression or something else
      retval = visit(tree);
    }
    return retval;
  }

  // ============================================================
  // RelativePath Expressions -
  // https://www.w3.org/TR/xpath-31/#id-relative-path-expressions
  // ============================================================

  @Override
  protected IExpression handleRelativepathexpr(RelativepathexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      assert left != null;

      ParseTree operatorTree = ctx.getChild(idx);
      IExpression right = visit(ctx.getChild(idx + 1));

      int type = ((TerminalNode) operatorTree).getSymbol().getType();

      IExpression retval;
      switch (type) {
      case Metapath10Lexer.SLASH:
        retval = new RelativeSlashPath(left, right);
        break;
      case Metapath10Lexer.SS:
        retval = new RelativeDoubleSlashPath(left, right);
        break;
      default:
        throw new UnsupportedOperationException(((TerminalNode) operatorTree).getSymbol().getText());
      }
      return retval;
    });
  }

  // ================================================
  // Steps - https://www.w3.org/TR/xpath-31/#id-steps
  // ================================================

  @Override
  protected IExpression handleForwardstep(ForwardstepContext ctx) {
    AbbrevforwardstepContext abbrev = ctx.abbrevforwardstep();

    Step retval;
    if (abbrev == null) {
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
      retval = new Step(axis, parseNodeTest(ctx.nodetest(), false));
    } else {
      retval = new Step(
          Axis.CHILDREN,
          parseNodeTest(
              ctx.nodetest(),
              abbrev.AT() != null));
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
    return new Step(axis, parseNodeTest(ctx.nodetest(), false));
  }

  // =======================================================
  // Node Tests - https://www.w3.org/TR/xpath-31/#node-tests
  // =======================================================

  protected INodeTestExpression parseNodeTest(NodetestContext ctx, boolean flag) {
    // TODO: implement kind test
    NametestContext nameTestCtx = ctx.nametest();
    return parseNameTest(nameTestCtx, flag);
  }

  protected INameTestExpression parseNameTest(NametestContext ctx, boolean flag) {
    ParseTree testType = ObjectUtils.requireNonNull(ctx.getChild(0));
    INameTestExpression retval;
    if (testType instanceof EqnameContext) {
      QName qname = EQNameUtils.parseName(
          ctx.eqname().getText(),
          flag ? getContext().getFlagPrefixResolver() : getContext().getModelPrefixResolver());
      retval = new NameTest(qname);
    } else { // wildcard
      retval = handleWildcard((WildcardContext) testType);
    }
    return retval;
  }

  @Override
  protected Wildcard handleWildcard(WildcardContext ctx) {
    Predicate<IDefinitionNodeItem<?, ?>> matcher = null;
    if (ctx.STAR() == null) {
      if (ctx.CS() != null) {
        // specified prefix, any local-name
        String prefix = ctx.NCName().getText();
        String namespace = getContext().lookupNamespaceForPrefix(prefix);
        if (namespace == null) {
          throw new IllegalStateException(String.format("Prefix '%s' did not map to a namespace.", prefix));
        }
        matcher = new Wildcard.MatchAnyLocalName(namespace);
      } else if (ctx.SC() != null) {
        // any prefix, specified local-name
        matcher = new Wildcard.MatchAnyNamespace(ctx.NCName().getText());
      } else {
        // specified braced namespace, any local-name
        String bracedUriLiteral = ctx.BracedURILiteral().getText();
        String namespace = bracedUriLiteral.substring(2, bracedUriLiteral.length() - 1);
        matcher = new Wildcard.MatchAnyLocalName(namespace);
      }
    } // star needs no matcher: any prefix, any local-name

    return new Wildcard(matcher);
  }

  // ======================================================================
  // Predicates within Steps - https://www.w3.org/TR/xpath-31/#id-predicate
  // ======================================================================

  @Override
  protected IExpression handleAxisstep(AxisstepContext ctx) {
    IExpression step = visit(ctx.getChild(0));
    ParseTree predicateTree = ctx.getChild(1);
    assert predicateTree != null;

    List<IExpression> predicates = parsePredicates(predicateTree, 0);

    return predicates.isEmpty() ? step : new PredicateExpression(step, predicates);
  }

  // ===========================================================
  // Abbreviated Syntax - https://www.w3.org/TR/xpath-31/#abbrev
  // ===========================================================

  @Override
  protected IExpression handleAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    int numChildren = ctx.getChildCount();

    IExpression retval;
    if (numChildren == 1) {
      retval = new ModelInstance(parseNodeTest(ctx.nodetest(), false));
    } else {
      // this is an AT test
      retval = new Flag(parseNodeTest(ctx.nodetest(), true));
    }
    return retval;
  }

  @Override
  protected IExpression handleAbbrevreversestep(AbbrevreversestepContext ctx) {
    return Axis.PARENT;
  }

  // ======================================================================
  // Constructing Sequences - https://www.w3.org/TR/xpath-31/#construct_seq
  // ======================================================================

  @Override
  protected IExpression handleRangeexpr(RangeexprContext ctx) {
    assert ctx.getChildCount() == 3;

    IExpression left = visit(ctx.getChild(0));
    IExpression right = visit(ctx.getChild(2));

    return new Range(left, right);
  }

  // ========================================================================
  // Combining Node Sequences - https://www.w3.org/TR/xpath-31/#combining_seq
  // ========================================================================

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
      assert left != null;

      ParseTree operatorTree = ctx.getChild(idx);
      IExpression right = visit(ctx.getChild(idx + 1));

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

  // ======================================================================
  // Arithmetic Expressions - https://www.w3.org/TR/xpath-31/#id-arithmetic
  // ======================================================================

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
      assert left != null;

      ParseTree operatorTree = ctx.getChild(idx);
      IExpression right = visit(ctx.getChild(idx + 1));

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

    IExpression retval = visit(ctx.getChild(idx));
    if (negateCount % 2 != 0) {
      retval = new Negate(retval);
    }
    return retval;
  }

  // =====================================================
  // String Concatenation Expressions -
  // https://www.w3.org/TR/xpath-31/#id-string-concat-expr
  // =====================================================

  @Override
  protected IExpression handleStringconcatexpr(StringconcatexprContext ctx) {
    return handleNAiryCollection(ctx, children -> {
      assert children != null;
      return new StringConcat(children);
    });
  }

  // =======================================================================
  // Comparison Expressions - https://www.w3.org/TR/xpath-31/#id-comparisons
  // =======================================================================

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

  // ============================================================================
  // Logical Expressions - https://www.w3.org/TR/xpath-31/#id-logical-expressions
  // ============================================================================

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

  // =========================================================================
  // Conditional Expressions - https://www.w3.org/TR/xpath-31/#id-conditionals
  // =========================================================================

  @Override
  protected IExpression handleIfexpr(IfexprContext ctx) {
    IExpression testExpr = visit(ctx.expr());
    IExpression thenExpr = visit(ctx.exprsingle(0));
    IExpression elseExpr = visit(ctx.exprsingle(1));

    return new If(testExpr, thenExpr, elseExpr);
  }

  // =========================================================================
  // Simple map operator (!) - https://www.w3.org/TR/xpath-31/#id-map-operator
  // =========================================================================

  @Override
  protected IExpression handleSimplemapexpr(SimplemapexprContext context) {
    return handleGroupedNAiry(context, 0, 2, (ctx, idx, left) -> {
      // the next child is "!"
      assert "!".equals(ctx.getChild(idx).getText());
      IExpression right = ctx.getChild(idx + 1).accept(this);

      return new SimpleMap(left, right);
    });
  }
}
