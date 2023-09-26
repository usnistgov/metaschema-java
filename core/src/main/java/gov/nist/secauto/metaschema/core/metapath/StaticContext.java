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

public final class StaticContext {
  @Nullable
  private final URI baseUri;
  @NonNull
  private final Map<String, URI> knownNamespaces;

  @NonNull
  public static StaticContext newInstance() {
    return builder().build();
  }

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

  @Nullable
  public URI getUriForPrefix(@NonNull String prefix) {
    return knownNamespaces.get(prefix);
  }

  /**
   * Generate a new dynamic context.
   *
   * @return the generated dynamic context
   */
  @NonNull
  public DynamicContext newDynamicContext() {
    return new DynamicContext(this);
  }

  public static class Builder {
    private URI baseUri;
    @NonNull
    private final Map<String, URI> knownNamespaces = new ConcurrentHashMap<>();

    private Builder() {
      knownNamespaces.put(
          MetapathConstants.PREFIX_METAPATH,
          MetapathConstants.NS_METAPATH);
      knownNamespaces.put(
          MetapathConstants.PREFIX_XML_SCHEMA,
          MetapathConstants.NS_XML_SCHEMA);
      knownNamespaces.put(
          MetapathConstants.PREFIX_XPATH_FUNCTIONS,
          MetapathConstants.NS_XPATH_FUNCTIONS);
      knownNamespaces.put(
          MetapathConstants.PREFIX_XPATH_FUNCTIONS_MATH,
          MetapathConstants.NS_XPATH_FUNCTIONS_MATH);
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

    @NonNull
    public Builder namespace(@NonNull String prefix, @NonNull URI uri) {
      this.knownNamespaces.put(prefix, uri);
      return this;
    }

    @NonNull
    public StaticContext build() {
      return new StaticContext(
          baseUri,
          CollectionUtil.unmodifiableMap(knownNamespaces));
    }
  }
}
