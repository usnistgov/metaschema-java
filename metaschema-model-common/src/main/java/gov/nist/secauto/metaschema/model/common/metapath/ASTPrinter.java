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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.ASTPrinter.State;

import org.jetbrains.annotations.NotNull;

public final class ASTPrinter
    extends AbstractExpressionVisitor<String, @NotNull State> {

  private static final ASTPrinter SINGLETON = new ASTPrinter();

  /**
   * Get the singleton instance.
   * 
   * @return the instance
   */
  public static ASTPrinter instance() {
    return SINGLETON;
  }

  private ASTPrinter() {
    // disable construction
  }

  @Override
  protected String visitChildren(IExpression expr, State context) {
    context.push();
    String result = super.visitChildren(expr, context);
    context.pop();
    return result;
  }

  @Override
  protected String aggregateResult(String result, String nextResult, State context) {
    StringBuilder buffer = new StringBuilder();
    if (result != null) {
      buffer.append(result);
      // buffer.append(" ar "+System.lineSeparator());
    }

    buffer.append(context.getIndentation())
        .append(nextResult);
    return buffer.toString();
  }

  @Override
  protected String defaultResult() {
    return "";
  }

  /**
   * Append the {@code childResult} to the record produced for the current node.
   * 
   * @param expr
   *          the current node
   * @param childResult
   *          the output generated for the curren't node's children
   * @param context
   *          the output context state
   * @return the string representation of the node tree for the current node and its children
   */
  protected String appendNode(IExpression expr, String childResult, State context) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(context.getIndentation())
        .append(expr.toASTString());
    if (childResult != null) {
      buffer.append(System.lineSeparator())
          .append(childResult);
    }
    return buffer.toString();
  }

  /**
   * Visit a node and produce a string representation of its the node tree.
   * 
   * @param expression
   *          the node to build the node tree for
   * @return the string representation of the node tree for the provided expression node and its
   *         children
   */
  public String visit(IExpression expression) {
    return visit(expression, new State());
  }

  @Override
  public String visitAddition(Addition expr, State context) {
    return appendNode(expr, super.visitAddition(expr, context), context);
  }

  @Override
  public String visitAnd(And expr, State context) {
    return appendNode(expr, super.visitAnd(expr, context), context);
  }

  @Override
  public String visitStep(Step expr, State context) {
    return appendNode(expr, super.visitStep(expr, context), context);
  }

  @Override
  public String visitValueComparison(ValueComparison expr, State context) {
    return appendNode(expr, super.visitValueComparison(expr, context), context);
  }

  @Override
  public String visitGeneralComparison(GeneralComparison expr, State context) {
    return appendNode(expr, super.visitGeneralComparison(expr, context), context);
  }

  @Override
  public String visitContextItem(ContextItem expr, State context) {
    return appendNode(expr, super.visitContextItem(expr, context), context);
  }

  @Override
  public String visitDecimalLiteral(DecimalLiteral expr, State context) {
    return appendNode(expr, super.visitDecimalLiteral(expr, context), context);
  }

  @Override
  public String visitDivision(Division expr, State context) {
    return appendNode(expr, super.visitDivision(expr, context), context);
  }

  @Override
  public String visitExcept(@NotNull Except expr, State context) {
    return appendNode(expr, super.visitExcept(expr, context), context);
  }

  @Override
  public String visitFlag(Flag expr, State context) {
    return appendNode(expr, super.visitFlag(expr, context), context);
  }

  @Override
  public String visitFunctionCall(FunctionCall expr, State context) {
    return appendNode(expr, super.visitFunctionCall(expr, context), context);
  }

  @Override
  public String visitIntegerDivision(IntegerDivision expr, State context) {
    return appendNode(expr, super.visitIntegerDivision(expr, context), context);
  }

  @Override
  public String visitIntegerLiteral(IntegerLiteral expr, State context) {
    return appendNode(expr, super.visitIntegerLiteral(expr, context), context);
  }

  @Override
  public String visitIntersect(@NotNull Intersect expr, State context) {
    return appendNode(expr, super.visitIntersect(expr, context), context);
  }

  @Override
  public String visitMetapath(Metapath expr, State context) {
    return appendNode(expr, super.visitMetapath(expr, context), context);
  }

  @Override
  public String visitModulo(Modulo expr, State context) {
    return appendNode(expr, super.visitModulo(expr, context), context);
  }

  @Override
  public String visitModelInstance(ModelInstance expr, State context) {
    return appendNode(expr, super.visitModelInstance(expr, context), context);
  }

  @Override
  public String visitMultiplication(Multiplication expr, State context) {
    return appendNode(expr, super.visitMultiplication(expr, context), context);
  }

  @Override
  public String visitName(Name expr, State context) {
    return appendNode(expr, super.visitName(expr, context), context);
  }

  @Override
  public String visitNegate(Negate expr, State context) {
    return appendNode(expr, super.visitNegate(expr, context), context);
  }

  @Override
  public String visitOr(Or expr, State context) {
    return appendNode(expr, super.visitOr(expr, context), context);
  }

  @Override
  public String visitParentItem(ParentItem expr, State context) {
    return appendNode(expr, super.visitParentItem(expr, context), context);
  }

  @Override
  public String visitPredicate(@NotNull Predicate expr, State context) {
    return appendNode(expr, super.visitPredicate(expr, context), context);
  }

  @Override
  public String visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, State context) {
    return appendNode(expr, super.visitRelativeDoubleSlashPath(expr, context), context);
  }

  @Override
  public String visitRelativeSlashPath(RelativeSlashPath expr, State context) {
    return appendNode(expr, super.visitRelativeSlashPath(expr, context), context);
  }

  @Override
  public String visitRootDoubleSlashPath(RootDoubleSlashPath expr, State context) {
    return appendNode(expr, super.visitRootDoubleSlashPath(expr, context), context);
  }

  @Override
  public String visitRootSlashOnlyPath(RootSlashOnlyPath expr, State context) {
    return appendNode(expr, super.visitRootSlashOnlyPath(expr, context), context);
  }

  @Override
  public String visitRootSlashPath(RootSlashPath expr, State context) {
    return appendNode(expr, super.visitRootSlashPath(expr, context), context);
  }

  @Override
  public String visitStringConcat(StringConcat expr, State context) {
    return appendNode(expr, super.visitStringConcat(expr, context), context);
  }

  @Override
  public String visitStringLiteral(StringLiteral expr, State context) {
    return appendNode(expr, super.visitStringLiteral(expr, context), context);
  }

  @Override
  public String visitSubtraction(Subtraction expr, State context) {
    return appendNode(expr, super.visitSubtraction(expr, context), context);
  }

  @Override
  public String visitUnion(Union expr, State context) {
    return appendNode(expr, super.visitUnion(expr, context), context);
  }

  @Override
  public String visitWildcard(Wildcard expr, State context) {
    return appendNode(expr, super.visitWildcard(expr, context), context);
  }

  static class State {
    private int indentation; // 0;
    private int lastIndentation; // 0;
    private String indentationPadding = "";

    public String getIndentation() {
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

    public State push() {
      indentation++;
      return this;
    }

    public State pop() {
      indentation--;
      return this;
    }
  }
}
