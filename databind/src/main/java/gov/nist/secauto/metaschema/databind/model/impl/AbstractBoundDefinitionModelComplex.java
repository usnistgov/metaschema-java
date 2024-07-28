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

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public abstract class AbstractBoundDefinitionModelComplex<A extends Annotation>
    implements IBoundDefinitionModelComplex {
  @NonNull
  private final Class<? extends IBoundObject> clazz;
  @NonNull
  private final A annotation;
  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Lazy<IBoundModule> module;
  @NonNull
  private final Lazy<QName> qname;
  @NonNull
  private final Lazy<QName> definitionQName;
  @Nullable
  private final Method beforeDeserializeMethod;
  @Nullable
  private final Method afterDeserializeMethod;

  protected AbstractBoundDefinitionModelComplex(
      @NonNull Class<? extends IBoundObject> clazz,
      @NonNull A annotation,
      @NonNull Class<? extends IBoundModule> moduleClass,
      @NonNull IBindingContext bindingContext) {
    this.clazz = clazz;
    this.annotation = annotation;
    this.bindingContext = bindingContext;
    this.module = ObjectUtils.notNull(Lazy.lazy(() -> bindingContext.registerModule(moduleClass)));
    this.qname = ObjectUtils.notNull(Lazy.lazy(() -> getContainingModule().toModelQName(getEffectiveName())));
    this.definitionQName = ObjectUtils.notNull(Lazy.lazy(() -> getContainingModule().toModelQName(getName())));
    this.beforeDeserializeMethod = ClassIntrospector.getMatchingMethod(
        clazz,
        "beforeDeserialize",
        Object.class);
    this.afterDeserializeMethod = ClassIntrospector.getMatchingMethod(
        clazz,
        "afterDeserialize",
        Object.class);
  }

  @Override
  public Class<? extends IBoundObject> getBoundClass() {
    return clazz;
  }

  public A getAnnotation() {
    return annotation;
  }

  @Override
  @NonNull
  public IBoundModule getContainingModule() {
    return ObjectUtils.notNull(module.get());
  }

  @Override
  @NonNull
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @SuppressWarnings("null")
  @Override
  public final QName getXmlQName() {
    return qname.get();
  }

  @SuppressWarnings("null")
  @Override
  public final QName getDefinitionQName() {
    return definitionQName.get();
  }

  @Override
  public boolean isInline() {
    return getBoundClass().getEnclosingClass() != null;
  }

  @Override
  public Method getBeforeDeserializeMethod() {
    return beforeDeserializeMethod;
  }

  @Override
  public Method getAfterDeserializeMethod() {
    return afterDeserializeMethod;
  }

  // @Override
  // public String getJsonKeyFlagName() {
  // // definition items never have a JSON key
  // return null;
  // }

  @Override
  public IBoundObject deepCopyItem(IBoundObject item, IBoundObject parentInstance) throws BindingException {
    IBoundObject instance = newInstance(() -> item.getMetaschemaData());

    callBeforeDeserialize(instance, parentInstance);

    deepCopyItemInternal(item, instance);

    callAfterDeserialize(instance, parentInstance);

    return instance;
  }

  protected void deepCopyItemInternal(@NonNull IBoundObject fromObject, @NonNull IBoundObject toObject)
      throws BindingException {
    for (IBoundInstanceFlag instance : getFlagInstances()) {
      instance.deepCopy(fromObject, toObject);
    }
  }
}
