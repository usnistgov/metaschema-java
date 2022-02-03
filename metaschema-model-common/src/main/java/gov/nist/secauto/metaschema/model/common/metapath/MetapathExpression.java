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

import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Lexer;
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Parser;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ASTPrinter;
import gov.nist.secauto.metaschema.model.common.metapath.ast.BuildAstVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IMetaschemaContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.MetaschemaInstanceEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.XPathFunctions;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

public class MetapathExpression {
  @NotNull
  public static final MetapathExpression CONTEXT_NODE = new MetapathExpression(".", new ContextItem());

  @NotNull
  public static MetapathExpression compile(@NotNull String path) throws MetapathException {
    @NotNull
    MetapathExpression retval;
    if (".".equals(path)) {
      retval = MetapathExpression.CONTEXT_NODE;
    } else {
      try {
        metapath10Lexer lexer = new metapath10Lexer(CharStreams.fromString(path));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        metapath10Parser parser = new metapath10Parser(tokens);
        parser.addErrorListener(new FailingErrorListener());

        @SuppressWarnings("null")
        @NotNull
        ParseTree tree = parser.expr();
        // CSTPrinter printer = new CSTPrinter();
        // printer.print(tree, Arrays.asList(parser.getRuleNames()));
        retval = new MetapathExpression(path, tree);
      } catch (Exception ex) {
        throw new MetapathException(String.format("unable to compile path '%s'", path), ex);
      }
    }
    return retval;
  }

  public enum ResultType {
    NUMBER,
    STRING,
    BOOLEAN,
    SEQUENCE,
    NODE;
  }

  private final String path;
  @NotNull
  private final IExpression node;

  @SuppressWarnings("null")
  public MetapathExpression(@NotNull String path, @NotNull ParseTree tree) {
    this(path, new BuildAstVisitor().visit(tree));
  }

  public MetapathExpression(@NotNull String path, @NotNull IExpression expr) {
    this.path = path;
    this.node = expr;
  }

  public String getPath() {
    return path;
  }

  @NotNull
  public IExpression getASTNode() {
    return node;
  }

  @Override
  public String toString() {
    return new ASTPrinter().visit(getASTNode());
  }

  @SuppressWarnings("unchecked")
  public <T> T evaluateAs(@NotNull INodeItem item, @NotNull ResultType resultType) {
    ISequence<?> result = item.evaluateMetapath(this);
    return (T) toResultType(result, resultType);
  }

  @SuppressWarnings("unchecked")
  public <T> T evaluateAs(@NotNull INodeItem item, @NotNull DynamicContext context, @NotNull ResultType resultType) {
    ISequence<?> result = item.evaluateMetapath(this, context);
    return (T) toResultType(result, resultType);
  }

  protected Object toResultType(@NotNull ISequence<?> result, @NotNull ResultType resultType) {
    Object retval;
    switch (resultType) {
    case BOOLEAN:
      retval = XPathFunctions.fnBoolean(result).toBoolean();
      break;
    case NODE:
      retval = FunctionUtils.getFirstItem(result, false);
      break;
    case NUMBER:
      INumericItem numeric = FunctionUtils.toNumeric(result, false);
      retval = numeric == null ? null : numeric.asDecimal();
      break;
    case SEQUENCE:
      retval = result;
      break;
    case STRING:
      IItem item = FunctionUtils.getFirstItem(result, false);
      retval = item == null ? "" : XPathFunctions.fnDataItem(item).asString();
      break;
    default:
      throw new UnsupportedOperationException(String.format("unsupported result type '%s'", resultType.name()));
    }
    return retval;
  }

  @NotNull
  public ISequence<?> evaluate(@NotNull INodeItem item) {
    return item.evaluateMetapath(this);
  }

  @NotNull
  public ISequence<?> evaluate(@NotNull INodeItem item, @NotNull DynamicContext context) {
    return item.evaluateMetapath(this, context);
  }

  @NotNull
  public IInstanceSet evaluateMetaschemaInstance(IMetaschemaContext context) {
    IExpression node = getASTNode();
    Class<? extends IItem> type = node.getStaticResultType();
    if (!INodeItem.class.isAssignableFrom(type)) {
      throw new UnsupportedOperationException(String
          .format("The expression '%s' with static type '%s' is not a node expression", getPath(), type.getName()));
    }
    return new MetaschemaInstanceEvaluationVisitor().visit(node, context);
  }
}
