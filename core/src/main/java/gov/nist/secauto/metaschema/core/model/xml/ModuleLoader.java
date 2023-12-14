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

package gov.nist.secauto.metaschema.core.model.xml;

import gov.nist.secauto.metaschema.core.model.AbstractLoader;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.impl.XmlModule;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMADocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.MetaschemaImportType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides methods to load a Metaschema expressed in XML.
 * <p>
 * Loaded Metaschema instances are cached to avoid the need to load them for
 * every use. Any Metaschema imported is also loaded and cached automatically.
 */
public class ModuleLoader
    extends AbstractLoader<IXmlModule> {
  private boolean resolveEntities; // = false;

  @NonNull
  private final List<IModulePostProcessor> modulePostProcessors;

  /**
   * Construct a new Metaschema loader.
   */
  public ModuleLoader() {
    this(CollectionUtil.emptyList());
  }

  /**
   * Construct a new Metaschema loader, which use the provided module post
   * processors when loading a module.
   *
   * @param modulePostProcessors
   *          post processors to perform additional module customization when
   *          loading
   */
  public ModuleLoader(@NonNull List<IModulePostProcessor> modulePostProcessors) {
    this.modulePostProcessors = CollectionUtil.unmodifiableList(new ArrayList<>(modulePostProcessors));
  }

  /**
   * Get the set of additional constraints associated with this loader.
   *
   * @return the set of constraints
   */
  @NonNull
  protected List<IModulePostProcessor> getModulePostProcessors() {
    return modulePostProcessors;
  }

  /**
   * Enable a mode that allows XML entity resolution. This may be needed to parse
   * some resource files that contain entities. Enabling entity resolution is a
   * less secure, which requires trust in the resource content being parsed.
   */
  public void allowEntityResolution() {
    resolveEntities = true;
  }

  /**
   * Parse the {@code resource} based on the provided {@code xmlObject}.
   *
   * @param resource
   *          the URI of the resource being parsed
   * @param xmlObject
   *          the XML beans object to parse
   * @param importedModules
   *          previously parsed Metaschema modules imported by the provided
   *          {@code resource}
   * @return the parsed resource as a Metaschema module
   * @throws MetaschemaException
   *           if an error occurred while parsing the XML beans object
   */
  protected IXmlModule newXmlMetaschema(
      @NonNull URI resource,
      @NonNull METASCHEMADocument xmlObject,
      @NonNull List<IXmlModule> importedModules) throws MetaschemaException {
    IXmlModule retval = new XmlModule(resource, xmlObject, importedModules);

    for (IModulePostProcessor postProcessor : getModulePostProcessors()) {
      postProcessor.processModule(retval);
    }
    return retval;
  }

  @Override
  protected IXmlModule parseResource(@NonNull URI resource, @NonNull Deque<URI> visitedResources)
      throws IOException {
    // parse this Metaschema module
    METASCHEMADocument xmlObject = parseModule(resource);

    // now check if this Metaschema imports other metaschema
    int size = xmlObject.getMETASCHEMA().sizeOfImportArray();
    @NonNull Map<URI, IXmlModule> importedModules;
    if (size == 0) {
      importedModules = ObjectUtils.notNull(Collections.emptyMap());
    } else {
      try {
        importedModules = new LinkedHashMap<>();
        for (MetaschemaImportType imported : xmlObject.getMETASCHEMA().getImportList()) {
          URI importedResource = URI.create(imported.getHref());
          importedResource = ObjectUtils.notNull(resource.resolve(importedResource));
          importedModules.put(importedResource, loadInternal(importedResource, visitedResources));
        }
      } catch (MetaschemaException ex) {
        throw new IOException(ex);
      }
    }

    // now create this metaschema
    Collection<IXmlModule> values = importedModules.values();
    try {
      return newXmlMetaschema(resource, xmlObject, new ArrayList<>(values));
    } catch (MetaschemaException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Parse the provided XML resource as a Metaschema module.
   *
   * @param resource
   *          the resource to parse
   * @return the XMLBeans representation of the Metaschema module
   * @throws IOException
   *           if a parsing error occurred
   */
  protected METASCHEMADocument parseModule(@NonNull URI resource) throws IOException {
    METASCHEMADocument metaschemaXml;
    try {
      XmlOptions options = new XmlOptions();
      if (resolveEntities) {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
          // factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
          throw new IOException(ex);
        }
        // options.setLoadEntityBytesLimit(204800);
        // options.setLoadUseDefaultResolver();
        options.setEntityResolver(new EntityResolver() {

          @Override
          public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            String effectiveSystemId = systemId;
            // TODO: It's very odd that the system id looks like this. Need to investigate.
            if (effectiveSystemId.startsWith("file://file://")) {
              effectiveSystemId = effectiveSystemId.substring(14);
            }
            URI resolvedSystemId = resource.resolve(effectiveSystemId);
            return new InputSource(resolvedSystemId.toString());
          }

        });
        options.setLoadDTDGrammar(true);
      }
      options.setBaseURI(resource);
      options.setLoadLineNumbers();
      metaschemaXml = METASCHEMADocument.Factory.parse(resource.toURL(), options);
    } catch (XmlException ex) {
      throw new IOException(ex);
    }
    return metaschemaXml;
  }

}
