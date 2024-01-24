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
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionModelField;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingModule;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public class BindingModule
    extends AbstractModule<
        IBindingModule,
        IBindingDefinitionModel,
        IBindingDefinitionFlag,
        IBindingDefinitionModelField,
        IBindingDefinitionAssembly>
    implements IBindingModule {
  @NonNull
  private final URI location;
  @NonNull
  private final METASCHEMA binding;
  @NonNull
  private final Lazy<IDocumentNodeItem> nodeItem;
  @NonNull
  private final Map<String, IDefinition> definitions;
  @NonNull
  private final Map<String, IBindingDefinitionFlag> flagDefinitions;
  @NonNull
  private final Map<String, IBindingDefinitionModelField> fieldDefinitions;
  @NonNull
  private final Map<String, IBindingDefinitionAssembly> assemblyDefinitions;
  @NonNull
  private final Map<String, IBindingDefinitionAssembly> rootAssemblyDefinitions;

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
  public BindingModule( // NOPMD - unavoidable
      @NonNull URI resource,
      @NonNull IBoundDefinitionModelAssembly rootDefinition,
      @NonNull METASCHEMA binding,
      @NonNull List<IBindingModule> importedModules) throws MetaschemaException {
    super(importedModules);
    this.location = ObjectUtils.requireNonNull(resource, "resource");
    this.binding = binding;
    this.definitions = new LinkedHashMap<>();
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
        IBindingDefinitionAssembly definition = new DefinitionAssemblyGlobal(
            (METASCHEMA.DefineAssembly) obj,
            objInstance,
            globalAssemblyPosition++,
            this,
            nodeItemFactory);
        String name = definition.getName();
        definitions.put(name, definition);
        assemblyDefinitions.put(name, definition);
        if (definition.isRoot()) {
          rootAssemblyDefinitions.put(name, definition);
        }
      } else if (obj instanceof METASCHEMA.DefineField) {
        IBindingDefinitionModelField definition = new DefinitionFieldGlobal(
            (METASCHEMA.DefineField) obj,
            objInstance,
            globalFieldPosition++,
            this);
        String name = definition.getName();
        definitions.put(name, definition);
        fieldDefinitions.put(name, definition);
      } else if (obj instanceof METASCHEMA.DefineFlag) {
        IBindingDefinitionFlag definition = new DefinitionFlagGlobal(
            (METASCHEMA.DefineFlag) obj,
            objInstance,
            globalFlagPosition++,
            this);
        String name = definition.getName();
        definitions.put(name, definition);
        flagDefinitions.put(name, definition);
      } else {
        throw new IllegalStateException(
            String.format("Unrecognized definition class '%s' in module '%s'.",
                obj.getClass(),
                resource.toASCIIString()));
      }
    }
    this.nodeItem = ObjectUtils.notNull(Lazy.lazy(() -> {
      return nodeItemFactory.newDocumentNodeItem(rootDefinition, resource, binding);
    }));
  }

  @Override
  @NonNull
  public METASCHEMA getBinding() {
    return binding;
  }

  @Override
  public IDocumentNodeItem getBoundNodeItem() {
    return ObjectUtils.notNull(nodeItem.get());
  }

  @Override
  @NonNull
  public URI getLocation() {
    return location;
  }

  @Override
  public MarkupLine getName() {
    return ObjectUtils.requireNonNull(getBinding().getSchemaName());
  }

  @Override
  public String getVersion() {
    return ObjectUtils.requireNonNull(getBinding().getSchemaVersion());
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelSupport.remarks(getBinding().getRemarks());
  }

  @Override
  public String getShortName() {
    return ObjectUtils.requireNonNull(getBinding().getShortName());
  }

  @Override
  public URI getXmlNamespace() {
    return ObjectUtils.requireNonNull(getBinding().getNamespace());
  }

  @Override
  public URI getJsonBaseUri() {
    return ObjectUtils.requireNonNull(getBinding().getJsonBaseUri());
  }

  @NonNull
  public Collection<IDefinition> getDefinitions() {
    return ObjectUtils.notNull(definitions.values());
  }

  private Map<String, IBindingDefinitionAssembly> getAssemblyDefinitionMap() {
    return assemblyDefinitions;
  }

  @Override
  public Collection<IBindingDefinitionAssembly> getAssemblyDefinitions() {
    return ObjectUtils.notNull(getAssemblyDefinitionMap().values());
  }

  @Override
  public IBindingDefinitionAssembly getAssemblyDefinitionByName(@NonNull String name) {
    return getAssemblyDefinitionMap().get(name);
  }

  private Map<String, IBindingDefinitionModelField> getFieldDefinitionMap() {
    return fieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IBindingDefinitionModelField> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  @Override
  public IBindingDefinitionModelField getFieldDefinitionByName(@NonNull String name) {
    return getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public List<IBindingDefinitionModel> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  private Map<String, IBindingDefinitionFlag> getFlagDefinitionMap() {
    return flagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IBindingDefinitionFlag> getFlagDefinitions() {
    return getFlagDefinitionMap().values();
  }

  @Override
  public IBindingDefinitionFlag getFlagDefinitionByName(@NonNull String name) {
    return getFlagDefinitionMap().get(name);
  }

  private Map<String, ? extends IBindingDefinitionAssembly> getRootAssemblyDefinitionMap() {
    return rootAssemblyDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IBindingDefinitionAssembly> getRootAssemblyDefinitions() {
    return getRootAssemblyDefinitionMap().values();
  }
}
