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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an ordered collection of Metapath expression results.
 * <p>
 * Items is a sequence are typically ordered based on their position in the
 * original node graph based on a depth first ordering.
 *
 * @param <ITEM_TYPE>
 *          the Java type of the items in a sequence
 */
public interface ISequence<ITEM_TYPE extends IItem> extends List<ITEM_TYPE> {
  @SuppressWarnings("rawtypes")
  ISequence EMPTY = new EmptyListImpl<>();

  /**
   * Get an empty sequence.
   *
   * @param <ITEM_TYPE>
   *          the item type
   * @return the empty sequence
   */
  @SuppressWarnings({ "unchecked", "null" })
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> empty() {
    return EMPTY;
  }

  /**
   * Construct a new sequence containing the provided {@code item}.
   * <p>
   * If the item is {@code null} and empty sequence will be created.
   *
   * @param <ITEM_TYPE>
   *          the type of items contained in the sequence.
   * @param item
   *          the item to add to the sequence
   * @return the new sequence
   */
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of( // NOPMD - intentional
      @Nullable ITEM_TYPE item) {
    ISequence<ITEM_TYPE> retval;
    if (item == null) {
      retval = empty();
    } else {
      retval = new SingletonSequenceImpl<>(item);
    }
    return retval;
  }

  /**
   * Construct a new sequence containing the provided {@code items}.
   *
   * @param <ITEM_TYPE>
   *          the type of items contained in the sequence.
   * @param items
   *          the items to add to the sequence
   * @return the new sequence
   */
  @SafeVarargs
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of( // NOPMD - intentional
      @NonNull ITEM_TYPE... items) {
    return of(ObjectUtils.notNull(Arrays.asList(items)));
  }

  /**
   * Construct a new sequence containing the provided {@code items}.
   *
   * @param <ITEM_TYPE>
   *          the type of items contained in the sequence.
   * @param items
   *          the items to add to the sequence
   * @return the new sequence
   */
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of( // NOPMD - intentional
      @NonNull List<ITEM_TYPE> items) {
    ISequence<ITEM_TYPE> retval;
    if (items.isEmpty()) {
      retval = empty();
    } else if (items.size() == 1) {
      retval = new SingletonSequenceImpl<>(ObjectUtils.notNull(items.iterator().next()));
    } else {
      retval = new ListSequenceImpl<>(items);
    }
    return retval;
  }

  /**
   * Construct a new sequence containing the provided {@code items}.
   *
   * @param <ITEM_TYPE>
   *          the type of items contained in the sequence.
   * @param items
   *          the items to add to the sequence
   * @return the new sequence
   */
  // TODO: remove null check on callers
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of( // NOPMD - intentional
      Stream<ITEM_TYPE> items) {
    return items == null ? empty() : new StreamSequenceImpl<>(items);
  }

  @Override
  default Iterator<ITEM_TYPE> iterator() {
    return asList().listIterator();
  }

  /**
   * Get the items in this sequence as a {@link List}.
   *
   * @return a list containing all the items of the sequence
   */
  @NonNull
  List<ITEM_TYPE> asList();

  /**
   * Get the items in this sequence as a {@link Stream}.
   *
   * @return a stream containing all the items of the sequence
   */
  // TODO: rename to "stream"
  @NonNull
  Stream<ITEM_TYPE> asStream();

  /**
   * Get a stream guaranteed to be backed by a list.
   *
   * @return the stream
   */
  @NonNull
  default Stream<ITEM_TYPE> safeStream() {
    return ObjectUtils.notNull(asList().stream());
  }

  /**
   * This optional operation ensures that a list is used to back this sequence.
   * <p>
   * If a stream is currently backing this sequence, the stream will be collected
   * into a list. This ensures the sequence can be visited multiple times.
   *
   * @return the resulting sequence
   */
  @NonNull
  ISequence<ITEM_TYPE> collect();

  /**
   * Determine if this sequence is empty.
   *
   * @return {@code true} if the sequence contains no items, or {@code false}
   *         otherwise
   */
  @Override
  boolean isEmpty();

  /**
   * Get the count of items in this sequence.
   *
   * @return the count of items
   */
  @Override
  int size();

  /**
   * Iterate over each item in the sequence using the provided {@code action}.
   *
   * @param action
   *          code to execute for each item
   */
  @Override
  void forEach(Consumer<? super ITEM_TYPE> action);

  /**
   * A {@link Collector} implementation to generates a sequence from a stream of
   * Metapath items.
   *
   * @param <ITEM_TYPE>
   *          the Java type of the items
   * @return a collector that will generate a sequence
   */
  @NonNull
  static <ITEM_TYPE extends IItem> Collector<ITEM_TYPE, ?, ISequence<ITEM_TYPE>> toSequence() {
    return new Collector<ITEM_TYPE, List<ITEM_TYPE>, ISequence<ITEM_TYPE>>() {

      @Override
      public Supplier<List<ITEM_TYPE>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<ITEM_TYPE>, ITEM_TYPE> accumulator() {
        return (list, value) -> list.add(value);
      }

      @Override
      public BinaryOperator<List<ITEM_TYPE>> combiner() {
        return (list1, list2) -> {
          list1.addAll(list2);
          return list1;
        };
      }

      @Override
      public Function<List<ITEM_TYPE>, ISequence<ITEM_TYPE>> finisher() {
        return list -> of(ObjectUtils.notNull(list));
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Collections.emptySet();
      }
    };
  }

  /**
   * Apply the provided {@code mapFunction} to each item in the sequence.
   *
   * @param <T>
   *          the Java type of the provided items
   * @param <R>
   *          the Java type of the resulting items
   * @param mapFunction
   *          the map function to apply to each item in the provided sequence
   * @param seq
   *          the sequence of items to map
   * @return a new sequence containing the mapped items
   */
  static <T extends R, R extends IItem> ISequence<R> map(
      @NonNull Function<T, R> mapFunction,
      @NonNull ISequence<T> seq) {
    return seq.safeStream()
        .map(item -> mapFunction.apply(item))
        .collect(toSequence());
  }

  @Override
  default boolean contains(Object obj) {
    return asList().contains(obj);
  }

  @Override
  default Object[] toArray() {
    return asList().toArray();
  }

  @Override
  default <T> T[] toArray(T[] array) {
    return asList().toArray(array);
  }

  @Override
  default boolean add(ITEM_TYPE item) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean remove(Object obj) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean containsAll(Collection<?> collection) {
    return asList().containsAll(collection);
  }

  @Override
  default boolean addAll(Collection<? extends ITEM_TYPE> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean removeAll(Collection<?> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean retainAll(Collection<?> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  default void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean addAll(int index, Collection<? extends ITEM_TYPE> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  default ITEM_TYPE get(int index) {
    return asList().get(index);
  }

  @Override
  default ITEM_TYPE set(int index, ITEM_TYPE element) {
    throw new UnsupportedOperationException();
  }

  @Override
  default void add(int index, ITEM_TYPE element) {
    throw new UnsupportedOperationException();
  }

  @Override
  default ITEM_TYPE remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  default int indexOf(Object obj) {
    return asList().indexOf(obj);
  }

  @Override
  default int lastIndexOf(Object obj) {
    return asList().lastIndexOf(obj);
  }

  @Override
  default ListIterator<ITEM_TYPE> listIterator() {
    return asList().listIterator();
  }

  @Override
  default ListIterator<ITEM_TYPE> listIterator(int index) {
    return asList().listIterator(index);
  }

  @Override
  default List<ITEM_TYPE> subList(int fromIndex, int toIndex) {
    return asList().subList(fromIndex, toIndex);
  }
}
