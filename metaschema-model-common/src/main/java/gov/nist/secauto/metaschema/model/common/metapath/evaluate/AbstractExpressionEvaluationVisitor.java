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

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Addition;
import gov.nist.secauto.metaschema.model.common.metapath.ast.And;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.DecimalLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Division;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.FunctionCall;
import gov.nist.secauto.metaschema.model.common.metapath.ast.GeneralComparison;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.ValueComparison;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class AbstractExpressionEvaluationVisitor implements IExpressionEvaluationVisitor {

  @NotNull
  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> visitChildren(@NotNull IExpression expr,
      @NotNull INodeContext context) {
    ISequence<ITEM_TYPE> result = defaultResult();

    for (Iterator<@NotNull ? extends IExpression> itr = expr.getChildren().iterator(); itr.hasNext();) {
      if (!shouldVisitNextChild(expr, result, context)) {
        break;
      }

      @SuppressWarnings("null")
      IExpression childExpr = itr.next();
      ISequence<?> childResult = childExpr.accept(this, context);
      result = aggregateResult(result, childResult);
    }

    return result;
  }

  @SuppressWarnings("unused")
  protected <ITEM_TYPE extends IItem> boolean shouldVisitNextChild(IExpression expr, ISequence<ITEM_TYPE> result,
      INodeContext context) {
    return true;
  }

  @SuppressWarnings("unused")
  @NotNull
  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> aggregateResult(@NotNull ISequence<ITEM_TYPE> result,
      @NotNull ISequence<?> childResult) {
    return result;
  }

  @NotNull
  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> defaultResult() {
    return ISequence.empty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IItem> ISequence<T> visit(IExpression expr, INodeContext context) {
    ISequence<?> retval = expr.accept(this, context);
    return (ISequence<T>)retval;
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitAddition(Addition expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitAnd(And expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitValueComparison(ValueComparison expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitGeneralComparison(GeneralComparison expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitContextItem(ContextItem expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, INodeContext context) {
    return defaultResult();
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitDivision(Division expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitFunctionCall(FunctionCall expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, INodeContext context) {
    return defaultResult();
  }

  @Override
  public ISequence<?> visitMetapath(Metapath expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INumericItem> visitMod(Mod expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitMultiplication(Multiplication expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INumericItem> visitNegate(Negate expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitOr(Or expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitParenthesizedExpression(ParenthesizedExpression expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr,
      INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitRelativeSlashPath(RelativeSlashPath expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitRootDoubleSlashPath(RootDoubleSlashPath expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IDocumentNodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath expr, INodeContext context) {
    return defaultResult();
  }

  @Override
  public ISequence<?> visitRootSlashPath(RootSlashPath expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitStep(Step expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IStringItem> visitStringConcat(StringConcat expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IStringItem> visitStringLiteral(StringLiteral expr, INodeContext context) {
    return defaultResult();
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitSubtraction(Subtraction expr, INodeContext context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitUnion(Union expr, INodeContext context) {
    return visitChildren(expr, context);
  }
}
