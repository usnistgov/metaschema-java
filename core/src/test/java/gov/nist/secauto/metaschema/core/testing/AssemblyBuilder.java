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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import org.jmock.Expectations;
import org.jmock.Mockery;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class AssemblyBuilder
    extends AbstractModelBuilder<AssemblyBuilder>
    implements IModelInstanceBuilder {

  private List<FlagBuilder> flags;
  private List<? extends IModelInstanceBuilder> modelInstances;

  private AssemblyBuilder(@NonNull Mockery ctx) {
    super(ctx);
  }

  /**
   * Create a new builder using the provided mocking context.
   *
   * @param ctx
   *          the mocking context
   * @return the new builder
   */
  @NonNull
  public static AssemblyBuilder builder(@NonNull Mockery ctx) {
    return new AssemblyBuilder(ctx).reset();
  }

  @Override
  public AssemblyBuilder reset() {
    this.flags = CollectionUtil.emptyList();
    this.modelInstances = CollectionUtil.emptyList();
    return this;
  }

  /**
   * Use the provided flag instances for built fields.
   *
   * @param flags
   *          the flags to use
   * @return this builder
   */
  public AssemblyBuilder flags(@Nullable List<FlagBuilder> flags) {
    this.flags = flags == null ? CollectionUtil.emptyList() : flags;
    return this;
  }

  /**
   * Use the provided model instances for built fields.
   *
   * @param modelInstances
   *          the model instances to use
   * @return this builder
   */
  public AssemblyBuilder modelInstances(@Nullable List<? extends IModelInstanceBuilder> modelInstances) {
    this.modelInstances = modelInstances == null ? CollectionUtil.emptyList() : modelInstances;
    return this;
  }

  @Override
  @NonNull
  public IAssemblyInstanceAbsolute toInstance(
      @NonNull IAssemblyDefinition parent) {
    IAssemblyDefinition def = toDefinition();
    return toInstance(parent, def);
  }

  /**
   * Build a mocked assembly instance, using the provided definition, as a child
   * of the provided parent.
   *
   * @param parent
   *          the parent containing the new instance
   * @param definition
   *          the definition to base the instance on
   * @return the new mocked instance
   */
  @NonNull
  public IAssemblyInstanceAbsolute toInstance(
      @NonNull IAssemblyDefinition parent,
      @NonNull IAssemblyDefinition definition) {
    validate();

    IAssemblyInstanceAbsolute retval = mock(IAssemblyInstanceAbsolute.class);
    applyNamedInstance(retval, definition, parent);
    return retval;
  }

  /**
   * Build a mocked assembly definition.
   *
   * @return the new mocked definition
   */
  @SuppressWarnings("null")
  @NonNull
  public IAssemblyDefinition toDefinition() {
    validate();

    IAssemblyDefinition retval = mock(IAssemblyDefinition.class);
    applyDefinition(retval);

    Map<QName, IFlagInstance> flags = this.flags.stream()
        .map(builder -> builder.toInstance(retval))
        .collect(Collectors.toUnmodifiableMap(
            IFlagInstance::getXmlQName,
            Function.identity()));

    Map<QName, ? extends INamedModelInstanceAbsolute> modelInstances = this.modelInstances.stream()
        .map(builder -> builder.toInstance(retval))
        .collect(Collectors.toUnmodifiableMap(
            INamedModelInstanceAbsolute::getXmlQName,
            Function.identity()));

    getContext().checking(new Expectations() {
      {
        allowing(retval).getFlagInstances();
        will(returnValue(flags.values()));
        flags.forEach((key, value) -> {
          allowing(retval).getFlagInstanceByName(with(key));
          will(returnValue(value));
        });
        allowing(retval).getModelInstances();
        will(returnValue(modelInstances.values()));
        modelInstances.forEach((key, value) -> {
          allowing(retval).getNamedModelInstanceByName(with(key));
          will(returnValue(value));
        });
      }
    });

    return retval;
  }
}
