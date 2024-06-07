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
import gov.nist.secauto.metaschema.core.metapath.MetapathEvaluationFeature;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class PredicateExpression implements IExpression {
  @NonNull
  private final IExpression base;
  @NonNull
  private final List<IExpression> predicates;

  /**
   * Construct a new predicate expression.
   *
   * @param base
   *          the base to evaluate against
   * @param predicates
   *          the expression(s) to apply as a filter
   */
  public PredicateExpression(@NonNull IExpression base, @NonNull List<IExpression> predicates) {
    this.base = base;
    this.predicates = predicates;
  }

  /**
   * Get the base sub-expression.
   *
   * @return the sub-expression
   */
  @NonNull
  public IExpression getBase() {
    return base;
  }

  /**
   * Retrieve the list of predicates to filter with.
   *
   * @return the list of predicates
   */
  @NonNull
  public List<IExpression> getPredicates() {
    return predicates;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return ObjectUtils.notNull(
        Stream.concat(Stream.of(getBase()), getPredicates().stream()).collect(Collectors.toList()));
  }

  @Override
  public @NonNull ISequence<? extends IItem> accept(@NonNull DynamicContext dynamicContext,
      @NonNull ISequence<?> focus) {

    ISequence<?> retval = getBase().accept(dynamicContext, focus);

    if (dynamicContext.getConfiguration().isFeatureEnabled(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES)) {
      // evaluate the predicates for this step
      AtomicInteger index = new AtomicInteger();

      Stream<? extends IItem> stream = ObjectUtils.notNull(
          retval.stream().map(item -> {
            // build a positional index of the items
            return Map.entry(BigInteger.valueOf(index.incrementAndGet()), item);
          }).filter(entry -> {
            @SuppressWarnings("null")
            @NonNull IItem item = entry.getValue();

            // return false if any predicate evaluates to false
            return !predicates.stream()
                .map(predicateExpr -> {
                  boolean bool;
                  if (predicateExpr instanceof IntegerLiteral) {
                    // reduce the result to the matching item
                    BigInteger predicateIndex = ((IntegerLiteral) predicateExpr).getValue();

                    // get the position of the item
                    final BigInteger position = entry.getKey();

                    // it is a match if the position matches
                    bool = position.equals(predicateIndex);
                  } else {
                    ISequence<?> innerFocus = ISequence.of(item);
                    ISequence<?> predicateResult = predicateExpr.accept(dynamicContext, innerFocus);
                    bool = FnBoolean.fnBoolean(predicateResult).toBoolean();
                  }
                  return bool;
                }).anyMatch(x -> !x);
          }).map(Entry::getValue));

      retval = ISequence.of(stream);
    }
    return retval;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NonNull IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitPredicate(this, context);
  }

}
