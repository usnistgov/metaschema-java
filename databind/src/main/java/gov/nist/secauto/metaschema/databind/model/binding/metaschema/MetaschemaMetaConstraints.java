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

import gov.nist.secauto.metaschema.core.datatype.adapter.TokenAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.UriAdapter;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IMetaschemaData;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines constraint rules to be applied to an existing set of Metaschema
 * module-based models.
 */
@SuppressWarnings({
    "PMD.DataClass",
    "PMD.FieldNamingConventions",
})
@MetaschemaAssembly(
    formalName = "External Module Constraints",
    description = "Defines constraint rules to be applied to an existing set of Metaschema module-based models.",
    name = "metaschema-meta-constraints",
    moduleClass = MetaschemaModelModule.class,
    rootName = "metaschema-meta-constraints")
public final class MetaschemaMetaConstraints implements IBoundObject {
  private final IMetaschemaData __metaschemaData;

  @BoundAssembly(
      useName = "definition-context")
  private DefinitionContext _definitionContext;

  @BoundAssembly(
      useName = "context",
      minOccurs = 1,
      maxOccurs = -1,
      groupAs = @GroupAs(name = "contexts", inJson = JsonGroupAsBehavior.LIST))
  private List<MetapathContext> _contexts;

  public MetaschemaMetaConstraints() {
    this(null);
  }

  public MetaschemaMetaConstraints(IMetaschemaData data) {
    this.__metaschemaData = data;
  }

  @Override
  public IMetaschemaData getMetaschemaData() {
    return __metaschemaData;
  }

  public DefinitionContext getDefinitionContext() {
    return _definitionContext;
  }

  public void setDefinitionContext(DefinitionContext value) {
    _definitionContext = value;
  }

  public List<MetapathContext> getContexts() {
    return _contexts;
  }

  public void setContexts(List<MetapathContext> value) {
    _contexts = value;
  }

  /**
   * Add a new {@link MetapathContext} item to the underlying collection.
   *
   * @param item
   *          the item to add
   * @return {@code true}
   */
  public boolean addContext(MetapathContext item) {
    MetapathContext value = ObjectUtils.requireNonNull(item, "item cannot be null");
    if (_contexts == null) {
      _contexts = new LinkedList<>();
    }
    return _contexts.add(value);
  }

  /**
   * Remove the first matching {@link MetapathContext} item from the underlying
   * collection.
   *
   * @param item
   *          the item to remove
   * @return {@code true} if the item was removed or {@code false} otherwise
   */
  public boolean removeContext(MetapathContext item) {
    MetapathContext value = ObjectUtils.requireNonNull(item, "item cannot be null");
    return _contexts != null && _contexts.remove(value);
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }

  @MetaschemaAssembly(
      name = "definition-context",
      moduleClass = MetaschemaModelModule.class)
  public static final class DefinitionContext implements IBoundObject {
    private final IMetaschemaData __metaschemaData;

    @BoundFlag(
        name = "name",
        required = true,
        typeAdapter = TokenAdapter.class)
    private String _name;

    @BoundFlag(
        name = "namespace",
        required = true,
        typeAdapter = UriAdapter.class)
    private URI _namespace;

    @BoundAssembly(
        useName = "constraints",
        minOccurs = 1)
    private AssemblyConstraints _constraints;

    @BoundField(
        formalName = "Remarks",
        description = "Any explanatory or helpful information to be provided about the remarks parent.",
        useName = "remarks")
    private Remarks _remarks;

    public DefinitionContext() {
      this(null);
    }

    public DefinitionContext(IMetaschemaData data) {
      this.__metaschemaData = data;
    }

    @Override
    public IMetaschemaData getMetaschemaData() {
      return __metaschemaData;
    }

    public String getName() {
      return _name;
    }

    public void setName(String value) {
      _name = value;
    }

    public URI getNamespace() {
      return _namespace;
    }

    public void setNamespace(URI value) {
      _namespace = value;
    }

    public AssemblyConstraints getConstraints() {
      return _constraints;
    }

    public void setConstraints(AssemblyConstraints value) {
      _constraints = value;
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
}
