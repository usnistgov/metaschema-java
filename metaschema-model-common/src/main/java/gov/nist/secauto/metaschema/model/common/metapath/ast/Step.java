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

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IExpressionEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IExpressionVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An immutable expression that combines the evaluation of a sub-expression, with the evaluation of
 * a series of predicate expressions that filter the result of the evaluation.
 */
public class Step implements IExpression {
  @NotNull
  private final IExpression step;
  @NotNull
  private final List<@NotNull IExpression> predicates;
  @NotNull
  private final Class<? extends IItem> staticResultType;

  /**
   * Construct a new step expression.
   * 
   * @param stepExpr
   *          the sub-expression to evaluate before filtering with the predicates
   * @param predicates
   *          the expressions to apply as a filter
   */
  @SuppressWarnings("null")
  public Step(@NotNull IExpression stepExpr, @NotNull List<@NotNull IExpression> predicates) {
    this.step = stepExpr;
    this.predicates = predicates;
    this.staticResultType = ExpressionUtils.analyzeStaticResultType(IItem.class, List.of(step));
  }

  /**
   * Get the step's sub-expression.
   * 
   * @return the sub-expression
   */
  @NotNull
  public IExpression getStep() {
    return step;
  }

  /**
   * Retrieve the list of predicates to filter with.
   * 
   * @return the list of predicates
   */
  @NotNull
  public List<@NotNull IExpression> getPredicates() {
    return predicates;
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends IExpression> getChildren() {
    List<@NotNull IExpression> retval;
    if (!predicates.isEmpty()) {
      retval = Stream.concat(Stream.of(step), predicates.stream()).collect(Collectors.toList());
    } else {
      retval = Collections.singletonList(step);
    }
    return retval;
  }

  @Override
  public Class<? extends IItem> getStaticResultType() {
    return staticResultType;
  }

  @Override
  public ISequence<?> accept(IExpressionEvaluationVisitor visitor, INodeContext context) {
    return visitor.visitStep(this, context);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitStep(this, context);
  }

  @Override
  public String toString() {
    return new ASTPrinter().visit(this);
  }
}
