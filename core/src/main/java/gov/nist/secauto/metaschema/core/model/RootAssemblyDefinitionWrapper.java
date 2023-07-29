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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.ICardinalityConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IUniqueConstraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Wraps an {@link IAssemblyDefinition} that is a {@link IRootAssemblyDefinition}.
 *
 * @param <T>
 *          the type of the wrapped definition
 */
public class RootAssemblyDefinitionWrapper<T extends IAssemblyDefinition> implements IRootAssemblyDefinition {
  // TODO: find a better way to support this, e.g. abstract class
  @NonNull
  private final T rootDefinition;

  /**
   * Construct a new wrapper that delgates method calls to the underlying definition implementing root
   * semantics.
   *
   * @param rootDefinition
   *          the definition to wrap
   */
  public RootAssemblyDefinitionWrapper(@NonNull T rootDefinition) {
    if (!rootDefinition.isRoot()) {
      throw new IllegalArgumentException(
          "Provided definition is not a root assembly: " + rootDefinition.toCoordinates());
    }
    this.rootDefinition = rootDefinition;

  }

  /**
   * Get the associated definition.
   *
   * @return the definition
   */
  @NonNull
  protected T getRootDefinition() {
    return rootDefinition;
  }

  @Override
  public String getFormalName() {
    return getRootDefinition().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return getRootDefinition().getDescription();
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return getRootDefinition().getProperties();
  }

  @Override
  public String getName() {
    return getRootDefinition().getName();
  }

  @Override
  public String getUseName() {
    return getRootDefinition().getUseName();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getRootDefinition().getRemarks();
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    return getRootDefinition().getContainingMetaschema();
  }

  @Override
  public boolean isRoot() {
    // always true for a root definition
    return true;
  }

  @SuppressWarnings("null")
  @Override
  public String getRootName() {
    return getRootDefinition().getRootName();
  }

  @Override
  public boolean isInline() {
    // always false, since this is a root
    return false;
  }

  @Override
  public IAssemblyInstance getInlineInstance() {
    // always null, since this is a root
    return null;
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    // always INHERITED, since roots are always inherited
    return ModuleScopeEnum.INHERITED;
  }

  @Override
  public boolean hasJsonKey() {
    // always null, since this is a root
    return false;
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    // always null, since this is a root
    return null;
  }

  @Override
  public IFlagInstance getFlagInstanceByName(String name) {
    return getRootDefinition().getFlagInstanceByName(name);
  }

  @Override
  public Collection<? extends IFlagInstance> getFlagInstances() {
    return getRootDefinition().getFlagInstances();
  }

  @Override
  public Collection<? extends IModelInstance> getModelInstances() {
    return getRootDefinition().getModelInstances();
  }

  @Override
  public Collection<? extends INamedModelInstance> getNamedModelInstances() {
    return getRootDefinition().getNamedModelInstances();
  }

  @Override
  public INamedModelInstance getModelInstanceByName(String name) {
    return getRootDefinition().getModelInstanceByName(name);
  }

  @Override
  public Collection<? extends IFieldInstance> getFieldInstances() {
    return getRootDefinition().getFieldInstances();
  }

  @Override
  public IFieldInstance getFieldInstanceByName(String name) {
    return getRootDefinition().getFieldInstanceByName(name);
  }

  @Override
  public Collection<? extends IAssemblyInstance> getAssemblyInstances() {
    return getRootDefinition().getAssemblyInstances();
  }

  @Override
  public IAssemblyInstance getAssemblyInstanceByName(String name) {
    return getRootDefinition().getAssemblyInstanceByName(name);
  }

  @Override
  public List<? extends IChoiceInstance> getChoiceInstances() {
    return getRootDefinition().getChoiceInstances();
  }

  @Override
  public List<? extends IConstraint> getConstraints() {
    return getRootDefinition().getConstraints();
  }

  @Override
  public List<? extends IIndexConstraint> getIndexConstraints() {
    return getRootDefinition().getIndexConstraints();
  }

  @Override
  public List<? extends IUniqueConstraint> getUniqueConstraints() {
    return getRootDefinition().getUniqueConstraints();
  }

  @Override
  public List<? extends ICardinalityConstraint> getHasCardinalityConstraints() {
    return getRootDefinition().getHasCardinalityConstraints();
  }

  @Override
  public List<? extends IAllowedValuesConstraint> getAllowedValuesConstraints() {
    return getRootDefinition().getAllowedValuesConstraints();
  }

  @Override
  public List<? extends IMatchesConstraint> getMatchesConstraints() {
    return getRootDefinition().getMatchesConstraints();
  }

  @Override
  public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return getRootDefinition().getIndexHasKeyConstraints();
  }

  @Override
  public List<? extends IExpectConstraint> getExpectConstraints() {
    return getRootDefinition().getExpectConstraints();
  }

  @Override
  public void addConstraint(@NonNull IIndexConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IUniqueConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull ICardinalityConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IAllowedValuesConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IMatchesConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IIndexHasKeyConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }

  @Override
  public void addConstraint(@NonNull IExpectConstraint constraint) {
    getRootDefinition().addConstraint(constraint);
  }
}
