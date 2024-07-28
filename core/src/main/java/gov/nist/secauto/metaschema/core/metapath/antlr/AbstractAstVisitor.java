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

package gov.nist.secauto.metaschema.core.metapath.antlr; // NOPMD requires a large number of public methods

import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevforwardstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AbbrevreversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AdditiveexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AndexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArgumentContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArgumentlistContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrayconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrowexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ArrowfunctionspecifierContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.AxisstepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ComparisonexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ContextitemexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.CurlyarrayconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.EnclosedexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.EqnameContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ExprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ExprsingleContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ForexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ForwardaxisContext;
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
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MapkeyexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MapvalueexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MetapathContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.MultiplicativeexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NametestContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NodetestContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.NumericliteralContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.OrexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ParenthesizedexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PostfixexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PredicateContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PredicatelistContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.PrimaryexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.QuantifiedexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.RangeexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.RelativepathexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ReverseaxisContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ReversestepContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleforbindingContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleforclauseContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletbindingContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimpleletclauseContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SimplemapexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.SquarearrayconstructorContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.StepexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.StringconcatexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnaryexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnarylookupContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.UnionexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ValuecompContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.ValueexprContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.VarnameContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.VarrefContext;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10.WildcardContext;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;

import java.util.function.Function;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractAstVisitor<R> // NOPMD
    extends Metapath10BaseVisitor<R> {

  /**
   * This dispatch method will call the node handler on a leaf node or if multiple
   * child expressions exist. Otherwise, it will delegate to the single child
   * expression.
   *
   * @param <T>
   *          the visitor context type
   * @param ctx
   *          the visitor context
   * @param handler
   *          the node handler
   * @return the result
   */
  protected <T extends RuleContext> R handle(T ctx, @NonNull Function<T, R> handler) {
    T context = ObjectUtils.requireNonNull(ctx);

    R retval;
    if (context.getChildCount() == 1 && context.getChild(0) instanceof ParserRuleContext) {
      // delegate to the child expression, since this expression doesn't require any
      // action
      retval = context.getChild(0).accept(this);
    } else {
      retval = handler.apply(context);
    }
    return retval;
  }

  /**
   * This dispatch method expects a single child expression which will be called.
   * Other cases will result in an exception.
   *
   * @param <T>
   *          the visitor context type
   * @param ctx
   *          the visitor context
   * @return the result
   * @throws IllegalStateException
   *           if there was not a single child expression
   */
  protected <T extends RuleContext> R delegateToChild(@NonNull T ctx) {
    if (ctx.getChildCount() == 1) {
      return ctx.getChild(0).accept(this);
    }
    throw new IllegalStateException("a single child expression was expected");
  }

  // ============================================================
  // Expressions - https://www.w3.org/TR/xpath-31/#id-expressions
  // ============================================================

  @Override
  public R visitMetapath(MetapathContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleExpr(@NonNull ExprContext ctx);

  @Override
  public R visitExpr(ExprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleExpr(ctx));
  }

  @Override
  public R visitExprsingle(ExprsingleContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  // ============================================================================
  // Primary Expressions - https://www.w3.org/TR/xpath-31/#id-primary-expressions
  // ============================================================================

  @Override
  public R visitPrimaryexpr(PrimaryexprContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  // =================================================================
  // Literal Expressions - https://www.w3.org/TR/xpath-31/#id-literals
  // =================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleStringLiteral(@NonNull LiteralContext ctx);

  @Override
  public R visitLiteral(LiteralContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleStringLiteral(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleNumericLiteral(@NonNull NumericliteralContext ctx);

  @Override
  public R visitNumericliteral(NumericliteralContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleNumericLiteral(ctx));
  }

  // ==================================================================
  // Variable References - https://www.w3.org/TR/xpath-31/#id-variables
  // ==================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleVarref(@NonNull VarrefContext ctx);

  @Override
  public R visitVarref(VarrefContext ctx) {
    assert ctx != null;
    return handleVarref(ctx);
  }

  @Override
  public R visitVarname(VarnameContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  // ====================================================
  // Parenthesized Expressions -
  // https://www.w3.org/TR/xpath-31/#id-paren-expressions
  // ====================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleEmptyParenthesizedexpr(@NonNull ParenthesizedexprContext ctx);

  @Override
  public R visitParenthesizedexpr(ParenthesizedexprContext ctx) {
    assert ctx != null;
    ExprContext expr = ctx.expr();
    return expr == null ? handleEmptyParenthesizedexpr(ctx) : visit(expr);
  }

  // ==========================================================
  // Context Item Expression -
  // https://www.w3.org/TR/xpath-31/#id-context-item-expression
  // ==========================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleContextitemexpr(@NonNull ContextitemexprContext ctx);

  @Override
  public R visitContextitemexpr(ContextitemexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleContextitemexpr(ctx));
  }

  // =========================================================================
  // Static Function Calls - https://www.w3.org/TR/xpath-31/#id-function-calls
  // =========================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleFunctioncall(@NonNull FunctioncallContext ctx);

  @Override
  public R visitFunctioncall(FunctioncallContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleFunctioncall(ctx));
  }

  @Override
  public R visitArgumentlist(ArgumentlistContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitArgument(ArgumentContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  // =======================================================================
  // Enclosed Expressions - https://www.w3.org/TR/xpath-31/#id-enclosed-expr
  // =======================================================================

  @Override
  public R visitEnclosedexpr(EnclosedexprContext ctx) {
    ExprContext expr = ctx.expr();
    return expr == null ? null : expr.accept(this);
  }

  // =========================================================================
  // Filter Expressions - https://www.w3.org/TR/xpath-31/#id-filter-expression
  // =========================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handlePostfixexpr(@NonNull PostfixexprContext ctx);

  @Override
  public R visitPostfixexpr(PostfixexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handlePostfixexpr(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handlePredicate(@NonNull PredicateContext ctx);

  @Override
  public R visitPredicate(PredicateContext ctx) {
    assert ctx != null;
    return handlePredicate(ctx);
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleLookup(@NonNull LookupContext ctx);

  @Override
  public R visitLookup(LookupContext ctx) {
    assert ctx != null;
    return handleLookup(ctx);
  }

  // ======================================================================
  // Path Expressions - https://www.w3.org/TR/xpath-31/#id-path-expressions
  // ======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handlePathexpr(@NonNull PathexprContext ctx);

  @Override
  public R visitPathexpr(PathexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handlePathexpr(ctx));
  }

  // ============================================================
  // RelativePath Expressions -
  // https://www.w3.org/TR/xpath-31/#id-relative-path-expressions
  // ============================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleRelativepathexpr(@NonNull RelativepathexprContext ctx);

  @Override
  public R visitRelativepathexpr(RelativepathexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleRelativepathexpr(ctx));
  }

  // ================================================
  // Steps - https://www.w3.org/TR/xpath-31/#id-steps
  // ================================================

  @Override
  public R visitStepexpr(StepexprContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleForwardstep(@NonNull ForwardstepContext ctx);

  @Override
  public R visitForwardstep(ForwardstepContext ctx) {
    assert ctx != null;
    // this will either call the handler or forward for AbbrevforwardstepContext
    return handle(ctx, (context) -> handleForwardstep(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleReversestep(@NonNull ReversestepContext ctx);

  @Override
  public R visitReversestep(ReversestepContext ctx) {
    assert ctx != null;
    // this will either call the handler or forward for AbbrevreversestepContext
    return handle(ctx, (context) -> handleReversestep(ctx));
  }

  // ======================================================================
  // Predicates within Steps - https://www.w3.org/TR/xpath-31/#id-predicate
  // ======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleAxisstep(@NonNull AxisstepContext ctx);

  @Override
  public R visitAxisstep(AxisstepContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleAxisstep(ctx));
  }

  @Override
  public R visitPredicatelist(PredicatelistContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  // ===========================================
  // Axes - https://www.w3.org/TR/xpath-31/#axes
  // ===========================================

  @Override
  public R visitForwardaxis(ForwardaxisContext ctx) {
    // should never be called, since this is handled by handleForwardstep
    throw new IllegalStateException();
  }

  @Override
  public R visitReverseaxis(ReverseaxisContext ctx) {
    // should never be called, since this is handled by handleReversestep
    throw new IllegalStateException();
  }

  // =======================================================
  // Node Tests - https://www.w3.org/TR/xpath-31/#node-tests
  // =======================================================

  @Override
  public R visitNodetest(NodetestContext ctx) {
    // should never be called, since this is handled by the calling context
    throw new IllegalStateException();
  }

  @Override
  public R visitNametest(NametestContext ctx) {
    // should never be called, since this is handled by the calling context
    throw new IllegalStateException();
  }

  @Override
  public R visitEqname(EqnameContext ctx) {
    // should never be called, since this is handled by the calling context
    throw new IllegalStateException();
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  @NonNull
  protected abstract R handleWildcard(@NonNull WildcardContext ctx);

  @Override
  public R visitWildcard(WildcardContext ctx) {
    assert ctx != null;
    return handleWildcard(ctx);
  }

  // ===========================================================
  // Abbreviated Syntax - https://www.w3.org/TR/xpath-31/#abbrev
  // ===========================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleAbbrevforwardstep(@NonNull AbbrevforwardstepContext ctx);

  @Override
  public R visitAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    assert ctx != null;
    return handleAbbrevforwardstep(ctx);
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleAbbrevreversestep(@NonNull AbbrevreversestepContext ctx);

  @Override
  public R visitAbbrevreversestep(AbbrevreversestepContext ctx) {
    assert ctx != null;
    return handleAbbrevreversestep(ctx);
  }

  // ======================================================================
  // Constructing Sequences - https://www.w3.org/TR/xpath-31/#construct_seq
  // ======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleRangeexpr(@NonNull RangeexprContext ctx);

  @Override
  public R visitRangeexpr(RangeexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleRangeexpr(ctx));
  }

  // ========================================================================
  // Combining Node Sequences - https://www.w3.org/TR/xpath-31/#combining_seq
  // ========================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleUnionexpr(@NonNull UnionexprContext ctx);

  @Override
  public R visitUnionexpr(UnionexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleUnionexpr(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleIntersectexceptexpr(@NonNull IntersectexceptexprContext ctx);

  @Override
  public R visitIntersectexceptexpr(IntersectexceptexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleIntersectexceptexpr(ctx));
  }

  // ======================================================================
  // Arithmetic Expressions - https://www.w3.org/TR/xpath-31/#id-arithmetic
  // ======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleAdditiveexpr(@NonNull AdditiveexprContext ctx);

  @Override
  public R visitAdditiveexpr(AdditiveexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleAdditiveexpr(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleMultiplicativeexpr(@NonNull MultiplicativeexprContext ctx);

  @Override
  public R visitMultiplicativeexpr(MultiplicativeexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleMultiplicativeexpr(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleUnaryexpr(@NonNull UnaryexprContext ctx);

  @Override
  public R visitUnaryexpr(UnaryexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleUnaryexpr(ctx));
  }

  @Override
  public R visitValueexpr(ValueexprContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  // =====================================================
  // String Concatenation Expressions -
  // https://www.w3.org/TR/xpath-31/#id-string-concat-expr
  // =====================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleStringconcatexpr(@NonNull StringconcatexprContext ctx);

  @Override
  public R visitStringconcatexpr(StringconcatexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleStringconcatexpr(ctx));
  }

  // =======================================================================
  // Comparison Expressions - https://www.w3.org/TR/xpath-31/#id-comparisons
  // =======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleComparisonexpr(@NonNull ComparisonexprContext ctx);

  @Override
  public R visitComparisonexpr(ComparisonexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleComparisonexpr(ctx));
  }

  @Override
  public R visitValuecomp(ValuecompContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitGeneralcomp(GeneralcompContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  // ============================================================================
  // Logical Expressions - https://www.w3.org/TR/xpath-31/#id-logical-expressions
  // ============================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleOrexpr(@NonNull OrexprContext ctx);

  @Override
  public R visitOrexpr(OrexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleOrexpr(ctx));
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleAndexpr(@NonNull AndexprContext ctx);

  @Override
  public R visitAndexpr(AndexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleAndexpr(ctx));
  }

  // ====================================================================
  // For Expressions - https://www.w3.org/TR/xpath-31/#id-for-expressions
  // ====================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleForexpr(@NonNull ForexprContext ctx);

  @Override
  public R visitForexpr(ForexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleForexpr(ctx));
  }

  @Override
  public R visitSimpleforclause(SimpleforclauseContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitSimpleforbinding(SimpleforbindingContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  // ====================================================================
  // Let Expressions - https://www.w3.org/TR/xpath-31/#id-let-expressions
  // ====================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleLet(@NonNull LetexprContext ctx);

  @Override
  public R visitLetexpr(LetexprContext ctx) {
    assert ctx != null;
    return handleLet(ctx);
  }

  @Override
  public R visitSimpleletclause(SimpleletclauseContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitSimpleletbinding(SimpleletbindingContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  // ======================================================================
  // Map Constructors - https://www.w3.org/TR/xpath-31/#id-map-constructors
  // ======================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleMapConstructor(@NonNull MapconstructorContext ctx);

  @Override
  public R visitMapconstructor(MapconstructorContext ctx) {
    assert ctx != null;
    return handleMapConstructor(ctx);
  }

  @Override
  public R visitMapconstructorentry(MapconstructorentryContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitMapkeyexpr(MapkeyexprContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  @Override
  public R visitMapvalueexpr(MapvalueexprContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  // ==============================================================
  // Array Constructors - https://www.w3.org/TR/xpath-31/#id-arrays
  // ==============================================================

  @Override
  public R visitArrayconstructor(ArrayconstructorContext ctx) {
    assert ctx != null;
    return delegateToChild(ctx);
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleArrayConstructor(@NonNull SquarearrayconstructorContext ctx);

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleArrayConstructor(@NonNull CurlyarrayconstructorContext ctx);

  @Override
  public R visitSquarearrayconstructor(SquarearrayconstructorContext ctx) {
    assert ctx != null;
    return handleArrayConstructor(ctx);
  }

  @Override
  public R visitCurlyarrayconstructor(CurlyarrayconstructorContext ctx) {
    assert ctx != null;
    return handleArrayConstructor(ctx);
  }

  @Override
  public R visitKeyspecifier(KeyspecifierContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleUnarylookup(@NonNull UnarylookupContext ctx);

  @Override
  public R visitUnarylookup(UnarylookupContext ctx) {
    assert ctx != null;
    return handleUnarylookup(ctx);
  }

  // =========================================================================
  // Conditional Expressions - https://www.w3.org/TR/xpath-31/#id-conditionals
  // =========================================================================

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleIfexpr(@NonNull IfexprContext ctx);

  @Override
  public R visitIfexpr(IfexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleIfexpr(ctx));
  }

  /*
   * =============================================================================
   * ===== Quantified Expressions -
   * https://www.w3.org/TR/xpath-31/#id-quantified-expressions
   * =============================================================================
   * =====
   */

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleQuantifiedexpr(@NonNull QuantifiedexprContext ctx);

  @Override
  public R visitQuantifiedexpr(QuantifiedexprContext ctx) {
    assert ctx != null;
    return handleQuantifiedexpr(ctx);
  }

  /*
   * =========================================================================
   * Simple map operator (!) - https://www.w3.org/TR/xpath-31/#id-map-operator
   * =========================================================================
   */

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleSimplemapexpr(@NonNull SimplemapexprContext ctx);

  @Override
  public R visitSimplemapexpr(SimplemapexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleSimplemapexpr(ctx));
  }

  /*
   * ======================================================================= Arrow
   * operator (=>) - https://www.w3.org/TR/xpath-31/#id-arrow-operator
   * =======================================================================
   */

  /**
   * Handle the provided expression.
   *
   * @param ctx
   *          the provided expression context
   * @return the result
   */
  protected abstract R handleArrowexpr(@NonNull ArrowexprContext ctx);

  @Override
  public R visitArrowexpr(ArrowexprContext ctx) {
    assert ctx != null;
    return handle(ctx, (context) -> handleArrowexpr(ctx));
  }

  @Override
  public R visitArrowfunctionspecifier(ArrowfunctionspecifierContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }
}
