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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IDefinitionTypeInfo;

import java.util.Set;

import javax.lang.model.element.Modifier;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractPropertyTypeInfo<PARENT extends IDefinitionTypeInfo>
    extends AbstractTypeInfo<PARENT>
    implements IPropertyTypeInfo {

  public AbstractPropertyTypeInfo(@NonNull PARENT parentDefinition) {
    super(parentDefinition);
  }

  @Override
  public Set<IFlagContainer> build(@NonNull TypeSpec.Builder builder) {

    TypeName javaFieldType = getJavaFieldType();
    FieldSpec.Builder field = FieldSpec.builder(javaFieldType, getJavaFieldName())
        .addModifiers(Modifier.PRIVATE);
    assert field != null;

    final Set<IFlagContainer> retval = buildField(field);

    FieldSpec valueField = ObjectUtils.notNull(field.build());
    builder.addField(valueField);

    buildExtraMethods(builder, valueField);
    return retval;
  }

  protected void buildExtraMethods(TypeSpec.Builder builder, FieldSpec valueField) {

    TypeName javaFieldType = getJavaFieldType();
    String propertyName = getPropertyName();
    {
      MethodSpec.Builder method = MethodSpec.methodBuilder("get" + propertyName)
          .returns(javaFieldType)
          .addModifiers(Modifier.PUBLIC);
      assert method != null;
      method.addStatement("return $N", valueField);
      builder.addMethod(method.build());
    }

    {
      ParameterSpec valueParam = ParameterSpec.builder(javaFieldType, "value").build();
      MethodSpec.Builder method = MethodSpec.methodBuilder("set" + propertyName)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(valueParam);
      assert method != null;
      method.addStatement("$N = $N", valueField, valueParam);
      builder.addMethod(method.build());
    }
  }

  /**
   * Generate the Java field associated with this property.
   *
   * @param builder
   *          the field builder
   * @return the set of definitions used by this field
   */
  protected Set<IFlagContainer> buildField(@NonNull FieldSpec.Builder builder) {
    buildFieldJavadoc(builder);
    return CollectionUtil.emptySet();
  }
}
