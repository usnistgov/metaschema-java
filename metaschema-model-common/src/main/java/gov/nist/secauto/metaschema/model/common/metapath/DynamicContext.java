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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.DefaultFunction.CallingContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynamicContext {
  @NotNull
  private final StaticContext staticContext;
  @NotNull
  private final ZoneId implicitTimeZone;
  @NotNull
  private final ZonedDateTime currentDateTime;
  @NotNull
  private final Map<@NotNull URI, IDocumentNodeItem> availableDocuments;
  private final Map<@NotNull CallingContext, ISequence<?>> functionResultCache;
  private CachingLoader documentLoader;

  @SuppressWarnings("null")
  public DynamicContext(@NotNull StaticContext staticContext) {
    this.staticContext = staticContext;

    Clock clock = Clock.systemDefaultZone();

    this.implicitTimeZone = clock.getZone();
    this.currentDateTime = ZonedDateTime.now(clock);
    this.availableDocuments = new HashMap<>();
    this.functionResultCache = new HashMap<>();
  }

  @NotNull
  public StaticContext getStaticContext() {
    return staticContext;
  }

  @NotNull
  public ZoneId getImplicitTimeZone() {
    return implicitTimeZone;
  }

  @NotNull
  public ZonedDateTime getCurrentDateTime() {
    return currentDateTime;
  }

  @SuppressWarnings("null")
  @NotNull
  public Map<@NotNull URI, INodeItem> getAvailableDocuments() {
    return Collections.unmodifiableMap(availableDocuments);
  }

  public IDocumentLoader getDocumentLoader() {
    return documentLoader;
  }

  public void setDocumentLoader(@NotNull IDocumentLoader documentLoader) {
    this.documentLoader = new CachingLoader(documentLoader);
  }

  public ISequence<?> getCachedResult(@NotNull CallingContext callingContext) {
    return functionResultCache.get(callingContext);
  }

  public void cacheResult(@NotNull CallingContext callingContext, @NotNull ISequence<?> result) {
    ISequence<?> old = functionResultCache.put(callingContext, result);
    assert old == null;
  }

  private class CachingLoader implements IDocumentLoader {
    @NotNull
    private final IDocumentLoader proxy;

    public CachingLoader(@NotNull IDocumentLoader proxy) {
      this.proxy = proxy;
    }

    @Override
    public @Nullable EntityResolver getEntityResolver() {
      return new ContextEntityResolver();
    }

    @Override
    public EntityResolver setEntityResolver(@NotNull EntityResolver resolver) {
      // we delegate to the document loader proxy, so the resolver should be set there
      throw new UnsupportedOperationException("Set the resolver on the proxy");
    }

    protected IDocumentLoader getProxiedDocumentLoader() {
      return proxy;
    }

    @Override
    public @NotNull IDocumentNodeItem loadAsNodeItem(@NotNull InputSource source) throws IOException {
      String systemId = source.getSystemId();
      URI uri = ObjectUtils.notNull(URI.create(systemId));
      IDocumentNodeItem retval = availableDocuments.get(uri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(source);
        availableDocuments.put(uri, retval);
      }
      return retval;
    }

    public class ContextEntityResolver implements EntityResolver {

      /**
       * Provides an {@link InputSource} for the provided {@code systemId} after attempting to resolve
       * this system identifier.
       * <p>
       * This implementation of an {@link EntityResolver} will perform the following operations in order:
       * <ol>
       * <li>Resolves the {@code systemId} against the base URI provided by the
       * {@link StaticContext#getBaseUri()} method, if this method returns a non-{@code null} result, to
       * get a localized resource identifier.</li>
       * <li>It will then delegate to the EntityResolver provided by the
       * {@link IDocumentLoader#getEntityResolver()} method, if the result is not-{@code null}, to get the
       * {@link InputSource}.</li>
       * <li>If no InputSource is provided by the previous step, then an InputSource will be created from
       * the URI resolved in the first step, if possible.
       * <li>If an InputSource is still not provided, then an InputSource will be created from the
       * provided {@code systemId}.
       * </ol>
       */
      @Override
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        URI baseUri = getStaticContext().getBaseUri();

        String uri;
        if (baseUri == null) {
          uri = systemId;
        } else {
          URI resolvedUri = baseUri.resolve(systemId);
          uri = resolvedUri.toASCIIString();
        }

        EntityResolver resolver = getProxiedDocumentLoader().getEntityResolver();
        InputSource retval = resolver == null ? null : resolver.resolveEntity(null, uri);
        if (retval == null) {
          retval = new InputSource(uri);
        }

        return retval;
      }

    }
  }
}
