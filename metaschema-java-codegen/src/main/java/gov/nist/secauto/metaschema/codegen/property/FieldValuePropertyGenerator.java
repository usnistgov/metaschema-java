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
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.codegen.FieldJavaClassGenerator;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * Represents the "value" of a field object.
 */
public class FieldValuePropertyGenerator extends AbstractPropertyGenerator<FieldJavaClassGenerator> {
//  private static final Logger logger = LogManager.getLogger(FieldValuePropertyGenerator.class);

  public FieldValuePropertyGenerator(@NotNull FieldJavaClassGenerator generator) {
    super(generator);
  }

  @Override
  public TypeName getJavaType() {
    return ClassName
        .get(getClassGenerator().getDefinition().getDatatype().getJavaClass());
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
    return getClassGenerator().getDefinition().getJsonValueKeyName();
  }

  @Override
  protected Set<INamedModelDefinition> buildField(FieldSpec.Builder builder) {

    IFieldDefinition definition = getClassGenerator().getDefinition();
    AnnotationSpec.Builder fieldValue = AnnotationSpec.builder(FieldValue.class);

    IJavaTypeAdapter<?> valueDataType = definition.getDatatype();

    // a field object always has a single value
    if (definition.hasJsonValueKeyFlagInstance()) {
      // do nothing, the annotation will be on the flag
    } else {
      fieldValue.addMember("name", "$S", definition.getJsonValueKeyName());
    }

    fieldValue.addMember("typeAdapter", "$T.class", valueDataType.getClass());

    builder.addAnnotation(fieldValue.build());
    return Collections.emptySet();
  }
}
