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
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An implementation of
 * <a href="https://www.w3.org/TR/xpath-31/#id-let-expressions">Let
 * expression</a> supporting variable value binding.
 */
public class Let implements IExpression { // NOPMD class name ok
  @NonNull
  private final Name name;
  @NonNull
  private final IExpression boundExpression;
  @NonNull
  private final IExpression returnExpression;

  /**
   * Construct a new Let CST expression.
   *
   * @param name
   *          the variable name
   * @param boundExpression
   *          the expression bound to the variable
   * @param returnExpression
   *          the inner expression to evaluate with the variable in-scope
   */
  public Let(@NonNull Name name, @NonNull IExpression boundExpression, @NonNull IExpression returnExpression) {
    this.name = name;
    this.boundExpression = boundExpression;
    this.returnExpression = returnExpression;
  }

  /**
   * Get the variable name.
   *
   * @return the variable name
   */
  @NonNull
  public Name getName() {
    return name;
  }

  /**
   * Get the expression bound to the variable.
   *
   * @return the bound expression
   */
  @NonNull
  public IExpression getBoundExpression() {
    return boundExpression;
  }

  /**
   * Get the inner expression to evaluate with the variable in-scope.
   *
   * @return the inner expression
   */
  @NonNull
  public IExpression getReturnExpression() {
    return returnExpression;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return ObjectUtils.notNull(
        List.of(boundExpression, returnExpression));
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitLet(this, context);
  }

  @Override
  public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    ISequence<?> result = getBoundExpression().accept(dynamicContext, focus);

    String name = getName().getValue();

    DynamicContext subDynamicContext = dynamicContext.subContext();

    subDynamicContext.bindVariableValue(name, result);

    return getReturnExpression().accept(subDynamicContext, focus);
  }
}
