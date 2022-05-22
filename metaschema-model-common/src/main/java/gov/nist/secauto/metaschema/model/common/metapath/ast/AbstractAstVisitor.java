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

import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10BaseVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.AbbrevforwardstepContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.AbbrevreversestepContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.AdditiveexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.AndexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ArgumentContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ArgumentlistContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ArrowexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.AxisstepContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ComparisonexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ContextitemexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.EqnameContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ExprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ExprsingleContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ForwardaxisContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ForwardstepContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.FunctioncallContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.GeneralcompContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.IntersectexceptexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.LiteralContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.MetapathContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.MultiplicativeexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.NametestContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.NumericliteralContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.OrexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ParenthesizedexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.PathexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.PostfixexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.PredicateContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.PredicatelistContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.PrimaryexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.RelativepathexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ReverseaxisContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ReversestepContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.StepexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.StringconcatexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.UnaryexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.UnionexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ValuecompContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.ValueexprContext;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser.WildcardContext;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class AbstractAstVisitor<R>
    extends metapath10BaseVisitor<R> {

  protected <T extends RuleContext> R handle(T ctx, @NotNull Function<T, R> handler) {
    T context = ObjectUtils.requireNonNull(ctx);

    R retval;
    if (context.getChildCount() == 1 && context.getChild(0) instanceof ParserRuleContext) {
      // delegate to the child expression, since this expression doesn't require any action
      retval = context.getChild(0).accept(this);
    } else {
      retval = handler.apply(context);
    }
    return retval;
  }

  protected <T extends RuleContext> R passThrough(T ctx) {
    T context = ObjectUtils.requireNonNull(ctx);

    R retval;
    if (context.getChildCount() == 0) {
      throw new IllegalStateException("there should always be a child expression");
      // retval = defaultResult();
    } else if (context.getChildCount() == 1) {
      retval = context.getChild(0).accept(this);
    } else {
      throw new IllegalStateException("a single child expression was expected");
    }
    return retval;
  }

  @Override
  public R visitMetapath(MetapathContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleExpr(ExprContext ctx);

  @Override
  public R visitExpr(ExprContext ctx) {
    return handle(ctx, (context) -> handleExpr(ctx));
  }

  @Override
  public R visitExprsingle(ExprsingleContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleOrexpr(OrexprContext ctx);

  @Override
  public R visitOrexpr(OrexprContext ctx) {
    return handle(ctx, (context) -> handleOrexpr(ctx));
  }

  protected abstract R handleAndexpr(AndexprContext ctx);

  @Override
  public R visitAndexpr(AndexprContext ctx) {
    return handle(ctx, (context) -> handleAndexpr(ctx));
  }

  protected abstract R handleComparisonexpr(ComparisonexprContext ctx);

  @Override
  public R visitComparisonexpr(ComparisonexprContext ctx) {
    return handle(ctx, (context) -> handleComparisonexpr(ctx));
  }

  protected abstract R handleStringconcatexpr(StringconcatexprContext ctx);

  @Override
  public R visitStringconcatexpr(StringconcatexprContext ctx) {
    return handle(ctx, (context) -> handleStringconcatexpr(ctx));
  }

  protected abstract R handleAdditiveexpr(AdditiveexprContext ctx);

  @Override
  public R visitAdditiveexpr(AdditiveexprContext ctx) {
    return handle(ctx, (context) -> handleAdditiveexpr(ctx));
  }

  protected abstract R handleMultiplicativeexpr(MultiplicativeexprContext ctx);

  @Override
  public R visitMultiplicativeexpr(MultiplicativeexprContext ctx) {
    return handle(ctx, (context) -> handleMultiplicativeexpr(ctx));
  }

  protected abstract R handleUnionexpr(UnionexprContext ctx);

  @Override
  public R visitUnionexpr(UnionexprContext ctx) {
    return handle(ctx, (context) -> handleUnionexpr(ctx));
  }

  protected abstract R handleIntersectexceptexpr(IntersectexceptexprContext ctx);

  @Override
  public R visitIntersectexceptexpr(IntersectexceptexprContext ctx) {
    return handle(ctx, (context) -> handleIntersectexceptexpr(ctx));
  }

  protected abstract R handleArrowexpr(ArrowexprContext ctx);

  @Override
  public R visitArrowexpr(ArrowexprContext ctx) {
    return handle(ctx, (context) -> handleArrowexpr(ctx));
  }

  protected abstract R handleUnaryexpr(UnaryexprContext ctx);

  @Override
  public R visitUnaryexpr(UnaryexprContext ctx) {
    return handle(ctx, (context) -> handleUnaryexpr(ctx));
  }

  @Override
  public R visitValueexpr(ValueexprContext ctx) {
    return passThrough(ctx);
  }

  @Override
  public R visitGeneralcomp(GeneralcompContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitValuecomp(ValuecompContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  protected abstract R handlePathexpr(PathexprContext ctx);

  @Override
  public R visitPathexpr(PathexprContext ctx) {
    return handle(ctx, (context) -> handlePathexpr(ctx));
  }

  protected abstract R handleRelativepathexpr(RelativepathexprContext ctx);

  @Override
  public R visitRelativepathexpr(RelativepathexprContext ctx) {
    return handle(ctx, (context) -> handleRelativepathexpr(ctx));
  }

  @Override
  public R visitStepexpr(StepexprContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleAxisstep(AxisstepContext ctx);

  @Override
  public R visitAxisstep(AxisstepContext ctx) {
    return handle(ctx, (context) -> handleAxisstep(ctx));
  }

  protected abstract R handleForwardstep(ForwardstepContext ctx);

  @Override
  public R visitForwardstep(ForwardstepContext ctx) {
    return handle(ctx, (context) -> handleForwardstep(ctx));
  }

  @Override
  public R visitForwardaxis(ForwardaxisContext ctx) {
    // should never be called, since this is handled by handleForwardstep
    throw new IllegalStateException();
  }

  protected abstract R handleAbbrevforwardstep(AbbrevforwardstepContext ctx);

  @Override
  public R visitAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    return handleAbbrevforwardstep(ctx);
  }

  protected abstract R handleReversestep(ReversestepContext ctx);

  @Override
  public R visitReversestep(ReversestepContext ctx) {
    return handle(ctx, (context) -> handleReversestep(ctx));
  }

  @Override
  public R visitReverseaxis(ReverseaxisContext ctx) {
    // should never be called, since this is handled by handleReversestep
    throw new IllegalStateException();
  }

  protected abstract R handleAbbrevreversestep(AbbrevreversestepContext ctx);

  @Override
  public R visitAbbrevreversestep(AbbrevreversestepContext ctx) {
    return handleAbbrevreversestep(ctx);
  }

  @Override
  public R visitNametest(NametestContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleWildcard(WildcardContext ctx);

  @Override
  public R visitWildcard(WildcardContext ctx) {
    return handleWildcard(ctx);
  }

  protected abstract R handlePostfixexpr(PostfixexprContext ctx);

  @Override
  public R visitPostfixexpr(PostfixexprContext ctx) {
    return handle(ctx, (context) -> handlePostfixexpr(ctx));
  }

  @Override
  public R visitArgumentlist(ArgumentlistContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitPredicatelist(PredicatelistContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitPredicate(PredicateContext ctx) {
    // should never be called, since this is handled by the parent expression
    throw new IllegalStateException();
  }

  @Override
  public R visitPrimaryexpr(PrimaryexprContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleStringLiteral(LiteralContext ctx);
  
  @Override
  public R visitLiteral(LiteralContext ctx) {
    return handle(ctx, (context) -> handleStringLiteral(ctx));
  }

  protected abstract R handleNumericLiteral(NumericliteralContext ctx);

  @Override
  public R visitNumericliteral(NumericliteralContext ctx) {
    return handle(ctx, (context) -> handleNumericLiteral(ctx));
  }

  @Override
  public R visitParenthesizedexpr(ParenthesizedexprContext ctx) {
    return ctx.expr().accept(this);
  }

  protected abstract R handleContextitemexpr(ContextitemexprContext ctx);

  @Override
  public R visitContextitemexpr(ContextitemexprContext ctx) {
    return handle(ctx, (context) -> handleContextitemexpr(ctx));
  }

  protected abstract R handleFunctioncall(FunctioncallContext ctx);

  @Override
  public R visitFunctioncall(FunctioncallContext ctx) {
    return handle(ctx, (context) -> handleFunctioncall(ctx));
  }

  @Override
  public R visitArgument(ArgumentContext ctx) {
    return passThrough(ctx);
  }

  protected abstract R handleEqname(EqnameContext ctx);

  @Override
  public R visitEqname(EqnameContext ctx) {
    return handleEqname(ctx);
  }
}
