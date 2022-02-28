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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance;

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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Wildcard;

import org.jetbrains.annotations.NotNull;

public class AbstractExpressionVisitor<RESULT, CONTEXT> implements IExpressionVisitor<RESULT, CONTEXT> {

  protected RESULT visitChildren(@NotNull IExpression expr, CONTEXT context) {
    RESULT result = defaultResult();

    for (IExpression childExpr : expr.getChildren()) {
      if (!shouldVisitNextChild(expr, result, context)) {
        break;
      }

      RESULT childResult = childExpr.accept(this, context);
      result = aggregateResult(result, childResult);
    }

    return result;
  }

  protected boolean shouldVisitNextChild(@NotNull IExpression expr, RESULT result, CONTEXT context) {
    return true;
  }

  protected RESULT aggregateResult(RESULT result, RESULT nextResult) {
    return nextResult;
  }

  protected RESULT defaultResult() {
    return null;
  }

  @Override
  public RESULT visitAddition(Addition expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitAnd(And expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitStep(Step expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitValueComparison(ValueComparison expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitGeneralComparison(GeneralComparison expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitContextItem(ContextItem expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitDecimalLiteral(DecimalLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitDivision(Division expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitFlag(Flag expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitFunctionCall(FunctionCall expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitIntegerDivision(IntegerDivision expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitIntegerLiteral(IntegerLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitMetapath(Metapath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitMod(Mod expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitModelInstance(ModelInstance expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitMultiplication(Multiplication expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitName(Name expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitNegate(Negate expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitOr(Or expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitParenthesizedExpression(ParenthesizedExpression expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitRelativeSlashPath(RelativeSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitRootDoubleSlashPath(RootDoubleSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitRootSlashOnlyPath(RootSlashOnlyPath expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitRootSlashPath(RootSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitStringConcat(StringConcat expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitStringLiteral(StringLiteral expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitSubtraction(Subtraction expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitUnion(Union expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitWildcard(Wildcard expr, CONTEXT context) {
    return defaultResult();
  }
}