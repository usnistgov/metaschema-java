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

import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractNamedModelDefinition<DELEGATE extends INamedModelDefinition>
    extends AbstractDefinition<DELEGATE>
    implements INamedModelDefinition {

  private Map<@NotNull String, FlagInstance> flagInstances;

  /**
   * Construct a new definition that may contain flags based on a delegate definition.
   * 
   * @param delegate
   *          the definition to delegate to
   */
  public AbstractNamedModelDefinition(@NotNull DELEGATE delegate) {
    super(delegate);
  }

  /**
   * Lazy load flag instances.
   */
  protected synchronized void initializeFlagInstances() {
    if (flagInstances == null) {
      flagInstances = new LinkedHashMap<>();

      // duplicate these instances so they have unique hashCode and equals values
      for (@NotNull
      IFlagInstance instance : getDelegate().getFlagInstances()) {
        FlagDefinition definition = new FlagDefinition(instance.getDefinition());
        FlagInstance newInstance = new FlagInstance(instance, definition, this);
        flagInstances.put(newInstance.getEffectiveName(), newInstance);
      }
    }
  }

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, FlagInstance> getFlagInstanceMap() {
    initializeFlagInstances();
    return Collections.unmodifiableMap(flagInstances);
  }

  @Override
  public FlagInstance getFlagInstanceByName(String name) {
    return getFlagInstanceMap().get(name);
  }

  @Override
  public boolean hasJsonKey() {
    return getDelegate().hasJsonKey();
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    IFlagInstance instance = getDelegate().getJsonKeyFlagInstance();
    return instance != null ? getFlagInstanceByName(instance.getEffectiveName()) : null;
  }

  @Override
  @NotNull
  public List<@NotNull ? extends IConstraint> getConstraints() {
    return getDelegate().getConstraints();
  }

  @Override
  @NotNull
  public List<@NotNull ? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
    return getDelegate().getAllowedValuesContraints();
  }

  @Override
  @NotNull
  public List<@NotNull ? extends IMatchesConstraint> getMatchesConstraints() {
    return getDelegate().getMatchesConstraints();
  }

  @Override
  @NotNull
  public List<@NotNull ? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
    return getDelegate().getIndexHasKeyConstraints();
  }

  @Override
  @NotNull
  public List<@NotNull ? extends IExpectConstraint> getExpectConstraints() {
    return getDelegate().getExpectConstraints();
  }
}
