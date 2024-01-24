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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// add support for default namespace
/**
 * The implementation of a Metapath
 * <a href="https://www.w3.org/TR/xpath-31/#static_context">static context</a>.
 */
public final class StaticContext {
  @NonNull
  private static final Map<String, URI> WELL_KNOWN_NAMESPACES;

  static {
    Map<String, URI> knownNamespaces = new ConcurrentHashMap<>();
    knownNamespaces.put(
        MetapathConstants.PREFIX_METAPATH,
        MetapathConstants.NS_METAPATH);
    knownNamespaces.put(
        MetapathConstants.PREFIX_XML_SCHEMA,
        MetapathConstants.NS_XML_SCHEMA);
    knownNamespaces.put(
        MetapathConstants.PREFIX_XPATH_FUNCTIONS,
        MetapathConstants.NS_METAPATH_FUNCTIONS);
    knownNamespaces.put(
        MetapathConstants.PREFIX_XPATH_FUNCTIONS_MATH,
        MetapathConstants.NS_METAPATH_FUNCTIONS_MATH);
    WELL_KNOWN_NAMESPACES = CollectionUtil.unmodifiableMap(knownNamespaces);
  }

  @Nullable
  private final URI baseUri;
  @NonNull
  private final Map<String, URI> knownNamespaces;

  /**
   * Get the mapping of prefix to namespace URI for all well-known namespaces
   * provided by default to the static context.
   * <p>
   * These namespaces can be overridden using the
   * {@link Builder#namespace(String, URI)} method.
   *
   * @return the mapping of prefix to namespace URI for all well-known namespaces
   */
  @SuppressFBWarnings("MS_EXPOSE_REP")
  public static Map<String, URI> getWellKnownNamespaces() {
    return WELL_KNOWN_NAMESPACES;
  }

  /**
   * Create a new static context instance using default values.
   *
   * @return a new static context instance
   */
  @NonNull
  public static StaticContext instance() {
    return builder().build();
  }

  /**
   * Create a new static context builder that allows for fine-grained adjustments
   * when creating a new static context.
   *
   * @return a new builder
   */
  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  private StaticContext(
      @Nullable URI baseUri,
      @NonNull Map<String, URI> knownNamespaces) {
    this.baseUri = baseUri;
    this.knownNamespaces = knownNamespaces;
  }

  /**
   * Get the static base URI to use in resolving URIs handled by the Metapath
   * processor. This URI, if provided, will be used when a document base URI is
   * not available.
   *
   * @return the base URI or {@code null} if not defined
   */
  @Nullable
  public URI getBaseUri() {
    synchronized (this) {
      return baseUri;
    }
  }

  /**
   * Get the namespace URI associated with the provided {@code prefix}, if any is
   * bound.
   * <p>
   * This method uses the namespaces set by the
   * {@link Builder#namespace(String, URI)} method, falling back to the well-known
   * namespace bindings when a prefix match is not found.
   * <p>
   * The well-known namespace bindings can be retrieved using the
   * {@link StaticContext#getWellKnownNamespaces()} method.
   *
   * @param prefix
   *          the namespace prefix
   * @return the namespace URI bound to the prefix, or {@code null} if no
   *         namespace is bound to the prefix
   * @see Builder#namespace(String, URI)
   * @see #getWellKnownNamespaces()
   */
  @Nullable
  public URI lookupNamespaceURIForPrefix(@NonNull String prefix) {
    URI retval = knownNamespaces.get(prefix);
    if (retval == null) {
      // fall back to well-known namespaces
      retval = WELL_KNOWN_NAMESPACES.get(prefix);
    }
    return retval;
  }

  /**
   * Get the namespace associated with the provided {@code prefix} as a string, if
   * any is bound.
   *
   * @param prefix
   *          the namespace prefix
   * @return the namespace string bound to the prefix, or {@code null} if no
   *         namespace is bound to the prefix
   */
  @Nullable
  public String lookupNamespaceForPrefix(@NonNull String prefix) {
    URI result = lookupNamespaceURIForPrefix(prefix);
    return result == null ? null : result.toASCIIString();
  }

  /**
   * Generate a new dynamic context.
   *
   * @return the generated dynamic context
   */
  @NonNull
  public DynamicContext dynamicContext() {
    return new DynamicContext(this);
  }

  /**
   * A builder used to generate the static context.
   */
  public static final class Builder {
    private URI baseUri;
    @NonNull
    private final Map<String, URI> namespaces = new ConcurrentHashMap<>();

    private Builder() {
      namespaces.put(
          MetapathConstants.PREFIX_METAPATH,
          MetapathConstants.NS_METAPATH);
      namespaces.put(
          MetapathConstants.PREFIX_XML_SCHEMA,
          MetapathConstants.NS_XML_SCHEMA);
      namespaces.put(
          MetapathConstants.PREFIX_XPATH_FUNCTIONS,
          MetapathConstants.NS_METAPATH_FUNCTIONS);
      namespaces.put(
          MetapathConstants.PREFIX_XPATH_FUNCTIONS_MATH,
          MetapathConstants.NS_METAPATH_FUNCTIONS_MATH);
    }

    /**
     * Sets the static base URI to use in resolving URIs handled by the Metapath
     * processor, when a document base URI is not available. There is only a single
     * base URI. Subsequent calls to this method will change the base URI.
     *
     * @param uri
     *          the base URI to use
     * @return this builder
     */
    @NonNull
    public Builder baseUri(@NonNull URI uri) {
      this.baseUri = uri;
      return this;
    }

    /**
     * Adds a new prefix to namespace URI binding to the mapping of
     * <a href="https://www.w3.org/TR/xpath-31/#dt-static-namespaces">statically
     * known namespaces</a>.
     * <p>
     * A namespace set by this method can be resolved using the
     * {@link StaticContext#lookupNamespaceForPrefix(String)} method.
     * <p>
     * Well-known namespace bindings are used by default, which can be retrieved
     * using the {@link StaticContext#getWellKnownNamespaces()} method.
     *
     * @param prefix
     *          the prefix to associate with the namespace, which may be
     * @param uri
     *          the namespace URI
     * @return this builder
     * @see StaticContext#lookupNamespaceForPrefix(String)
     * @see StaticContext#lookupNamespaceURIForPrefix(String)
     * @see StaticContext#getWellKnownNamespaces()
     */
    @NonNull
    public Builder namespace(@NonNull String prefix, @NonNull URI uri) {
      this.namespaces.put(prefix, uri);
      return this;
    }

    /**
     * Construct a new static context using the information provided to the builder.
     *
     * @return the new static context
     */
    @NonNull
    public StaticContext build() {
      return new StaticContext(
          baseUri,
          CollectionUtil.unmodifiableMap(namespaces));
    }
  }
}
