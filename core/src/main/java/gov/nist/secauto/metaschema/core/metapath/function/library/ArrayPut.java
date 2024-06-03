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

public final class ArrayPut {
  @NonNull
  public static final IFunction SIGNATURE = IFunction.builder()
      .name("put")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS_ARRAY)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("array")
          .type(IArrayItem.class)
          .one()
          .build())
      .argument(IArgument.builder()
          .name("position")
          .type(IIntegerItem.class)
          .one()
          .build())
      .argument(IArgument.builder()
          .name("member")
          .type(IItem.class)
          .zeroOrMore()
          .build())
      .returnType(IArrayItem.class)
      .returnOne()
      .functionHandler(ArrayPut::execute)
      .build();

  private ArrayPut() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static <T extends ICollectionValue> ISequence<? extends IArrayItem<T>> execute(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {
    IArrayItem<T> array = FunctionUtils.asType(ObjectUtils.requireNonNull(
        arguments.get(0).getFirstItem(true)));
    IIntegerItem position = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1).getFirstItem(true)));
    @SuppressWarnings("unchecked") T member = (T) arguments.get(2).toCollectionValue();

    return put(array, position, member).asSequence();
  }

  /**
   * An implementation of XPath 3.1 <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-array-put">array:put</a>.
   *
   * @param <T>
   *          the type of items in the given Metapath array
   * @param array
   *          the target Metapath array
   * @param positionItem
   *          the integer position of the item to replace
   * @param member
   *          the Metapath item to replace the identified array member with
   * @return a new array containing the modification
   * @throws ArrayException
   *           if the position is not in the range of 1 to array:size
   */
  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> put(
      @NonNull IArrayItem<T> array,
      @NonNull IIntegerItem positionItem,
      @NonNull T member) {
    return put(array, positionItem.asInteger().intValueExact(), member);
  }

  @NonNull
  public static <T extends ICollectionValue> IArrayItem<T> put(@NonNull IArrayItem<T> array, int position,
      @NonNull T member) {
    List<T> copy = new ArrayList<>(array);
    try {
      copy.set(position - 1, member);
    } catch (IndexOutOfBoundsException ex) {
      throw new ArrayException(
          ArrayException.INDEX_OUT_OF_BOUNDS,
          String.format("The position %d is outside the range of values for the array of size '%d'.",
              position,
              copy.size()),
          ex);
    }

    return IArrayItem.ofCollection(copy);
  }
}
