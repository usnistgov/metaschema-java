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
import gov.nist.secauto.metaschema.core.metapath.impl.AbstractMapItem;
import gov.nist.secauto.metaschema.core.metapath.impl.MapItemN;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents a mapping of {@link IMapKey} keys to values.
 *
 * @param <VALUE>
 *          the value type
 */
public interface IMapItem<VALUE extends ICollectionValue>
    extends IFunction, IItem, Map<IMapKey, VALUE>, IPrintable {
  /**
   * Get an empty, immutable map item.
   *
   * @param <V>
   *          the value Java type
   * @return an immutable map item
   */
  @NonNull
  static <V extends ICollectionValue> IMapItem<V> empty() {
    return AbstractMapItem.empty();
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
  Map<IMapKey, VALUE> getValue();

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
  default ISequence<IMapItem<VALUE>> asSequence() {
    return ISequence.of(this);
  }

  /**
   * Get a new, immutable map item that contains the items in the provided map.
   *
   * @param <V>
   *          the value Java type
   * @param map
   *          the map whose items are to be added to the new map
   * @return a map item containing the specified entries
   */
  @NonNull
  static <V extends ICollectionValue> IMapItem<V> ofCollection( // NOPMD - intentional
      @NonNull Map<IMapKey, V> map) {
    return map.isEmpty() ? empty() : new MapItemN<>(map);
  }

  /**
   * Returns an unmodifiable map item containing zero mappings.
   *
   * @param <V>
   *          the value Java type
   * @return an empty {@code IMapItem}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <V extends ICollectionValue> IMapItem<V> of() {
    return AbstractMapItem.empty();
  }

  /**
   * Returns an unmodifiable map item containing a single mapping.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the mapping's key
   * @param v1
   *          the mapping's value
   * @return a map item containing the specified mapping
   * @throws NullPointerException
   *           if the key or the value is {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue> IMapItem<V> of(@NonNull K k1, @NonNull V v1) {
    return new MapItemN<>(entry(k1, v1));
  }

  /**
   * Returns an unmodifiable map item containing two mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if the keys are duplicates
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue> IMapItem<V> of(
      @NonNull K k1, @NonNull V v1,
      @NonNull K k2, @NonNull V v2) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2));
  }

  /**
   * Returns an unmodifiable map item containing three mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3));
  }

  /**
   * Returns an unmodifiable map item containing four mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3,
          @NonNull K k4, @NonNull V v4) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4));
  }

  /**
   * Returns an unmodifiable map item containing five mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3,
          @NonNull K k4, @NonNull V v4,
          @NonNull K k5, @NonNull V v5) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4),
        entry(k5, v5));
  }

  /**
   * Returns an unmodifiable map item containing six mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @param k6
   *          the sixth mapping's key
   * @param v6
   *          the sixth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings({
      "PMD.ExcessiveParameterList",
      "PMD.ShortMethodName"
  })
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3,
          @NonNull K k4, @NonNull V v4,
          @NonNull K k5, @NonNull V v5,
          @NonNull K k6, @NonNull V v6) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4),
        entry(k5, v5),
        entry(k6, v6));
  }

  /**
   * Returns an unmodifiable map item containing seven mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @param k6
   *          the sixth mapping's key
   * @param v6
   *          the sixth mapping's value
   * @param k7
   *          the seventh mapping's key
   * @param v7
   *          the seventh mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings({
      "PMD.ExcessiveParameterList",
      "PMD.ShortMethodName"
  })
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue> IMapItem<V> of(
      @NonNull K k1, @NonNull V v1,
      @NonNull K k2, @NonNull V v2,
      @NonNull K k3, @NonNull V v3,
      @NonNull K k4, @NonNull V v4,
      @NonNull K k5, @NonNull V v5,
      @NonNull K k6, @NonNull V v6,
      @NonNull K k7, @NonNull V v7) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4),
        entry(k5, v5),
        entry(k6, v6),
        entry(k7, v7));
  }

  /**
   * Returns an unmodifiable map item containing eight mappings. See
   * <a href="#unmodifiable">Unmodifiable Maps</a> for details.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @param k6
   *          the sixth mapping's key
   * @param v6
   *          the sixth mapping's value
   * @param k7
   *          the seventh mapping's key
   * @param v7
   *          the seventh mapping's value
   * @param k8
   *          the eighth mapping's key
   * @param v8
   *          the eighth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings({
      "PMD.ExcessiveParameterList",
      "PMD.ShortMethodName"
  })
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3,
          @NonNull K k4, @NonNull V v4,
          @NonNull K k5, @NonNull V v5,
          @NonNull K k6, @NonNull V v6,
          @NonNull K k7, @NonNull V v7,
          @NonNull K k8, @NonNull V v8) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4),
        entry(k5, v5),
        entry(k6, v6),
        entry(k7, v7),
        entry(k8, v8));
  }

  /**
   * Returns an unmodifiable map item containing nine mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @param k6
   *          the sixth mapping's key
   * @param v6
   *          the sixth mapping's value
   * @param k7
   *          the seventh mapping's key
   * @param v7
   *          the seventh mapping's value
   * @param k8
   *          the eighth mapping's key
   * @param v8
   *          the eighth mapping's value
   * @param k9
   *          the ninth mapping's key
   * @param v9
   *          the ninth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings({
      "PMD.ExcessiveParameterList",
      "PMD.ShortMethodName"
  })
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> of(
          @NonNull K k1, @NonNull V v1,
          @NonNull K k2, @NonNull V v2,
          @NonNull K k3, @NonNull V v3,
          @NonNull K k4, @NonNull V v4,
          @NonNull K k5, @NonNull V v5,
          @NonNull K k6, @NonNull V v6,
          @NonNull K k7, @NonNull V v7,
          @NonNull K k8, @NonNull V v8,
          @NonNull K k9, @NonNull V v9) {
    return new MapItemN<>(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4), entry(k5, v5), entry(k6, v6),
        entry(k7, v7), entry(k8, v8), entry(k9, v9));
  }

  /**
   * Returns an unmodifiable map item containing ten mappings.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param k1
   *          the first mapping's key
   * @param v1
   *          the first mapping's value
   * @param k2
   *          the second mapping's key
   * @param v2
   *          the second mapping's value
   * @param k3
   *          the third mapping's key
   * @param v3
   *          the third mapping's value
   * @param k4
   *          the fourth mapping's key
   * @param v4
   *          the fourth mapping's value
   * @param k5
   *          the fifth mapping's key
   * @param v5
   *          the fifth mapping's value
   * @param k6
   *          the sixth mapping's key
   * @param v6
   *          the sixth mapping's value
   * @param k7
   *          the seventh mapping's key
   * @param v7
   *          the seventh mapping's value
   * @param k8
   *          the eighth mapping's key
   * @param v8
   *          the eighth mapping's value
   * @param k9
   *          the ninth mapping's key
   * @param v9
   *          the ninth mapping's value
   * @param k10
   *          the tenth mapping's key
   * @param v10
   *          the tenth mapping's value
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any key or value is {@code null}
   */
  @SuppressWarnings({
      "PMD.ExcessiveParameterList",
      "PMD.ShortMethodName"
  })
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue> IMapItem<V> of(
      @NonNull K k1, @NonNull V v1,
      @NonNull K k2, @NonNull V v2,
      @NonNull K k3, @NonNull V v3,
      @NonNull K k4, @NonNull V v4,
      @NonNull K k5, @NonNull V v5,
      @NonNull K k6, @NonNull V v6,
      @NonNull K k7, @NonNull V v7,
      @NonNull K k8, @NonNull V v8,
      @NonNull K k9, @NonNull V v9,
      @NonNull K k10, @NonNull V v10) {
    return new MapItemN<>(
        entry(k1, v1),
        entry(k2, v2),
        entry(k3, v3),
        entry(k4, v4),
        entry(k5, v5),
        entry(k6, v6),
        entry(k7, v7),
        entry(k8, v8),
        entry(k9, v9),
        entry(k10, v10));
  }

  /**
   * Returns an unmodifiable map item containing keys and values extracted from
   * the given entries. The entries themselves are not stored in the map.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param entries
   *          {@code Map.Entry}s containing the keys and values from which the map
   *          is populated
   * @return a map item containing the specified mappings
   * @throws IllegalArgumentException
   *           if there are any duplicate keys
   * @throws NullPointerException
   *           if any entry, key, or value is {@code null}, or if the
   *           {@code entries} array is {@code null}
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> ofEntries(Map.Entry<IMapKey, ? extends V>... entries) {
    return entries.length == 0 ? empty() : new MapItemN<>(entries);
  }

  /**
   * Returns an unmodifiable {@link Entry} containing the given key and value.
   *
   * @param <V>
   *          the value's type
   * @param key
   *          the key
   * @param value
   *          the value
   * @return an {@code Map.Entry} containing the specified key and value
   * @throws NullPointerException
   *           if the key or value is {@code null}
   */
  @NonNull
  static <V extends ICollectionValue> Map.Entry<IMapKey, V> entry(@NonNull IAnyAtomicItem key, @NonNull V value) {
    return entry(key.asMapKey(), value);
  }

  /**
   * Returns an unmodifiable {@link Entry} containing the given key and value.
   *
   * @param <V>
   *          the value's type
   * @param key
   *          the key
   * @param value
   *          the value
   * @return an {@code Map.Entry} containing the specified key and value
   * @throws NullPointerException
   *           if the key or value is {@code null}
   */
  @SuppressWarnings("null")
  @NonNull
  static <V extends ICollectionValue> Map.Entry<IMapKey, V> entry(@NonNull IMapKey key, @NonNull V value) {
    return Map.entry(key, value);
  }

  /**
   * Returns an unmodifiable Map item containing the entries of the given Map. The
   * given Map must not be null, and it must not contain any null keys or values.
   * If the given Map is subsequently modified, the returned Map will not reflect
   * such modifications.
   *
   * @param <K>
   *          the map item's key type
   * @param <V>
   *          the map item's value type
   * @param map
   *          a map item from which entries are drawn, must be non-null
   * @return a map item containing the entries of the given {@code Map}
   * @throws NullPointerException
   *           if map is null, or if it contains any null keys or values
   */
  @SuppressWarnings("unchecked")
  @NonNull
  static <K extends IAnyAtomicItem, V extends ICollectionValue>
      IMapItem<V> copyOf(Map<? extends IMapKey, ? extends V> map) {
    return map instanceof IMapItem
        ? (IMapItem<V>) map
        : map.isEmpty()
            ? empty()
            : new MapItemN<>(new LinkedHashMap<>(map));
  }
}
