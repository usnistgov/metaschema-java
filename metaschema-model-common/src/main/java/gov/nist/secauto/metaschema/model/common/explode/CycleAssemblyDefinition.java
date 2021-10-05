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

import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IUniqueConstraint;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This assembly definition implementation represents a cycle in the Metaschema model where a
 * sequence of assemblies references the first assembly in the sequence.
 */
public class CycleAssemblyDefinition implements AssemblyDefinition {
  @NotNull
  private final ProxiedAssemblyDefinition cycle;

  /**
   * Create a new assembly definition that references the assembly that is the head and tail of a
   * cycle.
   * 
   * @param cycle
   *          the cycled assembly definition
   */
  public CycleAssemblyDefinition(@NotNull ProxiedAssemblyDefinition cycle) {
    this.cycle = cycle;
  }

  @Override
  public void initializeModel(AssemblyDefinitionResolver resolver) {
    // do nothing, the proxy is already initialized
  }

  /**
   * Get the referenced assembly in the cycle.
   * 
   * @return the assembly
   */
  @NotNull
  protected ProxiedAssemblyDefinition getCycle() {
    return cycle;
  }

  @Override
  public Map<@NotNull String, ? extends IFlagInstance> getFlagInstanceMap() {
    return getCycle().getFlagInstanceMap();
  }

  @Override
  public boolean hasJsonKey() {
    return getCycle().hasJsonKey();
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    return getCycle().getJsonKeyFlagInstance();
  }

  @Override
  public String getName() {
    return getCycle().getName();
  }

  @Override
  public String getUseName() {
    return getCycle().getUseName();
  }

  @Override
  public String getXmlNamespace() {
    return getCycle().getXmlNamespace();
  }

  @Override
  public String toCoordinates() {
    return getCycle().toCoordinates();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getCycle().getRemarks();
  }

  @Override
  public Map<@NotNull String, ? extends INamedModelInstance> getNamedModelInstanceMap() {
    return getCycle().getNamedModelInstanceMap();
  }

  @Override
  public Map<@NotNull String, ? extends IFieldInstance> getFieldInstanceMap() {
    return getCycle().getFieldInstanceMap();
  }

  @Override
  public Map<@NotNull String, ? extends IAssemblyInstance> getAssemblyInstanceMap() {
    return getCycle().getAssemblyInstanceMap();
  }

  @Override
  public List<? extends IChoiceInstance> getChoiceInstances() {
    return getCycle().getChoiceInstances();
  }

  @Override
  public Collection<? extends IModelInstance> getModelInstances() {
    return getCycle().getModelInstances();
  }

  @Override
  public boolean isRoot() {
    return getCycle().isRoot();
  }

  @Override
  public String getRootName() {
    return getCycle().getRootName();
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return getCycle().getConstraints();
  }

  @Override
  public List<? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
    return getCycle().getAllowedValuesContraints();
  }

  @Override
  public List<? extends IMatchesConstraint> getMatchesConstraints() {
    return getCycle().getMatchesConstraints();
  }

  @Override
  public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return getCycle().getIndexHasKeyConstraints();
  }

  @Override
  public List<? extends IExpectConstraint> getExpectConstraints() {
    return getCycle().getExpectConstraints();
  }

  @Override
  public List<? extends IIndexConstraint> getIndexConstraints() {
    return getCycle().getIndexConstraints();
  }

  @Override
  public List<? extends IUniqueConstraint> getUniqueConstraints() {
    return getCycle().getUniqueConstraints();
  }

  @Override
  public List<? extends ICardinalityConstraint> getHasCardinalityConstraints() {
    return getCycle().getHasCardinalityConstraints();
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return getCycle().getContainingMetaschema();
  }

  @Override
  public String getFormalName() {
    return getCycle().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return getCycle().getDescription();
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
