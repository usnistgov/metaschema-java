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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class CustomCollectors {
  /**
   * An implementation of {@link Function#identity()} that respects non-nullness.
   *
   * @param <T>
   *          the Java type of the identity object
   * @return the identity function
   */
  @SuppressWarnings("null")
  @NonNull
  public static <T> Function<T, T> identity() {
    return Function.identity();
  }

  /**
   * Joins a sequence of string values using oxford-style serial commas.
   *
   * @param conjunction
   *          the conjunction to use after the penultimate comma (e.g., and, or)
   * @return a collector that will perform the joining
   */
  public static Collector<CharSequence, ?, String> joiningWithOxfordComma(@NonNull String conjunction) {
    return Collectors.collectingAndThen(Collectors.toList(), withOxfordComma(conjunction));
  }

  private static Function<List<CharSequence>, String> withOxfordComma(@NonNull String conjunction) {
    return list -> {
      int size = list.size();
      if (size < 2) {
        return String.join("", list);
      }
      if (size == 2) {
        return String.join(" " + conjunction + " ", list);
      }
      // else there are 3 or more
      int last = size - 1;
      return String.join(", " + conjunction + " ",
          String.join(", ", list.subList(0, last)),
          list.get(last));
    };
  }

  /**
   * Produce a new stream with duplicates removed based on the provided
   * {@code keyMapper}. When a duplicate key is encountered, the second item is
   * used. The original sequencing is preserved if the input stream is sequential.
   *
   * @param <V>
   *          the item value for the streams
   * @param <K>
   *          the key type
   * @param stream
   *          the stream to reduce
   * @param keyMapper
   *          the key function to use to find unique items
   * @return a new stream
   */
  public static <V, K> Stream<V> distinctByKey(
      @NonNull Stream<V> stream,
      @NonNull Function<? super V, ? extends K> keyMapper) {
    return distinctByKey(stream, keyMapper, (key, value1, value2) -> value2);
  }

  /**
   * Produce a new stream with duplicates removed based on the provided
   * {@code keyMapper}. When a duplicate key is encountered, the provided
   * {@code duplicateHandler} is used to determine which item to keep. The
   * original sequencing is preserved if the input stream is sequential.
   *
   * @param <V>
   *          the item value for the streams
   * @param <K>
   *          the key type
   * @param stream
   *          the stream to reduce
   * @param keyMapper
   *          the key function to use to find unique items
   * @param duplicateHander
   *          used to determine which of two duplicates to keep
   * @return a new stream
   */
  public static <V, K> Stream<V> distinctByKey(
      @NonNull Stream<V> stream,
      @NonNull Function<? super V, ? extends K> keyMapper,
      @NonNull DuplicateHandler<K, V> duplicateHander) {
    Map<K, V> uniqueRoles = stream
        .collect(toMap(
            keyMapper,
            identity(),
            duplicateHander,
            LinkedHashMap::new));
    return uniqueRoles.values().stream();
  }

  /**
   * Produces a map collector that uses the provided key and value mappers, and a
   * duplicate hander to manage duplicate key insertion.
   *
   * @param <T>
   *          the item Java type
   * @param <K>
   *          the map key Java type
   * @param <V>
   *          the map value Java type
   * @param keyMapper
   *          the function used to produce the map's key based on the provided
   *          item
   * @param valueMapper
   *          the function used to produce the map's value based on the provided
   *          item
   * @param duplicateHander
   *          the handler used to manage duplicate key insertion
   * @return the collector
   */
  @NonNull
  public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
      @NonNull Function<? super T, ? extends K> keyMapper,
      @NonNull Function<? super T, ? extends V> valueMapper,
      @NonNull DuplicateHandler<K, V> duplicateHander) {
    return toMap(keyMapper, valueMapper, duplicateHander, HashMap::new);
  }

  /**
   * Produces a map collector that uses the provided key and value mappers, and a
   * duplicate hander to manage duplicate key insertion.
   *
   * @param <T>
   *          the item Java type
   * @param <K>
   *          the map key Java type
   * @param <V>
   *          the map value Java type
   * @param <M>
   *          the Java type of the resulting map
   * @param keyMapper
   *          the function used to produce the map's key based on the provided
   *          item
   * @param valueMapper
   *          the function used to produce the map's value based on the provided
   *          item
   * @param duplicateHander
   *          the handler used to manage duplicate key insertion
   * @param supplier
   *          the supplier used to create the resulting map
   * @return the collector
   */
  @NonNull
  public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(
      @NonNull Function<? super T, ? extends K> keyMapper,
      @NonNull Function<? super T, ? extends V> valueMapper,
      @NonNull DuplicateHandler<K, V> duplicateHander,
      Supplier<M> supplier) {
    return ObjectUtils.notNull(
        Collector.of(
            supplier,
            (map, item) -> {
              K key = keyMapper.apply(item);
              V value = Objects.requireNonNull(valueMapper.apply(item));
              V oldValue = map.get(key);
              if (oldValue != null) {
                value = duplicateHander.handle(key, oldValue, value);
              }
              map.put(key, value);
            },
            (map1, map2) -> {
              map2.forEach((key, value) -> {
                V oldValue = map1.get(key);
                V newValue = value;
                if (oldValue != null) {
                  newValue = duplicateHander.handle(key, oldValue, value);
                }
                map1.put(key, newValue);
              });
              return map1;
            }));
  }

  /**
   * A handler that supports resolving duplicate keys while inserting values into
   * a map.
   *
   * @param <K>
   *          the Java type of the map's keys
   * @param <V>
   *          the Java type of the map's values
   */
  @FunctionalInterface
  public interface DuplicateHandler<K, V> {
    /**
     * The handler callback.
     *
     * @param key
     *          the duplicate key
     * @param value1
     *          the first value associated with the key
     * @param value2
     *          the second value associated with the key
     * @return the value to insert into the map
     */
    @NonNull
    V handle(K key, @NonNull V value1, V value2);
  }

  /**
   * A binary operator that will always use the first of two values.
   *
   * @param <T>
   *          the item type
   * @return the operator
   */
  @NonNull
  public static <T> BinaryOperator<T> useFirstMapper() {
    return (value1, value2) -> value1;
  }

  /**
   * A binary operator that will always use the second of two values.
   *
   * @param <T>
   *          the item type
   * @return the operator
   */
  @NonNull
  public static <T> BinaryOperator<T> useLastMapper() {
    return (value1, value2) -> value2;
  }

  private CustomCollectors() {
    // disable construction
  }
}
