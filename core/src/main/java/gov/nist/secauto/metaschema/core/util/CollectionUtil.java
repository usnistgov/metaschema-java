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

package gov.nist.secauto.metaschema.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class CollectionUtil {
  /**
   * Get a {@link Stream} for the provided {@link Iterable}.
   *
   * @param <T>
   *          the type to iterate on
   * @param iterator
   *          the iterator
   * @return the stream
   */
  public static <T> Stream<T> toStream(@NonNull Iterator<T> iterator) {
    Iterable<T> iterable = toIterable(iterator);
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  /**
   * Get an {@link Iterable} for the provided {@link Stream}.
   *
   * @param <T>
   *          the type to iterate on
   * @param stream
   *          the stream to iterate over
   * @return the resulting iterable instance
   */
  @NonNull
  public static <T> Iterable<T> toIterable(@NonNull Stream<T> stream) {
    return toIterable(ObjectUtils.notNull(stream.iterator()));
  }

  /**
   * Get an {@link Iterable} for the provided {@link Iterator}.
   *
   * @param <T>
   *          the type to iterate on
   * @param iterator
   *          the iterator
   * @return the resulting iterable instance
   */
  @NonNull
  public static <T> Iterable<T> toIterable(@NonNull Iterator<T> iterator) {
    return () -> iterator;
  }

  /**
   * Get a reverse {@link Iterable} for the provided {@link List}.
   *
   * @param <T>
   *          the type to iterate on
   * @param list
   *          the list of items to iterate over
   * @return the resulting iterable instance
   */
  @NonNull
  public static <T> Iterable<T> toDescendingIterable(@NonNull List<T> list) {
    return toIterable(descendingIterator(list));
  }

  /**
   * Convert the provided {@link Iterable} to a list of the same generic type.
   *
   * @param <T>
   *          the collection item's generic type
   * @param iterable
   *          the Iterable to convert to a list
   * @return the list
   */
  @NonNull
  public static <T> List<T> toList(Iterable<T> iterable) {
    return ObjectUtils.notNull(StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList()));
  }

  /**
   * Convert the provided {@link Iterator} to a list of the same generic type.
   *
   * @param <T>
   *          the collection item's generic type
   * @param iterator
   *          the Iterator to convert to a list
   * @return the list
   */
  @NonNull
  public static <T> List<T> toList(Iterator<T> iterator) {
    return ObjectUtils.notNull(
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
            .collect(Collectors.toList()));
  }

  /**
   * Get a reverse {@link Iterator} for the provided {@link List}.
   *
   * @param <T>
   *          the type to iterate on
   * @param list
   *          the list of items to iterate over
   * @return the resulting Iterator instance
   */
  @NonNull
  public static <T> Iterator<T> descendingIterator(@NonNull List<T> list) {
    Iterator<T> retval;
    if (list instanceof LinkedList) {
      retval = ((LinkedList<T>) list).descendingIterator();
    } else if (list instanceof ArrayList) {
      retval = IntStream.range(0, list.size())
          .map(i -> list.size() - 1 - i)
          .mapToObj(list::get).iterator();
    } else {
      throw new UnsupportedOperationException();
    }
    return ObjectUtils.notNull(retval);
  }

  /**
   * Require that the provided collection contains at least a single item.
   *
   * @param <T>
   *          the Java type of the collection
   * @param <U>
   *          the Java type of the collection's items
   * @param collection
   *          the collection to test
   * @return the provided collection
   * @throws IllegalStateException
   *           if the collection is empty
   */
  @NonNull
  public static <T extends Collection<U>, U> T requireNonEmpty(@NonNull T collection) {
    if (collection.isEmpty()) {
      throw new IllegalStateException();
    }
    return collection;
  }

  /**
   * Require that the provided collection contains at least a single item.
   *
   * @param <T>
   *          the Java type of the collection
   * @param <U>
   *          the Java type of the collection's items
   * @param collection
   *          the collection to test
   * @param message
   *          the exception message to use if the collection is empty
   * @return the provided collection
   * @throws IllegalStateException
   *           if the collection is empty
   */
  @NonNull
  public static <T extends Collection<U>, U> T requireNonEmpty(@NonNull T collection, @NonNull String message) {
    if (collection.isEmpty()) {
      throw new IllegalStateException(message);
    }
    return collection;
  }

  /**
   * An implementation of {@link Collections#unmodifiableCollection(Collection)}
   * that respects non-nullness.
   *
   * @param <T>
   *          the collection's item type
   * @param collection
   *          the collection
   * @return an unmodifiable view of the collection
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Collection<T> unmodifiableCollection(@NonNull Collection<T> collection) {
    return Collections.unmodifiableCollection(collection);
  }

  /**
   * An implementation of {@link Collections#singleton(Object)} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the set items
   * @param instance
   *          the singleton item to use
   * @return an unmodifiable set containing the singleton item
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> singleton(@NonNull T instance) {
    return Collections.singleton(instance);
  }

  /**
   * An implementation of {@link Collections#emptySet()} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the set items
   * @return an unmodifiable empty set
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> emptySet() {
    return Collections.emptySet();
  }

  /**
   * An implementation of {@link Collections#unmodifiableSet(Set)} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the set items
   * @param set
   *          the set to prevent modification of
   * @return an unmodifiable view of the set
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> unmodifiableSet(@NonNull Set<T> set) {
    return Collections.unmodifiableSet(set);
  }

  /**
   * Provides an unmodifiable list containing the provided list.
   * <p>
   * If the provided list is {@code null}, an empty list will be provided.
   *
   * @param <T>
   *          the Java type of the list items
   * @param list
   *          the list, which may be {@code null}
   * @return an unmodifiable list containing the items
   */
  @NonNull
  public static <T> List<T> listOrEmpty(@Nullable List<T> list) {
    return list == null ? emptyList() : unmodifiableList(list);
  }

  /**
   * Generates a new unmodifiable list containing the provided items.
   * <p>
   * If the provided array is {@code null}, an empty list will be provided.
   *
   * @param <T>
   *          the Java type of the list items
   * @param array
   *          the array of items to use to populate the list, which may be
   *          {@code null}
   * @return an unmodifiable list containing the items
   */
  @SafeVarargs
  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> listOrEmpty(@Nullable T... array) {
    return array == null || array.length == 0 ? emptyList() : unmodifiableList(Arrays.asList(array));
  }

  /**
   * An implementation of {@link Collections#emptyList()} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the list items
   * @return an unmodifiable empty list
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> emptyList() {
    return Collections.emptyList();
  }

  /**
   * An implementation of {@link Collections#unmodifiableList(List)} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the list items
   * @param list
   *          the list to prevent modification of
   * @return an unmodifiable view of the list
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> unmodifiableList(@NonNull List<T> list) {
    return Collections.unmodifiableList(list);
  }

  /**
   * An implementation of {@link Collections#singletonList(Object)} that respects
   * non-nullness.
   *
   * @param <T>
   *          the Java type of the list items
   * @param instance
   *          the singleton item to use
   * @return an unmodifiable list containing the singleton item
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> singletonList(@NonNull T instance) {
    return Collections.singletonList(instance);
  }

  /**
   * An implementation of {@link Collections#emptyMap()} that respects
   * non-nullness.
   *
   * @param <K>
   *          the Java type of the map's keys
   * @param <V>
   *          the Java type of the map's values
   * @return an unmodifiable empty map
   */
  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> emptyMap() {
    return Collections.emptyMap();
  }

  /**
   * An implementation of {@link Collections#singletonMap(Object, Object)} that
   * respects non-nullness.
   *
   * @param <K>
   *          the Java type of the map's keys
   * @param <V>
   *          the Java type of the map's values
   * @param key
   *          the singleton key
   * @param value
   *          the singleton value
   * @return an unmodifiable map containing the singleton entry
   */
  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> singletonMap(@NonNull K key, @NonNull V value) {
    return Collections.singletonMap(key, value);
  }

  /**
   * An implementation of {@link Collections#unmodifiableMap(Map)} that respects
   * non-nullness.
   *
   * @param map
   *          the map to prevent modification of
   * @param <K>
   *          the Java type of the map's keys
   * @param <V>
   *          the Java type of the map's values
   * @return an unmodifiable view of the map
   */
  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> unmodifiableMap(@NonNull Map<K, V> map) {
    return Collections.unmodifiableMap(map);
  }

  private CollectionUtil() {
    // disable construction
  }
}
