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

import gov.nist.secauto.metaschema.core.metapath.impl.AbstractSequence;
import gov.nist.secauto.metaschema.core.metapath.impl.SequenceN;
import gov.nist.secauto.metaschema.core.metapath.impl.SingletonSequence;
import gov.nist.secauto.metaschema.core.metapath.impl.StreamSequence;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
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
 * @param <ITEM>
 *          the Java type of the items in a sequence
 */
@SuppressWarnings("PMD.ShortMethodName")
public interface ISequence<ITEM extends IItem> extends List<ITEM>, IStringValued, ICollectionValue {
  /**
   * Get an empty sequence.
   *
   * @param <T>
   *          the item type
   * @return the empty sequence
   */
  @SuppressWarnings("null")
  @NonNull
  static <T extends IItem> ISequence<T> empty() {
    return AbstractSequence.empty();
  }

  @Override
  default Iterator<ITEM> iterator() {
    return getValue().listIterator();
  }

  /**
   * Get the items in this sequence as a {@link List}.
   *
   * @return a list containing all the items of the sequence
   */
  @NonNull
  List<ITEM> getValue();

  /**
   * Get the items in this sequence as a {@link Stream}.
   *
   * @return a stream containing all the items of the sequence
   */
  @Override
  @NonNull
  Stream<ITEM> stream();

  /**
   * Retrieves the first item in a sequence. If the sequence is empty, a
   * {@code null} result is returned. If requireSingleton is {@code true} and the
   * sequence contains more than one item, a {@link TypeMetapathException} is
   * thrown.
   *
   * @param <T>
   *          the item type to return derived from the provided sequence
   * @param items
   *          the sequence to retrieve the first item from
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if
   *          the sequence contains more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws TypeMetapathException
   *           if the sequence contains more than one item and requireSingleton is
   *           {@code true}
   */
  static <T extends IItem> T getFirstItem(@NonNull ISequence<T> items, boolean requireSingleton) {
    return getFirstItem(items.stream(), requireSingleton);
  }

  /**
   * Retrieves the first item in a sequence. If the sequence is empty, a
   * {@code null} result is returned. If requireSingleton is {@code true} and the
   * sequence contains more than one item, a {@link TypeMetapathException} is
   * thrown.
   *
   * @param <T>
   *          the item type to return derived from the provided sequence
   * @param items
   *          the sequence to retrieve the first item from
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if
   *          the sequence contains more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws TypeMetapathException
   *           if the sequence contains more than one item and requireSingleton is
   *           {@code true}
   */
  static <T extends IItem> T getFirstItem(@NonNull Stream<T> items, boolean requireSingleton) {
    return items.limit(2)
        .reduce((t, u) -> {
          if (requireSingleton) {
            throw new InvalidTypeMetapathException(
                null,
                String.format("sequence expected to contain only one item, but found multiple"));
          }
          return t;
        }).orElse(null);
  }

  @Nullable
  default ITEM getFirstItem(boolean requireSingleton) {
    return getFirstItem(this, requireSingleton);
  }

  @NonNull
  default ICollectionValue toArrayMember() {
    ICollectionValue retval;
    switch (size()) {
    case 0:
      retval = ISequence.empty();
      break;
    case 1:
      retval = ObjectUtils.notNull(stream().findFirst().get());
      break;
    default:
      retval = this;
    }
    return retval;
  }

  /**
   * Get a stream guaranteed to be backed by a list.
   *
   * @return the stream
   */
  @NonNull
  default Stream<ITEM> safeStream() {
    return ObjectUtils.notNull(getValue().stream());
  }

  @SuppressWarnings("null")
  @Override
  default Stream<? extends IItem> flatten() {
    // TODO: Is a safe stream needed here?
    return safeStream();
  }

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
        return List::add;
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
        return list -> ofCollection(ObjectUtils.notNull(list));
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Collections.emptySet();
      }
    };
  }

  @Override
  default ISequence<ITEM> asSequence() {
    return this;
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
        .map(mapFunction::apply)
        .collect(toSequence());
  }

  /**
   * Returns an unmodifiable sequence containing the provided {@code item}.
   * <p>
   * If the item is {@code null} and empty sequence will be created.
   *
   * @param <T>
   *          the type of items contained in the sequence.
   * @param item
   *          the item to add to the sequence
   * @return the new sequence
   */
  @NonNull
  static <T extends IItem> ISequence<T> of( // NOPMD - intentional
      @Nullable T item) {
    return item == null ? empty() : new SingletonSequence<>(item);
  }

  /**
   * Returns an unmodifiable sequence containing the provided {@code items}.
   *
   * @param <ITEM_TYPE>
   *          the type of items contained in the sequence.
   * @param items
   *          the items to add to the sequence
   * @return the new sequence
   */
  @NonNull
  static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> ofCollection( // NOPMD - intentional
      @NonNull List<ITEM_TYPE> items) {
    ISequence<ITEM_TYPE> retval;
    if (items.isEmpty()) {
      retval = empty();
    } else if (items.size() == 1) {
      retval = new SingletonSequence<>(ObjectUtils.notNull(items.iterator().next()));
    } else {
      retval = new SequenceN<>(items);
    }
    return retval;
  }

  /**
   * Returns an unmodifiable sequence containing the provided {@code items}.
   *
   * @param <T>
   *          the type of items contained in the sequence.
   * @param items
   *          the items to add to the sequence
   * @return the new sequence
   */
  // TODO: remove null check on callers
  @NonNull
  static <T extends IItem> ISequence<T> of( // NOPMD - intentional
      Stream<T> items) {
    return items == null ? empty() : new StreamSequence<>(items);
  }

  /**
   * Returns an unmodifiable sequence containing zero elements.
   *
   * @param <T>
   *          the item type
   * @return an empty {@code ISequence}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of() {
    return empty();
  }

  /**
   * Returns an unmodifiable sequence containing two items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2) {
    return new SequenceN<>(e1, e2);
  }

  /**
   * Returns an unmodifiable sequence containing three elements.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3) {
    return new SequenceN<>(e1, e2, e3);
  }

  /**
   * Returns an unmodifiable sequence containing four items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4) {
    return new SequenceN<>(e1, e2, e3, e4);
  }

  /**
   * Returns an unmodifiable sequence containing five items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5) {
    return new SequenceN<>(e1, e2, e3, e4, e5);
  }

  /**
   * Returns an unmodifiable sequence containing six items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @param e6
   *          the sixth item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5, T e6) {
    return new SequenceN<>(e1, e2, e3, e4, e5, e6);
  }

  /**
   * Returns an unmodifiable sequence containing seven items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @param e6
   *          the sixth item
   * @param e7
   *          the seventh item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5, T e6, T e7) {
    return new SequenceN<>(e1, e2, e3, e4, e5, e6, e7);
  }

  /**
   * Returns an unmodifiable sequence containing eight items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @param e6
   *          the sixth item
   * @param e7
   *          the seventh item
   * @param e8
   *          the eighth item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8) {
    return new SequenceN<>(e1, e2, e3, e4, e5, e6, e7, e8);
  }

  /**
   * Returns an unmodifiable sequence containing nine items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @param e6
   *          the sixth item
   * @param e7
   *          the seventh item
   * @param e8
   *          the eighth item
   * @param e9
   *          the ninth item
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8, T e9) {
    return new SequenceN<>(e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  /**
   * Returns an unmodifiable sequence containing ten items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @param e5
   *          the fifth item
   * @param e6
   *          the sixth item
   * @param e7
   *          the seventh item
   * @param e8
   *          the eighth item
   * @param e9
   *          the ninth item
   * @param e10
   *          the tenth item
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends IItem> ISequence<T> of(T e1, T e2, T e3, T e4, T e5, T e6, T e7, T e8, T e9, T e10) {
    return new SequenceN<>(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  /**
   * Returns an unmodifiable sequence containing an arbitrary number of items.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param items
   *          the items to be contained in the list
   * @return an {@code ISequence} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null} or if the array is {@code null}
   */
  @SafeVarargs
  @NonNull
  static <T extends IItem> ISequence<T> of(@NonNull T... items) {
    return items.length == 0 ? empty() : new SequenceN<>(items);
  }

  /**
   * Returns an unmodifiable sequence containing the items of the given
   * Collection, in its iteration order. The given Collection must not be null,
   * and it must not contain any null items. If the given Collection is
   * subsequently modified, the returned array item will not reflect such
   * modifications.
   *
   * @param <T>
   *          the {@code ISequence}'s item type
   * @param collection
   *          a {@code Collection} from which items are drawn, must be non-null
   * @return an {@code ISequence} containing the items of the given
   *         {@code Collection}
   * @throws NullPointerException
   *           if collection is null, or if it contains any nulls
   * @since 10
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <T extends IItem> ISequence<T> copyOf(Collection<? extends T> collection) {
    return collection instanceof IArrayItem
        ? (ISequence<T>) collection
        : collection.isEmpty()
            ? empty()
            : new SequenceN<>(new ArrayList<>(collection));
  }
}
