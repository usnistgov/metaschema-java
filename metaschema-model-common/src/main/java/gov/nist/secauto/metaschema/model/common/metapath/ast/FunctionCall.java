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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IExpressionEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.ExpressionVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionService;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;

import java.util.List;
import java.util.Objects;

public class FunctionCall implements IExpression<IItem> {
  private final IFunction function;
  private final List<IExpression<?>> arguments;

  public FunctionCall(String name, List<IExpression<?>> arguments) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(arguments);
    this.function = FunctionService.getInstance().getFunction(name, arguments);
    if (this.function == null) {
      throw new IllegalArgumentException(
          String.format("unable to find function with name '%s' having arity '%d'", name, arguments.size()));
    }
    this.arguments = arguments;
  }

  public IFunction getFunction() {
    return function;
  }

  @Override
  public Class<? extends IItem> getBaseResultType() {
    return function.getResult().getType();
  }

  @Override
  public Class<? extends IItem> getStaticResultType() {
    return getBaseResultType();
  }

  @Override
  public String toASTString() {
    return String.format("%s[name=%s]", getClass().getName(), getFunction().getName());
  }

  @Override
  public List<? extends IExpression<?>> getChildren() {
    return arguments;
  }

  @Override
  public ISequence<? extends IItem> accept(IExpressionEvaluationVisitor visitor, INodeContext context) {
    return visitor.visitFunctionCall(this, context);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(ExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFunctionCall(this, context);
  }

  @Override
  public String toString() {
    return new ASTPrinter().visit(this);
  }
}
