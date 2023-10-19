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
import gov.nist.secauto.metaschema.core.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An implementation of
 * <a href="https://www.w3.org/TR/xpath-31/#doc-xpath31-IfExpr">If
 * expression</a> supporting conditional evaluation.
 */
@SuppressWarnings("PMD.ShortClassName")
public class If
    extends AbstractExpression {
  private final IExpression testExpression;
  private final IExpression thenExpression;
  private final IExpression elseExpression;

  /**
   * Construct a new conditional expression.
   *
   * @param testExpression
   *          the first expression to evaluate
   * @param thenExpression
   *          the expression to evaluate if the test is {@code true}
   * @param elseExpression
   *          the expression to evaluate if the test is {@code false}
   */
  public If(
      @NonNull IExpression testExpression,
      @NonNull IExpression thenExpression,
      @NonNull IExpression elseExpression) {
    this.testExpression = testExpression;
    this.thenExpression = thenExpression;
    this.elseExpression = elseExpression;
  }

  /**
   * Get the "test" expression.
   *
   * @return the expression
   */
  protected IExpression getTestExpression() {
    return testExpression;
  }

  /**
   * Get the "then" expression.
   *
   * @return the expression
   */
  protected IExpression getThenExpression() {
    return thenExpression;
  }

  /**
   * Get the "else" expression.
   *
   * @return the expression
   */
  protected IExpression getElseExpression() {
    return elseExpression;
  }

  @Override
  public List<IExpression> getChildren() {
    return ObjectUtils.notNull(List.of(testExpression, thenExpression, elseExpression));
  }

  @Override
  public ISequence<?> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    ISequence<?> result = getTestExpression().accept(dynamicContext, focus);

    ISequence<?> retval;
    IBooleanItem effectiveResult = FnBoolean.fnBoolean(result);
    if (effectiveResult.toBoolean()) {
      retval = getThenExpression().accept(dynamicContext, focus);
    } else {
      retval = getElseExpression().accept(dynamicContext, focus);
    }
    return retval;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitIf(this, context);
  }
}
