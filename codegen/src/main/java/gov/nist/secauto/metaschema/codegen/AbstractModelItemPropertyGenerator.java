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

import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.Field;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;
import gov.nist.secauto.metaschema.model.info.instances.XmlGroupAsBehavior;

public abstract class AbstractModelItemPropertyGenerator<JAVA_TYPE extends JavaType>
    extends AbstractPropertyGenerator<AssemblyClassGenerator> {
  private final ModelInstance modelInstance;
  private final JAVA_TYPE javaType;

  /**
   * Constructs a new generator of cardinality information for a property item of a Metaschema
   * assembly.
   * 
   * @param instanceItemContext
   *          the property instance to associate the cardinality information with
   * @param classContext
   *          the containing class to generate
   * @param javaType
   */
  public AbstractModelItemPropertyGenerator(ModelInstance modelInstance,
      AssemblyClassGenerator classContext, JAVA_TYPE javaType) {
    super(classContext);
    this.modelInstance = modelInstance;
    this.javaType = javaType;
  }

  /**
   * Gets the details of the associated Metaschema model instance.
   * 
   * @return the name
   */
  protected ModelInstance getModelInstance() {
    return modelInstance;
  }

  @Override
  protected JAVA_TYPE getJavaType() {
    return javaType;
  }

  @Override
  protected String getInstanceName() {
    return getModelInstance().getInstanceName();
  }

  @Override
  public MarkupLine getDescription() {
    return getModelInstance().getDescription();
  }

  @Override
  public void buildField(FieldBuilder builder) {
    MarkupLine description = getDescription();
    builder.getJavadocBuilder().append(description.toHtml());

    StringBuilder fieldAnnotationBuilder = new StringBuilder();
    fieldAnnotationBuilder.append("name = \"");
    fieldAnnotationBuilder.append(getModelInstance().getDefinition().getName());
    fieldAnnotationBuilder.append("\"");

    String namespace = getModelInstance().getContainingMetaschema().getXmlNamespace().toString();
    String containingNamespace = getModelInstance().getContainingMetaschema().getXmlNamespace().toString();
    if (!containingNamespace.equals(namespace)) {
      fieldAnnotationBuilder.append(", namespace = \"");
      fieldAnnotationBuilder.append(namespace);
      fieldAnnotationBuilder.append("\"");
    }

    boolean isRequired = getModelInstance().getMinOccurs() > 0;
    if (isRequired) {
      fieldAnnotationBuilder.append(", required = true");
    }

    ModelInstance modelInstance = getModelInstance();
    if (modelInstance instanceof FieldInstance) {
      if (!((FieldInstance) modelInstance).hasXmlWrapper()) {
        fieldAnnotationBuilder.append(", inXmlWrapped = false");
      }
      builder.annotation(Field.class, fieldAnnotationBuilder.toString());
    } else if (modelInstance instanceof AssemblyInstance) {
      builder.annotation(Assembly.class, fieldAnnotationBuilder.toString());
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    if (maxOccurs == -1 || maxOccurs > 1) {

      StringBuilder groupAs = new StringBuilder();
      groupAs.append("name = \"");
      groupAs.append(getInstanceName());
      groupAs.append("\"");

      if (!containingNamespace.equals(namespace)) {
        groupAs.append(", namespace = \"");
        groupAs.append(namespace);
        groupAs.append("\"");
      }

      int minOccurs = modelInstance.getMinOccurs();
      if (minOccurs != 0) {
        groupAs.append(", minOccurs = ");
        groupAs.append(minOccurs);
      }
      if (maxOccurs != 1) {
        groupAs.append(", maxOccurs = ");
        groupAs.append(maxOccurs);
      }

      JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
      if (!JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(jsonGroupAsBehavior)) {
        groupAs.append(", inJson = "
            + gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior.class.getName() + ".");
        groupAs.append(jsonGroupAsBehavior.toString());
      }

      XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
      if (!XmlGroupAsBehavior.UNGROUPED.equals(xmlGroupAsBehavior)) {
        groupAs.append(", inXml = "
            + gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior.class.getName() + ".");
        groupAs.append(xmlGroupAsBehavior.toString());
      }
      builder.annotation(GroupAs.class, groupAs.toString());
    }
  }

}
