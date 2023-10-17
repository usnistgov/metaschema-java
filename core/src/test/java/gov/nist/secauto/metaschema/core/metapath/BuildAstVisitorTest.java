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

package gov.nist.secauto.metaschema.core.metapath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.core.metapath.antlr.BuildCSTVisitor;
import gov.nist.secauto.metaschema.core.metapath.antlr.FailingErrorListener;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10;
import gov.nist.secauto.metaschema.core.metapath.antlr.Metapath10Lexer;
import gov.nist.secauto.metaschema.core.metapath.cst.AbstractComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.And;
import gov.nist.secauto.metaschema.core.metapath.cst.GeneralComparison;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.ValueComparison;
import gov.nist.secauto.metaschema.core.metapath.function.ComparisonFunctions;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUuidItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.MockNodeItemFactory;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.TooManyStaticImports")
class BuildAstVisitorTest {
  @RegisterExtension
  Mockery context = new JUnit5Mockery();

  @SuppressWarnings("null")
  @NonNull
  private IDocumentNodeItem newTestDocument() {
    MockNodeItemFactory factory = new MockNodeItemFactory(context);
    return factory.document(URI.create("http://example.com/content"), "root",
        List.of(
            factory.flag("uuid", IUuidItem.random())),
        List.of(
            factory.field("field1", IStringItem.valueOf("field1")),
            factory.field("field2", IStringItem.valueOf("field2"), // NOPMD
                List.of(factory.flag("flag", IStringItem.valueOf("field2-flag"))))));
  }

  @NonNull
  private static DynamicContext newDynamicContext() {
    return StaticContext.builder()
        .build().newDynamicContext();
  }

  private static IExpression parseExpression(@NonNull String path) {

    Metapath10Lexer lexer = new Metapath10Lexer(CharStreams.fromString(path));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Metapath10 parser = new Metapath10(tokens);
    parser.addErrorListener(new FailingErrorListener());

    ParseTree tree = parser.expr();
    // ParseTreePrinter cstPrinter = new ParseTreePrinter(System.out);
    // cstPrinter.print(tree, Arrays.asList(parser.getRuleNames()));

    return new BuildCSTVisitor().visit(tree);
  }

  @Test
  void testAbbreviatedParentAxis() {
    // compile expression
    String path = "../field2";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);
    assert field != null;

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(field));
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field2"))))); // NOPMD

  }

  @Test
  void testParentAxisMatch() {
    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);
    assert field != null;

    // compile expression
    IItem result = MetapathExpression.compile("parent::root").evaluateAs(field, ResultType.NODE);
    assert result != null;

    assertAll(
        () -> assertInstanceOf(IRootAssemblyNodeItem.class, result),
        () -> assertEquals("root", ((IRootAssemblyNodeItem) result).getName()));
  }

  @Test
  void testParentAxisNonMatch() {
    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);
    assert field != null;

    // compile expression
    String path = "parent::other";
    IExpression ast = parseExpression(path);

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(field));
    assertTrue(result.isEmpty());
  }

  @Test
  void testParentAxisDocument() {
    // compile expression
    String path = "parent::other";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(document));
    assertTrue(result.isEmpty());
  }

  @Test
  void testAbbreviatedForwardAxisModelName() {
    String path = "./root";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(document));
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IRootAssemblyNodeItem.class),
            hasProperty("name", equalTo("root")))));
  }

  @Test
  void testAbbreviatedForwardAxisFlagName() {
    String path = "./@flag";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field2").evaluateAs(document, ResultType.NODE);
    assert field != null;

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(field));
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IFlagNodeItem.class),
            hasProperty("name", equalTo("flag")))));
  }

  @Test
  void testForwardstepChild() {
    String path = "child::*";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IRootAssemblyNodeItem root = MetapathExpression.compile("/root").evaluateAs(document, ResultType.NODE);
    assert root != null;

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(root));
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field1"))), // NOPMD
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field2"))))); // NOPMD
  }

  static Stream<Arguments> testComparison() {
    return Stream.of(
        Arguments.of("A = B", GeneralComparison.class, ComparisonFunctions.Operator.EQ),
        Arguments.of("A != B", GeneralComparison.class, ComparisonFunctions.Operator.NE),
        Arguments.of("A < B", GeneralComparison.class, ComparisonFunctions.Operator.LT),
        Arguments.of("A <= B", GeneralComparison.class, ComparisonFunctions.Operator.LE),
        Arguments.of("A > B", GeneralComparison.class, ComparisonFunctions.Operator.GT),
        Arguments.of("A >= B", GeneralComparison.class, ComparisonFunctions.Operator.GE),
        Arguments.of("A eq B", ValueComparison.class, ComparisonFunctions.Operator.EQ),
        Arguments.of("A ne B", ValueComparison.class, ComparisonFunctions.Operator.NE),
        Arguments.of("A lt B", ValueComparison.class, ComparisonFunctions.Operator.LT),
        Arguments.of("A le B", ValueComparison.class, ComparisonFunctions.Operator.LE),
        Arguments.of("A gt B", ValueComparison.class, ComparisonFunctions.Operator.GT),
        Arguments.of("A ge B", ValueComparison.class, ComparisonFunctions.Operator.GE));
  }

  @ParameterizedTest
  @MethodSource
  void testComparison(
      @NonNull String metapath,
      @NonNull Class<?> expectedClass,
      @NonNull ComparisonFunctions.Operator operator) {
    IExpression ast = parseExpression(metapath);

    assertAll(
        () -> assertEquals(expectedClass, ast.getClass()),
        () -> assertEquals(operator, ((AbstractComparison) ast).getOperator()));
  }

  static Stream<Arguments> testAnd() {
    return Stream.of(
        Arguments.of("true() and false()", IBooleanItem.FALSE),
        Arguments.of("false() and false()", IBooleanItem.FALSE),
        Arguments.of("false() and true()", IBooleanItem.FALSE),
        Arguments.of("true() and true()", IBooleanItem.TRUE));
  }

  @ParameterizedTest
  @MethodSource
  void testAnd(@NonNull String metapath, @NonNull IBooleanItem expectedResult) {
    IExpression ast = parseExpression(metapath);

    IDocumentNodeItem document = newTestDocument();
    ISequence<?> result = ast.accept(newDynamicContext(), ISequence.of(document));
    IItem resultItem = FunctionUtils.getFirstItem(result, false);
    assertAll(
        () -> assertEquals(And.class, ast.getClass()),
        () -> assertNotNull(resultItem),
        () -> assertThat(resultItem, instanceOf(IBooleanItem.class)),
        () -> assertEquals(expectedResult, resultItem));
  }

}
