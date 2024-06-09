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

package gov.nist.secauto.metaschema.core.metapath.function.library;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidArgumentFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyUriItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class FnBoolean {
  @NonNull
  static final IFunction SIGNATURE = IFunction.builder()
      .name("boolean")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
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
  @NonNull
  private static ISequence<IBooleanItem> execute(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<?> items = ObjectUtils.requireNonNull(arguments.get(0));

    IBooleanItem result = fnBoolean(items);
    return ISequence.of(result);
  }

  /**
   * Get the effective boolean value of the provided sequence.
   * <p>
   * Based on the XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-boolean">fn:boolean</a>
   * function.
   *
   * @param sequence
   *          the sequence to evaluate
   * @return the effective boolean value of the sequence
   */
  @NonNull
  public static IBooleanItem fnBoolean(@NonNull ISequence<?> sequence) {
    return IBooleanItem.valueOf(fnBooleanAsPrimitive(sequence));
  }

  /**
   * A helper method that gets the effective boolean value of the provided
   * sequence based on <a href="https://www.w3.org/TR/xpath-31/#id-ebv">XPath
   * 3.1</a>.
   *
   * @param sequence
   *          the sequence to evaluate
   * @return the effective boolean value
   */
  public static boolean fnBooleanAsPrimitive(@NonNull ISequence<?> sequence) {
    boolean retval = false;
    IItem first = sequence.getFirstItem(false);
    if (first != null) {
      if (first instanceof INodeItem) {
        retval = true;
      } else if (sequence.size() == 1) {
        retval = fnBooleanAsPrimitive(first);
      }
    }
    return retval;
  }

  /**
   * A helper method that gets the effective boolean value of the provided item
   * based on <a href="https://www.w3.org/TR/xpath-31/#id-ebv">XPath 3.1</a>.
   *
   * @param item
   *          the item to evaluate
   * @return the effective boolean value
   */
  public static boolean fnBooleanAsPrimitive(@NonNull IItem item) {
    boolean retval;
    if (item instanceof IBooleanItem) {
      retval = ((IBooleanItem) item).toBoolean();
    } else if (item instanceof INumericItem) {
      retval = ((INumericItem) item).toEffectiveBoolean();
    } else if (item instanceof IStringItem
        || item instanceof IAnyUriItem
        || item instanceof IUntypedAtomicItem) {
      String string = ((IAnyAtomicItem) item).asString();
      retval = !string.isBlank();
    } else {
      throw new InvalidArgumentFunctionException(
          InvalidArgumentFunctionException.INVALID_ARGUMENT_TYPE,
          String.format("Invalid argument type '%s'", item.getClass().getName()));
    }
    return retval;
  }
}
