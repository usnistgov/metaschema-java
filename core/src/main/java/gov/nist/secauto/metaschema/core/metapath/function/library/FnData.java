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
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidTypeFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAtomicValuedItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Implements
 * <a href= "https://www.w3.org/TR/xpath-functions-31/#func-data">fn:data</a>.
 */
public final class FnData {

  @NonNull
  static final IFunction SIGNATURE_NO_ARG = IFunction.builder()
      .name("data")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextDependent()
      .focusDependent()
      .returnType(IAnyAtomicItem.class)
      .returnOne()
      .functionHandler(FnData::executeNoArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("data")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("arg1")
          .type(IItem.class)
          .zeroOrMore()
          .build())
      .returnType(IAnyAtomicItem.class)
      .returnOne()
      .functionHandler(FnData::executeOneArg)
      .build();

  private FnData() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IAnyAtomicItem> executeNoArg(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    INodeItem item = FunctionUtils.requireTypeOrNull(INodeItem.class, focus);

    ISequence<IAnyAtomicItem> retval;
    if (item == null) {
      retval = ISequence.empty();
    } else {
      IAnyAtomicItem data = fnDataItem(item);
      retval = ISequence.of(data);
    }
    return retval;
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IAnyAtomicItem> executeOneArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<?> sequence = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));
    return fnData(sequence);
  }

  /**
   * An implementation of XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-data">fn:data</a>
   * supporting <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item
   * atomization</a>.
   *
   * @param sequence
   *          the sequence of items to atomize
   * @return the atomized result
   */
  @SuppressWarnings("null")
  @NonNull
  public static ISequence<IAnyAtomicItem> fnData(@NonNull ISequence<? extends IItem> sequence) {
    return ISequence.of(sequence.stream()
        .flatMap(FnData::atomize));
  }

  /**
   * An implementation of
   * <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item
   * atomization</a>.
   *
   * @param item
   *          the item to atomize
   * @return the atomized result
   */
  @NonNull
  public static IAnyAtomicItem fnDataItem(@NonNull IItem item) {
    IAnyAtomicItem retval = null;
    if (item instanceof IAtomicValuedItem) {
      retval = ((IAtomicValuedItem) item).toAtomicItem();
    }

    if (retval != null) {
      return retval;
    }
    throw new InvalidTypeFunctionException(InvalidTypeFunctionException.NODE_HAS_NO_TYPED_VALUE, item);
  }

  /**
   * An implementation of
   * <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item
   * atomization</a>.
   *
   * @param item
   *          the item to atomize
   * @return the atomized result
   */
  @NonNull
  public static Stream<IAnyAtomicItem> fnDataItem(@NonNull IArrayItem<?> item) {
    return ObjectUtils.notNull(item.stream().flatMap(member -> {
      Stream<IAnyAtomicItem> result;
      if (member instanceof IItem) {
        result = atomize((IItem) member);
      } else if (member instanceof ISequence) {
        result = ((ISequence<?>) member).stream()
            .flatMap(FnData::atomize);
      } else {
        throw new UnsupportedOperationException("array member not an item or sequence.");
      }
      return result;
    }));
  }

  /**
   * An implementation of
   * <a href="https://www.w3.org/TR/xpath-31/#id-atomization">item
   * atomization</a>.
   *
   * @param item
   *          the item to atomize
   * @return the atomized result
   */
  @NonNull
  public static Stream<IAnyAtomicItem> atomize(@NonNull IItem item) {
    Stream<IAnyAtomicItem> retval;
    if (item instanceof IAnyAtomicItem) {
      retval = ObjectUtils.notNull(Stream.of((IAnyAtomicItem) item));
    } else if (item instanceof IAtomicValuedItem) {
      retval = ObjectUtils.notNull(Stream.of(((IAtomicValuedItem) item).toAtomicItem()));
    } else if (item instanceof IArrayItem) {
      retval = fnDataItem((IArrayItem<?>) item);
    } else {
      throw new InvalidTypeFunctionException(InvalidTypeFunctionException.NODE_HAS_NO_TYPED_VALUE, item);
    }
    return retval;
  }
}
