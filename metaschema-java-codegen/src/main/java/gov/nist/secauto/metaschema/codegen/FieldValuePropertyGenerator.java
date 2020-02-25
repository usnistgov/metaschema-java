/**
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

import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the "value" of a field object.
 * 
 * @author davidwal
 *
 */
public class FieldValuePropertyGenerator extends AbstractPropertyGenerator<FieldClassGenerator> {
  private static final Logger logger = LogManager.getLogger(FieldValuePropertyGenerator.class);

  private FieldClassGenerator generator;

  public FieldValuePropertyGenerator(FieldClassGenerator generator, FieldClassGenerator classContext) {
    super(classContext);
    this.generator = generator;
  }

  protected FieldClassGenerator getGenerator() {
    return generator;
  }

  private DataType getValueDataType() {
    return getGenerator().getValueDatatype();
  }

  @Override
  public JavaType getJavaType() {
    return getValueDataType().getJavaType();
  }

  @Override
  protected String getInstanceName() {
    return "value";
  }

  @Override
  public MarkupLine getDescription() {
    return getGenerator().getDefinition().getDescription();
  }

  protected String getJsonPropertyName() {
    String retval = getGenerator().getDefinition().getJsonValueKeyName();
    if (retval == null) {
      throw new RuntimeException("Unable to determine property name");
    }
    return retval;
  }

  @Override
  protected void buildField(FieldBuilder builder) {
    // a field object always has a single value
    if (DataType.EMPTY.equals(getGenerator().getValueDatatype())) {
      String msg = String.format("In class '%s', the field has an empty value, but an instance was generated",
          getGenerator().getJavaType().getQualifiedClassName());
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    FieldDefinition fieldDefinition = getGenerator().getDefinition();
    switch (fieldDefinition.getJsonValueKeyType()) {
    case NONE:
    case LABEL:
      builder.annotation(JsonFieldValueName.class, String.format("name=\"%s\"", fieldDefinition.getJsonValueKeyName()));
      break;
    case FLAG:
      // do nothing, the annotation will be on the flag
      break;
    default:
      throw new UnsupportedOperationException("Invalid JSON valye key type: " + fieldDefinition.getJsonValueKeyType());
    }

    builder.annotation(FieldValue.class);
  }
}
