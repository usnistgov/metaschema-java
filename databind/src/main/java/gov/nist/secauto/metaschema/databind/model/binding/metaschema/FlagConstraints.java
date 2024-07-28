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

package gov.nist.secauto.metaschema.databind.model.binding.metaschema;

import gov.nist.secauto.metaschema.core.model.IMetaschemaData;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IConstraintBase;
import gov.nist.secauto.metaschema.databind.model.metaschema.IValueConstraintsBase;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({
    "PMD.DataClass",
    "PMD.ExcessivePublicCount",
    "PMD.FieldNamingConventions"
})
@MetaschemaAssembly(
    name = "flag-constraints",
    moduleClass = MetaschemaModelModule.class)
public final class FlagConstraints implements IValueConstraintsBase {
  private final IMetaschemaData __metaschemaData;

  @BoundAssembly(
      formalName = "Constraint Let Expression",
      useName = "let",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "lets", inJson = JsonGroupAsBehavior.LIST))
  private List<ConstraintLetExpression> _lets;

  @BoundChoiceGroup(
      minOccurs = 1,
      maxOccurs = -1,
      assemblies = {
          @BoundGroupedAssembly(formalName = "Allowed Values Constraint", useName = "allowed-values",
              binding = FlagAllowedValues.class),
          @BoundGroupedAssembly(formalName = "Expect Condition Constraint", useName = "expect",
              binding = FlagExpect.class),
          @BoundGroupedAssembly(formalName = "Index Has Key Constraint", useName = "index-has-key",
              binding = FlagIndexHasKey.class),
          @BoundGroupedAssembly(formalName = "Value Matches Constraint", useName = "matches",
              binding = FlagMatches.class)
      },
      groupAs = @GroupAs(name = "rules", inJson = JsonGroupAsBehavior.LIST))
  private List<? extends IConstraintBase> _rules;

  public FlagConstraints() {
    this(null);
  }

  public FlagConstraints(IMetaschemaData data) {
    this.__metaschemaData = data;
  }

  @Override
  public IMetaschemaData getMetaschemaData() {
    return __metaschemaData;
  }

  @Override
  public List<ConstraintLetExpression> getLets() {
    return _lets;
  }

  public void setLets(List<ConstraintLetExpression> value) {
    _lets = value;
  }

  /**
   * Add a new {@link ConstraintLetExpression} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addLet(ConstraintLetExpression item) {
    ConstraintLetExpression value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_lets == null) {
      _lets = new LinkedList<>();
    }
    return _lets.add(value);
  }

  /**
   * Remove the first matching {@link ConstraintLetExpression} item from the
   * underlying collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeLet(ConstraintLetExpression item) {
    ConstraintLetExpression value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _lets != null && _lets.remove(value);
  }

  @Override
  public List<? extends IConstraintBase> getRules() {
    return _rules;
  }

  public void setRules(List<? extends IConstraintBase> value) {
    _rules = value;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
