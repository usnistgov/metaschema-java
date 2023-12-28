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

package gov.nist.secauto.metaschema.core.testing;

import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModelElement;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelElement;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractModelBuilder<T extends AbstractModelBuilder<T>>
    extends MockFactory {

  private String name;

  protected AbstractModelBuilder(@NonNull Mockery ctx) {
    super(ctx);
  }

  @NonNull
  @SuppressWarnings("unchecked")
  public T reset() {
    this.name = null;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public T name(@NonNull String name) {
    this.name = name;
    return (T) this;
  }

  protected void validate() {
    ObjectUtils.requireNonEmpty(name, "name");
  }

  protected void applyDefinition(@NonNull IDefinition definition) {
    applyNamedModelElement(definition);
    getContext().checking(new Expectations() {
      {
        allowing(definition).getName();
        will(returnValue(name));
        allowing(definition).getEffectiveName();
        will(returnValue(name));
        allowing(definition).getUseName();
        will(returnValue(null));
        allowing(definition).getProperties();
        will(returnValue(CollectionUtil.emptyMap()));
      }
    });
  }

  protected <DEF extends IDefinition> void applyNamedInstance(
      @NonNull INamedInstance instance,
      @NonNull DEF definition,
      @NonNull IModelDefinition parent) {
    applyNamedModelElement(instance);
    getContext().checking(new Expectations() {
      {
        allowing(instance).getName();
        will(returnValue(name));
        allowing(instance).getEffectiveName();
        will(returnValue(name));
        allowing(instance).getUseName();
        will(returnValue(null));
        allowing(instance).getProperties();
        will(returnValue(CollectionUtil.emptyMap()));
        allowing(instance).getDefinition();
        will(returnValue(definition));
        allowing(instance).getContainingDefinition();
        will(returnValue(parent));
        allowing(instance).getParentContainer();
        will(returnValue(parent));

      }
    });
  }

  protected void applyNamedModelElement(@NonNull INamedModelElement element) {
    applyModelElement(element);
    getContext().checking(new Expectations() {
      {
        allowing(element).getFormalName();
        will(returnValue(null));
        allowing(element).getDescription();
        will(returnValue(null));
        allowing(element).getProperties();
        will(returnValue(CollectionUtil.emptyMap()));
        allowing(element).getUseName();
        will(returnValue(null));
      }
    });
  }

  protected void applyModelElement(@NonNull IModelElement element) {
    getContext().checking(new Expectations() {
      {
        allowing(element).getRemarks();
        will(returnValue(null));
      }
    });
  }
}
