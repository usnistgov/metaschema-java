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
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class MetapathExpression {

  public enum ResultType {
    NUMBER,
    STRING,
    BOOLEAN,
    SEQUENCE,
    NODE;
  }

  private static final Logger LOGGER = LogManager.getLogger(MetapathExpression.class);

  @NonNull
  public static final MetapathExpression CONTEXT_NODE = new MetapathExpression(".", ContextItem.instance());

  private final String path;
  @NonNull
  private final IExpression node;

  /**
   * Compiles a Metapath expression string.
   *
   * @param path
   *          the metapath expression
   * @return the compiled expression object
   * @throws MetapathException
   *           if an error occurred while compiling the Metapath expression
   */
  @NonNull
  public static MetapathExpression compile(@NonNull String path) {
    @NonNull MetapathExpression retval;
    if (".".equals(path)) {
      retval = CONTEXT_NODE;
    } else {
      try {
        metapath10Lexer lexer = new metapath10Lexer(CharStreams.fromString(path));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        metapath10Parser parser = new metapath10Parser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new FailingErrorListener());

        ParseTree tree = ObjectUtils.notNull(parser.expr());

        if (LOGGER.isDebugEnabled()) {
          try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try (PrintStream ps = new PrintStream(os, true, StandardCharsets.UTF_8)) {
              CSTPrinter printer = new CSTPrinter(ps);
              printer.print(tree, Arrays.asList(metapath10Parser.ruleNames));
              ps.flush();
            }
            LOGGER.atDebug().log(String.format("Metapath CST:%n%s", os.toString(StandardCharsets.UTF_8)));
          } catch (IOException ex) {
            LOGGER.atError().withThrowable(ex).log("An unexpected error occured while closing the steam.");
          }
        }

        IExpression expr = new BuildAstVisitor().visit(tree);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.atDebug().log(String.format("Metapath AST:%n%s", ASTPrinter.instance().visit(expr)));
        }
        retval = new MetapathExpression(path, expr);
      } catch (MetapathException | ParseCancellationException ex) {
        String msg = String.format("Unable to compile Metapath '%s'", path);
        LOGGER.atError().withThrowable(ex).log(msg);
        throw new MetapathException(msg, ex);
      }
    }
    return retval;
  }

  /**
   * Construct a new Metapath expression.
   *
   * @param path
   *          the Metapath as a string
   * @param expr
   *          the Metapath as a compiled abstract syntax tree (AST)
   */
  protected MetapathExpression(@NonNull String path, @NonNull IExpression expr) {
    this.path = path;
    this.node = expr;
  }

  /**
   * Get the original Metapath expression as a string.
   *
   * @return the expression
   */
  public String getPath() {
    return path;
  }

  /**
   * Get the compiled abstract syntax tree (AST) representation of the Metapath.
   *
   * @return the Metapath AST
   */
  @NonNull
  protected IExpression getASTNode() {
    return node;
  }

  @Override
  public String toString() {
    return ASTPrinter.instance().visit(getASTNode());
  }

  /**
   * Evaluate this Metapath expression using the provided {@code nodeContext} as the initial
   * evaluation context. The specific result type will be determined by the {@code resultType}
   * argument.
   *
   * @param <T>
   *          the expected result type
   * @param nodeContext
   *          the initial evaluation context
   * @param resultType
   *          the type of result to produce
   * @return the converted result
   * @throws TypeMetapathException
   *           if the provided sequence is incompatible with the requested result type
   * @throws MetapathException
   *           if an error occurred during evaluation
   * @see #toResultType(ISequence, ResultType)
   */
  @Nullable
  public <T> T evaluateAs(@NonNull INodeContext nodeContext, @NonNull ResultType resultType) {
    ISequence<?> result = evaluate(nodeContext);
    return toResultType(result, resultType);
  }

  /**
   * Evaluate this Metapath expression using the provided {@code nodeContext} as the initial
   * evaluation context. The specific result type will be determined by the {@code resultType}
   * argument.
   * <p>
   * This variant allow for reuse of a provided {@code dynamicContext}.
   *
   * @param <T>
   *          the expected result type
   * @param nodeContext
   *          the initial evaluation context
   * @param resultType
   *          the type of result to produce
   * @param dynamicContext
   *          the dynamic context to use for evaluation
   * @return the converted result
   * @throws TypeMetapathException
   *           if the provided sequence is incompatible with the requested result type
   * @throws MetapathException
   *           if an error occurred during evaluation
   * @see #toResultType(ISequence, ResultType)
   */
  @Nullable
  public <T> T evaluateAs(@NonNull INodeContext nodeContext, @NonNull ResultType resultType,
      @NonNull DynamicContext dynamicContext) {
    ISequence<?> result = evaluate(nodeContext, dynamicContext);
    return toResultType(result, resultType);
  }

  /**
   * Converts the provided {@code sequence} to the requested {@code resultType}.
   * <p>
   * The {@code resultType} determines the returned result, which is derived from the evaluation
   * result sequence, as follows:
   * <ul>
   * <li>BOOLEAN - the effective boolean result is produced using
   * {@link FnBoolean#fnBoolean(ISequence)}.</li>
   * <li>NODE - the first result item in the sequence is returned.</li>
   * <li>NUMBER - the sequence is cast to a number using
   * {@link IDecimalItem#cast(IAnyAtomicItem)}.</li>
   * <li>SEQUENCE - the evaluation result sequence.</li>
   * <li>STRING - the string value of the first result item in the sequence.</li>
   * </ul>
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
  @SuppressWarnings("PMD.NullAssignment") // for readability
  @Nullable
  protected <T> T toResultType(@NonNull ISequence<?> sequence, @NonNull ResultType resultType) {
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
      throw new InvalidTypeMetapathException(null, String.format("unsupported result type '%s'", resultType.name()));
    }

    @SuppressWarnings("unchecked") T retval = (T) result;
    return retval;
  }

  /**
   * Evaluate this Metapath expression using the provided {@code nodeContext} as the initial
   * evaluation context.
   *
   * @param <T>
   *          the type of items contained in the resulting sequence
   * @param nodeContext
   *          the initial evaluation context
   * @return a sequence of Metapath items representing the result of the evaluation
   * @throws MetapathException
   *           if an error occurred during evaluation
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public <T extends IItem> ISequence<T> evaluate(@NonNull INodeContext nodeContext) {
    return (ISequence<T>) evaluate(nodeContext, new StaticContext().newDynamicContext());
  }

  /**
   * Evaluate this Metapath expression using the provided {@code nodeContext} as the initial
   * evaluation context.
   * <p>
   * This variant allow for reuse of a provided {@code dynamicContext}.
   *
   * @param <T>
   *          the type of items contained in the resulting sequence
   * @param nodeContext
   *          the initial evaluation context
   * @param dynamicContext
   *          the dynamic context to use for evaluation
   * @return a sequence of Metapath items representing the result of the evaluation
   * @throws MetapathException
   *           if an error occurred during evaluation
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public <T extends IItem> ISequence<T> evaluate(@NonNull INodeContext nodeContext,
      @NonNull DynamicContext dynamicContext) {
    try {
      return (ISequence<T>) getASTNode().accept(dynamicContext,
          nodeContext);
    } catch (MetapathException ex) { // NOPMD - intentional
      throw new MetapathException(
          String.format("An error occurred while evaluating the expression '%s'.", getPath()), ex);
    }
  }
}
