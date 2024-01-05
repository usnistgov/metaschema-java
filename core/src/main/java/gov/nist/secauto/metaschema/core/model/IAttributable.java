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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A marker interface for implementations that support a mapping of a namespaced
 * key to a set of values.
 */
public interface IAttributable {
  @NonNull
  String DEFAULT_PROPERY_NAMESPACE = MetaschemaModelConstants.XML_NAMESPACE;

  @NonNull
  Map<Key, Set<String>> EMPTY = CollectionUtil.emptyMap();

  /**
   * Get the mapping of property key to values.
   *
   * @return the mapping
   */
  @NonNull
  default Map<Key, Set<String>> getProperties() {
    return EMPTY;
  }

  /**
   * Determine if a property is defined.
   *
   * @param key
   *          the key of the property
   * @return {@code true} if the property is defined or {@code false} otherwise
   */
  default boolean hasProperty(@NonNull Key key) {
    return getProperties().containsKey(key);
  }

  /**
   * Get the values associated with a given property key.
   *
   * @param key
   *          the key of the property
   * @return the values or an empty set
   */
  @NonNull
  default Set<String> getPropertyValues(@NonNull Key key) {
    Set<String> retval = getProperties().get(key);
    if (retval == null) {
      retval = CollectionUtil.emptySet();
    }
    return retval;
  }

  /**
   * Determine if a given property, with a given {@code key}, has the identified
   * {@code value}.
   *
   * @param key
   *          the key of the property
   * @param value
   *          the expected property value
   * @return {@code true} if the property value is defined or {@code false}
   *         otherwise
   */
  default boolean hasPropertyValue(@NonNull Key key, @NonNull String value) {
    Set<String> values = getProperties().get(key);
    return values != null && values.contains(value);
  }

  /**
   * Get a property key based on the provided name and namespace.
   *
   * @param name
   *          the name portion of a property key
   * @param namespace
   *          the namespace portion of a property key
   * @return the property key
   */
  @NonNull
  static Key key(@NonNull String name, @NonNull String namespace) {
    return new Key(name, namespace);
  }

  /**
   * Get a property key based on the provided name and the default namespace.
   *
   * @param name
   *          the name portion of a property key
   * @return the property key
   * @see #DEFAULT_PROPERY_NAMESPACE
   */
  @NonNull
  static Key key(@NonNull String name) {
    return new Key(name);
  }

  /**
   * Represents a property key based on a name and a namespace.
   */
  @SuppressWarnings("PMD.ShortClassName")
  final class Key {
    @NonNull
    private final String name;
    @NonNull
    private final String namespace;

    private Key(@NonNull String name) {
      this(name, DEFAULT_PROPERY_NAMESPACE);
    }

    private Key(@NonNull String name, @NonNull String namespace) {
      this.name = name;
      this.namespace = namespace;
    }

    /**
     * Get the property key's name portion.
     *
     * @return the name
     */
    @NonNull
    public String getName() {
      return name;
    }

    /**
     * Get the property key's namespace portion.
     *
     * @return the name
     */
    @NonNull
    public String getNamespace() {
      return namespace;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, namespace);
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Key)) {
        return false;
      }
      Key other = (Key) obj;
      return Objects.equals(name, other.name) && Objects.equals(namespace, other.namespace);
    }
  }
}
