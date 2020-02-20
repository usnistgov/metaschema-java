/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.xml.XmlMetaschema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class MetaschemaLoader {
  private static final Logger logger = LogManager.getLogger(MetaschemaLoader.class);

  private final Set<XmlMetaschema> loadedMetaschema = new LinkedHashSet<>();
  private final Map<URI, XmlMetaschema> metaschemaCache = new LinkedHashMap<>();

  public MetaschemaLoader() {
  }

  public Set<XmlMetaschema> getLoadedMetaschema() {
    return loadedMetaschema.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(loadedMetaschema);
  }

  protected Map<URI, XmlMetaschema> getMetaschemaCache() {
    return metaschemaCache;
  }

  public XmlMetaschema loadXmlMetaschema(File file) throws MetaschemaException, IOException {
    return loadXmlMetaschema(file.toURI());
  }

  /**
   * Loads a Metaschema from the specified URL.
   * 
   * @param url the URL to load the metaschema from
   * @return the loaded Metaschema or {@code null} if the Metaschema was not found
   * @throws MetaschemaException if an error occurred while processing the Metaschema definition
   * @throws IOException if an error occurred reading the Metaschema
   */
  public XmlMetaschema loadXmlMetaschema(URL url) throws MetaschemaException, IOException {
    try {
      URI resource = url.toURI();
      return loadXmlMetaschema(resource);
    } catch (URISyntaxException ex) {
      // this should not happen
      logger.error("Invalid url", ex);
      return null;
    }
  }

  protected XmlMetaschema loadXmlMetaschema(URI resource) throws MetaschemaException, IOException {
    if (!resource.isAbsolute()) {
      throw new IllegalStateException(String.format("The URI '%s' must be absolute.", resource.toString()));
    }
    return XmlMetaschema.loadMetaschema(resource, new Stack<>(), getMetaschemaCache());
  }
}
