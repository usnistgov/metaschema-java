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

package gov.nist.secauto.metaschema.model.common.explode;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * When exploding a model, this class ensures that a definition that is referenced is localized.
 *
 */
public class ProxiedAssemblyDefinition extends AbstractNamedModelDefinition<IAssemblyDefinition>
    implements AssemblyDefinition {
  private Map<@NotNull String, INamedModelInstance> namedModelInstances;
  private Map<@NotNull String, FieldInstance> fieldInstances;
  private Map<@NotNull String, AssemblyInstance> assemblyInstances;
  private List<@NotNull IModelInstance> modelInstances;

  /**
   * Create a new assembly definition that delegates to another definition.
   * 
   * @param delegate
   *          the proxied assembly definition
   */
  public ProxiedAssemblyDefinition(@NotNull IAssemblyDefinition delegate) {
    super(delegate);
  }

  @Override
  public void initializeModel(AssemblyDefinitionResolver resolver) {
    Map<@NotNull String, INamedModelInstance> namedModelInstances = new LinkedHashMap<>();
    Map<@NotNull String, FieldInstance> fieldInstances = new LinkedHashMap<>();
    Map<@NotNull String, AssemblyInstance> assemblyInstances = new LinkedHashMap<>();
    List<@NotNull IModelInstance> modelInstances = new ArrayList<>(getDelegate().getModelInstances().size());

    for (IModelInstance instance : getDelegate().getModelInstances()) {
      if (instance instanceof IFieldInstance) {
        IFieldInstance fieldInstance = (IFieldInstance) instance;
        FieldDefinition definition = new FieldDefinition(fieldInstance.getDefinition());
        FieldInstance field = new FieldInstance(fieldInstance, definition, this);
        namedModelInstances.put(field.getEffectiveName(), field);
        fieldInstances.put(field.getEffectiveName(), field);
        modelInstances.add(field);
      } else if (instance instanceof IAssemblyInstance) {
        IAssemblyInstance assemblyInstance = (IAssemblyInstance) instance;
        AssemblyDefinition definition = resolver.getAssemblyDefinition(assemblyInstance.getDefinition());
        AssemblyInstance assembly = new AssemblyInstance(assemblyInstance, definition, this);
        definition.initializeModel(resolver);
        resolver.pop(definition);
        namedModelInstances.put(assembly.getEffectiveName(), assembly);
        assemblyInstances.put(assembly.getEffectiveName(), assembly);
        modelInstances.add(assembly);
      } else if (instance instanceof IChoiceInstance) {
        // TODO: implement
      }
    }

    this.namedModelInstances
        = namedModelInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(namedModelInstances);
    this.fieldInstances
        = fieldInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fieldInstances);
    this.assemblyInstances
        = assemblyInstances.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(assemblyInstances);
    this.modelInstances
        = modelInstances.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(modelInstances);
  }

  @Override
  public boolean isRoot() {
    return getDelegate().isRoot();
  }

  @Override
  public String getRootName() {
    return getDelegate().getRootName();
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, INamedModelInstance> getNamedModelInstanceMap() {
    return namedModelInstances;
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, FieldInstance> getFieldInstanceMap() {
    return fieldInstances;
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, AssemblyInstance> getAssemblyInstanceMap() {
    return assemblyInstances;
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends IChoiceInstance> getChoiceInstances() {
    // TODO: implement
    return Collections.emptyList();
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends IModelInstance> getModelInstances() {
    return modelInstances;
  }

  @Override
  public List<@NotNull ? extends IIndexConstraint> getIndexConstraints() {
    return getDelegate().getIndexConstraints();
  }

  @Override
  public List<@NotNull ? extends IUniqueConstraint> getUniqueConstraints() {
    return getDelegate().getUniqueConstraints();
  }

  @Override
  public List<@NotNull ? extends ICardinalityConstraint> getHasCardinalityConstraints() {
    return getDelegate().getHasCardinalityConstraints();
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return getDelegate().getContainingMetaschema();
  }

  @Override
  public String getFormalName() {
    return getDelegate().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return getDelegate().getDescription();
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    return ModuleScopeEnum.LOCAL;
  }

  @Override
  public boolean isGlobal() {
    return false;
  }
}
