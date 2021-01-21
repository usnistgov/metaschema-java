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

import gov.nist.secauto.metaschema.binding.model.annotations.Collapsible;
import gov.nist.secauto.metaschema.codegen.binding.config.JavaTypeSupplier;
import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FieldClassGenerator extends AbstractClassGenerator<FieldDefinition> {
  private static final Logger logger = LogManager.getLogger(FieldClassGenerator.class);
  private final FieldValuePropertyGenerator fieldInstance;
  private boolean hasJsonValueKeyFlag = false;

  public FieldClassGenerator(FieldDefinition definition, JavaTypeSupplier supplier) {
    super(definition, supplier);

    if (!DataType.EMPTY.equals(getValueDatatype())) {
      this.fieldInstance = newFieldInstance(this);
    } else {
      this.fieldInstance = null;
    }
  }

  @Override
  protected boolean isRootClass() {
    return false;
  }

  public FieldValuePropertyGenerator newFieldInstance(FieldClassGenerator field) {
    FieldValuePropertyGenerator context = new FieldValuePropertyGenerator(field, this);
    addPropertyGenerator(context);
    return context;
  }

  @Override
  public FlagPropertyGenerator newFlagPropertyGenerator(FlagInstance instance) {
    if (instance.isJsonValueKeyFlag()) {
      hasJsonValueKeyFlag = true;
    }
    return super.newFlagPropertyGenerator(instance);
  }

  public DataType getValueDatatype() {
    gov.nist.secauto.metaschema.model.info.definitions.DataType type = getDefinition().getDatatype();
    DataType retval = DataType.lookupByDatatype(type);
    if (retval == null) {
      logger.warn("Unsupported datatype '{}', using String", type);
      retval = DataType.STRING;
    }
    return retval;
  }

  public FieldValuePropertyGenerator getFieldInstance() {
    return fieldInstance;
  }

  @Override
  protected void buildClass(ClassBuilder builder) {
    super.buildClass(builder);

    if (getDefinition().isCollapsible()) {
      if (getDefinition().hasJsonKey()) {
        logger.warn(
            "A field binding cannot implement a json-key and be collapsible. Ignoring the collapsible for class '{}'.",
            getJavaType().getQualifiedClassName());
      } else {
        builder.annotation(Collapsible.class);
      }
    }
    // FieldValuePropertyGenerator fieldInsatnce = getFieldInstance();
    // // no-arg constructor
    // writer.println("\t@JsonCreator");
    // writer.printf("\tpublic %s(%s value) {%n", getJavaType().getClassName(),
    // fieldInsatnce.getJavaType().getType(getJavaType()));
    // writer.printf("\t\tthis.%s = value;%n", fieldInsatnce.getVariableName());
    // writer.println("\t}");
    // writer.println();
  }

  public boolean hasJsonValueKeyFlag() {
    return hasJsonValueKeyFlag;
  }
}