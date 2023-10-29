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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IModelDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundChoiceGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ChoiceGroupTypeInfoImpl
    extends AbstractModelInstanceTypeInfo<IChoiceGroupInstance, IAssemblyDefinitionTypeInfo>
    implements IChoiceGroupTypeInfo {

  @NonNull
  private final List<INamedModelInstanceTypeInfo> modelInstances;

  public ChoiceGroupTypeInfoImpl(
      @NonNull IChoiceGroupInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parent) {
    super(instance, parent);
    this.modelInstances = ObjectUtils.notNull(getInstance().getNamedModelInstances().stream()
        .map(modelInstance -> {
          assert modelInstance != null;
          IAssemblyDefinitionTypeInfo parentTypeInfo = getParentDefinitionTypeInfo();
          return parentTypeInfo.getTypeResolver().getTypeInfo(modelInstance, parentTypeInfo);
        })
        .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public TypeName getJavaItemType() {
    return getParentDefinitionTypeInfo().getTypeResolver().getClassName(getInstance());
  }

  @Override
  public Set<IFlagContainer> buildField(FieldSpec.Builder builder) {
    Set<IFlagContainer> retval = new HashSet<>(super.buildField(builder));
    // Add all simple definition to the list of classes to build as child classes
    modelInstances.stream()
        .map(instance -> instance.getInstance().getDefinition())
        .filter(definition -> definition.isSimple())
        .forEachOrdered(definition -> retval.add(definition));
    return retval;
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

    IAssemblyDefinitionTypeInfo parentTypeInfo = getParentDefinitionTypeInfo();
    ITypeResolver typeResolver = parentTypeInfo.getTypeResolver();
    for (INamedModelInstanceTypeInfo instanceTypeInfo : modelInstances) {
      assert instanceTypeInfo != null;
      INamedModelInstance instance = instanceTypeInfo.getInstance();

      AnnotationSpec.Builder annotation = ObjectUtils.notNull(
          AnnotationSpec.builder(BoundChoiceGroup.ModelInstance.class));

      {
        String formalName = instance.getFormalName();
        if (formalName != null) {
          annotation.addMember("formalName", "$S", formalName);
        }
      }

      {
        MarkupLine description = instance.getDescription();
        if (description != null) {
          annotation.addMember("description", "$S", description.toMarkdown());
        }
      }

      {
        String useName = instance.getUseName();
        if (useName != null) {
          annotation.addMember("useName", "$S", useName);
        }
      }

      {
        Integer useIndex = instance.getUseIndex();
        if (useIndex != null) {
          annotation.addMember("useIndex", "$L", useIndex);
        }
      }

      {
        String namespace = instance.getXmlNamespace();
        if (namespace == null) {
          retval.addMember("namespace", "$S", "##none");
        } else if (!instance.getContainingModule().getXmlNamespace().toASCIIString().equals(namespace)) {
          retval.addMember("namespace", "$S", namespace);
        } // otherwise use the ##default
      }

      {
        MarkupMultiline remarks = instance.getRemarks();
        if (remarks != null) {
          retval.addMember("remarks", "$S", remarks.toMarkdown());
        }
      }

      {
        IModelDefinitionTypeInfo definitionTypeInfo = typeResolver.getTypeInfo(instance.getDefinition());
        retval.addMember("type", "$T.class", definitionTypeInfo.getClassName());
      }

      retval.addMember("modelInstances", "$L", annotation);
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      // requires a group-as
      retval.addMember("groupAs", "$L", generateGroupAsAnnotation().build());
    }
    return retval;
  }
}
