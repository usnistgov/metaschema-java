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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

//TODO: implement getProperties()
public final class DefinitionAssembly
    extends AbstractBoundDefinitionModelComplex<MetaschemaAssembly>
    implements IBoundDefinitionModelAssembly,
    IFeatureBoundContainerModelAssembly<
        IBoundInstanceModel<?>,
        IBoundInstanceModelNamed<?>,
        IBoundInstanceModelField<?>,
        IBoundInstanceModelAssembly,
        IBoundInstanceModelChoiceGroup> {

  @NonNull
  private final Lazy<FlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<AssemblyModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> constraints;
  @NonNull
  private final Lazy<QName> xmlRootQName;
  @NonNull
  private final Lazy<Map<String, IBoundProperty<?>>> jsonProperties;

  public static DefinitionAssembly newInstance(
      @NonNull Class<? extends IBoundObject> clazz,
      @NonNull IBindingContext bindingContext) {
    MetaschemaAssembly annotation = ModelUtil.getAnnotation(clazz, MetaschemaAssembly.class);
    Class<? extends IBoundModule> moduleClass = annotation.moduleClass();
    return new DefinitionAssembly(clazz, annotation, moduleClass, bindingContext);
  }

  private DefinitionAssembly(
      @NonNull Class<? extends IBoundObject> clazz,
      @NonNull MetaschemaAssembly annotation,
      @NonNull Class<? extends IBoundModule> moduleClass,
      @NonNull IBindingContext bindingContext) {
    super(clazz, annotation, moduleClass, bindingContext);

    String rootLocalName = ModelUtil.resolveNoneOrDefault(getAnnotation().rootName(), null);
    this.xmlRootQName = ObjectUtils.notNull(Lazy.lazy(() -> rootLocalName == null
        ? null
        : getContainingModule().toModelQName(rootLocalName)));
    this.flagContainer = ObjectUtils.notNull(Lazy.lazy(() -> new FlagContainerSupport(this, null)));
    this.modelContainer = ObjectUtils.notNull(Lazy.lazy(() -> new AssemblyModelContainerSupport(this)));
    this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
      IModelConstrained retval = new AssemblyConstraintSet();
      ValueConstraints valueAnnotation = getAnnotation().valueConstraints();
      ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);

      AssemblyConstraints assemblyAnnotation = getAnnotation().modelConstraints();
      ConstraintSupport.parse(assemblyAnnotation, ISource.modelSource(), retval);
      return retval;
    }));
    this.jsonProperties = ObjectUtils.notNull(Lazy.lazy(() -> getJsonProperties(null)));

    if (rootLocalName != null) {
      bindingContext.registerBindingMatcher(this);
    }
  }

  @Override
  protected void deepCopyItemInternal(IBoundObject fromObject, IBoundObject toObject) throws BindingException {
    // copy the flags
    super.deepCopyItemInternal(fromObject, toObject);

    for (IBoundInstanceModel<?> instance : getModelInstances()) {
      instance.deepCopy(fromObject, toObject);
    }
  }

  @Override
  public Map<String, IBoundProperty<?>> getJsonProperties() {
    return ObjectUtils.notNull(jsonProperties.get());
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  @SuppressWarnings("null")
  @NonNull
  public FlagContainerSupport getFlagContainer() {
    return flagContainer.get();
  }

  @Override
  @SuppressWarnings("null")
  @NonNull
  public AssemblyModelContainerSupport getModelContainer() {
    return modelContainer.get();
  }

  @Override
  @NonNull
  public IModelConstrained getConstraintSupport() {
    return ObjectUtils.notNull(constraints.get());
  }

  @Override
  @Nullable
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().formalName());
  }

  @Override
  @Nullable
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAnnotation().description());
  }

  @Override
  @NonNull
  public String getName() {
    return getAnnotation().name();
  }

  @Override
  @Nullable
  public Integer getIndex() {
    return ModelUtil.resolveDefaultInteger(getAnnotation().index());
  }

  @Override
  @Nullable
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().description());
  }

  @Override
  @Nullable
  public QName getRootXmlQName() {
    // Overriding this is more efficient, since it is already built
    return xmlRootQName.get();
  }

  @Override
  public boolean isRoot() {
    // Overriding this is more efficient, since the root name is derived from the
    // XML QName
    return getRootXmlQName() != null;
  }

  @Override
  @Nullable
  public String getRootName() {
    // Overriding this is more efficient, since it is already built
    QName qname = getRootXmlQName();
    return qname == null ? null : qname.getLocalPart();
  }

  @Override
  @Nullable
  public Integer getRootIndex() {
    return ModelUtil.resolveDefaultInteger(getAnnotation().rootIndex());
  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
