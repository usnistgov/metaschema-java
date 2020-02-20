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

package gov.nist.secauto.metaschema.model.xml;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.itl.metaschema.model.xml.DefineAssemblyDocument;
import gov.nist.itl.metaschema.model.xml.DefineFieldDocument;
import gov.nist.itl.metaschema.model.xml.DefineFlagDocument;
import gov.nist.itl.metaschema.model.xml.ExtensionType;
import gov.nist.itl.metaschema.model.xml.ImportDocument;
import gov.nist.itl.metaschema.model.xml.METASCHEMADocument;
import gov.nist.itl.metaschema.model.xml.METASCHEMADocument.METASCHEMA;
import gov.nist.itl.metaschema.model.xml.METASCHEMADocument.METASCHEMA.Extensions;
import gov.nist.itl.metaschema.model.xml.binding.ModelBindingDocument;
import gov.nist.secauto.metaschema.model.AbstractMetaschema;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.configuration.ModelBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Util;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class XmlMetaschema extends AbstractMetaschema {
  private static final Logger logger = LogManager.getLogger(XmlMetaschema.class);

  public static XmlMetaschema loadMetaschema(URI resource) throws MetaschemaException, IOException {
    return loadMetaschema(resource, new Stack<>(), new LinkedHashMap<>());
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
   *           if a parsing or processing error occurs
   * @throws MalformedURLException
   *           if the provided URI is malformed
   * @throws IOException
   *           if an IO error occurs while parsing
   */
  public static XmlMetaschema loadMetaschema(URI resource, Stack<URI> visitedMetaschema,
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
        metaschemaXml = METASCHEMADocument.Factory.parse(resource.toURL());
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
                loadMetaschema(importedResource, visitedMetaschema, metaschemaCache));
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

  protected static ModelBindingConfiguration getBindingConfiguration(METASCHEMADocument metaschema) {
    ModelBindingConfiguration retval = null;
    METASCHEMA modelMetaschema = metaschema.getMETASCHEMA();
    if (modelMetaschema.isSetExtensions()) {
      Extensions extensions = modelMetaschema.getExtensions();
      for (ExtensionType extensionInstance : extensions.getModelExtensionList()) {
        System.out.println("Extension Class: " + extensionInstance.getClass().getName());
        if (extensionInstance instanceof ModelBindingDocument.ModelBinding) {
          ModelBindingDocument.ModelBinding modelConfig = (ModelBindingDocument.ModelBinding) extensionInstance;
          if (modelConfig.isSetJava()) {
            ModelBindingDocument.ModelBinding.Java modelJava = modelConfig.getJava();
            retval = new ModelBindingConfiguration(modelJava.getPackageName());
            break;
          }
        }
      }
    }

    if (retval == null) {
      retval = ModelBindingConfiguration.NULL_CONFIG;
    }
    return retval;
  }

  private final METASCHEMADocument metaschema;
  private final Map<String, InfoElementDefinition> infoElementDefinitions;
  private final Map<String, XmlFlagDefinition> flagDefinitions;
  private final Map<String, XmlFieldDefinition> fieldDefinitions;
  private final Map<String, XmlAssemblyDefinition> assemblyDefinitions;

  /**
   * Constructs a new Metaschema instance.
   * 
   * @param resource
   *          the resource from which the metaschema was loaded
   * @param metaschemaXml
   *          the XML source of the metaschema definition bound to Java objects
   * @param importedMetaschema
   *          the definitions for any metaschema imported by this metaschema
   * @throws MetaschemaException
   *           if a processing error occurs
   */
  public XmlMetaschema(URI resource, METASCHEMADocument metaschemaXml, Map<URI, Metaschema> importedMetaschema)
      throws MetaschemaException {
    super(resource, getBindingConfiguration(metaschemaXml), importedMetaschema);
    this.metaschema = metaschemaXml;

    XmlCursor cursor = metaschema.getMETASCHEMA().newCursor();
    cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" +
        "$this/m:define-assembly|$this/m:define-field|$this/m:define-flag");

    // handle definitions in this metaschema
    Map<String, InfoElementDefinition> infoElementDefinitions = new LinkedHashMap<>();
    Map<String, XmlFlagDefinition> flagDefinitions = new LinkedHashMap<>();
    Map<String, XmlFieldDefinition> fieldDefinitions = new LinkedHashMap<>();
    Map<String, XmlAssemblyDefinition> assemblyDefinitions = new LinkedHashMap<>();

    while (cursor.toNextSelection()) {
      XmlObject obj = cursor.getObject();
      if (obj instanceof DefineFlagDocument.DefineFlag) {
        XmlFlagDefinition flag = new XmlFlagDefinition((DefineFlagDocument.DefineFlag) obj, this);
        logger.trace("New flag definition '{}'", Util.toCoordinates(flag));
        flagDefinitions.put(flag.getName(), flag);
        infoElementDefinitions.put(flag.getName(), flag);
      } else if (obj instanceof DefineFieldDocument.DefineField) {
        XmlFieldDefinition field = new XmlFieldDefinition((DefineFieldDocument.DefineField) obj, this);
        logger.trace("New field definition '{}'", Util.toCoordinates(field));
        fieldDefinitions.put(field.getName(), field);
        infoElementDefinitions.put(field.getName(), field);
      } else if (obj instanceof DefineAssemblyDocument.DefineAssembly) {
        XmlAssemblyDefinition assembly = new XmlAssemblyDefinition((DefineAssemblyDocument.DefineAssembly) obj, this);
        logger.trace("New assembly definition '{}'", Util.toCoordinates(assembly));
        assemblyDefinitions.put(assembly.getName(), assembly);
        infoElementDefinitions.put(assembly.getName(), assembly);
      }
    }
    this.flagDefinitions
        = flagDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagDefinitions);
    this.fieldDefinitions
        = fieldDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldDefinitions);
    this.assemblyDefinitions
        = assemblyDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyDefinitions);
    this.infoElementDefinitions = infoElementDefinitions.isEmpty() ? Collections.emptyMap()
        : Collections.unmodifiableMap(infoElementDefinitions);

    parseUsedDefinitions();
  }

  @Override
  public String getShortName() {
    return metaschema.getMETASCHEMA().getShortName();
  }

  @Override
  public URI getXmlNamespace() {
    return URI.create(metaschema.getMETASCHEMA().getNamespace());
  }

  @Override
  public String getPackageName() {
    String packageName = getBindingConfiguration().getPackageName();
    if (packageName == null) {
      packageName = NameConverter.standard.toPackageName(getXmlNamespace().toString());
    }
    return packageName;
  }

  @Override
  public Map<String, InfoElementDefinition> getInfoElementDefinitions() {
    return infoElementDefinitions;
  }

  @Override
  public Map<String, XmlAssemblyDefinition> getAssemblyDefinitions() {
    return assemblyDefinitions;
  }

  @Override
  public Map<String, XmlFieldDefinition> getFieldDefinitions() {
    return fieldDefinitions;
  }

  @Override
  public Map<String, XmlFlagDefinition> getFlagDefinitions() {
    return flagDefinitions;
  }

  @Override
  public XmlAssemblyDefinition getRootAssemblyDefinition() {
    return getAssemblyDefinitions().get(metaschema.getMETASCHEMA().getRoot());
  }
}
