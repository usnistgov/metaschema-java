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
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.codegen.property.IPropertyGenerator;
import gov.nist.secauto.metaschema.codegen.property.ModelInstancePropertyGenerator;
import gov.nist.secauto.metaschema.codegen.support.AnnotationUtils;
import gov.nist.secauto.metaschema.codegen.type.ITypeResolver;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IModelContainer;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AssemblyJavaClassGenerator
    extends AbstractJavaClassGenerator<IAssemblyDefinition> {

  /**
   * Constructs a new class generator based on the provided assembly definition.
   * 
   * @param definition
   *          the assembly definition
   * @param typeResolver
   *          the resolver to use to lookup Java type information for Metaschema objects
   */
  public AssemblyJavaClassGenerator(@NotNull IAssemblyDefinition definition, @NotNull ITypeResolver typeResolver) {
    super(definition, typeResolver);

    // create properties for the model instances
    processModel(getDefinition());
  }

  @Override
  protected boolean isRootClass() {
    return getDefinition().isRoot();
  }

  @Override
  protected void applyConstraints(AnnotationSpec.Builder annotation) {
    super.applyConstraints(annotation);

    AnnotationUtils.applyIndexConstraints(annotation, getDefinition().getIndexConstraints());
    AnnotationUtils.applyUniqueConstraints(annotation, getDefinition().getUniqueConstraints());
    AnnotationUtils.applyHasCardinalityConstraints(getDefinition(), annotation,
        getDefinition().getHasCardinalityConstraints());

  }

  @Override
  protected Set<INamedModelDefinition> buildClass(TypeSpec.Builder builder, ClassName className) throws IOException {
    Set<INamedModelDefinition> retval = new HashSet<>();
    retval.addAll(super.buildClass(builder, className));

    AnnotationSpec.Builder metaschemaAssembly = AnnotationSpec.builder(MetaschemaAssembly.class);
    IAssemblyDefinition definition = getDefinition();
    if (definition.isRoot()) {
      metaschemaAssembly.addMember("rootName", "$S", definition.getRootName());
    }
    applyConstraints(metaschemaAssembly);

    builder.addAnnotation(metaschemaAssembly.build());

    return retval;
  }

  private void processModel(IModelContainer model) {
    // create model instances for the model
    for (IModelInstance instance : model.getModelInstances()) {
      if (instance instanceof IChoiceInstance) {
        processModel((IChoiceInstance) instance);
        continue;
      }

      // else the instance is an object model instance with a name
      newObjectModelInstance((INamedModelInstance) instance);
    }
  }

  /**
   * Creates a new {@link IPropertyGenerator} for the provided {@link INamedModelInstance} and
   * registers it with this class generator.
   * 
   * @param instance
   *          the model instance to generate the property for
   * @return the new property generator
   */
  private ModelInstancePropertyGenerator newObjectModelInstance(INamedModelInstance instance) {
    ModelInstancePropertyGenerator retval = new ModelInstancePropertyGenerator(instance, this);
    addPropertyGenerator(retval);
    return retval;
  }
}