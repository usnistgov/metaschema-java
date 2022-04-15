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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.IDocumentLoader;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.ast.And;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IComparison.Operator;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Or;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Step;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ValueComparison;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.NodeItemType;

import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

class MetaschemaPathEvaluationVisitorTest {
  @RegisterExtension
  Mockery context = new JUnit5Mockery();
  @Mock
  private IDocumentLoader loader;

  MetaschemaPathEvaluationVisitor newMetaschemaPathEvaluationVisitor() {
    StaticContext staticContext = new StaticContext();

    @SuppressWarnings("null")
    @NotNull
    URI baseUri = new File("").getAbsoluteFile().toURI();
    staticContext.setBaseUri(baseUri);

    DynamicContext dynamicContext = staticContext.newDynamicContext();
    dynamicContext.setDocumentLoader(loader);

    return new MetaschemaPathEvaluationVisitor(dynamicContext);
  }

  private static Stream<Arguments> testAnd() {
    return Stream.of(
        Arguments.of(IBooleanItem.TRUE, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, IBooleanItem.FALSE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, IBooleanItem.FALSE, IBooleanItem.FALSE));
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @MethodSource
  void testAnd(IBooleanItem bool1, IBooleanItem bool2, IBooleanItem expectedResult) {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    INodeContext nodeContext = context.mock(INodeContext.class);

    IExpression exp1 = context.mock(IExpression.class, "exp1");
    IExpression exp2 = context.mock(IExpression.class, "exp2");

    context.checking(new Expectations() {
      {
        atMost(1).of(exp1).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(bool1)));
        atMost(1).of(exp2).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(bool2)));
      }
    });

    And expr = new And(List.of(exp1, exp2));

    ISequence<?> result = visitor.visitAnd(expr, nodeContext);
    assertEquals(ISequence.of(expectedResult), result);
  }

  private static Stream<Arguments> testOr() {
    return Stream.of(
        Arguments.of(IBooleanItem.TRUE, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.FALSE, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.FALSE, IBooleanItem.FALSE, IBooleanItem.FALSE));
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @MethodSource
  void testOr(IBooleanItem bool1, IBooleanItem bool2, IBooleanItem expectedResult) {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    INodeContext nodeContext = context.mock(INodeContext.class);

    IExpression exp1 = context.mock(IExpression.class, "exp1");
    IExpression exp2 = context.mock(IExpression.class, "exp2");

    context.checking(new Expectations() {
      {
        atMost(1).of(exp1).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(bool1)));
        atMost(1).of(exp2).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(bool2)));
      }
    });

    Or expr = new Or(List.of(exp1, exp2));

    ISequence<?> result = visitor.visitOr(expr, nodeContext);
    assertEquals(ISequence.of(expectedResult), result);
  }

  private static Stream<Arguments> testValueComparison() {
    return Stream.of(
        // boolean
        Arguments.of(IBooleanItem.TRUE, Operator.EQ, IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.FALSE, Operator.EQ, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.EQ, IBooleanItem.FALSE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, Operator.EQ, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.TRUE, Operator.NE, IBooleanItem.TRUE, IBooleanItem.FALSE),
        Arguments.of(IBooleanItem.FALSE, Operator.NE, IBooleanItem.FALSE, IBooleanItem.FALSE)
    // Arguments.of(IBooleanItem.TRUE, Operator.GE, IBooleanItem.TRUE, IBooleanItem.TRUE),
    // Arguments.of(IBooleanItem.TRUE, Operator.GT, IBooleanItem.TRUE, IBooleanItem.TRUE),
    // Arguments.of(IBooleanItem.TRUE, Operator.LE, IBooleanItem.TRUE, IBooleanItem.TRUE),
    // Arguments.of(IBooleanItem.TRUE, Operator.LT, IBooleanItem.TRUE, IBooleanItem.TRUE),
    );
  }

  @SuppressWarnings("null")
  @ParameterizedTest
  @MethodSource
  void testValueComparison(IItem leftItem, Operator operator, IItem rightItem, IBooleanItem expectedResult) {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    INodeContext nodeContext = context.mock(INodeContext.class);

    IExpression exp1 = context.mock(IExpression.class, "exp1");
    IExpression exp2 = context.mock(IExpression.class, "exp2");

    context.checking(new Expectations() {
      {
        atMost(1).of(exp1).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(leftItem)));
        atMost(1).of(exp2).accept(visitor, nodeContext);
        will(returnValue(ISequence.of(rightItem)));
      }
    });

    ValueComparison expr = new ValueComparison(exp1, operator, exp2);

    ISequence<?> result = visitor.visitValueComparison(expr, nodeContext);
    assertEquals(ISequence.of(expectedResult), result);
  }

  @Test
  void testRootSlashOnlyPathUsingDocument() {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    IDocumentNodeItem nodeContext = context.mock(IDocumentNodeItem.class);

    RootSlashOnlyPath expr = new RootSlashOnlyPath();

    ISequence<?> result = visitor.visitRootSlashOnlyPath(expr, nodeContext);
    assertEquals(ISequence.of(nodeContext), result);
  }

  @Test
  void testRootSlashOnlyPathUsingNonDocument() {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    INodeItem nodeContext = context.mock(INodeItem.class);

    RootSlashOnlyPath expr = new RootSlashOnlyPath();

    ISequence<?> result = visitor.visitRootSlashOnlyPath(expr, nodeContext);
    assertEquals(ISequence.empty(), result);
  }

  @Test
  void testStep() {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    IExpression stepExpr = context.mock(IExpression.class);
    ISequence<?> stepResult = context.mock(ISequence.class, "stepResult");
    IAssemblyNodeItem item = context.mock(IAssemblyNodeItem.class);

    context.checking(new Expectations() {
      {
        allowing(stepExpr).getStaticResultType();
        will(returnValue(IAssemblyNodeItem.class));
        oneOf(stepExpr).accept(visitor, item);
        will(returnValue(stepResult));

        oneOf(stepResult).asStream();
        will(returnValue(Stream.of(item)));

        allowing(item).getContextNodeItem();
        will(returnValue(item));
      }
    });

    Step expr = new Step(stepExpr, List.of());

    ISequence<?> result = visitor.visitStep(expr, item);
    assertEquals(ISequence.of(item), result);
  }

  @Test
  void testFlagWithName() {
    MetaschemaPathEvaluationVisitor visitor = newMetaschemaPathEvaluationVisitor();

    IModelNodeItem nodeContext = context.mock(IModelNodeItem.class);
    IFlagInstance instance = context.mock(IFlagInstance.class);
    IFlagNodeItem flagNode = context.mock(IFlagNodeItem.class);

    String flagName = "test";

    context.checking(new Expectations() {
      {
        allowing(nodeContext).getContextNodeItem();
        will(returnValue(nodeContext));
        allowing(nodeContext).getNodeItemType();
        will(returnValue(NodeItemType.ASSEMBLY));
        allowing(nodeContext).getFlagByName(flagName);
        will(returnValue(flagNode));

        allowing(flagNode).getInstance();
        will(returnValue(instance));

        allowing(instance).getEffectiveName();
        will(returnValue(flagName));

      }
    });

    Flag expr = new Flag(new Name(flagName));

    ISequence<?> result = visitor.visitFlag(expr, nodeContext);
    assertEquals(ISequence.of(flagNode), result);
  }
}
