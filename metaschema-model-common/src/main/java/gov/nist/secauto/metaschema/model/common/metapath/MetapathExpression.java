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
import gov.nist.secauto.metaschema.model.common.metapath.ast.CSTPrinter;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.TypeMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class MetapathExpression {
  private static final Logger LOGGER = LogManager.getLogger(MetapathExpression.class);

  @NotNull
  public static final MetapathExpression CONTEXT_NODE = new MetapathExpression(".", ContextItem.instance());

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

        ParseTree tree = ObjectUtils.notNull(parser.expr());

        if (LOGGER.isDebugEnabled()) {
          try (OutputStream os = new ByteArrayOutputStream()) {
            PrintStream ps = new PrintStream(os, true);
            CSTPrinter printer = new CSTPrinter(ps);
            printer.print(tree, Arrays.asList(metapath10Parser.ruleNames));
            ps.flush();
            LOGGER.atDebug().log(String.format("Metapath CST:%n%s", os.toString()));
          }
        }

        IExpression expr = new BuildAstVisitor().visit(tree);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.atDebug().log(String.format("Metapath AST:%n%s", ASTPrinter.instance().visit(expr)));
        }
        retval = new MetapathExpression(path, expr);
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

  protected MetapathExpression(@NotNull String path, @NotNull IExpression expr) {
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
    return ASTPrinter.instance().visit(getASTNode());
  }

  public <T> T evaluateAs(@NotNull INodeContext nodeContext, @NotNull ResultType resultType) {
    ISequence<?> result = evaluate(nodeContext);
    return toResultType(result, resultType);
  }

  public <T> T evaluateAs(@NotNull INodeContext nodeContext, @NotNull ResultType resultType,
      @NotNull DynamicContext dynamicContext) {
    ISequence<?> result = evaluate(nodeContext, dynamicContext);
    return toResultType(result, resultType);
  }

  /**
   * Converts the provided {@code sequence} to the requested {@code resultType}.
   * 
   * @param <T>
   *          the requested return value
   * @param sequence
   *          the sequence to convert
   * @param resultType
   *          the type of result to produce
   * @return the converted result
   * @throws TypeMetapathException
   *           if the provided sequence is incompatible with the requested result type
   */
  protected <T> T toResultType(@NotNull ISequence<?> sequence, @NotNull ResultType resultType) {
    Object result;
    switch (resultType) {
    case BOOLEAN:
      result = FnBoolean.fnBoolean(sequence).toBoolean();
      break;
    case NODE:
      result = FunctionUtils.getFirstItem(sequence, true);
      break;
    case NUMBER:
      INumericItem numeric = FunctionUtils.toNumeric(sequence, true);
      result = numeric == null ? null : numeric.asDecimal();
      break;
    case SEQUENCE:
      result = sequence;
      break;
    case STRING:
      IItem item = FunctionUtils.getFirstItem(sequence, true);
      result = item == null ? "" : FnData.fnDataItem(item).asString();
      break;
    default:
      throw new UnsupportedOperationException(String.format("unsupported result type '%s'", resultType.name()));
    }

    @SuppressWarnings("unchecked")
    T retval = (T) result;
    return retval;
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public <T extends IItem> ISequence<? extends T> evaluate(@NotNull INodeContext nodeContext) {
    return (ISequence<? extends T>)evaluate(nodeContext, new StaticContext().newDynamicContext());
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public <T extends IItem> ISequence<? extends T> evaluate(@NotNull INodeContext nodeContext, @NotNull DynamicContext context) {
    return (@NotNull ISequence<T>) getASTNode().accept(context, nodeContext);
  }
}
