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
import gov.nist.secauto.metaschema.core.metapath.StaticMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionService;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class StaticFunctionCall implements IExpression {
  @NonNull
  private final QName name;
  @NonNull
  private final List<IExpression> arguments;
  private IFunction function;

  /**
   * Construct a new function call expression.
   *
   * @param name
   *          the function name
   * @param arguments
   *          the expressions used to provide arguments to the function call
   */
  public StaticFunctionCall(@NonNull QName name, @NonNull List<IExpression> arguments) {
    this.name = Objects.requireNonNull(name, "name");
    this.arguments = Objects.requireNonNull(arguments, "arguments");
  }

  /**
   * Retrieve the associated function.
   *
   * @return the function or {@code null} if no function matched the defined name
   *         and arguments
   * @throws StaticMetapathException
   *           if the function was not found
   */
  public IFunction getFunction() {
    synchronized (this) {
      if (function == null) {
        function = FunctionService.getInstance().getFunction(name, arguments.size());
      }
      return function;
    }
  }

  @Override
  public List<IExpression> getChildren() {
    return arguments;
  }

  @Override
  public Class<? extends IItem> getBaseResultType() {
    Class<? extends IItem> retval = getFunction().getResult().getType();
    if (retval == null) {
      retval = IItem.class;
    }
    return retval;
  }

  @SuppressWarnings("null")
  @Override
  public String toASTString() {
    return String.format("%s[name=%s]", getClass().getName(), getFunction().getName());
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFunctionCall(this, context);
  }

  @Override
  public ISequence<?> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    List<ISequence<?>> arguments = ObjectUtils.notNull(getChildren().stream().map(expression -> {
      @NonNull ISequence<?> result = expression.accept(dynamicContext, focus);
      return result;
    }).collect(Collectors.toList()));

    IFunction function = getFunction();
    return function.execute(arguments, dynamicContext, focus);
  }
}
