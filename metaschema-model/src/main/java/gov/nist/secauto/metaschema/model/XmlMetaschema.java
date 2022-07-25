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

import gov.nist.secauto.metaschema.model.common.AbstractMetaschema;
import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IModelDefinition;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.GlobalFlagDefinitionType;
import gov.nist.secauto.metaschema.model.xmlbeans.METASCHEMADocument;
import gov.nist.secauto.metaschema.model.xmlbeans.METASCHEMADocument.METASCHEMA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class XmlMetaschema
    extends AbstractMetaschema {
  private static final Logger LOGGER = LogManager.getLogger(XmlMetaschema.class);

  @NotNull
  private final URI location;
  @NotNull
  private final METASCHEMADocument metaschema;
  private final Map<@NotNull String, ? extends IFlagDefinition> flagDefinitions;
  private final Map<@NotNull String, ? extends IFieldDefinition> fieldDefinitions;
  private final Map<@NotNull String, ? extends IAssemblyDefinition> assemblyDefinitions;
  private final Map<@NotNull String, ? extends IAssemblyDefinition> rootAssemblyDefinitions;

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
  XmlMetaschema( // NOPMD - unavoidable
      @NotNull URI resource,
      @NotNull METASCHEMADocument metaschemaXml,
      @NotNull List<@NotNull IMetaschema> importedMetaschema) throws MetaschemaException {
    super(importedMetaschema);
    this.location = ObjectUtils.requireNonNull(resource, "resource");
    this.metaschema = metaschemaXml;

    METASCHEMA metaschemaNode = metaschema.getMETASCHEMA();

    // handle definitions in this metaschema
    {
      // start with flag definitions
      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-flag");

      Map<@NotNull String, IFlagDefinition> flagDefinitions = new LinkedHashMap<>(); // NOPMD - intentional
      while (cursor.toNextSelection()) {
        GlobalFlagDefinitionType obj = ObjectUtils.notNull((GlobalFlagDefinitionType) cursor.getObject());
        XmlGlobalFlagDefinition flag = new XmlGlobalFlagDefinition(obj, this); // NOPMD - intentional
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New flag definition '{}'", flag.toCoordinates());
        }
        flagDefinitions.put(flag.getName(), flag);
      }
      this.flagDefinitions
          = flagDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flagDefinitions);
    }

    {
      // now field definitions
      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-field");

      Map<@NotNull String, IFieldDefinition> fieldDefinitions = new LinkedHashMap<>(); // NOPMD - intentional
      while (cursor.toNextSelection()) {
        GlobalFieldDefinitionType obj = ObjectUtils.notNull((GlobalFieldDefinitionType) cursor.getObject());
        XmlGlobalFieldDefinition field = new XmlGlobalFieldDefinition(obj, this); // NOPMD - intentional
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New field definition '{}'", field.toCoordinates());
        }
        fieldDefinitions.put(field.getName(), field);
      }
      this.fieldDefinitions
          = fieldDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldDefinitions);
    }

    {
      // finally assembly definitions
      Map<@NotNull String, IAssemblyDefinition> assemblyDefinitions = new LinkedHashMap<>(); // NOPMD - intentional
      Map<@NotNull String, IAssemblyDefinition> rootAssemblyDefinitions = new LinkedHashMap<>(); // NOPMD - intentional

      XmlCursor cursor = metaschemaNode.newCursor();
      cursor.selectPath(
          "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-assembly");

      while (cursor.toNextSelection()) {
        GlobalAssemblyDefinitionType obj = ObjectUtils.notNull((GlobalAssemblyDefinitionType) cursor.getObject());
        XmlGlobalAssemblyDefinition assembly = new XmlGlobalAssemblyDefinition(obj, this); // NOPMD - intentional
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New assembly definition '{}'", assembly.toCoordinates());
        }
        assemblyDefinitions.put(assembly.getName(), assembly);
        if (assembly.isRoot()) {
          rootAssemblyDefinitions.put(ObjectUtils.notNull(assembly.getRootName()), assembly);
        }
      }

      this.assemblyDefinitions
          = assemblyDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyDefinitions);
      this.rootAssemblyDefinitions = rootAssemblyDefinitions.isEmpty() ? Collections.emptyMap()
          : Collections.unmodifiableMap(rootAssemblyDefinitions);
    }
  }

  @NotNull
  @Override
  public URI getLocation() {
    return location;
  }

  /**
   * Get the XMLBeans representation of the Metaschema.
   * 
   * @return the XMLBean for the Metaschema
   */
  @NotNull
  protected METASCHEMADocument.METASCHEMA getXmlMetaschema() {
    return metaschema.getMETASCHEMA();
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getName() {
    return MarkupStringConverter.toMarkupString(getXmlMetaschema().getSchemaName());
  }

  @SuppressWarnings("null")
  @Override
  public String getVersion() {
    return getXmlMetaschema().getSchemaVersion();
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlMetaschema().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlMetaschema().getRemarks())
        : null;
  }

  @SuppressWarnings("null")
  @Override
  public String getShortName() {
    return getXmlMetaschema().getShortName();
  }

  @SuppressWarnings("null")
  @Override
  public URI getXmlNamespace() {
    return URI.create(getXmlMetaschema().getNamespace());
  }

  @SuppressWarnings("null")
  @Override
  public URI getJsonBaseUri() {
    return URI.create(getXmlMetaschema().getJsonBaseUri());
  }

  private Map<@NotNull String, ? extends IAssemblyDefinition> getAssemblyDefinitionMap() {
    return assemblyDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IAssemblyDefinition> getAssemblyDefinitions() {
    return getAssemblyDefinitionMap().values();
  }

  @Override
  public IAssemblyDefinition getAssemblyDefinitionByName(@NotNull String name) {
    return getAssemblyDefinitionMap().get(name);
  }

  private Map<@NotNull String, ? extends IFieldDefinition> getFieldDefinitionMap() {
    return fieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFieldDefinition> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getFieldDefinitionByName(@NotNull String name) {
    return getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends IModelDefinition> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  private Map<@NotNull String, ? extends IFlagDefinition> getFlagDefinitionMap() {
    return flagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IFlagDefinition> getFlagDefinitions() {
    return getFlagDefinitionMap().values();
  }

  @Override
  public IFlagDefinition getFlagDefinitionByName(@NotNull String name) {
    return getFlagDefinitionMap().get(name);
  }

  private Map<@NotNull String, ? extends IAssemblyDefinition> getRootAssemblyDefinitionMap() {
    return rootAssemblyDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return getRootAssemblyDefinitionMap().values();
  }
}
