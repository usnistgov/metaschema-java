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

package gov.nist.secauto.metaschema.databind.model.metaschema.impl;

import gov.nist.secauto.metaschema.core.datatype.DataTypeService;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.METASCHEMA.DefineAssembly.RootName;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.Property;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.Remarks;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.UseName;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ModelSupport {
  private ModelSupport() {
    // disable construction
  }

  @NonNull
  public static Map<QName, Set<String>> parseProperties(@NonNull List<Property> props) {
    return CollectionUtil.unmodifiableMap(ObjectUtils.notNull(props.stream()
        .collect(
            Collectors.groupingBy(
                (prop) -> new QName(prop.getNamespace().toASCIIString(), prop.getName()),
                Collectors.mapping(
                    (prop) -> prop.getValue(),
                    Collectors.toCollection(LinkedHashSet::new))))));
  }

  public static boolean yesOrNo(String allowOther) {
    return "yes".equals(allowOther);
  }

  @SuppressWarnings("PMD.ImplicitSwitchFallThrough")
  @NonNull
  public static ModuleScopeEnum moduleScope(@NonNull String value) {
    ModuleScopeEnum retval;
    switch (value) {
    case "local":
      retval = ModuleScopeEnum.LOCAL;
      break;
    case "global":
    default:
      retval = ModuleScopeEnum.INHERITED;
    }
    return retval;
  }

  @Nullable
  public static Integer index(@Nullable BigInteger index) {
    return index == null ? null : index.intValueExact();
  }

  @Nullable
  public static String useName(@Nullable UseName useName) {
    return useName == null ? null : useName.getName();
  }

  @Nullable
  public static Integer useIndex(@Nullable UseName useName) {
    BigInteger index = useName == null ? null : useName.getIndex();
    return index == null ? null : index.intValueExact();
  }

  @Nullable
  public static MarkupMultiline remarks(@Nullable Remarks remarks) {
    return remarks == null ? null : remarks.getRemark();
  }

  @NonNull
  public static IDataTypeAdapter<?> dataType(@Nullable String dataType) {
    IDataTypeAdapter<?> retval;
    if (dataType == null) {
      retval = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      retval = DataTypeService.getInstance().getJavaTypeAdapterByName(dataType);
      if (retval == null) {
        throw new IllegalStateException("Unrecognized data type: " + dataType);
      }
    }
    return retval;
  }

  @Nullable
  public static Object defaultValue(
      @Nullable String defaultValue,
      @NonNull IDataTypeAdapter<?> javaTypeAdapter) {
    return defaultValue == null ? null : ModelUtil.resolveDefaultValue(defaultValue, javaTypeAdapter);
  }

  public static int maxOccurs(@NonNull String maxOccurs) {
    return "unbounded".equals(maxOccurs) ? -1 : Integer.parseInt(maxOccurs);
  }

  public static String rootName(@Nullable RootName rootName) {
    return rootName == null ? null : rootName.getName();
  }

  public static Integer rootIndex(@Nullable RootName rootName) {
    BigInteger index = rootName == null ? null : rootName.getIndex();
    return index == null ? null : index.intValueExact();
  }
}
