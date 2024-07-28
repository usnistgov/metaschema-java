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

package gov.nist.secauto.metaschema.core.model.xml.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractModule;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IMetaschemaModule;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalAssemblyDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFieldDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.GlobalFlagDefinitionType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMADocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.METASCHEMADocument.METASCHEMA;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class XmlModule
    extends AbstractModule<
        IMetaschemaModule,
        IModelDefinition,
        IFlagDefinition,
        IFieldDefinition,
        IAssemblyDefinition>
    implements IMetaschemaModule {
  private static final Logger LOGGER = LogManager.getLogger(XmlModule.class);

  @NonNull
  private final URI location;
  @NonNull
  private final METASCHEMADocument module;
  private final Lazy<Definitions> definitions;

  /**
   * Constructs a new Metaschema instance.
   *
   * @param resource
   *          the resource from which the module was loaded
   * @param moduleXml
   *          the XML source of the module definition bound to Java objects
   * @param importedModules
   *          the modules imported by this module
   * @throws MetaschemaException
   *           if a processing error occurs
   */
  public XmlModule( // NOPMD - unavoidable
      @NonNull URI resource,
      @NonNull METASCHEMADocument moduleXml,
      @NonNull List<? extends IMetaschemaModule> importedModules) throws MetaschemaException {
    super(importedModules);
    this.location = ObjectUtils.requireNonNull(resource, "resource");
    Objects.requireNonNull(moduleXml.getMETASCHEMA());
    this.module = moduleXml;

    this.definitions = Lazy.lazy(() -> new Definitions(ObjectUtils.requireNonNull(module.getMETASCHEMA())));
  }

  @NonNull
  @Override
  public URI getLocation() {
    return location;
  }

  /**
   * Get the XMLBeans representation of the Metaschema module.
   *
   * @return the XMLBean for the Metaschema module
   */
  @NonNull
  protected METASCHEMADocument.METASCHEMA getXmlModule() {
    return ObjectUtils.notNull(module.getMETASCHEMA());
  }

  @SuppressWarnings("null")
  @Override
  public MarkupLine getName() {
    return MarkupStringConverter.toMarkupString(getXmlModule().getSchemaName());
  }

  @SuppressWarnings("null")
  @Override
  public String getVersion() {
    return getXmlModule().getSchemaVersion();
  }

  @SuppressWarnings("null")
  @Override
  public MarkupMultiline getRemarks() {
    return getXmlModule().isSetRemarks() ? MarkupStringConverter.toMarkupString(getXmlModule().getRemarks())
        : null;
  }

  @SuppressWarnings("null")
  @Override
  public String getShortName() {
    return getXmlModule().getShortName();
  }

  @SuppressWarnings("null")
  @Override
  public URI getXmlNamespace() {
    return URI.create(getXmlModule().getNamespace());
  }

  @SuppressWarnings("null")
  @Override
  public URI getJsonBaseUri() {
    return URI.create(getXmlModule().getJsonBaseUri());
  }

  @NonNull
  private Definitions getDefinitions() {
    return ObjectUtils.notNull(definitions.get());
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IAssemblyDefinition> getAssemblyDefinitions() {
    return getDefinitions().getAssemblyDefinitionMap().values();
  }

  @Override
  public IAssemblyDefinition getAssemblyDefinitionByName(@NonNull QName name) {
    return getDefinitions().getAssemblyDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFieldDefinition> getFieldDefinitions() {
    return getDefinitions().getFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getFieldDefinitionByName(@NonNull QName name) {
    return getDefinitions().getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public List<IModelDefinition> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFlagDefinition> getFlagDefinitions() {
    return getDefinitions().getFlagDefinitionMap().values();
  }

  @Override
  public IFlagDefinition getFlagDefinitionByName(@NonNull QName name) {
    return getDefinitions().getFlagDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return getDefinitions().getRootAssemblyDefinitionMap().values();
  }

  private final class Definitions {
    private final Map<QName, IFlagDefinition> flagDefinitions;
    private final Map<QName, IFieldDefinition> fieldDefinitions;
    private final Map<QName, IAssemblyDefinition> assemblyDefinitions;
    private final Map<QName, IAssemblyDefinition> rootAssemblyDefinitions;

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Definitions(@NonNull METASCHEMA metaschemaNode) {

      // handle definitions in this module
      // TODO: switch implementation to use the XmlObjectParser
      try (XmlCursor cursor = metaschemaNode.newCursor()) {
        assert cursor != null;

        this.flagDefinitions = parseFlags(cursor);
        this.fieldDefinitions = parseFields(cursor);
        this.assemblyDefinitions = parseAssemblies(cursor);
        this.rootAssemblyDefinitions = this.assemblyDefinitions.isEmpty()
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(this.assemblyDefinitions.values().stream()
                .filter(IAssemblyDefinition::isRoot)
                .collect(Collectors.toMap(
                    IAssemblyDefinition::getRootXmlQName,
                    Function.identity(),
                    (v1, v2) -> {
                      throw new IllegalStateException(
                          String.format("Duplicate root QName '%s' for root assemblies: %s and %s.",
                              v1.getName(),
                              v2.getName()));
                    },
                    LinkedHashMap::new)));
      }
    }

    @SuppressWarnings({
        "PMD.UseConcurrentHashMap",
        "PMD.AvoidInstantiatingObjectsInLoops"
    })
    private Map<QName, IFlagDefinition> parseFlags(@NonNull XmlCursor cursor) {
      cursor.push();

      // start with flag definitions
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-flag");

      Map<QName, IFlagDefinition> flags = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        GlobalFlagDefinitionType obj = ObjectUtils.notNull((GlobalFlagDefinitionType) cursor.getObject());
        XmlGlobalFlagDefinition flag = new XmlGlobalFlagDefinition(obj, XmlModule.this);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New flag definition '{}'", flag.toCoordinates());
        }
        flags.put(flag.getDefinitionQName(), flag);
      }

      cursor.pop();

      return flags.isEmpty()
          ? Collections.emptyMap()
          : Collections.unmodifiableMap(flags);
    }

    @SuppressWarnings({
        "PMD.UseConcurrentHashMap",
        "PMD.AvoidInstantiatingObjectsInLoops"
    })
    private Map<QName, IFieldDefinition> parseFields(@NonNull XmlCursor cursor) {
      cursor.push();

      // now field definitions
      cursor.selectPath("declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-field");

      Map<QName, IFieldDefinition> fields = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        GlobalFieldDefinitionType obj = ObjectUtils.notNull((GlobalFieldDefinitionType) cursor.getObject());
        XmlGlobalFieldDefinition field = new XmlGlobalFieldDefinition(obj, XmlModule.this);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New field definition '{}'", field.toCoordinates());
        }
        fields.put(field.getDefinitionQName(), field);
      }

      cursor.pop();

      return fields.isEmpty()
          ? Collections.emptyMap()
          : Collections.unmodifiableMap(fields);
    }

    @SuppressWarnings({
        "PMD.UseConcurrentHashMap",
        "PMD.AvoidInstantiatingObjectsInLoops"
    })
    private Map<QName, IAssemblyDefinition> parseAssemblies(XmlCursor cursor) {
      cursor.push();

      // finally assembly definitions
      cursor.selectPath(
          "declare namespace m='http://csrc.nist.gov/ns/oscal/metaschema/1.0';$this/m:define-assembly");

      Map<QName, IAssemblyDefinition> assemblies = new LinkedHashMap<>();
      while (cursor.toNextSelection()) {
        GlobalAssemblyDefinitionType obj = ObjectUtils.notNull((GlobalAssemblyDefinitionType) cursor.getObject());
        XmlGlobalAssemblyDefinition assembly = new XmlGlobalAssemblyDefinition(obj, XmlModule.this);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("New assembly definition '{}'", assembly.toCoordinates());
        }
        assemblies.put(assembly.getDefinitionQName(), assembly);
      }

      cursor.pop();

      return assemblies.isEmpty()
          ? Collections.emptyMap()
          : Collections.unmodifiableMap(assemblies);
    }

    public Map<QName, IFlagDefinition> getFlagDefinitionMap() {
      return flagDefinitions;
    }

    public Map<QName, IFieldDefinition> getFieldDefinitionMap() {
      return fieldDefinitions;
    }

    public Map<QName, IAssemblyDefinition> getAssemblyDefinitionMap() {
      return assemblyDefinitions;
    }

    public Map<QName, ? extends IAssemblyDefinition> getRootAssemblyDefinitionMap() {
      return rootAssemblyDefinitions;
    }

  }
}
