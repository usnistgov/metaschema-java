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
import gov.nist.secauto.metaschema.model.common.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.common.instance.XmlGroupAsBehavior;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelInstancePropertyGenerator
    extends AbstractPropertyGenerator<AssemblyJavaClassGenerator> {
  private static final Logger LOGGER = LogManager.getLogger(ModelInstancePropertyGenerator.class);

  private final INamedModelInstance modelInstance;

  /**
   * Constructs a new generator of cardinality information for a property item of a Metaschema
   * assembly.
   * 
   * @param modelInstance
   *          the property instance to associate the cardinality information with
   * @param classGenerator
   *          the containing class to generate
   */
  public ModelInstancePropertyGenerator(
      INamedModelInstance modelInstance,
      AssemblyJavaClassGenerator classGenerator) {
    super(classGenerator);
    this.modelInstance = modelInstance;
  }

  /**
   * Gets the details of the associated Metaschema model instance.
   * 
   * @return the name
   */
  protected INamedModelInstance getModelInstance() {
    return modelInstance;
  }

  @Override
  protected String getInstanceName() {
    INamedModelInstance modelInstance = getModelInstance();
    String retval = null;
    if (modelInstance.getMaxOccurs() == 1) {
      // an instance name only applies to an assembly or field. a choice does not have a name and doesn't
      // result in a generated instance.
      retval = modelInstance.getEffectiveName();
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
    if (modelInstance instanceof IFieldInstance && ((IFieldInstance) modelInstance).isSimple()) {
      retval = modelInstance.getDefinition().getDescription();
    }
    return retval;
  }

  @Override
  public Set<INamedModelDefinition> buildField(FieldSpec.Builder builder) {
    Set<INamedModelDefinition> retval = new HashSet<>();
    retval.addAll(super.buildField(builder));

    // determine which annotation to apply
    AnnotationSpec.Builder fieldAnnoation;
    INamedModelInstance modelInstance = getModelInstance();
    if (modelInstance instanceof IFieldInstance) {
      fieldAnnoation = AnnotationSpec.builder(Field.class);
    } else if (modelInstance instanceof IAssemblyInstance) {
      fieldAnnoation = AnnotationSpec.builder(Assembly.class);
    } else {
      throw new UnsupportedOperationException(String.format("Model instance '%s' of type '%s' is not supported.",
          modelInstance.getName(), modelInstance.getClass().getName()));
    }

    fieldAnnoation.addMember("useName", "$S", modelInstance.getEffectiveName());

    INamedModelDefinition definition = modelInstance.getDefinition();
    if (definition instanceof IFieldDefinition && ((IFieldDefinition) definition).isSimple()) {
      // do not generate a child class
    } else if (!definition.isGlobal()) {
      // this is a local definition that must be built as a child class
      retval.add(definition);
    }

    fieldAnnoation.addMember("namespace", "$S", modelInstance.getXmlNamespace());

    if (modelInstance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;
      IFieldDefinition fieldDefinition = (IFieldDefinition) definition;

      IJavaTypeAdapter<?> valueDataType = fieldDefinition.getDatatype();

      if (MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED != fieldInstance.isInXmlWrapped()) {
        fieldAnnoation.addMember("inXmlWrapped", "$L", fieldInstance.isInXmlWrapped());
      }
      if (fieldInstance.isSimple()) {
        // this is a simple field, without flags
        // we need to add the FieldValue annotation to the property
//        fieldAnnoation.addMember("valueName", "$S", fieldDefinition.getJsonValueKeyName());

        fieldAnnoation.addMember("typeAdapter", "$T.class", valueDataType.getClass());

        AnnotationUtils.applyAllowedValuesConstraints(fieldAnnoation, fieldDefinition.getAllowedValuesContraints());
        AnnotationUtils.applyIndexHasKeyConstraints(fieldAnnoation, fieldDefinition.getIndexHasKeyConstraints());
        AnnotationUtils.applyMatchesConstraints(fieldAnnoation, fieldDefinition.getMatchesConstraints());
        AnnotationUtils.applyExpectConstraints(fieldAnnoation, fieldDefinition.getExpectConstraints());
      }
    }

    int minOccurs = modelInstance.getMinOccurs();
    if (minOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS) {
      fieldAnnoation.addMember("minOccurs", "$L", minOccurs);
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    if (maxOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS) {
      fieldAnnoation.addMember("maxOccurs", "$L", maxOccurs);
    }

    if (maxOccurs == -1 || maxOccurs > 1) {
      fieldAnnoation.addMember("groupName", "$S", getInstanceName());
      fieldAnnoation.addMember("groupNamespace", "$S", modelInstance.getGroupAsXmlNamespace());

      JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
      assert jsonGroupAsBehavior != null;
      fieldAnnoation.addMember("inJson", "$T.$L",
          JsonGroupAsBehavior.class, jsonGroupAsBehavior.toString());

      XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
      assert xmlGroupAsBehavior != null;
      fieldAnnoation.addMember("inXml", "$T.$L",
          XmlGroupAsBehavior.class, xmlGroupAsBehavior.toString());
    }
    builder.addAnnotation(fieldAnnoation.build());
    return retval.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(retval);
  }

  @Override
  protected TypeName getJavaType() {
    INamedModelInstance instance = getModelInstance();

    TypeName item;
    if (instance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) instance;
      if (fieldInstance.isSimple()) {
        IJavaTypeAdapter<?> dataType = fieldInstance.getDefinition().getDatatype();
        // this is a simple value
        item = ClassName.get(dataType.getJavaClass());
      } else {
        item = getClassGenerator().getTypeResolver().getClassName(fieldInstance.getDefinition());
      }
    } else if (instance instanceof IAssemblyInstance) {
      IAssemblyInstance assemblyInstance = (IAssemblyInstance) instance;
      item = getClassGenerator().getTypeResolver().getClassName(assemblyInstance.getDefinition());
    } else {
      String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
      LOGGER.error(msg);
      throw new IllegalStateException(msg);
    }

    TypeName retval;
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        retval = ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(String.class), item);
      } else {
        retval = ParameterizedTypeName.get(ClassName.get(List.class), item);
      }
    } else {
      retval = item;
    }

    return retval;
  }

}
