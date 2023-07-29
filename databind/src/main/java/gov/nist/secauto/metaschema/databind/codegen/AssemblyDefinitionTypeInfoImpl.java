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

package gov.nist.secauto.metaschema.databind.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IModelContainer;
import gov.nist.secauto.metaschema.core.model.IModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

class AssemblyDefinitionTypeInfoImpl
    extends AbstractModelDefinitionTypeInfo<IAssemblyDefinition>
    implements IAssemblyDefinitionTypeInfo {

  public AssemblyDefinitionTypeInfoImpl(@NonNull IAssemblyDefinition definition, @NonNull ITypeResolver typeResolver) {
    super(definition, typeResolver);
  }

  @Override
  protected boolean initInstanceTypeInfos() {
    boolean retval = super.initInstanceTypeInfos();
    if (retval) {
      synchronized (this) {
        // create properties for the model instances
        processModel(getDefinition());
      }
    }
    return retval;
  }

  private void processModel(IModelContainer model) {
    // create model instances for the model
    for (IModelInstance instance : model.getModelInstances()) {
      assert instance != null;

      if (instance instanceof IChoiceInstance) {
        processModel((IChoiceInstance) instance);
        continue;
      }

      // else the instance is an object model instance with a name
      newObjectModelInstance((INamedModelInstance) instance);
    }
  }

  /**
   * Creates a new {@link IModelInstanceTypeInfo} for the provided {@link INamedModelInstance} and
   * registers it with this class generator.
   *
   * @param instance
   *          the model instance to generate the property for
   * @return the new property generator
   */
  @NonNull
  protected IModelInstanceTypeInfo newObjectModelInstance(@NonNull INamedModelInstance instance) {
    IModelInstanceTypeInfo retval = new ModelInstanceTypeInfoImpl(instance, this);
    addPropertyTypeInfo(retval);
    return retval;
  }

  @Override
  protected void buildConstraints(@NonNull TypeSpec.Builder builder) {
    super.buildConstraints(builder);

    IAssemblyDefinition definition = getDefinition();
    AnnotationUtils.buildAssemblyConstraints(builder, definition);
  }

  @Override
  protected Set<IFlagContainer> buildClass(TypeSpec.Builder builder, ClassName className) throws IOException {
    Set<IFlagContainer> retval = new HashSet<>();
    retval.addAll(super.buildClass(builder, className));

    AnnotationSpec.Builder metaschemaAssembly = ObjectUtils.notNull(AnnotationSpec.builder(MetaschemaAssembly.class));

    buildCommonProperties(metaschemaAssembly);

    IAssemblyDefinition definition = getDefinition();
    if (definition.isRoot()) {
      metaschemaAssembly.addMember("rootName", "$S", definition.getRootName());
    }

    MarkupMultiline remarks = definition.getRemarks();
    if (remarks != null) {
      metaschemaAssembly.addMember("remarks", "$S", remarks.toMarkdown());
    }

    builder.addAnnotation(metaschemaAssembly.build());
    buildConstraints(builder);
    return retval;
  }
}
