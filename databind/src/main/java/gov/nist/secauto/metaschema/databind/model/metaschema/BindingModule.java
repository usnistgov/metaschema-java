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

package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractModule;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.DefinitionAssemblyGlobal;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.DefinitionFieldGlobal;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.DefinitionFlagGlobal;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.ModelSupport;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class BindingModule
    extends AbstractModule<
        IBindingModule,
        IFlagContainer,
        IFlagDefinition,
        IFieldDefinition,
        IAssemblyDefinition>
    implements IBindingModule {
  @NonNull
  private final URI location;
  @NonNull
  private final METASCHEMA binding;
  private final Map<String, IDefinition> definitions;
  private final Map<String, IFlagDefinition> flagDefinitions;
  private final Map<String, IFieldDefinition> fieldDefinitions;
  private final Map<String, IAssemblyDefinition> assemblyDefinitions;
  private final Map<String, IAssemblyDefinition> rootAssemblyDefinitions;

  /**
   * Constructs a new Metaschema instance.
   *
   * @param resource
   *          the resource from which the module was loaded
   * @param binding
   *          the module definition bound to a Java object
   * @param importedModules
   *          the modules imported by this module
   * @throws MetaschemaException
   *           if a processing error occurs
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public BindingModule( // NOPMD - unavoidable
      @NonNull URI resource,
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

    for (Object definitionObj : binding.getDefinitions()) {

      if (definitionObj instanceof METASCHEMA.DefineAssembly) {
        IAssemblyDefinition definition = new DefinitionAssemblyGlobal(this, (METASCHEMA.DefineAssembly) definitionObj);
        String name = definition.getName();
        definitions.put(name, definition);
        assemblyDefinitions.put(name, definition);
        if (definition.isRoot()) {
          rootAssemblyDefinitions.put(name, definition);
        }
      } else if (definitionObj instanceof METASCHEMA.DefineField) {
        IFieldDefinition definition = new DefinitionFieldGlobal(this, (METASCHEMA.DefineField) definitionObj);
        String name = definition.getName();
        definitions.put(name, definition);
        fieldDefinitions.put(name, definition);
      } else if (definitionObj instanceof METASCHEMA.DefineFlag) {
        IFlagDefinition definition = new DefinitionFlagGlobal(this, (METASCHEMA.DefineFlag) definitionObj);
        String name = definition.getName();
        definitions.put(name, definition);
        flagDefinitions.put(name, definition);
      } else {
        throw new IllegalStateException(
            String.format("Unrecognized definition class '%s' in module '%s'.",
                definitionObj.getClass(),
                resource.toASCIIString()));
      }
    }
  }

  @NonNull
  public METASCHEMA getBinding() {
    return binding;
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

  private Map<String, IAssemblyDefinition> getAssemblyDefinitionMap() {
    return assemblyDefinitions;
  }

  @Override
  public Collection<IAssemblyDefinition> getAssemblyDefinitions() {
    return ObjectUtils.notNull(getAssemblyDefinitionMap().values());
  }

  @Override
  public IAssemblyDefinition getAssemblyDefinitionByName(@NonNull String name) {
    return getAssemblyDefinitionMap().get(name);
  }

  private Map<String, IFieldDefinition> getFieldDefinitionMap() {
    return fieldDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFieldDefinition> getFieldDefinitions() {
    return getFieldDefinitionMap().values();
  }

  @Override
  public IFieldDefinition getFieldDefinitionByName(@NonNull String name) {
    return getFieldDefinitionMap().get(name);
  }

  @SuppressWarnings("null")
  @Override
  public List<? extends IFlagContainer> getAssemblyAndFieldDefinitions() {
    return Stream.concat(getAssemblyDefinitions().stream(), getFieldDefinitions().stream())
        .collect(Collectors.toList());
  }

  private Map<String, IFlagDefinition> getFlagDefinitionMap() {
    return flagDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFlagDefinition> getFlagDefinitions() {
    return getFlagDefinitionMap().values();
  }

  @Override
  public IFlagDefinition getFlagDefinitionByName(@NonNull String name) {
    return getFlagDefinitionMap().get(name);
  }

  private Map<String, ? extends IAssemblyDefinition> getRootAssemblyDefinitionMap() {
    return rootAssemblyDefinitions;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IAssemblyDefinition> getRootAssemblyDefinitions() {
    return getRootAssemblyDefinitionMap().values();
  }
}
