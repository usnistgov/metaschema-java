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

package gov.nist.secauto.metaschema.model;

import gov.nist.secauto.metaschema.model.xml.XmlMetaschema;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.ImportDocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.METASCHEMADocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MetaschemaLoader {
  private static final Logger logger = LogManager.getLogger(MetaschemaLoader.class);

  private final Set<XmlMetaschema> loadedMetaschema = new LinkedHashSet<>();
  private final Map<URI, XmlMetaschema> metaschemaCache = new LinkedHashMap<>();
  private boolean resolveEntities = false;

  /**
   * Create a new Metaschema loader.
   */
  public MetaschemaLoader() {
  }

  public void allowEntityResolution() {
    resolveEntities = true;
  }

  /**
   * Retrieve the set of loaded Metaschema.
   * 
   * @return a set of loaded Metaschema
   */
  public Set<XmlMetaschema> getLoadedMetaschema() {
    return loadedMetaschema.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(loadedMetaschema);
  }

  /**
   * Retrieve a mapping of Metaschema resource URIs to loaded Metaschema.
   * 
   * @return the mapping
   */
  protected Map<URI, XmlMetaschema> getMetaschemaCache() {
    return metaschemaCache;
  }

  /**
   * Load a Metaschema from the specified URI.
   * 
   * @param resource
   *          the Metaschema resource to load
   * @return the loaded Metaschema instance for the specified file
   * @throws MetaschemaException
   *           if an error occurred while processing the Metaschema definition
   * @throws IOException
   *           if an error occurred parsing the Metaschema
   */
  public XmlMetaschema loadMetaschema(URI resource) throws MetaschemaException, IOException {
    return loadXmlMetaschema(resource, new Stack<>(), new LinkedHashMap<>());
  }

  /**
   * Load a Metaschema from the specified file.
   * 
   * @param file
   *          the Metaschema to load
   * @return the loaded Metaschema instance for the specified file
   * @throws MetaschemaException
   *           if an error occurred while processing the Metaschema definition
   * @throws IOException
   *           if an error occurred parsing the Metaschema
   */
  public XmlMetaschema loadXmlMetaschema(File file) throws MetaschemaException, IOException {
    return loadXmlMetaschema(file.toURI());
  }

  /**
   * Loads a Metaschema from the specified URL.
   * 
   * @param url
   *          the URL to load the metaschema from
   * @return the loaded Metaschema or {@code null} if the Metaschema was not found
   * @throws MetaschemaException
   *           if an error occurred while processing the Metaschema definition
   * @throws IOException
   *           if an error occurred parsing the Metaschema
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

  /**
   * Load an XML-based Metaschema from the specified URI.
   * 
   * @param resource
   *          the Metaschema resource to load
   * @return the loaded Metaschema instance for the specified file
   * @throws MetaschemaException
   *           if an error occurred while processing the Metaschema definition
   * @throws IOException
   *           if an error occurred parsing the Metaschema
   * @throws IllegalArgumentException
   *           if the provided URI is not absolute
   */
  protected XmlMetaschema loadXmlMetaschema(URI resource) throws MetaschemaException, IOException {
    if (!resource.isAbsolute()) {
      throw new IllegalArgumentException(String.format("The URI '%s' must be absolute.", resource.toString()));
    }
    return loadXmlMetaschema(resource, new Stack<>(), getMetaschemaCache());
  }

  /**
   * Loads a metaschema from the provided resource.
   * <p>
   * If the metaschema imports other metaschema, the provided visitedMetaschema can be used to track
   * circular inclusions. This is useful when this method recurses into included metaschema.
   * <p>
   * Previously loaded metaschema are provided by the metaschemaCache. This method will add the
   * current metaschema to the cache after all imported metaschema have been loaded.
   * 
   * @param resource
   *          the metaschema resource to load
   * @param visitedMetaschema
   *          a LIFO queue representing previously visited metaschema in an import chain
   * @param metaschemaCache
   *          a map of previously processed metaschema, keyed by the resource URI of the metaschema
   * @return the loaded metaschema
   * @throws MetaschemaException
   *           if an error occurred while processing the Metaschema definition
   * @throws MalformedURLException
   *           if the provided URI is malformed
   * @throws IOException
   *           if an error occurred parsing the Metaschema
   */
  protected XmlMetaschema loadXmlMetaschema(URI resource, Stack<URI> visitedMetaschema,
      Map<URI, XmlMetaschema> metaschemaCache) throws MetaschemaException, MalformedURLException, IOException {
    // first check if the current Metaschema has been visited to prevent cycles
    if (visitedMetaschema.contains(resource)) {
      throw new MetaschemaException("Cycle detected in metaschema includes for '" + resource + "'. Call stack: '"
          + visitedMetaschema.stream().map(n -> n.toString()).collect(Collectors.joining(",")));
    }

    XmlMetaschema retval = metaschemaCache.get(resource);
    if (retval == null) {
      logger.info("Loading metaschema '{}'", resource);
      // parse this metaschema
      METASCHEMADocument metaschemaXml;
      try {
        XmlOptions options = new XmlOptions();
        if (resolveEntities) {
          SAXParserFactory factory = SAXParserFactory.newInstance();

          try {
//            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", true);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "file"); // ,jar:file
            XMLReader reader = parser.getXMLReader();
            reader.setEntityResolver(new EntityResolver() {

              @Override
              public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return null;
              }
              
            });
            options.setLoadUseXMLReader(reader);
          } catch (SAXException | ParserConfigurationException ex) {
            throw new MetaschemaException(ex);
          }
//          options.setLoadEntityBytesLimit(204800);
//          options.setLoadUseDefaultResolver();
          options.setEntityResolver(new EntityResolver() {

              @Override
              public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                // It's very odd that the system id looks like this. Need to investigate. 
                if (systemId.startsWith("file://file://")) {
                  systemId = systemId.substring(14);
                }
                URI resolvedSystemId = resource.resolve(systemId);
                return new InputSource(resolvedSystemId.toString());
              }
              
            });
          options.setLoadDTDGrammar(true);
        }
        options.setBaseURI(resource);
        options.setLoadLineNumbers();
        metaschemaXml = (METASCHEMADocument) METASCHEMADocument.Factory.parse(resource.toURL(), options);
      } catch (XmlException e) {
        throw new MetaschemaException(e);
      }

      // now check if this Metaschema imports other metaschema
      int size = metaschemaXml.getMETASCHEMA().sizeOfImportArray();
      Map<URI, Metaschema> importedMetaschema;
      if (size == 0) {
        importedMetaschema = Collections.emptyMap();
      } else {
        visitedMetaschema.push(resource);
        try {
          importedMetaschema = new LinkedHashMap<>();
          for (ImportDocument.Import imported : metaschemaXml.getMETASCHEMA().getImportList()) {
            URI importedResource = URI.create(imported.getHref());
            importedResource = resource.resolve(importedResource);
            importedMetaschema.put(importedResource,
                loadXmlMetaschema(importedResource, visitedMetaschema, metaschemaCache));
          }
        } finally {
          visitedMetaschema.pop();
        }
      }

      // now create this metaschema
      retval = new XmlMetaschema(resource, metaschemaXml, importedMetaschema);
      metaschemaCache.put(resource, retval);
    } else {
      logger.debug("Found metaschema in cache '{}'", resource);
    }
    return retval;
  }
}
