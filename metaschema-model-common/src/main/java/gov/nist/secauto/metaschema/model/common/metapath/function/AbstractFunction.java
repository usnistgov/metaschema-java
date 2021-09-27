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

package gov.nist.secauto.metaschema.model.common.metapath.function;

import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.function.impl.Functions;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.MetapathDynamicException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractFunction implements IFunction {
  private final String name;
  private final List<IArgument> arguments;
  private final boolean unboundedArity;
  private final ISequenceType result;
  private final IFunctionHandler handler;

  protected AbstractFunction(String name, List<IArgument> arguments, boolean unboundedArity, ISequenceType result,
      IFunctionHandler handler) {
    this.name = name;
    this.arguments = arguments;
    this.unboundedArity = unboundedArity;
    this.result = result;
    this.handler = handler;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isArityUnbounded() {
    return unboundedArity;
  }

  @Override
  public int arity() {
    return arguments.size();
  }

  @Override
  public List<IArgument> getArguments() {
    return arguments;
  }

  @Override
  public ISequenceType getResult() {
    return result;
  }

  @Override
  public boolean isSupported(List<IExpression<?>> expressionArguments) {
    boolean retval;
    if (expressionArguments.isEmpty() && getArguments().isEmpty()) {
      // no arguments
      retval = true;
      // } else if (arity() == 1 && expressionArguments.isEmpty()) {
      // // the context item will be the argument
      // // TODO: check the context item for type compatibility
      // retval = true;
    } else {
      retval = true;
      // check that argument requirements are satisfied
      Iterator<IArgument> argumentIterator = getArguments().iterator();
      Iterator<IExpression<?>> expressionIterator = expressionArguments.iterator();

      IArgument argument = null;
      while (argumentIterator.hasNext()) {
        argument = argumentIterator.next();
        IExpression<?> expression = expressionIterator.hasNext() ? expressionIterator.next() : null;

        if (expression != null) {
          // is the expression supported by the argument?
          retval = argument.isSupported(expression);
          if (!retval) {
            break;
          }
        } else {
          // there are no more expression arguments. Make sure that the remaining arguments are optional
          if (!argument.getSequenceType().getOccurrence().isOptional()) {
            retval = false;
            break;
          }
        }
      }

      if (retval && expressionIterator.hasNext()) {
        if (isArityUnbounded()) {
          // check remaining expressions against the last argument
          while (expressionIterator.hasNext()) {
            IExpression<?> expression = expressionIterator.next();
            retval = argument.isSupported(expression);
            if (!retval) {
              break;
            }
          }
        } else {
          // there are extra expressions, which do not match the arguments
          retval = false;
        }
      }
    }
    return retval;
  }

  @Override
  public List<ISequence<?>> convertArguments(IFunction function, List<ISequence<?>> parameters) {
    List<ISequence<?>> retval = new ArrayList<>(parameters.size());

    Iterator<IArgument> argumentIterator = getArguments().iterator();
    Iterator<ISequence<?>> parametersIterator = parameters.iterator();

    IArgument argument = null;
    while (parametersIterator.hasNext()) {
      argument = argumentIterator.hasNext() ? argumentIterator.next() : function.isArityUnbounded() ? argument : null;
      ISequence<?> parameter = parametersIterator.next();

      // Occurrence occurrence = argument.getSequenceType().getOccurrence();
      // switch (occurrence) {
      // case ONE: {
      // IItem item = Functions.getFirstItem(parameter, true);
      //
      // parameter = item == null ? ISequence.empty() : ISequence.of(item);
      // break;
      // }
      // case ZERO_OR_ONE: {
      // IItem item = Functions.getFirstItem(parameter, false);
      //
      // parameter = item == null ? ISequence.empty() : ISequence.of(item);
      // break;
      // }
      // default:
      // // do nothing
      // }

      Class<? extends IItem> argumentClass = argument.getSequenceType().getType();
      if (argumentClass.isInstance(IAnyAtomicItem.class)) {
        // atomize
        parameter = Functions.fnData(parameter);
      }

      // TODO: https://www.w3.org/TR/xpath-31/#dt-function-conversion

      // check resulting values
      for (IItem item : parameter.asList()) {
        Class<? extends IItem> itemClass = item.getClass();
        if (!argumentClass.isAssignableFrom(itemClass)) {
          throw new MetapathDynamicException("err:XPTY0004",
              String.format("The type '%s' is not a subtype of '%s'", itemClass.getName(), argumentClass.getName()));
        }
      }

      retval.add(parameter);
    }
    return retval;
  }

  @Override
  public ISequence<?> execute(List<ISequence<?>> arguments) {
    return handler.execute(arguments);
  }

  @Override
  public String toString() {
    return toSignature();
  }

  @Override
  public String toSignature() {
    StringBuilder builder = new StringBuilder();

    // name
    builder.append(getName());

    // arguments
    builder.append("(");

    List<IArgument> arguments = getArguments();
    if (arguments.isEmpty()) {
      builder.append("()");
    } else {
      builder.append(arguments.stream().map(argument -> argument.toSignature()).collect(Collectors.joining(",")));

      if (isArityUnbounded()) {
        builder.append(", ...");
      }
    }
    builder.append(") as ");

    // return type
    builder.append(getResult().toSignature());

    return builder.toString();
  }
}
