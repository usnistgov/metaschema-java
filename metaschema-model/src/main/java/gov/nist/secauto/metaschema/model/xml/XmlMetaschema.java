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

package gov.nist.secauto.metaschema.model.xml;

import gov.nist.secauto.metaschema.model.IXmlMetaschema;
import gov.nist.secauto.metaschema.model.common.AbstractMetaschema;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.definitions.IXmlAssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlFieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlFlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.IXmlNamedModelDefinition;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.GlobalFlagDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.METASCHEMADocument;
import gov.nist.secauto.metaschema.model.xmlbeans.xml.METASCHEMADocument.METASCHEMA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XmlMetaschema extends AbstractMetaschema implements IXmlMetaschema {
  private static final Logger logger = LogManager.getLogger(XmlMetaschema.class);

  private final METASCHEMADocument metaschema;
  private final Map<String, ? extends IXmlFlagDefinition> flagDefinitions;
  private final Map<String, ? extends IXmlFieldDefinition> fieldDefinitions;
  private final Map<String, ? extends IXmlAssemblyDefinition> assemblyDefinitions;
  private final Map<String, ? extends IXmlAssemblyDefinition> rootAssemblyDefinitions;

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
  public XmlMetaschema(
      @NotNull URI resource,
      @NotNull METASCHEMADocument metaschemaXml,
      @NotNull Map<@NotNull URI, IMetaschema> importedMetaschema) throws MetaschemaException {
    super(resource, importedMetaschema);
    this.metaschema = metaschemaXml;

    METASCHEMA metaschemaNode = metaschema.getMETASCHEMA();

    // handle definitions in this metaschema
    {
      // start with flag definitions
      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" + "$this/m:define-flag");

      Map<String, IXmlFlagDefinition> flagDefinitions = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        @SuppressWarnings("null")
        @NotNull GlobalFlagDefinitionType obj = (GlobalFlagDefinitionType) cursor.getObject();
        IXmlFlagDefinition flag = new XmlGlobalFlagDefinition(obj, this);
        logger.trace("New flag definition '{}'", flag.toCoordinates());
        flagDefinitions.put(flag.getName(), flag);
      }
      this.flagDefinitions
          = flagDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagDefinitions);
    }

    {
      // now field definitions
      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" + "$this/m:define-field");

      Map<String, IXmlFieldDefinition> fieldDefinitions = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        @SuppressWarnings("null")
        @NotNull GlobalFieldDefinitionType obj = (GlobalFieldDefinitionType) cursor.getObject();
        XmlGlobalFieldDefinition field = new XmlGlobalFieldDefinition(obj, this);
        logger.trace("New field definition '{}'", field.toCoordinates());
        fieldDefinitions.put(field.getName(), field);
      }
      this.fieldDefinitions
          = fieldDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldDefinitions);
    }

    {
      // finally assembly definitions
      Map<String, IXmlAssemblyDefinition> assemblyDefinitions = new LinkedHashMap<>();
      Map<String, IXmlAssemblyDefinition> rootAssemblyDefinitions = new LinkedHashMap<>();

      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath(
          "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';" + "$this/m:define-assembly");

      while (cursor.toNextSelection()) {
        @SuppressWarnings("null")
        @NotNull GlobalAssemblyDefinitionType obj = (GlobalAssemblyDefinitionType) cursor.getObject();
        XmlGlobalAssemblyDefinition assembly = new XmlGlobalAssemblyDefinition(obj, this);
        logger.trace("New assembly definition '{}'", assembly.toCoordinates());
        assemblyDefinitions.put(assembly.getName(), assembly);
        if (assembly.isRoot()) {
          rootAssemblyDefinitions.put(assembly.getRootName(), assembly);
        }
      }

      this.assemblyDefinitions
          = assemblyDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyDefinitions);
      this.rootAssemblyDefinitions = rootAssemblyDefinitions.isEmpty() ? Collections.emptyMap()
          : Collections.unmodifiableMap(rootAssemblyDefinitions);
    }

    parseExportedDefinitions();
  }

  @SuppressWarnings("null")
  @Override
  public String getShortName() {
    return metaschema.getMETASCHEMA().getShortName();
  }

  @SuppressWarnings("null")
  @Override
  public URI getXmlNamespace() {
    return URI.create(metaschema.getMETASCHEMA().getNamespace());
  }

  @SuppressWarnings("null")
  @Override
  public Map<String, ? extends IXmlAssemblyDefinition> getAssemblyDefinitionMap() {
    return assemblyDefinitions;
  }

  @Override
  public Map<String, ? extends IXmlFieldDefinition> getFieldDefinitionMap() {
    return fieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public List<? extends IXmlNamedModelDefinition> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  @SuppressWarnings("null")
  @Override
  public Map<String, ? extends IXmlFlagDefinition> getFlagDefinitionMap() {
    return flagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Map<String, ? extends IXmlAssemblyDefinition> getRootAssemblyDefinitionMap() {
    return rootAssemblyDefinitions;
  }
}
