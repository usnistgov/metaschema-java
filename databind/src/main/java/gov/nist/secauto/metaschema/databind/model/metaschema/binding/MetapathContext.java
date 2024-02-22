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

package gov.nist.secauto.metaschema.databind.model.metaschema.binding;

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({
    "PMD.DataClass",
    "PMD.FieldNamingConventions",
})
@MetaschemaAssembly(
    name = "metapath-context",
    moduleClass = MetaschemaModelModule.class)
public class MetapathContext {
  @BoundField(
      description = "A Metapath expression identifying the model node that the constraints will be applied to.",
      useName = "metapath")
  private String _metapath;

  @BoundAssembly(
      useName = "constraints")
  private AssemblyConstraints _constraints;

  @BoundAssembly(
      useName = "metapath-context",
      maxOccurs = -1,
      groupAs = @GroupAs(name = "metapath-contexts", namespace = "##default", inJson = JsonGroupAsBehavior.LIST))
  private List<MetapathContext> _metapathContexts;

  @BoundField(
      formalName = "Remarks",
      description = "Any explanatory or helpful information to be provided about the remarks parent.",
      useName = "remarks")
  private Remarks _remarks;

  public String getMetapath() {
    return _metapath;
  }

  public void setMetapath(String value) {
    _metapath = value;
  }

  public AssemblyConstraints getConstraints() {
    return _constraints;
  }

  public void setConstraints(AssemblyConstraints value) {
    _constraints = value;
  }

  public List<MetapathContext> getMetapathContexts() {
    return _metapathContexts;
  }

  public void setMetapathContexts(List<MetapathContext> value) {
    _metapathContexts = value;
  }

  /**
   * Add a new {@link MetapathContext} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addMetapathContext(MetapathContext item) {
    MetapathContext value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_metapathContexts == null) {
      _metapathContexts = new LinkedList<>();
    }
    return _metapathContexts.add(value);
  }

  /**
   * Remove the first matching {@link MetapathContext} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeMetapathContext(MetapathContext item) {
    MetapathContext value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _metapathContexts != null && _metapathContexts.remove(value);
  }

  public Remarks getRemarks() {
    return _remarks;
  }

  public void setRemarks(Remarks value) {
    _remarks = value;
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }
}
