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
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ChoiceGroupTypeInfoImpl
    extends AbstractModelInstanceTypeInfo<IChoiceGroupInstance, IAssemblyDefinitionTypeInfo>
    implements IChoiceGroupTypeInfo {

  public ChoiceGroupTypeInfoImpl(
      @NonNull IChoiceGroupInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parent) {
    super(instance, parent);
  }

  @Override
  public TypeName getJavaItemType() {
    return getParentDefinitionTypeInfo().getTypeResolver().getClassName(getInstance());
  }

  @Override
  protected AnnotationSpec.Builder newBindingAnnotation() {
    return ObjectUtils.notNull(AnnotationSpec.builder(BoundChoiceGroup.class));
  }

  @Override
  public AnnotationSpec.Builder buildBindingAnnotation() {
    AnnotationSpec.Builder retval = super.buildBindingAnnotation();

    IChoiceGroupInstance choiceGroup = getInstance();

    String discriminator = choiceGroup.getJsonDiscriminatorProperty();
    if (!MetaschemaModelConstants.DEFAULT_JSON_DISCRIMINATOR_PROPERTY_NAME.equals(discriminator)) {
      retval.addMember("discriminator", "$S", discriminator);
    }

    int minOccurs = choiceGroup.getMinOccurs();
    if (minOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS) {
      retval.addMember("minOccurs", "$L", minOccurs);
    }

    int maxOccurs = choiceGroup.getMaxOccurs();
    if (maxOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS) {
      retval.addMember("maxOccurs", "$L", maxOccurs);
    }

    String jsonKeyName = choiceGroup.getJsonKeyFlagName();
    if (jsonKeyName != null) {
      retval.addMember("jsonKey", "$S", jsonKeyName);
    }

    IAssemblyDefinitionTypeInfo parentTypeInfo = getParentDefinitionTypeInfo();
    ITypeResolver typeResolver = parentTypeInfo.getTypeResolver();
    for (INamedModelInstance modelInstance : getInstance().getNamedModelInstances()) {
      assert modelInstance != null;
      IModelInstanceTypeInfo instanceTypeInfo = typeResolver.getTypeInfo(modelInstance, parentTypeInfo);

      AnnotationSpec.Builder annotation = instanceTypeInfo.buildBindingAnnotation();

      annotation.addMember("binding", "$T.class", instanceTypeInfo.getJavaItemType());

      if (modelInstance instanceof IFieldInstance) {
        retval.addMember("fields", "$L", annotation.build());
      } else if (modelInstance instanceof IAssemblyInstance) {
        retval.addMember("assemblies", "$L", annotation.build());
      }
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      // requires a group-as
      retval.addMember("groupAs", "$L", generateGroupAsAnnotation().build());
    }
    return retval;
  }
}
