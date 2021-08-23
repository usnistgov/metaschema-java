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

public class ASTPrinter extends AbstractExpressionVisitor<String, Void> {
  private int indentation = 0;
  private int lastIndentation = 0;
  private String indentationPadding = "";

  private String getIndentation() {
    if (indentation != lastIndentation) {
      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < indentation; i++) {
        buffer.append("  ");
      }
      lastIndentation = indentation;
      indentationPadding = buffer.toString();
    }
    return indentationPadding;
  }

  public String visit(IExpression expr) {
    return expr.accept(this, null);
  }

  @Override
  protected String visitChildren(IExpression expr, Void context) {
    indentation++;
    String result = super.visitChildren(expr, context);
    indentation--;
    return result;
  }

  @Override
  protected String aggregateResult(String result, String nextResult) {
    StringBuilder buffer = new StringBuilder();
    if (result != null) {
      buffer.append(result);
      buffer.append(System.lineSeparator());
    }

    buffer.append(getIndentation());
    buffer.append(nextResult);
    return buffer.toString();
  }

  @Override
  protected String defaultResult() {
    return super.defaultResult();
  }

  protected String appendNode(IExpression expr, String childResult) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getIndentation());
    buffer.append(expr.toASTString());
    if (childResult != null) {
      buffer.append(System.lineSeparator());
      buffer.append(childResult);
    }
    return buffer.toString();
  }

  @Override
  public String visitAddition(Addition expr, Void context) {
    return appendNode(expr, super.visitAddition(expr, context));
  }

  @Override
  public String visitAnd(And expr, Void context) {
    return appendNode(expr, super.visitAnd(expr, context));
  }

  @Override
  public String visitStep(Step expr, Void context) {
    return appendNode(expr, super.visitStep(expr, context));
  }

  @Override
  public String visitComparison(Comparison expr, Void context) {
    return appendNode(expr, super.visitComparison(expr, context));
  }

  @Override
  public String visitContextItem(ContextItem expr, Void context) {
    return appendNode(expr, super.visitContextItem(expr, context));
  }

  @Override
  public String visitDecimalLiteral(DecimalLiteral expr, Void context) {
    return appendNode(expr, super.visitDecimalLiteral(expr, context));
  }

  @Override
  public String visitDivision(Division expr, Void context) {
    return appendNode(expr, super.visitDivision(expr, context));
  }

  @Override
  public String visitFlag(Flag expr, Void context) {
    return appendNode(expr, super.visitFlag(expr, context));
  }

  @Override
  public String visitFunctionCall(FunctionCall expr, Void context) {
    return appendNode(expr, super.visitFunctionCall(expr, context));
  }

  @Override
  public String visitIntegerDivision(IntegerDivision expr, Void context) {
    return appendNode(expr, super.visitIntegerDivision(expr, context));
  }

  @Override
  public String visitIntegerLiteral(IntegerLiteral expr, Void context) {
    return appendNode(expr, super.visitIntegerLiteral(expr, context));
  }

  @Override
  public String visitMetapath(Metapath expr, Void context) {
    return appendNode(expr, super.visitMetapath(expr, context));
  }

  @Override
  public String visitMod(Mod expr, Void context) {
    return appendNode(expr, super.visitMod(expr, context));
  }

  @Override
  public String visitModelInstance(ModelInstance expr, Void context) {
    return appendNode(expr, super.visitModelInstance(expr, context));
  }

  @Override
  public String visitMultiplication(Multiplication expr, Void context) {
    return appendNode(expr, super.visitMultiplication(expr, context));
  }

  @Override
  public String visitName(Name expr, Void context) {
    return appendNode(expr, super.visitName(expr, context));
  }

  @Override
  public String visitNegate(Negate expr, Void context) {
    return appendNode(expr, super.visitNegate(expr, context));
  }

  @Override
  public String visitOr(OrNode expr, Void context) {
    return appendNode(expr, super.visitOr(expr, context));
  }

  @Override
  public String visitParenthesizedExpression(ParenthesizedExpression expr, Void context) {
    return appendNode(expr, super.visitParenthesizedExpression(expr, context));
  }

  @Override
  public String visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, Void context) {
    return appendNode(expr, super.visitRelativeDoubleSlashPath(expr, context));
  }

  @Override
  public String visitRelativeSlashPath(RelativeSlashPath expr, Void context) {
    return appendNode(expr, super.visitRelativeSlashPath(expr, context));
  }

  @Override
  public String visitRootDoubleSlashPath(RootDoubleSlashPath expr, Void context) {
    return appendNode(expr, super.visitRootDoubleSlashPath(expr, context));
  }

  @Override
  public String visitRootSlashOnlyPath(RootSlashOnlyPath expr, Void context) {
    return appendNode(expr, super.visitRootSlashOnlyPath(expr, context));
  }

  @Override
  public String visitRootSlashPath(RootSlashPath expr, Void context) {
    return appendNode(expr, super.visitRootSlashPath(expr, context));
  }

  @Override
  public String visitStringConcat(StringConcat expr, Void context) {
    return appendNode(expr, super.visitStringConcat(expr, context));
  }

  @Override
  public String visitStringLiteral(StringLiteral expr, Void context) {
    return appendNode(expr, super.visitStringLiteral(expr, context));
  }

  @Override
  public String visitSubtraction(Subtraction expr, Void context) {
    return appendNode(expr, super.visitSubtraction(expr, context));
  }

  @Override
  public String visitUnion(Union expr, Void context) {
    return appendNode(expr, super.visitUnion(expr, context));
  }

  @Override
  public String visitWildcard(Wildcard expr, Void context) {
    return appendNode(expr, super.visitWildcard(expr, context));
  }

}
