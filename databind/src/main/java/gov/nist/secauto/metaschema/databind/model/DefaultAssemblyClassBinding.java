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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.IModelContainerSupport;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.util.Objects;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefaultAssemblyClassBinding // NOPMD - ok
    extends AbstractClassBinding
    implements IAssemblyClassBinding {

  private final MetaschemaAssembly metaschemaAssembly;
  private final QName xmlRootQName;
  private final Lazy<ClassBindingFlagContainerSupport> flagContainer;
  private final Lazy<ClassBindingModelContainerSupport> modelContainer;
  private final Lazy<IModelConstrained> constraints;

  /**
   * Create a new {@link IClassBinding} for a Java bean annotated with the
   * {@link BoundAssembly} annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema assembly binding for the class
   */
  @NonNull
  public static DefaultAssemblyClassBinding createInstance(
      @NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    return new DefaultAssemblyClassBinding(clazz, bindingContext);
  }

  /**
   * Construct a new {@link IClassBinding} for a Java bean annotated with the
   * {@link BoundAssembly} annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  protected DefaultAssemblyClassBinding(@NonNull Class<?> clazz, @NonNull IBindingContext bindingContext) {
    super(clazz, bindingContext);
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              clazz.getName(),
              MetaschemaAssembly.class.getName())); // NOPMD
    }
    this.metaschemaAssembly = ObjectUtils.notNull(clazz.getAnnotation(MetaschemaAssembly.class));
    String namespace = ObjectUtils.notNull(ModelUtil.resolveNamespace(this.metaschemaAssembly.rootNamespace(), this));
    String localName = ModelUtil.resolveLocalName(this.metaschemaAssembly.rootName(), null);

    this.xmlRootQName = localName == null ? null : new QName(namespace, localName);

    this.flagContainer = Lazy.lazy(() -> new ClassBindingFlagContainerSupport(this, null));
    this.modelContainer = Lazy.lazy(() -> new ClassBindingModelContainerSupport(this));
    this.constraints = Lazy.lazy(() -> new AssemblyConstraintSupport(
        clazz.getAnnotation(ValueConstraints.class),
        clazz.getAnnotation(AssemblyConstraints.class),
        InternalModelSource.instance()));
  }

  @SuppressWarnings("null")
  @Override
  public IFlagContainerSupport<IBoundFlagInstance> getFlagContainer() {
    return flagContainer.get();
  }

  @SuppressWarnings("null")
  @Override
  public IModelContainerSupport<
      IBoundNamedModelInstance,
      IBoundNamedModelInstance,
      IBoundFieldInstance,
      IBoundAssemblyInstance,
      IChoiceInstance> getModelContainer() {
    return modelContainer.get();
  }

  /**
   * Get the {@link MetaschemaAssembly} annotation associated with this class.
   * This annotation provides information used by this class binding to control
   * binding behavior.
   *
   * @return the annotation
   */
  public MetaschemaAssembly getMetaschemaAssemblyAnnotation() {
    return metaschemaAssembly;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getMetaschemaAssemblyAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getMetaschemaAssemblyAnnotation().description());
  }

  @Override
  public @Nullable MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getMetaschemaAssemblyAnnotation().description());
  }

  @Override
  public String getName() {
    return getMetaschemaAssemblyAnnotation().name();
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IBoundAssemblyInstance getInlineInstance() {
    return null;
  }

  @Override
  public boolean isRoot() {
    // Overriding this is more efficient, since the root name is derived from the
    // XML QName
    return getRootXmlQName() != null;
  }

  @Override
  public String getRootName() {
    QName qname = getRootXmlQName();
    return qname == null ? null : qname.getLocalPart();
  }

  @Override
  public QName getRootXmlQName() {
    // Overriding this is more efficient, since it is already built
    return xmlRootQName;
  }

  @SuppressWarnings("null")
  @Override
  public IModelConstrained getConstraintSupport() {
    return constraints.get();
  }

  @Override
  protected void copyBoundObjectInternal(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    super.copyBoundObjectInternal(fromInstance, toInstance);

    for (IBoundNamedModelInstance property : getModelInstances()) {
      property.copyBoundObject(fromInstance, toInstance);
    }
  }

  @Override
  protected Class<? extends IMetaschema> getMetaschemaClass() {
    return getMetaschemaAssemblyAnnotation().metaschema();
  }

}
