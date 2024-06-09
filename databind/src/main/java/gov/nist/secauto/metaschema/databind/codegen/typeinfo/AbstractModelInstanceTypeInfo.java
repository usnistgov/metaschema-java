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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.model.IGroupable;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

abstract class AbstractModelInstanceTypeInfo<INSTANCE extends IModelInstanceAbsolute>
    extends AbstractInstanceTypeInfo<INSTANCE, IAssemblyDefinitionTypeInfo>
    implements IModelInstanceTypeInfo {

  protected AbstractModelInstanceTypeInfo(
      @NonNull INSTANCE instance,
      @NonNull IAssemblyDefinitionTypeInfo parentDefinition) {
    super(instance, parentDefinition);
  }

  @Override
  public String getBaseName() {
    INSTANCE instance = getInstance();
    String baseName = getInstance().getGroupAsName();
    if (baseName == null) {
      throw new IllegalStateException(String.format(
          "Unable to derive the property name, due to missing group as name, for '%s' in the module '%s'.",
          instance.toCoordinates(),
          instance.getContainingModule().getLocation()));
    }
    return baseName;
  }

  @Override
  public @NonNull TypeName getJavaFieldType() {
    TypeName item = getJavaItemType();

    @NonNull TypeName retval;
    IModelInstanceAbsolute instance = getInstance();
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        retval = ObjectUtils.notNull(
            ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), item));
      } else {
        retval = ObjectUtils.notNull(ParameterizedTypeName.get(ClassName.get(List.class), item));
      }
    } else {
      retval = item;
    }

    return retval;
  }

  @NonNull
  protected abstract AnnotationSpec.Builder newBindingAnnotation();

  @Override
  public Set<IModelDefinition> buildField(
      TypeSpec.Builder typeBuilder,
      FieldSpec.Builder fieldBuilder) {
    Set<IModelDefinition> retval = new HashSet<>(super.buildField(typeBuilder, fieldBuilder));

    AnnotationSpec.Builder annotation = newBindingAnnotation();

    retval.addAll(buildBindingAnnotation(typeBuilder, fieldBuilder, annotation));

    fieldBuilder.addAnnotation(annotation.build());

    return retval;
  }

  @NonNull
  protected AnnotationSpec.Builder generateGroupAsAnnotation() {
    AnnotationSpec.Builder groupAsAnnoation = AnnotationSpec.builder(GroupAs.class);

    IModelInstanceAbsolute modelInstance = getInstance();

    groupAsAnnoation.addMember("name", "$S",
        ObjectUtils.requireNonNull(modelInstance.getGroupAsName(), "The grouping name must be non-null"));

    // TODO: handle group-as namespace as a prefix

    JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
    assert jsonGroupAsBehavior != null;
    if (!IGroupable.DEFAULT_JSON_GROUP_AS_BEHAVIOR.equals(jsonGroupAsBehavior)) {
      groupAsAnnoation.addMember("inJson", "$T.$L",
          JsonGroupAsBehavior.class, jsonGroupAsBehavior.toString());
    }

    XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
    assert xmlGroupAsBehavior != null;
    if (!IGroupable.DEFAULT_XML_GROUP_AS_BEHAVIOR.equals(xmlGroupAsBehavior)) {
      groupAsAnnoation.addMember("inXml", "$T.$L",
          XmlGroupAsBehavior.class, xmlGroupAsBehavior.toString());
    }
    return groupAsAnnoation;
  }
}
