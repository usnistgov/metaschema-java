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

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.cst.Let.VariableDeclaration;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An implementation of the
 * <a href="https://www.w3.org/TR/xpath-31/#id-for-expressions">For
 * expression</a> supporting variable-based iteration.
 */
@SuppressWarnings("PMD.ShortClassName")
public class For implements IExpression {
  @NonNull
  private final Let.VariableDeclaration variable;
  @NonNull
  private final IExpression returnExpression;

  /**
   * Construct a new let expression using the provided variable and return clause.
   *
   * @param variable
   *          the variable declaration
   * @param returnExpr
   *          the return clause that makes use of variables for evaluation
   */
  public For(@NonNull VariableDeclaration variable, @NonNull IExpression returnExpr) {
    this.variable = variable;
    this.returnExpression = returnExpr;
  }

  /**
   * Get the variable declaration.
   *
   * @return the variable declaration expression
   */
  @NonNull
  protected Let.VariableDeclaration getVariable() {
    return variable;
  }

  /**
   * Get the return expression.
   *
   * @return the return expression
   */
  @NonNull
  protected IExpression getReturnExpression() {
    return returnExpression;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return ObjectUtils.notNull(
        List.of(returnExpression));
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFor(this, context);
  }

  @Override
  public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    Let.VariableDeclaration variable = getVariable();
    ISequence<?> variableResult = variable.getBoundExpression().accept(dynamicContext, focus);

    DynamicContext subDynamicContext = dynamicContext.subContext();

    List<IItem> retval = new LinkedList<>();
    for (IItem item : variableResult) {
      subDynamicContext.bindVariableValue(variable.getName(), ISequence.of(item));
      retval.addAll(getReturnExpression().accept(subDynamicContext, focus));
    }
    return ISequence.ofCollection(retval);
  }
}
