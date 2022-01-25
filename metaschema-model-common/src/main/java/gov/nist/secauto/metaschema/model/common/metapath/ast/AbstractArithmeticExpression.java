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

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An immutable binary expression that supports arithmetic evaluation. The result type is determined
 * through static analysis of the sub-expressions, which may result in a more specific type that is
 * a sub-class of the base result type.
 * 
 * @param <RESULT_TYPE>
 *          the base result of evaluating the arithmetic expression
 */
public abstract class AbstractArithmeticExpression<RESULT_TYPE extends IAnyAtomicItem>
    extends AbstractBinaryExpression
    implements IArithmeticExpression<RESULT_TYPE> {

  @NotNull
  private final Class<? extends RESULT_TYPE> staticResultType;

  /**
   * Construct a new arithmetic expression.
   * 
   * @param left
   *          the left side of the arithmetic operation
   * @param right
   *          the right side of the arithmetic operation
   * @param baseType
   *          the base result type of the expression result
   */
  @SuppressWarnings("null")
  public AbstractArithmeticExpression(@NotNull IExpression left, @NotNull IExpression right,
      @NotNull Class<RESULT_TYPE> baseType) {
    super(left, right);
    this.staticResultType = ExpressionUtils.analyzeStaticResultType(baseType, List.of(left, right));
  }

  @Override
  public abstract Class<RESULT_TYPE> getBaseResultType();

  @Override
  public Class<? extends RESULT_TYPE> getStaticResultType() {
    return staticResultType;
  }
}
