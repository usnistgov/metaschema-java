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

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ExpressionTestBase;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
class PredicateExpressionTest
    extends ExpressionTestBase {

  @Test
  void testPredicateWithValues() {
    DynamicContext dynamicContext = newDynamicContext();
    Mockery context = getContext();

    @SuppressWarnings("null")
    @NonNull IExpression stepExpr = context.mock(IExpression.class);
    ISequence<?> stepResult = context.mock(ISequence.class, "stepResult");
    @SuppressWarnings("null")
    @NonNull IAssemblyNodeItem item = context.mock(IAssemblyNodeItem.class);
    @SuppressWarnings({ "unchecked", "null" })
    @NonNull List<IExpression> predicates = context.mock(List.class, "predicates");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(stepExpr).getStaticResultType();
        will(returnValue(IAssemblyNodeItem.class));
        oneOf(stepExpr).accept(dynamicContext, ISequence.of(item));
        will(returnValue(stepResult));

        atMost(1).of(stepResult).stream();
        will(returnValue(Stream.of(item)));
        atMost(1).of(stepResult).getValue();
        will(returnValue(CollectionUtil.singletonList(item)));

        allowing(item).getNodeItem();
        will(returnValue(item));

        atMost(1).of(predicates).stream();
        will(returnValue(Stream.empty()));
        atMost(1).of(predicates).iterator();
        will(returnValue(Stream.empty()));
      }
    });

    PredicateExpression expr = new PredicateExpression(stepExpr, predicates);

    ISequence<?> result = expr.accept(dynamicContext, ISequence.of(item));
    assertEquals(ISequence.of(item), result, "Sequence does not match");
  }

  @Test
  void testPredicateWithoutValues() {
    DynamicContext dynamicContext = newDynamicContext().disablePredicateEvaluation();
    Mockery context = getContext();

    @SuppressWarnings("null")
    @NonNull IExpression stepExpr = context.mock(IExpression.class);
    ISequence<?> stepResult = context.mock(ISequence.class, "stepResult");
    @SuppressWarnings("null")
    @NonNull IAssemblyNodeItem item = context.mock(IAssemblyNodeItem.class);
    @SuppressWarnings({ "unchecked", "null" })
    @NonNull List<IExpression> predicates = context.mock(List.class, "predicates");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(stepExpr).getStaticResultType();
        will(returnValue(IAssemblyNodeItem.class));
        oneOf(stepExpr).accept(dynamicContext, ISequence.of(item));
        will(returnValue(stepResult));

        atMost(1).of(stepResult).stream();
        will(returnValue(Stream.of(item)));
        atMost(1).of(stepResult).getValue();
        will(returnValue(CollectionUtil.singletonList(item)));

        allowing(item).getNodeItem();
        will(returnValue(item));

        never(predicates).stream();
        never(predicates).iterator();
      }
    });

    PredicateExpression expr = new PredicateExpression(stepExpr, predicates);

    ISequence<?> result = expr.accept(dynamicContext, ISequence.of(item));
    assertEquals(ISequence.of(item), result, "Sequence does not match");
  }
}
