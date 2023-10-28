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
import com.squareup.javapoet.FieldSpec.Builder;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.impl.AnnotationGenerator;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;

import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FieldInstanceTypeInfoImpl
    extends AbstractNamedModelInstanceTypeInfo<IFieldInstance>
    implements IFieldInstanceTypeInfo {

  public FieldInstanceTypeInfoImpl(
      @NonNull IFieldInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parentDefinition) {
    super(instance, parentDefinition);
  }

  @Override
  public TypeName getJavaItemType() {
    TypeName retval;
    IFieldInstance fieldInstance = getInstance();
    if (fieldInstance.getDefinition().isSimple()) {
      IDataTypeAdapter<?> dataType = fieldInstance.getDefinition().getJavaTypeAdapter();
      // this is a simple value
      retval = ObjectUtils.notNull(ClassName.get(dataType.getJavaClass()));
    } else {
      retval = super.getJavaItemType();
    }
    return retval;
  }

  @Override
  protected AnnotationSpec.Builder newBindingAnnotation() {
    return ObjectUtils.notNull(AnnotationSpec.builder(BoundField.class));
  }

  @Override
  public Set<IFlagContainer> buildField(FieldSpec.Builder builder) {
    Set<IFlagContainer> retval = super.buildField(builder);

    IFieldDefinition fieldDefinition = getInstance().getDefinition();

    // handle the field value related info
    if (fieldDefinition.isSimple()) {
      // this is a simple field, without flags
      // we need to add the BoundFieldValue annotation to the property
      // fieldAnnoation.addMember("valueName", "$S",
      // fieldDefinition.getJsonValueKeyName());
      IDataTypeAdapter<?> valueDataType = fieldDefinition.getJavaTypeAdapter();

      Object defaultValue = fieldDefinition.getDefaultValue();

      if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType) || defaultValue != null) {
        AnnotationSpec.Builder boundFieldValueAnnotation = AnnotationSpec.builder(BoundFieldValue.class);

        if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType)) {
          boundFieldValueAnnotation.addMember("typeAdapter", "$T.class", valueDataType.getClass());
        }

        if (defaultValue != null) {
          boundFieldValueAnnotation.addMember("defaultValue", "$S", valueDataType.asString(defaultValue));
        }
        builder.addAnnotation(boundFieldValueAnnotation.build());
      }

      AnnotationGenerator.buildValueConstraints(builder, fieldDefinition);
    }
    return retval;
  }

  @Override
  protected void buildFieldBinding(Builder fieldSpec, AnnotationSpec.Builder bindingAnnotationSpec) {
    super.buildFieldBinding(fieldSpec, bindingAnnotationSpec);

    IFieldInstance fieldInstance = getInstance();

    if (MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED != fieldInstance.isInXmlWrapped()) {
      bindingAnnotationSpec.addMember("inXmlWrapped", "$L", fieldInstance.isInXmlWrapped());
    }

    IDataTypeAdapter<?> valueDataType = fieldInstance.getDefinition().getJavaTypeAdapter();
    Object defaultValue = fieldInstance.getDefaultValue();
    if (defaultValue != null) {
      bindingAnnotationSpec.addMember("defaultValue", "$S", valueDataType.asString(defaultValue));
    }
  }

}
