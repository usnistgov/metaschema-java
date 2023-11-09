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

package gov.nist.secauto.metaschema.databind.codegen.typeinfo;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IFieldDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;

import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

class FieldValueTypeInfoImpl
    extends AbstractPropertyTypeInfo<IFieldDefinitionTypeInfo>
    implements IFieldValueTypeInfo {

  public FieldValueTypeInfoImpl(@NonNull IFieldDefinitionTypeInfo parentDefinition) {
    super(parentDefinition);
  }

  @Override
  public String getBaseName() {
    return "value";
  }

  @SuppressWarnings("null")
  @Override
  public TypeName getJavaFieldType() {
    return ClassName.get(
        getParentDefinitionTypeInfo().getDefinition().getJavaTypeAdapter().getJavaClass());
  }

  @Override
  protected Set<IFlagContainer> buildField(FieldSpec.Builder builder) {

    IFieldDefinition definition = getParentDefinitionTypeInfo().getDefinition();
    AnnotationSpec.Builder fieldValue = AnnotationSpec.builder(BoundFieldValue.class);

    IDataTypeAdapter<?> valueDataType = definition.getJavaTypeAdapter();

    // a field object always has a single value
    if (!definition.hasJsonValueKeyFlagInstance()) {
      fieldValue.addMember("valueKeyName", "$S", definition.getJsonValueKeyName());
    } // else do nothing, the annotation will be on the flag

    if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType)) {
      fieldValue.addMember("typeAdapter", "$T.class", valueDataType.getClass());
    }

    Object defaultValue = definition.getDefaultValue();
    if (defaultValue != null) {
      fieldValue.addMember("defaultValue", "$S", valueDataType.asString(defaultValue));
    }

    Set<IFlagContainer> retval = super.buildField(builder);

    builder.addAnnotation(fieldValue.build());
    return retval;
  }
}
