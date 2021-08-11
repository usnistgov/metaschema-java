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

public class AbstractExpressionVisitor<RESULT, CONTEXT> implements ExpressionVisitor<RESULT, CONTEXT> {
  @Override
  public RESULT visit(Expression expr, CONTEXT context) {
    return expr.accept(this, context);
  }

  protected RESULT visitChildren(Expression expr, CONTEXT context) {
    RESULT result = defaultResult();
    int numChildren = expr.getChildCount();
    for (int idx = 0; idx < numChildren; idx++) {
      if (!shouldVisitNextChild(expr, result, context)) {
        break;
      }

      Expression childExpr = expr.getChild(idx);
      RESULT childResult = childExpr.accept(this, context);
      result = aggregateResult(result, childResult);
    }

    return result;
  }

  protected boolean shouldVisitNextChild(Expression expr, RESULT result, CONTEXT context) {
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
  public RESULT visitAxisStep(AxisStep expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitComparison(Comparison expr, CONTEXT context) {
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
  public RESULT visitDoubleSlashPath(DoubleSlashPath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitFlag(Flag expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitFunction(Function expr, CONTEXT context) {
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
  public RESULT visitOr(OrNode expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitParenthesizedExpression(ParenthesizedExpression expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitPostfix(PostfixExpr expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitRelativePath(RelativePath expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitSlashOnlyPath(SlashOnlyPath expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitSlashPath(SlashPath expr, CONTEXT context) {
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
  public RESULT visitTextTest(TextTest expr, CONTEXT context) {
    return defaultResult();
  }

  @Override
  public RESULT visitUnion(Union expr, CONTEXT context) {
    return visitChildren(expr, context);
  }

  @Override
  public RESULT visitWildcard(WildcardExpr expr, CONTEXT context) {
    return defaultResult();
  }
}
