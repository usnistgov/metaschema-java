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
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IGroupable;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ChoiceGroupTypeInfoImpl
    extends AbstractModelInstanceTypeInfo<IChoiceGroupInstance>
    implements IChoiceGroupTypeInfo {

  public ChoiceGroupTypeInfoImpl(
      @NonNull IChoiceGroupInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parent) {
    super(instance, parent);
  }

  @Override
  public TypeName getJavaItemType() {
    return getParentTypeInfo().getTypeResolver().getClassName(getInstance());
  }

  @Override
  protected AnnotationSpec.Builder newBindingAnnotation() {
    return ObjectUtils.notNull(AnnotationSpec.builder(BoundChoiceGroup.class));
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  @Override
  public Set<IModelDefinition> buildBindingAnnotation(
      TypeSpec.Builder typeBuilder,
      FieldSpec.Builder fieldBuilder,
      AnnotationSpec.Builder annotation) {
    IChoiceGroupInstance choiceGroup = getInstance();

    String discriminator = choiceGroup.getJsonDiscriminatorProperty();
    if (!IChoiceGroupInstance.DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME.equals(discriminator)) {
      annotation.addMember("discriminator", "$S", discriminator);
    }

    int minOccurs = choiceGroup.getMinOccurs();
    if (minOccurs != IGroupable.DEFAULT_GROUP_AS_MIN_OCCURS) {
      annotation.addMember("minOccurs", "$L", minOccurs);
    }

    int maxOccurs = choiceGroup.getMaxOccurs();
    if (maxOccurs != IGroupable.DEFAULT_GROUP_AS_MAX_OCCURS) {
      annotation.addMember("maxOccurs", "$L", maxOccurs);
    }

    String jsonKeyName = choiceGroup.getJsonKeyFlagInstanceName();
    if (jsonKeyName != null) {
      annotation.addMember("jsonKey", "$S", jsonKeyName);
    }

    Set<IModelDefinition> retval = new LinkedHashSet<>();

    IAssemblyDefinitionTypeInfo parentTypeInfo = getParentTypeInfo();
    ITypeResolver typeResolver = parentTypeInfo.getTypeResolver();

    Map<ClassName, List<INamedModelInstanceGrouped>> referencedDefinitions = new LinkedHashMap<>();
    Collection<? extends INamedModelInstanceGrouped> modelInstances = getInstance().getNamedModelInstances();
    for (INamedModelInstanceGrouped modelInstance : modelInstances) {
      ClassName className = typeResolver.getClassName(modelInstance.getDefinition());
      List<INamedModelInstanceGrouped> instances = referencedDefinitions.get(className);
      if (instances == null) {
        instances = new LinkedList<>(); // NOPMD needed
        referencedDefinitions.put(className, instances);
      }
      instances.add(modelInstance);
    }

    for (INamedModelInstanceGrouped modelInstance : modelInstances) {
      assert modelInstance != null;
      IGroupedNamedModelInstanceTypeInfo instanceTypeInfo = typeResolver.getTypeInfo(modelInstance, this);

      ClassName className = typeResolver.getClassName(modelInstance.getDefinition());
      retval.addAll(instanceTypeInfo.generateMemberAnnotation(
          annotation,
          typeBuilder,
          referencedDefinitions.get(className).size() > 1));
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      // requires a group-as
      annotation.addMember("groupAs", "$L", generateGroupAsAnnotation().build());
    }
    return retval;
  }
}
