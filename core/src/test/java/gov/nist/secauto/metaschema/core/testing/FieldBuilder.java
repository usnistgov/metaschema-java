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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jmock.Expectations;
import org.jmock.Mockery;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class FieldBuilder
    extends AbstractModelBuilder<FieldBuilder>
    implements IModelInstanceBuilder {

  private IDataTypeAdapter<?> dataTypeAdapter;
  private Object defaultValue = null;
  private List<FlagBuilder> flags;

  private FieldBuilder(@NonNull Mockery ctx) {
    super(ctx);
  }

  public static FieldBuilder builder(@NonNull Mockery ctx) {
    return new FieldBuilder(ctx).reset();
  }

  @Override
  public FieldBuilder reset() {
    this.dataTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    this.defaultValue = null;
    this.flags = CollectionUtil.emptyList();
    return this;
  }

  public FieldBuilder dataTypeAdapter(@NonNull IDataTypeAdapter<?> dataTypeAdapter) {
    this.dataTypeAdapter = dataTypeAdapter;
    return this;
  }

  public FieldBuilder defaultValue(@NonNull Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public FieldBuilder flags(@Nullable List<FlagBuilder> flags) {
    this.flags = flags == null ? CollectionUtil.emptyList() : flags;
    return this;
  }

  @Override
  @NonNull
  public IFieldInstance toInstance(
      @NonNull IAssemblyDefinition parent) {
    IFieldDefinition def = toDefinition();
    return toInstance(parent, def);
  }

  @NonNull
  public IFieldInstance toInstance(
      @NonNull IAssemblyDefinition parent,
      @NonNull IFieldDefinition definition) {
    validate();

    IFieldInstance retval = mock(IFieldInstance.class);
    applyNamedInstance(retval, definition, parent);
    return retval;
  }

  @NonNull
  public IFieldDefinition toDefinition() {
    validate();

    IFieldDefinition retval = mock(IFieldDefinition.class);
    applyDefinition(retval);

    Map<String, IFlagInstance> flags = this.flags.stream()
        .map(builder -> builder.toInstance(retval))
        .collect(Collectors.toUnmodifiableMap(
            IFlagInstance::getEffectiveName,
            Function.identity()));

    getContext().checking(new Expectations() {
      {
        allowing(retval).getJavaTypeAdapter();
        will(returnValue(dataTypeAdapter));
        allowing(retval).getDefaultValue();
        will(returnValue(defaultValue));
        allowing(retval).getFlagInstances();
        will(returnValue(flags.values()));
        flags.forEach((key, value) -> {
          allowing(retval).getFlagInstanceByName(with(key));
          will(returnValue(value));
        });
      }
    });

    return retval;
  }
}
