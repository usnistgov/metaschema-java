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

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.metapath.function.DefaultFunction.CallingContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.IUriResolver;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DynamicContext { // NOPMD - intentional data class
  @NonNull
  private final StaticContext staticContext;
  @NonNull
  private final ZoneId implicitTimeZone;
  @NonNull
  private final ZonedDateTime currentDateTime;
  @NonNull
  private final Map<URI, IDocumentNodeItem> availableDocuments;
  private final Map<CallingContext, ISequence<?>> functionResultCache;
  private CachingLoader documentLoader;
  @NonNull
  private final IMutableConfiguration<MetapathEvaluationFeature<?>> configuration;
  @NonNull
  private final Map<String, ISequence<?>> letVariableMap;

  @SuppressWarnings("null")
  public DynamicContext(@NonNull StaticContext staticContext) {
    this.staticContext = staticContext;

    Clock clock = Clock.systemDefaultZone();

    this.implicitTimeZone = clock.getZone();
    this.currentDateTime = ZonedDateTime.now(clock);
    this.availableDocuments = new HashMap<>();
    this.functionResultCache = new HashMap<>();
    this.configuration = new DefaultConfiguration<>();
    this.configuration.enableFeature(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES);
    this.letVariableMap = new ConcurrentHashMap<>();
  }

  @NonNull
  public StaticContext getStaticContext() {
    return staticContext;
  }

  @NonNull
  public ZoneId getImplicitTimeZone() {
    return implicitTimeZone;
  }

  @NonNull
  public ZonedDateTime getCurrentDateTime() {
    return currentDateTime;
  }

  @SuppressWarnings("null")
  @NonNull
  public Map<URI, INodeItem> getAvailableDocuments() {
    return Collections.unmodifiableMap(availableDocuments);
  }

  public IDocumentLoader getDocumentLoader() {
    return documentLoader;
  }

  public void setDocumentLoader(@NonNull IDocumentLoader documentLoader) {
    this.documentLoader = new CachingLoader(documentLoader);
  }

  public ISequence<?> getCachedResult(@NonNull CallingContext callingContext) {
    return functionResultCache.get(callingContext);
  }

  @NonNull
  public DynamicContext disablePredicateEvaluation() {
    this.configuration.disableFeature(MetapathEvaluationFeature.METAPATH_EVALUATE_PREDICATES);
    return this;
  }

  @NonNull
  public IConfiguration<MetapathEvaluationFeature<?>> getConfiguration() {
    return configuration;
  }

  public void cacheResult(@NonNull CallingContext callingContext, @NonNull ISequence<?> result) {
    ISequence<?> old = functionResultCache.put(callingContext, result);
    assert old == null;
  }

  @NonNull
  public ISequence<?> getVariableValue(String name) {
    return ObjectUtils.requireNonNull(letVariableMap.get(name));
  }

  public void setVariableValue(String name, ISequence<?> boundValue) {
    letVariableMap.put(name, boundValue);
  }

  public void clearVariableValue(String name) {
    letVariableMap.remove(name);
  }

  private class CachingLoader implements IDocumentLoader {
    @NonNull
    private final IDocumentLoader proxy;

    public CachingLoader(@NonNull IDocumentLoader proxy) {
      this.proxy = proxy;
    }

    @Override
    public IUriResolver getUriResolver() {
      return new ContextUriResolver();
    }

    @Override
    public void setUriResolver(@NonNull IUriResolver resolver) {
      // we delegate to the document loader proxy, so the resolver should be set there
      throw new UnsupportedOperationException("Set the resolver on the proxy");
    }

    @NonNull
    protected IDocumentLoader getProxiedDocumentLoader() {
      return proxy;
    }

    @Override
    public IDocumentNodeItem loadAsNodeItem(Path path) throws IOException {
      URI uri = path.toUri();
      IDocumentNodeItem retval = availableDocuments.get(uri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(path);
        availableDocuments.put(uri, retval);
      }
      return retval;
    }

    @Override
    public IDocumentNodeItem loadAsNodeItem(URL url) throws IOException, URISyntaxException {
      URI uri = ObjectUtils.notNull(url.toURI());
      IDocumentNodeItem retval = availableDocuments.get(uri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(uri);
        availableDocuments.put(uri, retval);
      }
      return retval;
    }

    @Override
    public IDocumentNodeItem loadAsNodeItem(URI uri) throws IOException {
      IDocumentNodeItem retval = availableDocuments.get(uri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(uri);
        availableDocuments.put(uri, retval);
      }
      return retval;
    }

    @Override
    public @NonNull IDocumentNodeItem loadAsNodeItem(
        @NonNull InputStream is,
        @NonNull URI documentUri) throws IOException {
      throw new UnsupportedOperationException();
      // return getProxiedDocumentLoader().loadAsNodeItem(is, documentUri);
    }

    public class ContextUriResolver implements IUriResolver {

      /**
       * {@inheritDoc}
       * <p>
       * This method first resolves the provided URI against the static context's base
       * URI.
       */
      @Override
      public URI resolve(URI uri) {
        URI baseUri = getStaticContext().getBaseUri();

        URI resolvedUri;
        if (baseUri == null) {
          resolvedUri = uri;
        } else {
          resolvedUri = ObjectUtils.notNull(baseUri.resolve(uri));
        }

        IUriResolver resolver = getProxiedDocumentLoader().getUriResolver();
        return resolver == null ? resolvedUri : resolver.resolve(resolvedUri);
      }
    }
  }
}
