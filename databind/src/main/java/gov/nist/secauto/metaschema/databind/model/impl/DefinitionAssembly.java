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
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBindingDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBindingInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelNamed;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefinitionAssembly
    extends AbstractBoundDefinitionFlagContainer<MetaschemaAssembly>
    implements IBoundDefinitionAssembly,
    IFeatureBoundDefinitionModelContainer<
        IBoundInstanceModel,
        IBoundInstanceModelNamed,
        IBoundInstanceModelField,
        IBoundInstanceModelAssembly,
        IBoundInstanceModelChoiceGroup> {
  @NonNull
  private final Lazy<FlagContainerSupport> flagContainer;
  @NonNull
  private final Lazy<AssemblyModelContainerSupport> modelContainer;
  @NonNull
  private final Lazy<IModelConstrained> constraints;
  @Nullable
  private final QName xmlRootQName;
  @NonNull
  private final Lazy<BindingDefinitionAssembly> binding;

  @NonNull
  static InstanceModelAssemblyComplex newInstance(
      @NonNull Field field,
      @NonNull DefinitionAssembly containingDefinition) {
    Class<?> itemType = IBindingInstanceModel.getItemType(field);
    IBindingContext bindingContext = containingDefinition.getDefinitionBinding().getBindingContext();
    IBoundDefinitionModel definition = bindingContext.getBoundDefinitionForClass(itemType);
    if (definition instanceof IBoundDefinitionAssembly) {
      return new InstanceModelAssemblyComplex(field, (DefinitionAssembly) definition, containingDefinition);
    }

    throw new IllegalStateException(String.format(
        "The field '%s' on class '%s' is not bound to a Metaschema field",
        field.toString(),
        field.getDeclaringClass().getName()));
  }

  public DefinitionAssembly(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    super(clazz, MetaschemaAssembly.class, bindingContext);

    String namespace = ObjectUtils.notNull(ModelUtil.resolveNamespace(getAnnotation().rootNamespace(), this));
    String localName = ModelUtil.resolveNoneOrDefault(getAnnotation().rootName(), null);

    this.xmlRootQName = localName == null ? null : new QName(namespace, localName);

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
    this.binding = ObjectUtils.notNull(Lazy.lazy(() -> new BindingDefinitionAssembly()));

    if (isRoot()) {
      bindingContext.registerBindingMatcher(this);
    }
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @SuppressWarnings("null")
  @Override
  @NonNull
  public BindingDefinitionAssembly getDefinitionBinding() {
    return binding.get();
  }

  @Override
  @Nullable
  public IBoundInstanceModelAssembly getInlineInstance() {
    // never inline
    return null;
  }

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
  public DefinitionAssembly getOwningDefinition() {
    return this;
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
  public Map<QName, Set<String>> getProperties() {
    // TODO: implement
    return CollectionUtil.emptyMap();
  }

  @Override
  @NonNull
  public String getName() {
    return getAnnotation().name();
  }

  @Override
  @Nullable
  public Integer getIndex() {
    return ModelUtil.resolveNullOrInteger(getAnnotation().index());
  }

  @Override
  @Nullable
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().description());
  }

  @Override
  @NonNull
  protected Class<? extends IBoundModule> getModuleClass() {
    return getAnnotation().moduleClass();
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
    return ModelUtil.resolveNullOrInteger(getAnnotation().rootIndex());
  }

  @Override
  @Nullable
  public QName getRootXmlQName() {
    // Overriding this is more efficient, since it is already built
    return xmlRootQName;
  }

  protected class BindingDefinitionAssembly
      extends AbstractBindingFlagContainerDefinition
      implements IBindingDefinitionAssembly {
    @NonNull
    private final Lazy<List<IBindingInstanceFlag>> flagInstanceBindings;

    private BindingDefinitionAssembly() {
      this.flagInstanceBindings = ObjectUtils.notNull(Lazy.lazy(() -> getFlagInstances().stream()
          .map(instance -> instance.getInstanceBinding())
          .collect(Collectors.toUnmodifiableList())));
    }

    @Override
    @NonNull
    public DefinitionAssembly getDefinition() {
      return DefinitionAssembly.this;
    }

    @SuppressWarnings("null")
    @Override
    @NonNull
    public List<IBindingInstanceFlag> getFlagInstanceBindings() {
      return flagInstanceBindings.get();
    }

    @Override
    @NonNull
    public Class<?> getBoundClass() {
      return DefinitionAssembly.this.getBoundClass();
    }

    @Override
    public boolean canHandleJsonPropertyName(String name) {
      return name.equals(getRootJsonName());
    }

    @Override
    public boolean canHandleXmlQName(QName qname) {
      return qname.equals(getRootXmlQName());
    }

    @Override
    protected void deepCopyItemInternal(Object fromObject, Object toObject) throws BindingException {
      super.deepCopyItemInternal(fromObject, toObject);

      for (IBoundInstanceModel instance : getModelInstances()) {
        instance.getInstanceBinding().deepCopy(fromObject, toObject);
      }
    }
  }

  // ----------------------------------------
  // - End annotation driven code - CPD-OFF -
  // ----------------------------------------
}
