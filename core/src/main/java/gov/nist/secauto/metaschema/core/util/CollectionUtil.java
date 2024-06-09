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

public final class CollectionUtil {

  private CollectionUtil() {
    // disable construction
  }

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

  @NonNull
  public static <T extends Collection<A>, A> T requireNonEmpty(@NonNull T collection) {
    if (collection.isEmpty()) {
      throw new IllegalStateException();
    }
    return collection;
  }

  @NonNull
  public static <T extends Collection<A>, A> T requireNonEmpty(@NonNull T collection, @NonNull String message) {
    if (collection.isEmpty()) {
      throw new IllegalStateException(message);
    }
    return collection;
  }

  /**
   * A wrapper of the {@link Collections#unmodifiableCollection(Collection)}
   * method that ensure a {@link NonNull} result is returned.
   *
   * @param <T>
   *          the collection's item type
   * @param collection
   *          the collection
   * @return a non-null unmodifiable instance of the provided collection
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Collection<T> unmodifiableCollection(@NonNull Collection<T> collection) {
    return Collections.unmodifiableCollection(collection);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> singleton(@NonNull T value) {
    return Collections.singleton(value);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> emptySet() {
    return Collections.emptySet();
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> Set<T> unmodifiableSet(@NonNull Set<T> set) {
    return Collections.unmodifiableSet(set);
  }

  @NonNull
  public static <T> List<T> listOrEmpty(@Nullable List<T> list) {
    return list == null ? emptyList() : list;
  }

  @SafeVarargs
  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> listOrEmpty(@Nullable T... array) {
    return array == null || array.length == 0 ? emptyList() : Arrays.asList(array);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> emptyList() {
    return Collections.emptyList();
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> unmodifiableList(@NonNull List<T> list) {
    return Collections.unmodifiableList(list);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <T> List<T> singletonList(@NonNull T instance) {
    return Collections.singletonList(instance);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> emptyMap() {
    return Collections.emptyMap();
  }

  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> singletonMap(@NonNull K key, @NonNull V value) {
    return Collections.singletonMap(key, value);
  }

  @SuppressWarnings("null")
  @NonNull
  public static <K, V> Map<K, V> unmodifiableMap(@NonNull Map<K, V> map) {
    return Collections.unmodifiableMap(map);
  }
}
