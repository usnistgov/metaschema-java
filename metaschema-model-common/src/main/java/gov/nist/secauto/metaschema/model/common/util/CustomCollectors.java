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

package gov.nist.secauto.metaschema.model.common.util;

import org.jetbrains.annotations.NotNull;

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

public final class CustomCollectors {
  private CustomCollectors() {
    // disable
  }

  public static Collector<CharSequence, ?, String> joiningWithOxfordComma(@NotNull String conjunction) {
    return Collectors.collectingAndThen(Collectors.toList(), withOxfordComma(conjunction));
  }

  private static Function<List<CharSequence>, String> withOxfordComma(@NotNull String conjunction) {
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
   * Produce a new stream with duplicates removed based on the provided {@code keyMapper}. When a
   * duplicate key is encountered, the second item is used. The original sequencing is preserved if
   * the input stream is sequential.
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
      @NotNull Stream<V> stream,
      @NotNull Function<? super V, ? extends K> keyMapper) {
    return distinctByKey(stream, keyMapper, (key, value1, value2) -> value2);
  }

  /**
   * Produce a new stream with duplicates removed based on the provided {@code keyMapper}. When a
   * duplicate key is encountered, the provided {@code duplicateHandler} is used to determine which
   * item to keep. The original sequencing is preserved if the input stream is sequential.
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
      @NotNull Stream<V> stream,
      @NotNull Function<? super V, ? extends K> keyMapper,
      @NotNull DuplicateHandler<K, V> duplicateHander) {
    Map<K, V> uniqueRoles = stream
        .collect(CustomCollectors.toMap(
            keyMapper,
            Function.identity(),
            duplicateHander,
            LinkedHashMap::new));
    return uniqueRoles.values().stream();
  }

  public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
      @NotNull Function<? super T, ? extends K> keyMapper,
      @NotNull Function<? super T, ? extends V> valueMapper,
      @NotNull DuplicateHandler<K, V> duplicateHander) {
    return toMap(keyMapper, valueMapper, duplicateHander, HashMap::new);
  }

  public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(
      @NotNull Function<? super T, ? extends K> keyMapper,
      @NotNull Function<? super T, ? extends V> valueMapper,
      @NotNull DuplicateHandler<K, V> duplicateHander,
      Supplier<M> supplier) {
    return Collector.of(
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
            if (oldValue != null) {
              value = duplicateHander.handle(key, oldValue, value);
            }
            map1.put(key, value);
          });
          return map1;
        });
  }

  @FunctionalInterface
  public interface DuplicateHandler<K, V> {
    @NotNull
    V handle(K key, @NotNull V value1, V value2);
  }

  @NotNull
  public static <T> BinaryOperator<T> useFirstMapper() {
    return (value1, value2) -> value1;
  }

  @NotNull
  public static <T> BinaryOperator<T> useLastMapper() {
    return (value1, value2) -> value2;
  }
}
