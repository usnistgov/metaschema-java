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

package gov.nist.secauto.metaschema.databind.codegen;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Set;

import javax.lang.model.element.Modifier;

import edu.umd.cs.findbugs.annotations.NonNull;

abstract class AbstractTypeInfo<PARENT extends IDefinitionTypeInfo> implements ITypeInfo {
  @NonNull
  private final PARENT parentDefinition;
  private String propertyName;
  private String fieldName;

  public AbstractTypeInfo(@NonNull PARENT parentDefinition) {
    this.parentDefinition = parentDefinition;
  }

  @NonNull
  protected PARENT getParentDefinitionTypeInfo() {
    return parentDefinition;
  }

  /**
   * The property name of the instance, which must be unique within the class.
   *
   * @return the name
   */
  @Override
  @NonNull
  public String getPropertyName() {
    synchronized (this) {
      if (this.propertyName == null) {
        String name = ClassUtils.toPropertyName(getBaseName());
        IDefinitionTypeInfo parent = getParentDefinitionTypeInfo();

        // first check if a property already exists with the same name

        if (parent.hasPropertyWithName(name)) {
          // append an integer value to make the name unique
          String newName;
          int index = 1;
          do {
            newName = ClassUtils.toPropertyName(name + Integer.toString(index));
            index++;
          } while (parent.hasPropertyWithName(newName));
          name = newName;
        }
        this.propertyName = name;
      }
      return ObjectUtils.notNull(this.propertyName);
    }
  }

  /**
   * Gets the name of the Java field for this property item.
   *
   * @return the Java field name
   */
  @Override
  @NonNull
  public final String getJavaFieldName() {
    synchronized (this) {
      if (this.fieldName == null) {
        this.fieldName = "_" + ClassUtils.toVariableName(getPropertyName());
      }
      return ObjectUtils.notNull(this.fieldName);
    }
  }

  @Override
  public Set<IFlagContainer> build(@NonNull TypeSpec.Builder builder, ITypeResolver typeResolver) {
    FieldSpec.Builder field = FieldSpec.builder(getJavaFieldType(), getJavaFieldName())
        .addModifiers(Modifier.PRIVATE);
    assert field != null;

    final Set<IFlagContainer> retval = buildField(field);

    FieldSpec valueField = ObjectUtils.notNull(field.build());
    builder.addField(valueField);

    {
      MethodSpec.Builder method = MethodSpec.methodBuilder("get" + getPropertyName())
          .returns(getJavaFieldType())
          .addModifiers(Modifier.PUBLIC);
      assert method != null;
      buildGetter(method, valueField);
      builder.addMethod(method.build());
    }

    {
      ParameterSpec valueParam = ParameterSpec.builder(getJavaFieldType(), "value").build();

      MethodSpec.Builder method = MethodSpec.methodBuilder("set" + getPropertyName())
          .addModifiers(Modifier.PUBLIC)
          .addParameter(valueParam);
      assert method != null;
      buildSetter(method, valueParam, valueField);
      builder.addMethod(method.build());
    }

    buildExtraMethods(builder, valueField, typeResolver);
    return retval;
  }

  /**
   * This method can be implemented by subclasses to create additional methods.
   *
   * @param builder
   *          the class builder
   * @param valueField
   *          the field corresponding to this property
   * @param typeResolver
   *          the resolver used to get type information
   */
  protected void buildExtraMethods( // NOPMD - intentional
      @NonNull TypeSpec.Builder builder,
      @NonNull FieldSpec valueField,
      @NonNull ITypeResolver typeResolver) {
    // do nothing by default
  }

  /**
   * Generate the Java field associated with this property.
   *
   * @param builder
   *          the field builder
   * @return the set of definitions used by this field
   */
  @NonNull
  protected abstract Set<IFlagContainer> buildField(@NonNull FieldSpec.Builder builder);

  /**
   * Generate the getter for the property.
   *
   * @param builder
   *          the method builder
   * @param valueField
   *          the field containing the value to get
   */
  protected void buildGetter(@NonNull MethodSpec.Builder builder, @NonNull FieldSpec valueField) {
    builder.addStatement("return $N", valueField);
  }

  /**
   * Generate the setter for the property.
   *
   * @param builder
   *          the method builder
   * @param valueParam
   *          the parameter value to set
   * @param valueField
   *          the field containing the value to set
   */
  protected void buildSetter(MethodSpec.Builder builder, ParameterSpec valueParam, FieldSpec valueField) {
    builder.addStatement("$N = $N", valueField, valueParam);
  }
}
