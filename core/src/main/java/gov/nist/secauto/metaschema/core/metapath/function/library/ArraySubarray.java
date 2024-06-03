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
import gov.nist.secauto.metaschema.core.metapath.ICollectionValue;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.ArrayException;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ArraySubarray {
  @NonNull
  public static final IFunction SIGNATURE_TWO_ARG = IFunction.builder()
      .name("subarray")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS_ARRAY)
      .argument(IArgument.builder()
          .name("array")
          .type(IArrayItem.class)
          .one()
          .build())
      .argument(IArgument.builder()
          .name("start")
          .type(IIntegerItem.class)
          .one()
          .build())
      .returnType(IArrayItem.class)
      .returnOne()
      .functionHandler(ArraySubarray::executeTwoArg)
      .build();
  @NonNull
  public static final IFunction SIGNATURE_THREE_ARG = IFunction.builder()
      .name("subarray")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS_ARRAY)
      .argument(IArgument.builder()
          .name("array")
          .type(IArrayItem.class)
          .one()
          .build())
      .argument(IArgument.builder()
          .name("start")
          .type(IIntegerItem.class)
          .one()
          .build())
      .argument(IArgument.builder()
          .name("length")
          .type(IIntegerItem.class)
          .one()
          .build())
      .returnType(IArrayItem.class)
      .returnOne()
      .functionHandler(ArraySubarray::executeThreeArg)
      .build();

  @SuppressWarnings("unused")
  @NonNull
  private static <T extends ICollectionValue> ISequence<IArrayItem<T>> executeTwoArg(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    IArrayItem<T> array = FunctionUtils.asType(ObjectUtils.requireNonNull(
        arguments.get(0).getFirstItem(true)));
    IIntegerItem start = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1).getFirstItem(true)));

    return ISequence.of(subarray(array, start));
  }

  @SuppressWarnings("unused")
  @NonNull
  private static <T extends ICollectionValue> ISequence<IArrayItem<T>> executeThreeArg(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    IArrayItem<T> array = FunctionUtils.asType(ObjectUtils.requireNonNull(
        arguments.get(0).getFirstItem(true)));
    IIntegerItem start = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1).getFirstItem(true)));
    IIntegerItem length = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(2).getFirstItem(true)));

    return ISequence.of(subarray(array, start, length));
  }

  /**
   * An implementation of XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-array-subarray">array:subarray</a>.
   *
   * @param <T>
   *          the type of items in the given Metapath array
   * @param array
   *          the target Metapath array
   * @param startItem
   *          the integer position of the item to start with (inclusive)
   * @return a new array consisting of the items in the identified range
   * @throws ArrayException
   *           if the position is not in the range of 1 to array:size
   */
  @SuppressWarnings("PMD.OnlyOneReturn")
  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> subarray(
      @NonNull IArrayItem<T> array,
      @NonNull IIntegerItem startItem) {
    return subarray(array, startItem.asInteger().intValueExact());
  }

  /**
   * An implementation of XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-array-subarray">array:subarray</a>.
   *
   * @param <T>
   *          the type of items in the given Metapath array
   * @param array
   *          the target Metapath array
   * @param startItem
   *          the integer position of the item to start with (inclusive)
   * @param lengthItem
   *          the integer count of items to include starting with the item at the
   *          start position
   * @return a new array consisting of the items in the identified range
   * @throws ArrayException
   *           if the length is negative or the position is not in the range of 1
   *           to array:size
   */
  @SuppressWarnings("PMD.OnlyOneReturn")
  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> subarray(
      @NonNull IArrayItem<T> array,
      @NonNull IIntegerItem startItem,
      @NonNull IIntegerItem lengthItem) {
    return subarray(array, startItem.asInteger().intValueExact(), lengthItem.asInteger().intValueExact());
  }

  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> subarray(@NonNull IArrayItem<T> array, int start) {
    return subarray(array, start, array.size() - start + 1);
  }

  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> subarray(@NonNull IArrayItem<T> array, int start,
      int length) {
    if (length < 0) {
      throw new ArrayException(
          ArrayException.NEGATIVE_ARRAY_LENGTH, String.format("The length '%d' is negative.", length));
    }

    List<T> copy;
    try {
      copy = array.subList(start - 1, start - 1 + length);
    } catch (IndexOutOfBoundsException ex) {
      throw new ArrayException(
          ArrayException.INDEX_OUT_OF_BOUNDS,
          String.format("The start + length (%d + %d) exceeds the array length '%d'.",
              start,
              length,
              array.size()),
          ex);
    }

    return IArrayItem.ofCollection(new ArrayList<>(copy));
  }
}
