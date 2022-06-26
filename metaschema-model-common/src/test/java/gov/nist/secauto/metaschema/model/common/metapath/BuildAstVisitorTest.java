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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Lexer;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUuidItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.MockItemFactory;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URI;
import java.util.List;

class BuildAstVisitorTest {
  @RegisterExtension
  Mockery context = new JUnit5Mockery();

  @SuppressWarnings("null")
  @NotNull
  private IDocumentNodeItem newTestDocument() {
    MockItemFactory factory = new MockItemFactory(context);
    return factory.document(URI.create("http://example.com/content"), "root",
        List.of(
            factory.flag("uuid", IUuidItem.random())),
        List.of(
            factory.field("field1", IStringItem.valueOf("field1")),
            factory.field("field2", IStringItem.valueOf("field2"),
                List.of(factory.flag("flag", IStringItem.valueOf("field2-flag"))))));
  }

  @NotNull
  private static DynamicContext newDynamicContext() {
    return new StaticContext().newDynamicContext();
  }

  private static IExpression parseExpression(@NotNull String path) {

    metapath10Lexer lexer = new metapath10Lexer(CharStreams.fromString(path));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    metapath10Parser parser = new metapath10Parser(tokens);
    parser.addErrorListener(new FailingErrorListener());

    ParseTree tree = parser.expr();
//    CSTPrinter cstPrinter = new CSTPrinter(System.out);
//    cstPrinter.print(tree, Arrays.asList(parser.getRuleNames()));

    return new BuildAstVisitor().visit(tree);
  }

  @Test
  void testAbbreviatedParentAxis() {
    // compile expression
    String path = "../field2";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), field);
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field2")))));
  }

  @Test
  void testParentAxisMatch() {
    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);

    // compile expression
    String path = "parent::root";
    IExpression ast = parseExpression(path);

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), field);
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IRootAssemblyNodeItem.class),
            hasProperty("name", equalTo("root")))));
  }

  @Test
  void testParentAxisNonMatch() {
    // select starting node
    IDocumentNodeItem document = newTestDocument();
    IFieldNodeItem field = MetapathExpression.compile("/root/field1").evaluateAs(document, ResultType.NODE);

    // compile expression
    String path = "parent::other";
    IExpression ast = parseExpression(path);

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), field);
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
    ISequence<?> result = ast.accept(newDynamicContext(), document);
    assertTrue(result.isEmpty());
  }

  @Test
  void testAbbreviatedForwardAxisModelName() {
    String path = "./root";
    IExpression ast = parseExpression(path);

    // select starting node
    IDocumentNodeItem document = newTestDocument();

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), document);
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

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), field);
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

    // evaluate
    ISequence<?> result = ast.accept(newDynamicContext(), root);
    assertThat(result.asList(), contains(
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field1"))),
        allOf(
            instanceOf(IFieldNodeItem.class),
            hasProperty("name", equalTo("field2")))));
  }

}
