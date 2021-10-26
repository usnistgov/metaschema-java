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

import gov.nist.secauto.metaschema.model.common.datatype.adapter.IAnyUriItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnDocFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.type.TypeMetapathException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractFunction implements IFunction {
  private static final Logger logger = LogManager.getLogger(AbstractFunction.class);
  private final String name;
  private final List<IArgument> arguments;
  private final boolean unboundedArity;
  private final ISequenceType result;
  private final FunctionExecutor handler;

  protected AbstractFunction(
      String name,
      List<IArgument> arguments,
      boolean unboundedArity,
      ISequenceType result,
      FunctionExecutor handler) {
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
    } else if ((expressionArguments.size() == getArguments().size())
        || (isArityUnbounded() && expressionArguments.size() > getArguments().size())) {
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
            @SuppressWarnings("null") boolean result = argument.isSupported(expression);
            if (!result) {
              retval = result;
              break;
            }
          }
        } else {
          // there are extra expressions, which do not match the arguments
          retval = false;
        }
      }
    } else {
      retval = false;
    }
    return retval;
  }

  @NotNull
  public static List<@NotNull ISequence<?>> convertArguments(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> parameters) {
    @NotNull List<@NotNull ISequence<?>> retval = new ArrayList<>(parameters.size());

    Iterator<IArgument> argumentIterator = function.getArguments().iterator();
    Iterator<@NotNull ISequence<?>> parametersIterator = parameters.iterator();

    IArgument argument = null;
    while (parametersIterator.hasNext()) {
      argument = argumentIterator.hasNext() ? argumentIterator.next() : function.isArityUnbounded() ? argument : null;

      if (argument == null) {
        throw new TypeMetapathException(TypeMetapathException.INVALID_TYPE_ERROR,
            String.format("argument signature doesn't match '%d'", function.toSignature()));
      }

      @SuppressWarnings("null") ISequence<?> parameter = parametersIterator.next();

      int size = parameter.size();
      Occurrence occurrence = argument.getSequenceType().getOccurrence();
      switch (occurrence) {
      case ONE: {
        if (size != 1) {
          throw new TypeMetapathException(TypeMetapathException.INVALID_TYPE_ERROR,
              String.format("a sequence of one expected, but found '%d'", size));
        }

        IItem item = FunctionUtils.getFirstItem(parameter, true);
        parameter = item == null ? ISequence.empty() : ISequence.of(item);
        break;
      }
      case ZERO_OR_ONE: {
        if (size > 1) {
          throw new TypeMetapathException(TypeMetapathException.INVALID_TYPE_ERROR,
              String.format("a sequence of zero or one expected, but found '%d'", size));
        }

        IItem item = FunctionUtils.getFirstItem(parameter, false);
        parameter = item == null ? ISequence.empty() : ISequence.of(item);
        break;
      }
      case ONE_OR_MORE:
        if (size < 1) {
          throw new TypeMetapathException(TypeMetapathException.INVALID_TYPE_ERROR,
              String.format("a sequence of zero or more expected, but found '%d'", size));
        }
        break;
      case ZERO:
        if (size != 0) {
          throw new TypeMetapathException(TypeMetapathException.INVALID_TYPE_ERROR,
              String.format("an empty sequence expected, but found '%d'", size));
        }
        break;
      case ZERO_OR_MORE:
      default:
        // do nothing
      }

      Class<? extends IItem> argumentClass = argument.getSequenceType().getType();

      // apply function conversion and type promotion to the parameter
      parameter = convertSequence(argument, parameter);

      // check resulting values
      for (IItem item : parameter.asList()) {
        Class<? extends IItem> itemClass = item.getClass();
        if (!argumentClass.isAssignableFrom(itemClass)) {
          throw new InvalidTypeMetapathException(
              String.format("The type '%s' is not a subtype of '%s'", itemClass.getName(), argumentClass.getName()));
        }
      }

      retval.add(parameter);
    }
    return retval;
  }

  /**
   * Based on XPath 3.1 <a href="https://www.w3.org/TR/xpath-31/#dt-function-conversion">function
   * conversion</a> rules.
   * 
   * @param argument
   *          the function argument signature details
   * @param sequence
   *          the sequence to convert
   * @return the converted sequence
   */
  @NotNull
  protected static ISequence<?> convertSequence(@NotNull IArgument argument, @NotNull ISequence<?> sequence) {
    @NotNull ISequence<?> retval;
    if (sequence.isEmpty()) {
      retval = ISequence.empty();
    } else {
      Class<? extends IItem> sequenceType = argument.getSequenceType().getType();

      List<@NotNull IItem> result = new ArrayList<>(sequence.size());

      boolean atomize = IAnyAtomicItem.class.isAssignableFrom(sequenceType);

      for (IItem item : sequence.asList()) {
        if (atomize) {
          item = XPathFunctions.fnDataItem(item);
        }

        if (IUntypedAtomicItem.class.isInstance(item)) {
          // TODO: apply cast to atomic type
        }

        // promote URIs to strings if a string is required
        if (IStringItem.class.equals(sequenceType) && IAnyUriItem.class.isInstance(item)) {
          item = IStringItem.cast((IAnyUriItem) item);
        }

        if (!sequenceType.isInstance(item)) {
          throw new InvalidTypeMetapathException(
              String.format("The type '%s' is not a subtype of '%s'", item.getClass().getName(),
                  sequenceType.getName()));
        }
        result.add(item);
      }
      retval = ISequence.of(result);
    }
    return retval;
  }

  @Override
  public ISequence<?> execute(List<@NotNull ISequence<?>> arguments, DynamicContext dynamicContext,
      INodeContext focus) {
    try {
      List<@NotNull ISequence<?>> convertedArguments = convertArguments(this, arguments);

      // logger.info(String.format("Executing function '%s' with arguments '%s'.", toSignature(),
      // convertedArguments.toString()));
      ISequence<?> result = handler.execute(this, convertedArguments, dynamicContext, focus.getContextNodeItem());

      // logger.info(String.format("Executed function '%s' with arguments '%s' producing result '%s'",
      // toSignature(), convertedArguments.toString(), result.asList().toString()));
      return result;
    } catch (MetapathException ex) {
      throw new MetapathException(String.format("Unable to execute function '%s'", toSignature()), ex);
    }
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
