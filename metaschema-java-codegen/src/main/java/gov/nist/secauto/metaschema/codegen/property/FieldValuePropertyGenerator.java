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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.codegen.FieldJavaClassGenerator;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;

/**
 * Represents the "value" of a field object.
 * 
 * @author davidwal
 *
 */
public class FieldValuePropertyGenerator
    extends AbstractPropertyGenerator<FieldJavaClassGenerator> {
  private static final Logger logger = LogManager.getLogger(FieldValuePropertyGenerator.class);

  public static DataType getValueDataType(FieldDefinition definition) {
    gov.nist.secauto.metaschema.model.definitions.DataType type = definition.getDatatype();
    DataType retval = DataType.lookupByDatatype(type);
    if (retval == null) {
      logger.warn("Unsupported datatype '{}', using {} instead", type,
          FieldJavaClassGenerator.DEFAULT_DATA_TYPE.toString());
      retval = FieldJavaClassGenerator.DEFAULT_DATA_TYPE;
    }
    return retval;
  }

  public FieldValuePropertyGenerator(FieldJavaClassGenerator generator) {
    super(generator);
  }

  @Override
  public TypeName getJavaType() {
    return FieldValuePropertyGenerator.getValueDataType(getClassGenerator().getDefinition()).getTypeName();
  }

  @Override
  protected String getInstanceName() {
    return "value";
  }

  @Override
  public MarkupLine getDescription() {
    return getClassGenerator().getDefinition().getDescription();
  }

  protected String getJsonPropertyName() {
    String retval = getClassGenerator().getDefinition().getJsonValueKeyName();
    if (retval == null) {
      throw new RuntimeException("Unable to determine property name");
    }
    return retval;
  }

  @Override
  protected Set<ObjectDefinition> buildField(FieldSpec.Builder builder) {

    FieldDefinition definition = getClassGenerator().getDefinition();
    AnnotationSpec.Builder fieldValue = AnnotationSpec.builder(FieldValue.class);

    DataType valueDataType = getValueDataType(definition);

    // a field object always has a single value
    if (DataType.EMPTY.equals(valueDataType)) {
      String msg = "A field should never be generated with an empty value. Something wierd happened.";
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    switch (definition.getJsonValueKeyType()) {
    case NONE:
    case STATIC_LABEL:
      fieldValue.addMember("name", "$S", definition.getJsonValueKeyName());
      break;
    case FLAG:
      // do nothing, the annotation will be on the flag
      break;
    default:
      throw new UnsupportedOperationException("Invalid JSON valye key type: " + definition.getJsonValueKeyType());
    }

    fieldValue.addMember("typeAdapter", "$T.class",
        valueDataType.getJavaTypeAdapterClass());

    builder.addAnnotation(fieldValue.build());
    return Collections.emptySet();
  }
}
