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

import gov.nist.secauto.metaschema.core.model.IAttributable;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModelElement;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelElement;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;

import java.net.URI;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractModelBuilder<T extends AbstractModelBuilder<T>>
    extends MockFactory {

  private String namespace;
  private String name;

  /**
   * Construct a new builder using the provided mocking context.
   *
   * @param ctx
   *          the mocking context
   */
  protected AbstractModelBuilder(@NonNull Mockery ctx) {
    super(ctx);
  }

  /**
   * Reset the builder back to a default state.
   *
   * @return this builder
   */
  @NonNull
  @SuppressWarnings("unchecked")
  public T reset() {
    this.name = null;
    this.namespace = null;
    return (T) this;
  }

  /**
   * Apply the provided namespace to use for names built using this builder.
   *
   * @param name
   *          the namespace to use
   * @return this builder
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public T namespace(@NonNull String name) {
    this.namespace = name;
    return (T) this;
  }

  /**
   * Apply the provided namespace to use for names built using this builder.
   *
   * @param name
   *          the namespace to use
   * @return this builder
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public T namespace(@NonNull URI name) {
    this.namespace = name.toASCIIString();
    return (T) this;
  }

  /**
   * Apply the provided names to use for names built using this builder.
   *
   * @param name
   *          the name to use
   * @return this builder
   */
  @SuppressWarnings("unchecked")
  @NonNull
  public T name(@NonNull String name) {
    this.name = name;
    return (T) this;
  }

  /**
   * Validate the data provided to this builder to ensure correct and required
   * information is provided.
   */
  protected void validate() {
    ObjectUtils.requireNonEmpty(name, "name");
  }

  /**
   * Apply expectations to the mocking context for the provided definition.
   *
   * @param definition
   *          the definition to apply mocking expectations for
   */
  protected void applyDefinition(@NonNull IDefinition definition) {
    applyModelElement(definition);
    applyNamed(definition);
    applyAttributable(definition);
  }

  /**
   * Apply expectations to the mocking context for the provided instance,
   * definition, and parent definition.
   *
   * @param <DEF>
   *          the Java type of the definition
   * @param instance
   *          the instance to apply mocking expectations for
   * @param definition
   *          the definition to apply mocking expectations for
   * @param parent
   *          the parent definition to apply mocking expectations for
   */
  protected <DEF extends IDefinition> void applyNamedInstance(
      @NonNull INamedInstance instance,
      @NonNull DEF definition,
      @NonNull IModelDefinition parent) {
    applyModelElement(instance);
    applyNamed(instance);
    applyAttributable(instance);
    getContext().checking(new Expectations() {
      {
        allowing(instance).getXmlNamespace();
        will(returnValue(namespace));
        allowing(instance).getXmlQName();
        will(returnValue(new QName(namespace, name)));
        allowing(instance).getDefinition();
        will(returnValue(definition));
        allowing(instance).getContainingDefinition();
        will(returnValue(parent));
        allowing(instance).getParentContainer();
        will(returnValue(parent));
      }
    });
  }

  /**
   * Apply expectations to the mocking context for the provided named model
   * element.
   *
   * @param element
   *          the named model element to apply mocking expectations for
   */
  protected void applyNamed(@NonNull INamedModelElement element) {
    getContext().checking(new Expectations() {
      {
        allowing(element).getName();
        will(returnValue(name));
        allowing(element).getUseName();
        will(returnValue(null));
        allowing(element).getEffectiveName();
        will(returnValue(name));
        allowing(element).getFormalName();
        will(returnValue(null));
        allowing(element).getDescription();
        will(returnValue(null));
        allowing(element).getUseName();
        will(returnValue(null));
      }
    });
  }

  /**
   * Apply expectations to the mocking context for the provided attributable
   * element.
   *
   * @param element
   *          the element to apply mocking expectations for
   */
  protected void applyAttributable(@NonNull IAttributable element) {
    getContext().checking(new Expectations() {
      {
        allowing(element).getProperties();
        will(returnValue(CollectionUtil.emptyMap()));
      }
    });
  }

  /**
   * Apply expectations to the mocking context for the provided model element.
   *
   * @param element
   *          the model element to apply mocking expectations for
   */
  protected void applyModelElement(@NonNull IModelElement element) {
    getContext().checking(new Expectations() {
      {
        allowing(element).getRemarks();
        will(returnValue(null));
      }
    });
  }
}
