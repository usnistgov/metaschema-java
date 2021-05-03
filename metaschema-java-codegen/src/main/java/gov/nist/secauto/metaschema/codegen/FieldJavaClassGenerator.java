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
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.codegen.property.FieldValuePropertyGenerator;
import gov.nist.secauto.metaschema.codegen.property.FlagPropertyGenerator;
import gov.nist.secauto.metaschema.codegen.property.PropertyGenerator;
import gov.nist.secauto.metaschema.codegen.type.TypeResolver;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.ObjectDefinition;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FieldJavaClassGenerator
    extends AbstractJavaClassGenerator<FieldDefinition> {
  private static final Logger logger = LogManager.getLogger(FieldJavaClassGenerator.class);

  private final FieldValuePropertyGenerator fieldValueInstance;
  private boolean hasJsonValueKeyFlag = false;

  /**
   * Constructs a new class generator based on the provided field definition.
   * 
   * @param definition
   *          the assembly definition
   * @param typeResolver
   *          the resolver to use to lookup Java type information for Metaschema objects
   */
  public FieldJavaClassGenerator(FieldDefinition definition, TypeResolver typeResolver) {
    super(definition, typeResolver);
    this.fieldValueInstance = newFieldValueInstance();
  }

  @Override
  public FlagPropertyGenerator newFlagPropertyGenerator(FlagInstance<?> instance) {
    // check for a JSON "value key"
    if (instance.isJsonValueKeyFlag()) {
      hasJsonValueKeyFlag = true;
    }
    return super.newFlagPropertyGenerator(instance);
  }

  @Override
  protected boolean isRootClass() {
    // a field is never eligible to be a root
    return false;
  }

  @Override
  protected Set<ObjectDefinition> buildClass(TypeSpec.Builder builder) throws IOException {
    Set<ObjectDefinition> retval = new HashSet<>();
    retval.addAll(super.buildClass(builder));

    if (getFieldValueInstance() == null) {
      // this is an "empty" field, which will be treated as an assembly
      builder.addAnnotation(MetaschemaAssembly.class);
    } else {
      AnnotationSpec.Builder metaschemaField = AnnotationSpec.builder(MetaschemaField.class);
      boolean isCollapsible = false;
      if (getDefinition().isCollapsible()) {
        if (getDefinition().hasJsonKey()) {
          logger.warn(
              "A field binding cannot implement a json-key and be collapsible."
                  + " Ignoring the collapsible for class '{}'.",
              getTypeResolver().getClassName(getDefinition()).canonicalName());
        } else {
          isCollapsible = true;
        }
      }
      metaschemaField.addMember("isCollapsible", "$L", isCollapsible);

      builder.addAnnotation(metaschemaField.build());
    }
    return retval;
  }

  /**
   * Gets the property instance for the field's value.
   * 
   * @return the field's value property instance, or {@code null} if the field's data type is "empty"
   */
  public FieldValuePropertyGenerator getFieldValueInstance() {
    return fieldValueInstance;
  }

  /**
   * Determines if the definition associated with this class has a JSON "value key", and that this
   * class has a property who's value will be used as the name of the value property in JSON.
   * 
   * @return {@code true} if a JSON "value key" is configured, or {@code false} otherwise
   */
  public boolean hasJsonValueKeyFlag() {
    return hasJsonValueKeyFlag;
  }

  /**
   * Creates a new {@link PropertyGenerator} for the the field's value and registers it with this
   * class generator.
   * 
   * @return the new property generator
   */
  public FieldValuePropertyGenerator newFieldValueInstance() {
    FieldValuePropertyGenerator retval = new FieldValuePropertyGenerator(this);
    addPropertyGenerator(retval);
    return retval;
  }
}