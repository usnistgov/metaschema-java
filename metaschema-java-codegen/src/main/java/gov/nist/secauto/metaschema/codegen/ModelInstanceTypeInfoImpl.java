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

package gov.nist.secauto.metaschema.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.binding.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundField;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagContainer;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

import edu.umd.cs.findbugs.annotations.NonNull;

class ModelInstanceTypeInfoImpl
    extends AbstractInstanceTypeInfo<INamedModelInstance, IAssemblyDefinitionTypeInfo>
    implements IModelInstanceTypeInfo {
  private static final Logger LOGGER = LogManager.getLogger(ModelInstanceTypeInfoImpl.class);

  public ModelInstanceTypeInfoImpl(@NonNull INamedModelInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parentDefinition) {
    super(instance, parentDefinition);
  }

  @Override
  public @NonNull String getBaseName() {
    INamedModelInstance modelInstance = getInstance();
    String retval;
    if (modelInstance.getMaxOccurs() == -1 || modelInstance.getMaxOccurs() > 1) {
      retval = ObjectUtils.notNull(modelInstance.getGroupAsName());
    } else {
      retval = modelInstance.getEffectiveName();
    }
    return retval;
  }

  @Override
  public String getItemBaseName() {
    return getInstance().getEffectiveName();
  }

  @Override
  public @NonNull TypeName getJavaItemType() {
    INamedModelInstance instance = getInstance();

    TypeName retval;
    if (instance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) instance;
      if (fieldInstance.isSimple()) {
        IDataTypeAdapter<?> dataType = fieldInstance.getDefinition().getJavaTypeAdapter();
        // this is a simple value
        retval = ObjectUtils.notNull(ClassName.get(dataType.getJavaClass()));
      } else {
        retval = getParentDefinitionTypeInfo().getTypeResolver().getClassName(fieldInstance.getDefinition());
      }
    } else if (instance instanceof IAssemblyInstance) {
      IAssemblyInstance assemblyInstance = (IAssemblyInstance) instance;
      retval = getParentDefinitionTypeInfo().getTypeResolver().getClassName(assemblyInstance.getDefinition());
    } else {
      String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
      LOGGER.error(msg);
      throw new IllegalStateException(msg);
    }
    return retval;
  }

  @Override
  public @NonNull TypeName getJavaFieldType() {
    TypeName item = getJavaItemType();

    @NonNull TypeName retval;
    INamedModelInstance instance = getInstance();
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

  @Override
  public Set<IFlagContainer> buildField(FieldSpec.Builder builder) { // NOPMD - intentional
    Set<IFlagContainer> retval = new HashSet<>();
    retval.addAll(super.buildField(builder));

    // determine which annotation to apply
    AnnotationSpec.Builder javaFieldAnnoation;
    INamedModelInstance modelInstance = getInstance();
    if (modelInstance instanceof IFieldInstance) {
      javaFieldAnnoation = AnnotationSpec.builder(BoundField.class);
    } else if (modelInstance instanceof IAssemblyInstance) {
      javaFieldAnnoation = AnnotationSpec.builder(BoundAssembly.class);
    } else {
      throw new UnsupportedOperationException(String.format("ModelContainer instance '%s' of type '%s' is not supported.",
          modelInstance.getName(), modelInstance.getClass().getName()));
    }

    String formalName = modelInstance.getEffectiveFormalName();
    if (formalName != null) {
      javaFieldAnnoation.addMember("formalName", "$S", formalName);
    }

    MarkupLine description = modelInstance.getEffectiveDescription();
    if (description != null) {
      javaFieldAnnoation.addMember("description", "$S", description.toMarkdown());
    }

    javaFieldAnnoation.addMember("useName", "$S", modelInstance.getEffectiveName());

    IFlagContainer definition = modelInstance.getDefinition();
    if (definition.isInline() && !(definition instanceof IFieldDefinition && definition.isSimple())) {
      // this is an inline definition that must be built as a child class
      retval.add(definition);
    }

    String namespace = modelInstance.getXmlNamespace();
    if (namespace == null) {
      javaFieldAnnoation.addMember("namespace", "$S", "##none");
    } else if (!modelInstance.getContainingMetaschema().getXmlNamespace().toASCIIString().equals(namespace)) {
      javaFieldAnnoation.addMember("namespace", "$S", namespace);
    } // otherwise use the ##default

    int minOccurs = modelInstance.getMinOccurs();
    if (minOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS) {
      javaFieldAnnoation.addMember("minOccurs", "$L", minOccurs);
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    if (maxOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS) {
      javaFieldAnnoation.addMember("maxOccurs", "$L", maxOccurs);
    }

    MarkupMultiline remarks = modelInstance.getRemarks();
    if (remarks != null) {
      javaFieldAnnoation.addMember("remarks", "$S", remarks.toMarkdown());
    }

    if (modelInstance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;

      if (MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED != fieldInstance.isInXmlWrapped()) {
        javaFieldAnnoation.addMember("inXmlWrapped", "$L", fieldInstance.isInXmlWrapped());
      }
    }

    builder.addAnnotation(javaFieldAnnoation.build());

    if (modelInstance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;
      if (fieldInstance.isSimple()) {
        // this is a simple field, without flags
        // we need to add the BoundFieldValue annotation to the property
        // fieldAnnoation.addMember("valueName", "$S", fieldDefinition.getJsonValueKeyName());
        IFieldDefinition fieldDefinition = (IFieldDefinition) definition;
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

        AnnotationUtils.buildValueConstraints(builder, fieldDefinition);
      }
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      AnnotationSpec.Builder groupAsAnnoation = AnnotationSpec.builder(GroupAs.class);

      groupAsAnnoation.addMember("name", "$S",
          ObjectUtils.requireNonNull(modelInstance.getGroupAsName(), "The grouping name must be non-null"));

      String groupAsNamespace = modelInstance.getGroupAsXmlNamespace();
      if (groupAsNamespace == null) {
        groupAsAnnoation.addMember("namespace", "$S", "##none");
      } else if (!modelInstance.getContainingMetaschema().getXmlNamespace().toASCIIString().equals(groupAsNamespace)) {
        groupAsAnnoation.addMember("namespace", "$S", groupAsNamespace);
      } // otherwise use the ##default

      JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
      assert jsonGroupAsBehavior != null;
      if (!MetaschemaModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR.equals(jsonGroupAsBehavior)) {
        groupAsAnnoation.addMember("inJson", "$T.$L",
            JsonGroupAsBehavior.class, jsonGroupAsBehavior.toString());
      }

      XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
      assert xmlGroupAsBehavior != null;
      if (!MetaschemaModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR.equals(xmlGroupAsBehavior)) {
        groupAsAnnoation.addMember("inXml", "$T.$L",
            XmlGroupAsBehavior.class, xmlGroupAsBehavior.toString());
      }

      builder.addAnnotation(groupAsAnnoation.build());
    }

    return retval.isEmpty() ? CollectionUtil.emptySet() : CollectionUtil.unmodifiableSet(retval);
  }

  @Override
  public void buildExtraMethods(@NonNull TypeSpec.Builder builder, @NonNull FieldSpec valueField,
      @NonNull ITypeResolver typeResolver) {
    INamedModelInstance instance = getInstance();
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      TypeName itemType = getJavaItemType();
      ParameterSpec valueParam = ParameterSpec.builder(itemType, "item").build();

      String itemPropertyName = ClassUtils.toPropertyName(getItemBaseName());

      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        IFlagInstance jsonKey = instance.getJsonKeyFlagInstance();
        if (jsonKey == null) {
          throw new IllegalStateException(
              String.format("JSON key not defined for property: %s", instance.toCoordinates()));
        }

        // get the json key property on the instance's definition
        IModelDefinitionTypeInfo instanceTypeInfo = typeResolver.getTypeInfo(instance.getDefinition());
        IFlagInstanceTypeInfo jsonKeyTypeInfo = instanceTypeInfo.getFlagInstanceTypeInfo(jsonKey);

        if (jsonKeyTypeInfo == null) {
          throw new IllegalStateException(
              String.format("Unable to identify JSON key for property: %s", instance.toCoordinates()));
        }

        {
          // create add method
          MethodSpec.Builder method = MethodSpec.methodBuilder("add" + itemPropertyName)
              .addParameter(valueParam)
              .returns(itemType)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Add a new {@link $T} item to the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to add\n")
              .addJavadoc("@return the existing {@link $T} item in the collection or {@code null} if not item exists\n",
                  itemType)
              .addStatement("$1T value = $2T.requireNonNull($3N,\"$3N value cannot be null\")",
                  itemType, ObjectUtils.class, valueParam)
              .addStatement("$1T key = $2T.requireNonNull($3N.$4N(),\"$3N key cannot be null\")",
                  String.class, ObjectUtils.class, valueParam, "get" + jsonKeyTypeInfo.getPropertyName())
              .beginControlFlow("if ($N == null)", valueField)
              .addStatement("$N = new $T<>()", valueField, LinkedHashMap.class)
              .endControlFlow()
              .addStatement("return $N.put(key, value)", valueField);

          builder.addMethod(method.build());
        }
        {
          // create remove method
          MethodSpec.Builder method = MethodSpec.methodBuilder("remove" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Remove the {@link $T} item from the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to remove\n")
              .addJavadoc("@return {@code true} if the item was removed or {@code false} otherwise\n")
              .addStatement("$1T value = $2T.requireNonNull($3N,\"$3N value cannot be null\")",
                  itemType, ObjectUtils.class, valueParam)
              .addStatement("$1T key = $2T.requireNonNull($3N.$4N(),\"$3N key cannot be null\")",
                  String.class, ObjectUtils.class, valueParam, "get" + jsonKeyTypeInfo.getPropertyName())
              .addStatement("return $1N == null ? false : $1N.remove(key, value)", valueField);
          builder.addMethod(method.build());
        }
      } else {
        {
          // create add method
          MethodSpec.Builder method = MethodSpec.methodBuilder("add" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Add a new {@link $T} item to the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to add\n")
              .addJavadoc("@return {@code true}\n")
              .addStatement("$T value = $T.requireNonNull($N,\"$N cannot be null\")",
                  itemType, ObjectUtils.class, valueParam, valueParam)
              .beginControlFlow("if ($N == null)", valueField)
              .addStatement("$N = new $T<>()", valueField, LinkedList.class)
              .endControlFlow()
              .addStatement("return $N.add(value)", valueField);

          builder.addMethod(method.build());
        }

        {
          // create remove method
          MethodSpec.Builder method = MethodSpec.methodBuilder("remove" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Remove the first matching {@link $T} item from the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to remove\n")
              .addJavadoc("@return {@code true} if the item was removed or {@code false} otherwise\n")
              .addStatement("$T value = $T.requireNonNull($N,\"$N cannot be null\")",
                  itemType, ObjectUtils.class, valueParam, valueParam)
              .addStatement("return $1N == null ? false : $1N.remove(value)", valueField);
          builder.addMethod(method.build());
        }
      }
    }
  }
}
