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
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public interface IFunction {

  /**
   * Retrieve the name of the function.
   * 
   * @return the function's name
   */
  String getName();

  /**
   * Retrieve the list of function arguments.
   * 
   * @return the function arguments or an empty list if there are none
   */
  List<IArgument> getArguments();

  /**
   * Determine the number of arguments the function has.
   * 
   * @return the number of function arguments
   */
  int arity();

  /**
   * Determines if the final argument can be repeated.
   * 
   * @return {@code true} if the final argument can be repeated or {@code false} otherwise
   */
  boolean isArityUnbounded();

  /**
   * Retrieve the function result sequence type.
   * 
   * @return the function result sequence type
   */
  ISequenceType getResult();

  /**
   * Determines by static analysis if the function supports the expression arguments provided.
   * 
   * @param arguments
   *          the expression arguments to evaluate
   * @return {@code true} if the arguments are supported or {@code false} otherwise
   */
  boolean isSupported(List<IExpression<?>> arguments);

  ISequence<?> execute(List<ISequence<?>> arguments);

  /**
   * Get the signature of the function as a string.
   * 
   * @return the signature
   */
  String toSignature();

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private List<IArgument> arguments = new LinkedList<>();
    private boolean allowUnboundedArity = false;
    private Class<? extends IItem> returnType = IItem.class;
    private Occurrence returnOccurrence = Occurrence.ONE;
    private IFunctionHandler functionHandler = null;

    public Builder() {
      this(null);
    }

    public Builder(String name) {
      this.name = name;
    }

    public Builder name(String name) {
      Objects.requireNonNull(name, "name");
      if (name.isBlank()) {
        throw new IllegalArgumentException("the name must be non-blank");
      }
      this.name = name.trim();
      return this;
    }

    public Builder returnType(Class<? extends IItem> type) {
      Objects.requireNonNull(type, "type");
      this.returnType = type;
      return this;
    }

    public Builder returnZeroOrOne() {
      return returnOccurrence(Occurrence.ZERO_OR_ONE);
    }

    public Builder returnOne() {
      return returnOccurrence(Occurrence.ONE);
    }

    public Builder returnZeroOrMore() {
      return returnOccurrence(Occurrence.ZERO_OR_MORE);
    }

    public Builder returnOneOrMore() {
      return returnOccurrence(Occurrence.ONE_OR_MORE);
    }

    public Builder returnOccurrence(Occurrence occurrence) {
      Objects.requireNonNull(occurrence, "occurrence");
      this.returnOccurrence = occurrence;
      return this;
    }

    public Builder argument(IArgument.Builder builder) {
      return argument(builder.build());
    }

    public Builder argument(IArgument argument) {
      Objects.requireNonNull(argument, "argument");
      this.arguments.add(argument);
      return this;
    }

    public Builder allowUnboundedArity(boolean allow) {
      this.allowUnboundedArity = allow;
      return this;
    }

    public Builder functionHandler(IFunctionHandler functionHandler) {
      Objects.requireNonNull(functionHandler, "functionHandler");
      this.functionHandler = functionHandler;
      return this;
    }

    protected void validate() throws IllegalStateException {
      if (name == null) {
        throw new IllegalStateException("the name must not be null");
      }
      //
      // if (returnType == null && returnOccurrence != null) {
      // throw new IllegalStateException("if the return type is null, the return occurrence must also be
      // null");
      // } else if (returnType != null && returnOccurrence == null) {
      // throw new IllegalStateException("if the return occurrence is null, the return type must also be
      // null");
      // }
      //

      if (returnType == null) {
        throw new IllegalStateException("the return type must not be null");
      }

      if (returnOccurrence == null) {
        throw new IllegalStateException("the return occurrence must not be null");
      }

      if (allowUnboundedArity && arguments.isEmpty()) {
        throw new IllegalStateException("to allow unbounded arity, at least one argument must be provided");
      }

      if (functionHandler == null) {
        throw new IllegalStateException("the function handler must not be null");
      }
    }

    public IFunction build() throws IllegalStateException {
      validate();
      ISequenceType sequenceType;
      if (returnType == null) {
        sequenceType = ISequenceType.EMPTY;
      } else {
        sequenceType = new SequenceTypeImpl(returnType, returnOccurrence);
      }
      return new StaticFunction(name, new ArrayList<>(arguments), allowUnboundedArity, sequenceType, functionHandler);
    }

  }
}
