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

import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.ModelContainer;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.info.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class AssemblyClassGenerator extends AbstractClassGenerator<AssemblyDefinition> {
  private static final Logger logger = LogManager.getLogger(AssemblyClassGenerator.class);

  public AssemblyClassGenerator(AssemblyDefinition definition, JavaTypeSupplier supplier) {
    super(definition, supplier);

    processModel(getDefinition());
  }

  private void processModel(ModelContainer model) {
    for (InfoElementInstance instance : model.getInstances()) {
      if (instance instanceof ChoiceInstance) {
        processModel((ChoiceInstance) instance);
        continue;
      }

      // else the instance is a model instance with a named instance
      ModelInstance modelInstance = (ModelInstance) instance;
      newModelInstance(modelInstance);
    }
  }

  public PropertyGenerator newModelInstance(ModelInstance instance) {
    JavaType itemJavaType;
    if (instance instanceof FieldInstance) {
      FieldInstance fieldInstance = (FieldInstance) instance;
      if (fieldInstance.getDefinition().getFlagInstances().isEmpty()) {
        DataType dataType = DataType.lookupByDatatype(fieldInstance.getDatatype());
        // this is a simple value
        itemJavaType = dataType.getJavaType();
      } else {
        itemJavaType = getJavaTypeSupplier().getClassJavaType(instance.getDefinition());
      }
    } else if (instance instanceof AssemblyInstance) {
      itemJavaType = getJavaTypeSupplier().getClassJavaType(instance.getDefinition());
    } else {
      String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    PropertyGenerator context;
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        context = new MapInstanceGenerator(instance, itemJavaType, this);
      } else {
        context = new ListInstanceGenerator(instance, itemJavaType, this);
      }
    } else {
      context = new SingletonInstanceGenerator(instance, itemJavaType, this);
    }

    addPropertyGenerator(context);
    return context;
  }

  @Override
  protected boolean isRootClass() {
    return Objects.equals(getDefinition(), getDefinition().getContainingMetaschema().getRootAssemblyDefinition());
  }

  @Override
  protected void buildClass(ClassBuilder builder) {
    super.buildClass(builder);

    AssemblyDefinition definition = getDefinition();
    if (Objects.equals(definition, definition.getContainingMetaschema().getRootAssemblyDefinition())) {

      StringBuilder instanceBuilder = new StringBuilder();
      instanceBuilder.append("name = \"");
      instanceBuilder.append(getDefinition().getName());
      instanceBuilder.append("\"");

      String namespace = getXmlNamespace().toString();
      String containingNamespace = getDefinition().getContainingMetaschema().getXmlNamespace().toString();
      if (!containingNamespace.equals(namespace)) {
        instanceBuilder.append(", namespace = \"");
        instanceBuilder.append(namespace);
        instanceBuilder.append("\"");
      }

      builder.annotation(RootWrapper.class, instanceBuilder.toString());
    }
  }

}