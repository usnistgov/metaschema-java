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

import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
  private CachingLoader documentLoader;

  @SuppressWarnings("null")
  public DynamicContext(@NotNull StaticContext staticContext) {
    this.staticContext = staticContext;

    Clock clock = Clock.systemDefaultZone();

    this.implicitTimeZone = clock.getZone();
    this.currentDateTime = ZonedDateTime.now(clock);
    this.availableDocuments = new HashMap<>();
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

  public IDocumentLoader getNonCachedDocumentLoader() {
    return documentLoader != null ? documentLoader.getProxiedDocumentLoader() : null;
  }

  public void setDocumentLoader(@NotNull IDocumentLoader documentLoader) {
    this.documentLoader = new CachingLoader(documentLoader);
  }

  private class CachingLoader implements IDocumentLoader {
    @NotNull
    private final IDocumentLoader proxy;

    public CachingLoader(@NotNull IDocumentLoader proxy) {
      this.proxy = proxy;
    }

    protected IDocumentLoader getProxiedDocumentLoader() {
      return proxy;
    }

    @Override
    public synchronized IDocumentNodeItem loadAsNodeItem(@NotNull URL url) throws IOException {
      IDocumentNodeItem retval;
      try {
        retval = availableDocuments.get(url.toURI());
      } catch (URISyntaxException ex) {
        throw new IOException(ex);
      }
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(url);
      }
      return retval;
    }

    @Override
    public synchronized IDocumentNodeItem loadAsNodeItem(File file) throws FileNotFoundException, IOException {
      IDocumentNodeItem retval = availableDocuments.get(file.getCanonicalFile().toURI());
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(file);
      }
      return retval;
    }

    @Override
    public synchronized IDocumentNodeItem loadAsNodeItem(InputStream is, URI documentUri) throws IOException {
      IDocumentNodeItem retval = availableDocuments.get(documentUri);
      if (retval == null) {
        retval = getProxiedDocumentLoader().loadAsNodeItem(is, documentUri);
        availableDocuments.put(documentUri, retval);
      }
      return retval;
    }
  }
}
