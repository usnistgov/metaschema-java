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

package gov.nist.secauto.metaschema.codegen.property;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.codegen.JavaClassGenerator;
import gov.nist.secauto.metaschema.codegen.support.ClassUtils;
import gov.nist.secauto.metaschema.codegen.type.TypeResolver;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;
import gov.nist.secauto.metaschema.model.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.instances.ModelInstance;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Responsible for supporting the generation of variables and methods related to a Java bean
 * property derived from a Metaschema flag, field, or assembly instance. These instances are part of
 * a Metaschema definition for which a Java class is to be generated.
 * 
 * @param <CLASS_GENERATOR>
 *          the containing Java class generator
 */
public abstract class AbstractPropertyGenerator<CLASS_GENERATOR extends JavaClassGenerator>
    implements PropertyGenerator {
  private final CLASS_GENERATOR classGenerator;
  private String propertyName;
  private String variableName;

  /**
   * Create a new property generator that used as part of generating the provided Java class.
   * 
   * @param classGenerator
   *          the containing Java class generator
   */
  public AbstractPropertyGenerator(CLASS_GENERATOR classGenerator) {
    this.classGenerator = classGenerator;
  }

  /**
   * Retrieves the Java class generator that this property will generate an individual class property
   * for.
   * 
   * @return the containing Java class generator
   */
  protected CLASS_GENERATOR getClassGenerator() {
    return classGenerator;
  }

  /**
   * The property name of the instance, which must be unique within the class.
   * 
   * @return the name
   */
  @Override
  public String getPropertyName() {
    if (this.propertyName == null) {
      String name = ClassUtils.toPropertyName(getInstanceName());
      if (name == null) {
        throw new NullPointerException("Instance name was null. Perhaps there is a missing group-as?");
      }
      // first check if a property already exists with the same name
      if (classGenerator.hasPropertyWithName(name)) {
        // append an integer value to make the name unique
        String newName;
        int index = 1;
        do {
          newName = ClassUtils.toPropertyName(name + Integer.toString(index));
          index++;
        } while (classGenerator.hasPropertyWithName(newName));
        name = newName;
      }

      this.propertyName = name;
    }
    return this.propertyName;
  }

  /**
   * Gets the name of the Java field for this property item.
   * 
   * @return the Java field name
   */
  public final String getJavaFieldName() {
    if (this.variableName == null) {
      this.variableName = "_" + ClassUtils.toVariableName(getPropertyName());
    }
    return this.variableName;
  }

  /**
   * Gets the type of the associated Java field for the property.
   * 
   * @return the Java type for the field
   */
  protected abstract TypeName getJavaType();

  /**
   * Get the name to use for the property. If the property is a collection type, then this will be the
   * group-as name, else this will be the use name.
   * 
   * @return the name
   * @see InfoElementInstance#getUseName()
   * @see ModelInstance#getGroupAsName()
   */
  protected abstract String getInstanceName();

  // protected void writeVariableJavadoc(PrintWriter writer) {
  // AbstractMarkupString description = getDescription();
  // if (description != null) {
  // writer.println("\t/**");
  // writer.println("\t * " + description.toHTML());
  // writer.println("\t */");
  // }
  // }

  @Override
  public Set<ObjectDefinition> build(TypeSpec.Builder builder, TypeResolver typeResolver) {
    FieldSpec.Builder field = FieldSpec.builder(getJavaType(), getJavaFieldName())
        .addModifiers(Modifier.PRIVATE);

    final Set<ObjectDefinition> retval = buildField(field);

    FieldSpec valueField = field.build();
    builder.addField(valueField);

    {
      MethodSpec.Builder getter = MethodSpec.methodBuilder("get" + getPropertyName())
          .returns(getJavaType())
          .addModifiers(Modifier.PUBLIC);
      buildGetter(getter, valueField);
      builder.addMethod(getter.build());
    }

    {
      MethodSpec.Builder setter = MethodSpec.methodBuilder("set" + getPropertyName())
          .addModifiers(Modifier.PUBLIC);
      ParameterSpec valueParam = ParameterSpec.builder(getJavaType(), "value").build();
      setter.addParameter(valueParam);
      buildSetter(setter, valueParam, valueField);
      builder.addMethod(setter.build());
    }
    return retval;
  }

  protected Set<ObjectDefinition> buildField(FieldSpec.Builder builder) {
    MarkupLine description = getDescription();
    if (description != null) {
      builder.addJavadoc("$S", description.toHtml());
    }
    return Collections.emptySet();
  }

  protected void buildGetter(MethodSpec.Builder builder, FieldSpec valueField) {
    builder.addStatement("return $N", valueField);
  }

  private void buildSetter(MethodSpec.Builder builder, ParameterSpec valueParam, FieldSpec valueField) {
    builder.addStatement("$N = $N", valueField, valueParam);
  }
}
