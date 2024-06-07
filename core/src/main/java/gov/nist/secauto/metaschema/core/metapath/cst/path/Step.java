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

package gov.nist.secauto.metaschema.core.metapath.cst.path;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.cst.ExpressionUtils;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpressionVisitor;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An immutable expression that combines the evaluation of a sub-expression,
 * with the evaluation of a series of predicate expressions that filter the
 * result of the evaluation.
 */
public class Step implements IExpression { // NOPMD - intentional

  @NonNull
  private final Axis axisExpression;
  @NonNull
  private final INodeTestExpression stepExpression;
  @NonNull
  private final Class<? extends IItem> staticResultType;

  /**
   * Construct a new stepExpression expression.
   *
   * @param axis
   *          the axis to evaluate against
   * @param step
   *          the sub-expression to evaluate before filtering with the predicates
   */
  @SuppressWarnings("null")
  public Step(@NonNull Axis axis, @NonNull INodeTestExpression step) {
    this.axisExpression = axis;
    this.stepExpression = step;
    this.staticResultType = ExpressionUtils.analyzeStaticResultType(IItem.class, List.of(step));
  }

  /**
   * Get the axis to use for the step.
   *
   * @return the step axis to use
   */
  @NonNull
  public Axis getAxis() {
    return axisExpression;
  }

  /**
   * Get the step expression's sub-expression.
   *
   * @return the sub-expression
   */
  @NonNull
  public INodeTestExpression getStep() {
    return stepExpression;
  }

  @Override
  public Class<? extends IItem> getStaticResultType() {
    return staticResultType;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return ObjectUtils.notNull(List.of(getAxis(), getStep()));
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitStep(this, context);
  }

  @Override
  public ISequence<?> accept(DynamicContext dynamicContext, ISequence<?> focus) {

    ISequence<? extends INodeItem> axisResult = getAxis().accept(dynamicContext, focus);
    return getStep().accept(dynamicContext, axisResult);
  }

  @SuppressWarnings("null")
  @Override
  public String toASTString() {
    return String.format("%s[axis=%s]", getClass().getName(), getAxis().name());
  }
}
