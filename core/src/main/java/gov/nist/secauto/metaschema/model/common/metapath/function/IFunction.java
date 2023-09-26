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

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IFunction {
  enum FunctionProperty {
    /**
     * Indicates that the function will produce identical results for the same arguments (see XPath 3.1
     * <a href="https://www.w3.org/TR/xpath-functions-31/#dt-deterministic">deterministic</a>). If not
     * assigned to a function definition, a function call with the same arguments is not guaranteed to
     * produce the same results in the same order for subsequent calls within the same execution
     * context.
     */
    DETERMINISTIC,
    /**
     * Indicates that the result of the function depends on property values within the static or dynamic
     * context and the provided arguments (see XPath 3.1
     * <a href="https://www.w3.org/TR/xpath-functions-31/#dt-context-dependent">context-dependent</a>).
     * If not assigned to a function definition, a call will not be affected by the property values
     * within the static or dynamic context and will not have any arguments.
     */
    CONTEXT_DEPENDENT,
    /**
     * Indicates that the result of the function depends on the current focus (see XPath 3.1
     * <a href="https://www.w3.org/TR/xpath-functions-31/#dt-focus-independent">focus-dependent</a>). If
     * not assigned to a function definition, a call will not be affected by the current focus.
     */
    FOCUS_DEPENDENT,
    /**
     * The function allows the last argument to be repeated any number of times.
     */
    UNBOUNDED_ARITY;
  }

  /**
   * Retrieve the name of the function.
   *
   * @return the function's name
   */
  @NonNull
  String getName();

  /**
   * Retrieve the set of assigned function properties.
   *
   * @return the set of properties or an empty set
   */
  @NonNull
  Set<FunctionProperty> getProperties();

  /**
   * Retrieve the list of function arguments.
   *
   * @return the function arguments or an empty list if there are none
   */
  @NonNull
  List<IArgument> getArguments();

  /**
   * Determine the number of arguments the function has.
   *
   * @return the number of function arguments
   */
  int arity();

  /**
   * Determines if the result of the function call will produce identical results when provided the
   * same implicit or explicit arguments.
   *
   * @return {@code true} if function is deterministic or {@code false} otherwise
   * @see FunctionProperty#DETERMINISTIC
   */
  default boolean isDeterministic() {
    return getProperties().contains(FunctionProperty.DETERMINISTIC);
  }

  /**
   * Determines if the result of the function call depends on property values within the static or
   * dynamic context and the provided arguments.
   *
   * @return {@code true} if function is context dependent or {@code false} otherwise
   * @see FunctionProperty#CONTEXT_DEPENDENT
   */
  default boolean isContextDepenent() {
    return getProperties().contains(FunctionProperty.CONTEXT_DEPENDENT);
  }

  /**
   * Determines if the result of the function call depends on the current focus.
   *
   * @return {@code true} if function is focus dependent or {@code false} otherwise
   * @see FunctionProperty#FOCUS_DEPENDENT
   */
  default boolean isFocusDepenent() {
    return getProperties().contains(FunctionProperty.FOCUS_DEPENDENT);
  }

  /**
   * Determines if the final argument can be repeated.
   *
   * @return {@code true} if the final argument can be repeated or {@code false} otherwise
   * @see FunctionProperty#UNBOUNDED_ARITY
   */
  default boolean isArityUnbounded() {
    return getProperties().contains(FunctionProperty.UNBOUNDED_ARITY);
  }

  /**
   * Retrieve the function result sequence type.
   *
   * @return the function result sequence type
   */
  @NonNull
  ISequenceType getResult();

  // /**
  // * Determines by static analysis if the function supports the expression arguments provided.
  // *
  // * @param arguments
  // * the expression arguments to evaluate
  // * @return {@code true} if the arguments are supported or {@code false} otherwise
  // */
  // boolean isSupported(List<IExpression<?>> arguments);

  @NonNull
  ISequence<?> execute(
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      @NonNull ISequence<?> focus) throws MetapathException;

  /**
   * Get the signature of the function as a string.
   *
   * @return the signature
   */
  String toSignature();

  @NonNull
  static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("PMD.LooseCoupling")
  class Builder {
    private String name;
    @SuppressWarnings("null")
    @NonNull
    private final EnumSet<FunctionProperty> properties = EnumSet.noneOf(FunctionProperty.class);
    @NonNull
    private final List<IArgument> arguments = new LinkedList<>();
    private Class<? extends IItem> returnType = IItem.class;
    private Occurrence returnOccurrence = Occurrence.ONE;
    private IFunctionExecutor functionHandler;

    @NonNull
    public Builder name(@NonNull String name) {
      Objects.requireNonNull(name, "name");
      if (name.isBlank()) {
        throw new IllegalArgumentException("the name must be non-blank");
      }
      this.name = name.trim();
      return this;
    }

    @NonNull
    public Builder deterministic() {
      properties.add(FunctionProperty.DETERMINISTIC);
      return this;
    }

    @NonNull
    public Builder nonDeterministic() {
      properties.remove(FunctionProperty.DETERMINISTIC);
      return this;
    }

    @NonNull
    public Builder contextDependent() {
      properties.add(FunctionProperty.CONTEXT_DEPENDENT);
      return this;
    }

    @NonNull
    public Builder contextIndependent() {
      properties.remove(FunctionProperty.CONTEXT_DEPENDENT);
      return this;
    }

    @NonNull
    public Builder focusDependent() {
      properties.add(FunctionProperty.FOCUS_DEPENDENT);
      return this;
    }

    @NonNull
    public Builder focusIndependent() {
      properties.remove(FunctionProperty.FOCUS_DEPENDENT);
      return this;
    }

    @NonNull
    public Builder allowUnboundedArity(boolean allow) {
      if (allow) {
        properties.add(FunctionProperty.UNBOUNDED_ARITY);
      } else {
        properties.remove(FunctionProperty.UNBOUNDED_ARITY);
      }
      return this;
    }

    @NonNull
    public Builder returnType(@NonNull Class<? extends IItem> type) {
      Objects.requireNonNull(type, "type");
      this.returnType = type;
      return this;
    }

    @NonNull
    public Builder returnZeroOrOne() {
      return returnOccurrence(Occurrence.ZERO_OR_ONE);
    }

    @NonNull
    public Builder returnOne() {
      return returnOccurrence(Occurrence.ONE);
    }

    @NonNull
    public Builder returnZeroOrMore() {
      return returnOccurrence(Occurrence.ZERO_OR_MORE);
    }

    @NonNull
    public Builder returnOneOrMore() {
      return returnOccurrence(Occurrence.ONE_OR_MORE);
    }

    @NonNull
    public Builder returnOccurrence(@NonNull Occurrence occurrence) {
      Objects.requireNonNull(occurrence, "occurrence");
      this.returnOccurrence = occurrence;
      return this;
    }

    @NonNull
    public Builder argument(@NonNull IArgument.Builder builder) {
      return argument(builder.build());
    }

    @NonNull
    public Builder argument(@NonNull IArgument argument) {
      Objects.requireNonNull(argument, "argument");
      this.arguments.add(argument);
      return this;
    }

    @NonNull
    public Builder functionHandler(@NonNull IFunctionExecutor handler) {
      Objects.requireNonNull(handler, "handler");
      this.functionHandler = handler;
      return this;
    }

    @NonNull
    public IFunction build() {
      ISequenceType sequenceType;
      if (returnType == null) {
        sequenceType = ISequenceType.EMPTY;
      } else {
        sequenceType = new SequenceTypeImpl(
            returnType,
            ObjectUtils.requireNonNull(returnOccurrence, "the return occurrence must not be null"));
      }

      if (properties.contains(FunctionProperty.UNBOUNDED_ARITY) && arguments.isEmpty()) {
        throw new IllegalStateException("to allow unbounded arity, at least one argument must be provided");
      }

      return new DefaultFunction(
          ObjectUtils.requireNonNull(name, "the name must not be null"),
          properties,
          new ArrayList<>(arguments),
          sequenceType,
          ObjectUtils.requireNonNull(functionHandler, "the function handler must not be null"));
    }
  }
}
