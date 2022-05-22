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

package gov.nist.secauto.metaschema.model.common.metapath.function.library;

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidArgumentFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyUriItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FnBoolean {
  @NotNull
  static final IFunction SIGNATURE = IFunction.builder()
      .name("boolean")
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("arg")
          .type(IItem.class)
          .zeroOrMore()
          .build())
      .returnType(IBooleanItem.class)
      .returnOne()
      .functionHandler(FnBoolean::execute)
      .build();

  private FnBoolean() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NotNull
  private static ISequence<IBooleanItem> execute(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments,
      @NotNull DynamicContext dynamicContext,
      INodeItem focus) {

    @SuppressWarnings("null")
    ISequence<?> items = arguments.iterator().next();

    IBooleanItem result = fnBoolean(items);
    return ISequence.of(result);
  }

  /**
   * Get the effective boolean value of the provided sequence.
   * <p>
   * Based on the XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-boolean">fn:boolean</a> function.
   * 
   * @param sequence
   *          the sequence to evaluate
   * @return the effective boolean value of the sequence
   */
  @NotNull
  public static IBooleanItem fnBoolean(@Nullable ISequence<?> sequence) {
    IBooleanItem retval;
    if (sequence == null) {
      retval = IBooleanItem.FALSE;
    } else {
      retval = IBooleanItem.valueOf(fnBooleanAsPrimitive(sequence));
    }
    return retval;
  }

  /**
   * A helper method that gets the effective boolean value of the provided sequence based on
   * <a href="https://www.w3.org/TR/xpath-31/#id-ebv">XPath 3.1</a>.
   * 
   * @param sequence
   *          the sequence to evaluate
   * @return the effective boolean value
   */
  // TODO: fix misspelled Primative
  public static boolean fnBooleanAsPrimitive(@NotNull ISequence<?> sequence) {
    boolean retval = false;
    if (!sequence.isEmpty()) {
      List<? extends IItem> items = sequence.asList();
      IItem first = ObjectUtils.notNull(items.iterator().next());
      if (first instanceof INodeItem) {
        retval = true;
      } else if (items.size() == 1) {
        retval = fnBooleanAsPrimitive(first);
      }
    }
    return retval;
  }

  /**
   * A helper method that gets the effective boolean value of the provided item based on
   * <a href="https://www.w3.org/TR/xpath-31/#id-ebv">XPath 3.1</a>.
   * 
   * @param item
   *          the item to evaluate
   * @return the effective boolean value
   */
  public static boolean fnBooleanAsPrimitive(@NotNull IItem item) {
    boolean retval;
    if (item instanceof IBooleanItem) {
      retval = ((IBooleanItem) item).toBoolean();
    } else if (item instanceof IStringItem) {
      String string = ((IStringItem) item).asString();
      retval = !string.isBlank();
    } else if (item instanceof INumericItem) {
      retval = ((INumericItem) item).toEffectiveBoolean();
    } else if (item instanceof IUntypedAtomicItem) {
      String string = ((IUntypedAtomicItem) item).asString();
      retval = !string.isBlank();
    } else if (item instanceof IAnyUriItem) {
      String string = ((IAnyUriItem) item).asString();
      retval = !string.isBlank();
    } else {
      throw new InvalidArgumentFunctionMetapathException(InvalidArgumentFunctionMetapathException.INVALID_ARGUMENT_TYPE,
          String.format("Invalid argument type '%s'", item.getClass().getName()));
    }
    return retval;
  }
}
