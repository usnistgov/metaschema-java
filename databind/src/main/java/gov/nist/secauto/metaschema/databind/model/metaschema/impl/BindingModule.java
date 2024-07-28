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

package gov.nist.secauto.metaschema.databind.model.metaschema.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.AbstractModule;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IMetaschemaModule;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.METASCHEMA;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

public class BindingModule
    extends AbstractModule<
        IMetaschemaModule,
        IModelDefinition,
        IFlagDefinition,
        IFieldDefinition,
        IAssemblyDefinition>
    implements IMetaschemaModule {
  @NonNull
  private final URI location;
  @NonNull
  private final METASCHEMA binding;
  @NonNull
  private final Lazy<IDocumentNodeItem> nodeItem;
  @NonNull
  private final Map<QName, IFlagDefinition> flagDefinitions;
  @NonNull
  private final Map<QName, IFieldDefinition> fieldDefinitions;
  @NonNull
  private final Map<QName, IAssemblyDefinition> assemblyDefinitions;
  @NonNull
  private final Map<QName, IAssemblyDefinition> rootAssemblyDefinitions;

  /**
   * Constructs a new Metaschema instance.
   *
   * @param resource
   *          the resource from which the module was loaded
   * @param rootDefinition
   *          the underlying definition binding for the module
   * @param binding
   *          the module definition object bound to a Java object
   * @param importedModules
   *          the modules imported by this module
   * @throws MetaschemaException
   *           if a processing error occurs
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  public BindingModule( // NOPMD - unavoidable
      @NonNull URI resource,
      @NonNull IBoundDefinitionModelAssembly rootDefinition,
      @NonNull METASCHEMA binding,
      @NonNull List<? extends IMetaschemaModule> importedModules) throws MetaschemaException {
    super(importedModules);
    this.location = ObjectUtils.requireNonNull(resource, "resource");
    this.binding = binding;
    this.flagDefinitions = new LinkedHashMap<>();
    this.fieldDefinitions = new LinkedHashMap<>();
    this.assemblyDefinitions = new LinkedHashMap<>();
    this.rootAssemblyDefinitions = new LinkedHashMap<>();

    // create instance position counters
    int globalFlagPosition = 0;
    int globalFieldPosition = 0;
    int globalAssemblyPosition = 0;

    IBoundInstanceModelChoiceGroup instance = ObjectUtils.requireNonNull(
        rootDefinition.getChoiceGroupInstanceByName("definitions"));
    INodeItemFactory nodeItemFactory = INodeItemFactory.instance();
    for (Object obj : binding.getDefinitions()) {
      IBoundInstanceModelGroupedAssembly objInstance
          = (IBoundInstanceModelGroupedAssembly) instance.getItemInstance(obj);

      if (obj instanceof METASCHEMA.DefineAssembly) {
        IAssemblyDefinition definition = new DefinitionAssemblyGlobal(
            (METASCHEMA.DefineAssembly) obj,
            objInstance,
            globalAssemblyPosition++,
            this,
            nodeItemFactory);
        QName name = definition.getDefinitionQName();
        assemblyDefinitions.put(name, definition);
        if (definition.isRoot()) {
          rootAssemblyDefinitions.put(name, definition);
        }
      } else if (obj instanceof METASCHEMA.DefineField) {
        IFieldDefinition definition = new DefinitionFieldGlobal(
            (METASCHEMA.DefineField) obj,
            objInstance,
            globalFieldPosition++,
            this);
        QName name = definition.getDefinitionQName();
        fieldDefinitions.put(name, definition);
      } else if (obj instanceof METASCHEMA.DefineFlag) {
        IFlagDefinition definition = new DefinitionFlagGlobal(
            (METASCHEMA.DefineFlag) obj,
            objInstance,
            globalFlagPosition++,
            this);
        QName name = definition.getDefinitionQName();
        flagDefinitions.put(name, definition);
      } else {
        throw new IllegalStateException(
            String.format("Unrecognized definition class '%s' in module '%s'.",
                obj.getClass(),
                resource.toASCIIString()));
      }
    }
    this.nodeItem
        = ObjectUtils.notNull(Lazy.lazy(() -> nodeItemFactory.newDocumentNodeItem(rootDefinition, resource, binding)));
  }

  @NonNull
  public METASCHEMA getBinding() {
    return binding;
  }

  @Override
  public IDocumentNodeItem getNodeItem() {
    return ObjectUtils.notNull(nodeItem.get());
  }

  @Override
  @NonNull
  public URI getLocation() {
    return location;
  }

  @Override
  public MarkupLine getName() {
    MarkupLine retval = getBinding().getSchemaName();
    if (retval == null) {
      throw new IllegalStateException(String.format("The schema name is NULL for module '%s'.", getLocation()));
    }
    return retval;
  }

  @Override
  public String getVersion() {
    String retval = getBinding().getSchemaVersion();
    if (retval == null) {
      throw new IllegalStateException(String.format("The schema version is NULL for module '%s'.", getLocation()));
    }
    return retval;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelSupport.remarks(getBinding().getRemarks());
  }

  @Override
  public String getShortName() {
    String retval = getBinding().getShortName();
    if (retval == null) {
      throw new IllegalStateException(String.format("The schema short name is NULL for module '%s'.", getLocation()));
    }
    return retval;
  }

  @Override
  public URI getXmlNamespace() {
    URI retval = getBinding().getNamespace();
    if (retval == null) {
      throw new IllegalStateException(
          String.format("The XML schema namespace is NULL for module '%s'.", getLocation()));
    }
    return retval;
  }

  @Override
  public URI getJsonBaseUri() {
    URI retval = getBinding().getJsonBaseUri();
    if (retval == null) {
      throw new IllegalStateException(String.format("The JSON schema URI is NULL for module '%s'.", getLocation()));
    }
    return retval;
  }

  private Map<QName, IAssemblyDefinition> getAssemblyDefinitionMap() {
    return assemblyDefinitions;
  }

  @Override
  public Collection<IAssemblyDefinition> getAssemblyDefinitions() {
    return ObjectUtils.notNull(getAssemblyDefinitionMap().values());
  }

  @Override
  public IAssemblyDefinition getAssemblyDefinitionByName(@NonNull QName name) {
    return getAssemblyDefinitionMap().get(name);
  }

  private Map<QName, IFieldDefinition> getFieldDefinitionMap() {
    return fieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFieldDefinition> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getFieldDefinitionByName(@NonNull QName name) {
    return getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public List<IModelDefinition> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  private Map<QName, IFlagDefinition> getFlagDefinitionMap() {
    return flagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFlagDefinition> getFlagDefinitions() {
    return getFlagDefinitionMap().values();
  }

  @Override
  public IFlagDefinition getFlagDefinitionByName(@NonNull QName name) {
    return getFlagDefinitionMap().get(name);
  }

  private Map<QName, ? extends IAssemblyDefinition> getRootAssemblyDefinitionMap() {
    return rootAssemblyDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return getRootAssemblyDefinitionMap().values();
  }
}
