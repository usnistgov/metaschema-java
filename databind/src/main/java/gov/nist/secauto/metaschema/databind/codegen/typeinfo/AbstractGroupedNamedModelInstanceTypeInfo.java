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
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractGroupedNamedModelInstanceTypeInfo<I extends INamedModelInstanceGrouped>
    implements IGroupedNamedModelInstanceTypeInfo {
  @NonNull
  private final I instance;
  @NonNull
  private final IChoiceGroupTypeInfo parentTypeInfo;

  protected AbstractGroupedNamedModelInstanceTypeInfo(
      @NonNull I instance,
      @NonNull IChoiceGroupTypeInfo parentTypeInfo) {
    this.instance = instance;
    this.parentTypeInfo = parentTypeInfo;
  }

  protected abstract Class<? extends Annotation> getBindingAnnotation();

  protected abstract void applyInstanceAnnotation(
      @NonNull AnnotationSpec.Builder instanceAnnotation,
      @NonNull AnnotationSpec.Builder choiceGroupAnnotation);

  @NonNull
  protected I getInstance() {
    return instance;
  }

  protected IChoiceGroupTypeInfo getChoiceGroupTypeInfo() {
    return parentTypeInfo;
  }

  @Override
  public Set<IModelDefinition> generateMemberAnnotation(
      @NonNull AnnotationSpec.Builder choiceGroupAnnotation,
      @NonNull TypeSpec.Builder typeBuilder,
      boolean requireExtension) {

    AnnotationSpec.Builder memberAnnotation = ObjectUtils.notNull(AnnotationSpec.builder(getBindingAnnotation()));

    TypeInfoUtils.buildCommonBindingAnnotationValues(getInstance(), memberAnnotation);

    Set<IModelDefinition> retval = new LinkedHashSet<>();

    I instance = getInstance();
    IModelDefinition definition = getInstance().getDefinition();

    IChoiceGroupTypeInfo choiceGroupTypeInfo = getChoiceGroupTypeInfo();
    ITypeResolver typeResolver = choiceGroupTypeInfo.getParentTypeInfo().getTypeResolver();

    ClassName itemTypeName;
    if (definition.isInline()) {
      // these definitions will be generated as standalone child classes
      itemTypeName = typeResolver.getClassName(definition);
      retval.add(definition);
    } else if (requireExtension) {
      // these definitions will be generated as an extension of a global class
      ClassName extendedClassName = typeResolver.getClassName(definition);
      itemTypeName = typeResolver.getSubclassName(
          choiceGroupTypeInfo.getParentTypeInfo().getClassName(),
          ObjectUtils.notNull(StringUtils.capitalize(instance.getEffectiveDisciminatorValue())),
          definition);

      TypeSpec.Builder subClass = TypeSpec.classBuilder(itemTypeName);
      subClass.superclass(extendedClassName);
      subClass.addModifiers(Modifier.PUBLIC, Modifier.STATIC); // , Modifier.FINAL);
      // subClass.addField(
      // FieldSpec.builder(String.class, "DISCRIMINATOR", Modifier.PUBLIC,
      // Modifier.STATIC, Modifier.FINAL)
      // .initializer("\"" + instance.getEffectiveDisciminatorValue() + "\"")
      // .build());
      typeBuilder.addType(subClass.build());
    } else {
      // reference the global class
      itemTypeName = typeResolver.getClassName(definition);
    }

    memberAnnotation.addMember("binding", "$T.class", itemTypeName);

    applyInstanceAnnotation(memberAnnotation, choiceGroupAnnotation);

    return retval;
  }

}
