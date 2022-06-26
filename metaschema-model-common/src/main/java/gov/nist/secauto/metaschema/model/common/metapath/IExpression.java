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

import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IExpression {
  /**
   * Retrieve the child expressions associated with this expression.
   * 
   * @return a list of expressions, which may be empty
   */
  @NotNull
  List<@NotNull ? extends IExpression> getChildren();

  /**
   * The minimum expected result type to be produced when evaluating the expression. The result may be
   * a sub-class or sub-interface of this value.
   * 
   * @return the base result type
   */
  @NotNull
  default Class<? extends IItem> getBaseResultType() {
    return IItem.class;
  }

  /**
   * The expected result type produced by evaluating the expression. The result must be the same or a
   * sub-class or sub-interface of the value provided by {@link #getBaseResultType()}.
   * <p>
   * This method can be overloaded to provide static analysis of the expression to determine a more
   * specific result type.
   * 
   * @return the result type
   */
  @NotNull
  default Class<? extends IItem> getStaticResultType() {
    return getBaseResultType();
  }

  /**
   * Produce a string representation of this expression including the expression's name.
   * <p>
   * This method can be overloaded to provide a more appropriate representation of the expression.
   * 
   * @return a string representing the data elements of the expression
   */
  @SuppressWarnings("null")
  @NotNull
  default String toASTString() {
    return String.format("%s[]", getClass().getName());
  }

  /**
   * Provides a double dispatch callback for visitor handling.
   * 
   * @param dynamicContext
   *          the dynamic evaluation context
   * @param context
   *          the initial focus node item
   * @return the result of evaluation
   */
  @NotNull
  ISequence<? extends IItem> accept(@NotNull DynamicContext dynamicContext, @NotNull INodeContext context);

  /**
   * Provides a double dispatch callback for visitor handling.
   * 
   * @param <RESULT>
   *          the type of the evaluation result
   * @param <CONTEXT>
   *          the type of the visitor context
   * @param visitor
   *          the visitor calling this method
   * @param context
   *          the visitor context
   * @return the result of evaluation
   */
  <RESULT, CONTEXT> RESULT accept(@NotNull IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context);
}
