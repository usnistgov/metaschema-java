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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractBoundInstanceModelGroupedNamed<
    A extends Annotation>
    implements IBoundInstanceModelGroupedNamed {
  @NonNull
  private final A annotation;
  @NonNull
  private final IBoundInstanceModelChoiceGroup choiceGroupInstance;

  protected AbstractBoundInstanceModelGroupedNamed(
      @NonNull A annotation,
      @NonNull IBoundInstanceModelChoiceGroup choiceGroupInstance) {
    this.annotation = annotation;
    this.choiceGroupInstance = choiceGroupInstance;
  }

  public A getAnnotation() {
    return annotation;
  }

  @Override
  public IBoundInstanceModelChoiceGroup getParentContainer() {
    return choiceGroupInstance;
  }

  @Override
  public IBoundDefinitionAssembly getContainingDefinition() {
    return getParentContainer().getContainingDefinition();
  }

  @Override
  public String getJsonKeyFlagName() {
    return getParentContainer().getJsonKeyFlagName();
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    // TODO: implement
    return CollectionUtil.emptyMap();
  }

  @Override
  public String getName() {
    return getDefinition().getName();
  }

  @Override
  public IBoundDefinitionModelComplex getDefinition() {
    return getInstance().getDefinition();
  }

  @Override
  public Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    return getDefinition().getDefinitionBinding().deepCopyItem(item, parentInstance);
  }

  @Override
  public void callBeforeDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getDefinition().getDefinitionBinding().callBeforeDeserialize(targetObject, parentObject);
  }

  @Override
  public void callAfterDeserialize(Object targetObject, Object parentObject) throws BindingException {
    getDefinition().getDefinitionBinding().callAfterDeserialize(targetObject, parentObject);
  }

}
