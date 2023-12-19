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

package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.datatype.DataTypeService;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA.DefineFlag;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefinitionFlagGlobal implements IFlagDefinition {
  @NonNull
  private final METASCHEMA.DefineFlag binding;
  @NonNull
  private final Map<QName, Set<String>> properties;

  public DefinitionFlagGlobal(@NonNull DefineFlag binding) {
    this.binding = binding;
    this.properties = ObjectUtils.notNull(binding.getProps().stream()
        .collect(
            Collectors.groupingBy(
                (prop) -> new QName(prop.getNamespace().toASCIIString(), prop.getName()),
                Collectors.mapping(
                    (prop) -> prop.getValue(),
                    Collectors.toUnmodifiableSet()))));
  }

  @NonNull
  protected METASCHEMA.DefineFlag getBinding() {
    return binding;
  }

  @Override
  public IDataTypeAdapter<?> getJavaTypeAdapter() {
    String asType = getBinding().getAsType();
    IDataTypeAdapter<?> retval;
    if (asType == null) {
      retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      retval = DataTypeService.getInstance().getJavaTypeAdapterByName(asType);
      if (retval == null) {
        throw new IllegalStateException("Unrecognized data type: " + asType);
      }
    }
    return retval;
  }

  @Override
  public String getFormalName() {
    return getBinding().getFormalName();
  }

  @Override
  public MarkupLine getDescription() {
    return getBinding().getDescription();
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return properties;
  }

  @Override
  public String getName() {
    return ObjectUtils.notNull(getBinding().getName());
  }

  @Override
  public ModuleScopeEnum getModuleScope() {
    return ModuleScopeEnum.valueOf(getBinding().getScope());
  }

  @Override
  public boolean isInline() {
    // TODO Auto-generated method stub
    return IFlagDefinition.super.isInline();
  }

  @Override
  public Object getDefaultValue() {
    // TODO Auto-generated method stub
    return IFlagDefinition.super.getDefaultValue();
  }

  @Override
  public Integer getIndex() {
    // TODO Auto-generated method stub
    return IFlagDefinition.super.getIndex();
  }

  @Override
  public String getUseName() {
    // TODO Auto-generated method stub
    return IFlagDefinition.super.getUseName();
  }

  @Override
  public Integer getUseIndex() {
    // TODO Auto-generated method stub
    return IFlagDefinition.super.getUseIndex();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return getBinding().getRemarks().getRemark();
  }

  @Override
  public IModule<?, ?, ?, ?, ?> getContainingModule() {
    getBinding().
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement");
  }

  @Override
  public IValueConstrained getConstraintSupport() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement");
  }

  @Override
  public IFlagInstance getInlineInstance() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("implement");
  }

}
