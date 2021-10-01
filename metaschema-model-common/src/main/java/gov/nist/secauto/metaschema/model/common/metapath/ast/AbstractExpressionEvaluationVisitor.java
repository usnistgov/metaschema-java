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

import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
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

public class AbstractExpressionEvaluationVisitor<CONTEXT> implements ExpressionEvaluationVisitor<CONTEXT> {

  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> visitChildren(IExpression<ITEM_TYPE> expr, CONTEXT context) {
    ISequence<ITEM_TYPE> result = defaultResult();
    int numChildren = expr.getChildCount();
    for (int idx = 0; idx < numChildren; idx++) {
      if (!shouldVisitNextChild(expr, result, context)) {
        break;
      }

      IExpression<?> childExpr = expr.getChild(idx);
      ISequence<?> childResult = childExpr.accept(this, context);
      result = aggregateResult(result, childResult);
    }

    return result;
  }

  protected <ITEM_TYPE extends IItem> boolean shouldVisitNextChild(IExpression<ITEM_TYPE> expr,
      ISequence<ITEM_TYPE> result, CONTEXT context) {
    return true;
  }

  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> aggregateResult(ISequence<ITEM_TYPE> result,
      ISequence<?> childResult) {
    return result;
  }

  protected <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> defaultResult() {
    return ISequence.empty();
  }

  public <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> visit(IExpression<?> expr, CONTEXT context) {
    @SuppressWarnings("unchecked")
    ISequence<ITEM_TYPE> retval = (ISequence<ITEM_TYPE>) expr.accept(this, context);
    return retval;
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitAddition(Addition expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitAnd(And expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitComparison(Comparison expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitContextItem(ContextItem expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitDivision(Division expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitFunctionCall(FunctionCall expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public ISequence<?> visitMetapath(Metapath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INumericItem> visitMod(Mod expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitMultiplication(Multiplication expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INumericItem> visitNegate(Negate expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IBooleanItem> visitOr(OrNode expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitParenthesizedExpression(ParenthesizedExpression expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitRelativeSlashPath(RelativeSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public ISequence<? extends INodeItem> visitRootSlashPath(RootSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends INodeItem> visitStep(Step expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IStringItem> visitStringConcat(StringConcat expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<? extends IStringItem> visitStringLiteral(StringLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> visitSubtraction(Subtraction expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public ISequence<?> visitUnion(Union expr, CONTEXT context) {
    return visitChildren(expr, context);
  }
}
