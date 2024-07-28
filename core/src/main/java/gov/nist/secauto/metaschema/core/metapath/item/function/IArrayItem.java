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

package gov.nist.secauto.metaschema.core.metapath.item.function;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ICollectionValue;
import gov.nist.secauto.metaschema.core.metapath.IPrintable;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.ISequenceType;
import gov.nist.secauto.metaschema.core.metapath.impl.AbstractArrayItem;
import gov.nist.secauto.metaschema.core.metapath.impl.ArrayItemN;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A representation of a Metapath array item type.
 * <p>
 * Instances of this interface are required to enforce non-mutability for array
 * contents.
 *
 * @param <ITEM>
 *          the Metapath item type of array members
 */
@SuppressWarnings("PMD.ShortMethodName")
public interface IArrayItem<ITEM extends ICollectionValue> extends IFunction, IItem, List<ITEM>, IPrintable {
  /**
   * Get an empty, immutable array item.
   *
   * @param <T>
   *          the item Java type
   * @return an immutable map item
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> empty() {
    return AbstractArrayItem.empty();
  }

  @Override
  default QName getQName() {
    return AbstractArrayItem.QNAME;
  }

  @Override
  default Set<FunctionProperty> getProperties() {
    return AbstractArrayItem.PROPERTIES;
  }

  @Override
  default boolean isDeterministic() {
    return true;
  }

  @Override
  default boolean isContextDepenent() {
    return false;
  }

  @Override
  default boolean isFocusDepenent() {
    return false;
  }

  @Override
  default List<IArgument> getArguments() {
    return AbstractArrayItem.ARGUMENTS;
  }

  @Override
  default int arity() {
    return 1;
  }

  @Override
  default boolean isArityUnbounded() {
    return false;
  }

  @Override
  default ISequenceType getResult() {
    return AbstractArrayItem.RESULT;
  }

  @Override
  ISequence<?> execute(List<ISequence<?>> arguments, DynamicContext dynamicContext, ISequence<?> focus);

  @Override
  default String toSignature() {
    return "array()";
  }

  @Override
  List<ITEM> getValue();

  @Override
  default boolean hasValue() {
    return true;
  }

  /**
   * Determine if this sequence is empty.
   *
   * @return {@code true} if the sequence contains no items, or {@code false}
   *         otherwise
   */
  @Override
  default boolean isEmpty() {
    return getValue().isEmpty();
  }

  /**
   * Get the count of items in this sequence.
   *
   * @return the count of items
   */
  @Override
  default int size() {
    return getValue().size();

  }

  @Override
  default boolean contains(Object obj) {
    return getValue().contains(obj);
  }

  @Override
  default Object[] toArray() {
    return getValue().toArray();
  }

  @Override
  default <T> T[] toArray(T[] array) {
    return getValue().toArray(array);
  }

  @Override
  default boolean containsAll(Collection<?> collection) {
    return getValue().containsAll(collection);
  }

  @Override
  default ITEM get(int index) {
    return getValue().get(index);
  }

  @Override
  default int indexOf(Object obj) {
    return getValue().indexOf(obj);
  }

  @Override
  default int lastIndexOf(Object obj) {
    return getValue().lastIndexOf(obj);
  }

  @Override
  default ListIterator<ITEM> listIterator() {
    return getValue().listIterator();
  }

  @Override
  default ListIterator<ITEM> listIterator(int index) {
    return getValue().listIterator(index);
  }

  @Override
  default List<ITEM> subList(int fromIndex, int toIndex) {
    return getValue().subList(fromIndex, toIndex);
  }

  /**
   * A {@link Collector} implementation to generates a sequence from a stream of
   * Metapath items.
   *
   * @param <T>
   *          the Java type of the items
   * @return a collector that will generate a sequence
   */
  @NonNull
  static <T extends ICollectionValue> Collector<T, ?, IArrayItem<T>> toArrayItem() {
    return new Collector<T, List<T>, IArrayItem<T>>() {

      @SuppressWarnings("null")
      @Override
      public Supplier<List<T>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<T>, T> accumulator() {
        return List::add;
      }

      @Override
      public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
          list1.addAll(list2);
          return list1;
        };
      }

      @Override
      public Function<List<T>, IArrayItem<T>> finisher() {
        return list -> ofCollection(ObjectUtils.notNull(list));
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Collections.emptySet();
      }
    };
  }

  @Override
  default ISequence<? extends IArrayItem<ITEM>> asSequence() {
    return ISequence.of(this);
  }

  @SuppressWarnings("null")
  @Override
  default Stream<? extends IItem> flatten() {
    return stream()
        .flatMap(ICollectionValue::flatten);
  }

  /**
   * Get a new, immutable array item that contains the items in the provided list.
   *
   * @param <T>
   *          the item Java type
   * @param items
   *          the list whose items are to be added to the new array
   * @return an array item containing the specified entries
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> ofCollection( // NOPMD - intentional
      @NonNull List<T> items) {
    return items.isEmpty() ? empty() : new ArrayItemN<>(items);
  }

  /**
   * Returns an unmodifiable array item containing zero elements.
   *
   * @param <T>
   *          the item type
   * @return an empty {@code IArrayItem}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of() {
    return AbstractArrayItem.empty();
  }

  /**
   * Returns an unmodifiable array item containing one item.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param e1
   *          the single item
   * @return an {@code IArrayItem} containing the specified item
   * @throws NullPointerException
   *           if the item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1) {
    return new ArrayItemN<>(e1);
  }

  /**
   * Returns an unmodifiable array item containing two items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2) {
    return new ArrayItemN<>(e1, e2);
  }

  /**
   * Returns an unmodifiable array item containing three elements.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3) {
    return new ArrayItemN<>(e1, e2, e3);
  }

  /**
   * Returns an unmodifiable array item containing four items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param e1
   *          the first item
   * @param e2
   *          the second item
   * @param e3
   *          the third item
   * @param e4
   *          the fourth item
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4) {
    return new ArrayItemN<>(e1, e2, e3, e4);
  }

  /**
   * Returns an unmodifiable array item containing five items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5);
  }

  /**
   * Returns an unmodifiable array item containing six items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5, @NonNull T e6) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5, e6);
  }

  /**
   * Returns an unmodifiable array item containing seven items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5, @NonNull T e6, @NonNull T e7) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5, e6, e7);
  }

  /**
   * Returns an unmodifiable array item containing eight items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5, @NonNull T e6, @NonNull T e7, @NonNull T e8) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5, e6, e7, e8);
  }

  /**
   * Returns an unmodifiable array item containing nine items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null}
   */
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5, @NonNull T e6, @NonNull T e7, @NonNull T e8, @NonNull T e9) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  /**
   * Returns an unmodifiable array item containing ten items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
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
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T e1, @NonNull T e2, @NonNull T e3, @NonNull T e4,
      @NonNull T e5, @NonNull T e6, @NonNull T e7, @NonNull T e8, @NonNull T e9, @NonNull T e10) {
    return new ArrayItemN<>(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  /**
   * Returns an unmodifiable array item containing an arbitrary number of items.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param items
   *          the items to be contained in the list
   * @return an {@code IArrayItem} containing the specified items
   * @throws NullPointerException
   *           if an item is {@code null} or if the array is {@code null}
   */
  @SafeVarargs
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> of(@NonNull T... items) {
    return items.length == 0 ? empty() : new ArrayItemN<>(items);
  }

  /**
   * Returns an unmodifiable array item containing the items of the given
   * Collection, in its iteration order. The given Collection must not be null,
   * and it must not contain any null items. If the given Collection is
   * subsequently modified, the returned array item will not reflect such
   * modifications.
   *
   * @param <T>
   *          the {@code IArrayItem}'s item type
   * @param collection
   *          a {@code Collection} from which items are drawn, must be non-null
   * @return an {@code IArrayItem} containing the items of the given
   *         {@code Collection}
   * @throws NullPointerException
   *           if collection is null, or if it contains any nulls
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <T extends ICollectionValue> IArrayItem<T> copyOf(@NonNull Collection<? extends T> collection) {
    return collection instanceof IArrayItem
        ? (IArrayItem<T>) collection
        : collection.isEmpty()
            ? empty()
            : new ArrayItemN<>(new ArrayList<>(collection));
  }
}
