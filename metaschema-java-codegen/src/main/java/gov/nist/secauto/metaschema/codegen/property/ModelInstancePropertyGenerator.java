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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.codegen.AssemblyJavaClassGenerator;
import gov.nist.secauto.metaschema.codegen.support.AnnotationUtils;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;
import gov.nist.secauto.metaschema.model.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.instances.ModelInstance;
import gov.nist.secauto.metaschema.model.instances.ObjectModelInstance;
import gov.nist.secauto.metaschema.model.instances.XmlGroupAsBehavior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

public class ModelInstancePropertyGenerator
    extends AbstractPropertyGenerator<AssemblyJavaClassGenerator> {
  private static final Logger logger = LogManager.getLogger(ModelInstancePropertyGenerator.class);

  private final ObjectModelInstance<?> modelInstance;

  /**
   * Constructs a new generator of cardinality information for a property item of a Metaschema
   * assembly.
   * 
   * @param modelInstance
   *          the property instance to associate the cardinality information with
   * @param classGenerator
   *          the containing class to generate
   */
  public ModelInstancePropertyGenerator(ObjectModelInstance<?> modelInstance,
      AssemblyJavaClassGenerator classGenerator) {
    super(classGenerator);
    this.modelInstance = modelInstance;
  }

  /**
   * Gets the details of the associated Metaschema model instance.
   * 
   * @return the name
   */
  protected ObjectModelInstance<?> getModelInstance() {
    return modelInstance;
  }

  @Override
  protected String getInstanceName() {
    ModelInstance modelInstance = getModelInstance();
    String retval = null;
    if (modelInstance.getMaxOccurs() == 1) {
      // an instance name only applies to an assembly or field. a choice does not have a name and doesn't
      // result in a generated instance.
      if (modelInstance instanceof AssemblyInstance) {
        retval = ((AssemblyInstance<?>) modelInstance).getUseName();
      } else if (modelInstance instanceof FieldInstance) {
        retval = ((FieldInstance<?>) modelInstance).getUseName();
      }
    } else if (modelInstance.getMaxOccurs() == -1 || modelInstance.getMaxOccurs() > 1) {
      retval = modelInstance.getGroupAsName();
    }
    return retval;
  }

  @Override
  public MarkupLine getDescription() {
    MarkupLine retval = null;
    // instances will only need a description if the instance has a simple structure, without flags or a
    // model. In such a case, the description will need to appear on the instance. Otherwise, the
    // description will appear on the resulting object.
    if (modelInstance instanceof ObjectModelInstance) {
      retval = ((ObjectModelInstance<?>) modelInstance).getDefinition().getDescription();
    }
    return retval;
  }

  @Override
  public Set<ObjectDefinition> buildField(FieldSpec.Builder builder) {
    Set<ObjectDefinition> retval = new HashSet<>();
    retval.addAll(super.buildField(builder));

    AnnotationSpec.Builder fieldAnnoation;
    ObjectModelInstance<?> modelInstance = getModelInstance();
    if (modelInstance instanceof FieldInstance) {
      FieldInstance<?> fieldInstance = (FieldInstance<?>) modelInstance;
      if (DataType.EMPTY.equals(fieldInstance.getDefinition().getDatatype())) {
        // treat "empty" fields as an assembly
        fieldAnnoation = AnnotationSpec.builder(Assembly.class);
      } else {
        fieldAnnoation = AnnotationSpec.builder(Field.class);
      }
    } else if (modelInstance instanceof AssemblyInstance) {
      fieldAnnoation = AnnotationSpec.builder(Assembly.class);
    } else {
      throw new UnsupportedOperationException(String.format("Model instance '%s' of type '%s' is not supported.",
          modelInstance.getName(), modelInstance.getClass().getName()));
    }

    ObjectDefinition definition = getModelInstance().getDefinition();
    if (!definition.isGlobal()) {
      retval.add(definition);
    }
    fieldAnnoation.addMember("name", "$S", getModelInstance().getUseName());

    String namespace = definition.getContainingMetaschema().getXmlNamespace().toString();
    String containingNamespace
        = getModelInstance().getContainingDefinition().getContainingMetaschema().getXmlNamespace().toString();
    if (!containingNamespace.equals(namespace)) {
      fieldAnnoation.addMember("namespace", "$S", namespace);
    }

    if (modelInstance instanceof FieldInstance) {
      FieldInstance<?> fieldInstance = (FieldInstance<?>) modelInstance;
      FieldDefinition fieldDefinition = (FieldDefinition) definition;
      DataType valueDataType = FieldValuePropertyGenerator.getValueDataType(fieldDefinition);

      // a field object always has a single value, unless its "empty", which is treated as an empty
      // assembly
      if (!DataType.EMPTY.equals(valueDataType)) {
        if (!fieldInstance.hasXmlWrapper()) {
          fieldAnnoation.addMember("inXmlWrapped", "$L", false);
        }
        if (fieldDefinition.getFlagInstances().isEmpty()) {
          // this is a simple field, without flags
          // we need to add the FieldValue annotation to the property

          fieldAnnoation.addMember("valueName", "$S", fieldDefinition.getJsonValueKeyName());

          fieldAnnoation.addMember("typeAdapter", "$T.class",
              valueDataType.getJavaTypeAdapterClass());
        }
      }
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    int minOccurs = modelInstance.getMinOccurs();

    if (minOccurs != 0) {
      fieldAnnoation.addMember("minOccurs", "$L", minOccurs);
    }
    if (maxOccurs != 1) {
      fieldAnnoation.addMember("maxOccurs", "$L", maxOccurs);
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      fieldAnnoation.addMember("groupName", "$S", getInstanceName());

      if (!containingNamespace.equals(namespace)) {
        fieldAnnoation.addMember("groupNamespace", "$S", namespace);
      }

      JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
      if (!AnnotationUtils.getDefaultValue(Field.class, "inJson").equals(jsonGroupAsBehavior)) {
        fieldAnnoation.addMember("inJson", "$T.$L",
            gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior.class,
            jsonGroupAsBehavior.toString());
      }

      XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
      if (!AnnotationUtils.getDefaultValue(Field.class, "inXml").equals(xmlGroupAsBehavior)) {
        fieldAnnoation.addMember("inXml", "$T.$L",
            gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior.class,
            xmlGroupAsBehavior.toString());
      }
    }
    builder.addAnnotation(fieldAnnoation.build());
    return retval.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(retval);
  }

  @Override
  protected TypeName getJavaType() {
    ObjectModelInstance<?> instance = getModelInstance();

    TypeName item;
    if (instance instanceof FieldInstance) {
      FieldInstance<?> fieldInstance = (FieldInstance<?>) instance;
      if (fieldInstance.getDefinition().getFlagInstances().isEmpty()) {
        DataType dataType = DataType.lookupByDatatype(fieldInstance.getDefinition().getDatatype());
        // this is a simple value
        item = dataType.getTypeName();
      } else {
        item = getClassGenerator().getTypeResolver().getClassName(fieldInstance.getDefinition());
      }
    } else if (instance instanceof AssemblyInstance) {
      AssemblyInstance<?> assemblyInstance = (AssemblyInstance<?>) instance;
      AssemblyDefinition assemblyDefinition = assemblyInstance.getDefinition();
      if (assemblyDefinition.getFlagInstances().isEmpty() && assemblyDefinition.getModelInstances().isEmpty()) {
        // make this a boolean type, since this is a marker without any contents
        // TODO: make sure global definitions of this type are suppressed
        item = DataType.BOOLEAN.getTypeName();
      } else {
        item = getClassGenerator().getTypeResolver().getClassName(assemblyInstance.getDefinition());
      }
    } else {
      String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    TypeName retval;
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        retval = ParameterizedTypeName.get(ClassName.get(LinkedHashMap.class), ClassName.get(String.class), item);
      } else {
        retval = ParameterizedTypeName.get(ClassName.get(LinkedList.class), item);
      }
    } else {
      retval = item;
    }

    return retval;
  }

}
