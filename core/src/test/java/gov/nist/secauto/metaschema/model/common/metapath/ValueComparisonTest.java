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

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.model.common.metapath.IComparison.Operator;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ValueComparisonTest
    extends ExpressionTestBase {

  private static Stream<Arguments> testValueComparison() { // NOPMD - false positive
    return Stream.of(
        // string
        Arguments.of(IStringItem.valueOf("AbC"), Operator.EQ, IStringItem.valueOf("AbC"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("AbC"), Operator.EQ, IStringItem.valueOf("xYz"), IBooleanItem.FALSE),
        Arguments.of(IStringItem.valueOf("A.1"), Operator.NE, IStringItem.valueOf("A.2"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("A.1"), Operator.NE, IStringItem.valueOf("A.1"), IBooleanItem.FALSE),
        Arguments.of(IStringItem.valueOf("A.3"), Operator.GE, IStringItem.valueOf("A.2"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("B\\1"), Operator.GE, IStringItem.valueOf("B\\1"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("A.1"), Operator.GE, IStringItem.valueOf("A.2"), IBooleanItem.FALSE),
        Arguments.of(IStringItem.valueOf("A"), Operator.LE, IStringItem.valueOf("A.2"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("B\\1"), Operator.LE, IStringItem.valueOf("C\\1"), IBooleanItem.TRUE),
        Arguments.of(IStringItem.valueOf("X#"), Operator.LE, IStringItem.valueOf("X"), IBooleanItem.FALSE),
        // boolean
        Arguments.of(IBooleanItem.TRUE, Operator.EQ, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.FALSE, Operator.EQ, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.EQ, IBooleanItem.FALSE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, Operator.EQ, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.TRUE, Operator.GE, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.GT, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.TRUE, Operator.LE, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.LT, IBooleanItem.TRUE, IBooleanItem.FALSE)

    );
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @MethodSource
  void testValueComparison(IItem leftItem, Operator operator, IItem rightItem, IBooleanItem expectedResult) {
    DynamicContext dynamicContext = newDynamicContext();
    Mockery context = getContext();

    INodeContext nodeContext = context.mock(INodeContext.class);

    IExpression exp1 = context.mock(IExpression.class, "exp1");
    IExpression exp2 = context.mock(IExpression.class, "exp2");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        atMost(1).of(exp1).accept(dynamicContext, nodeContext);
        will(returnValue(ISequence.of(leftItem)));
        atMost(1).of(exp2).accept(dynamicContext, nodeContext);
        will(returnValue(ISequence.of(rightItem)));
      }
    });

    ValueComparison expr = new ValueComparison(exp1, operator, exp2);

    ISequence<?> result = expr.accept(dynamicContext, nodeContext);
    assertEquals(ISequence.of(expectedResult), result, "Sequence does not match");
  }
}
