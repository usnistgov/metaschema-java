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

package gov.nist.secauto.metaschema.databind.codegen.typeinfo;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

class ModelInstanceTypeInfoImpl
    extends AbstractInstanceTypeInfo<INamedModelInstance, IAssemblyDefinitionTypeInfo>
    implements IModelInstanceTypeInfo {
  private static final Logger LOGGER = LogManager.getLogger(ModelInstanceTypeInfoImpl.class);

  public ModelInstanceTypeInfoImpl(@NonNull INamedModelInstance instance,
      @NonNull IAssemblyDefinitionTypeInfo parentDefinition) {
    super(instance, parentDefinition);
  }

  @Override
  public @NonNull String getBaseName() {
    INamedModelInstance modelInstance = getInstance();
    String retval;
    if (modelInstance.getMaxOccurs() == -1 || modelInstance.getMaxOccurs() > 1) {
      retval = ObjectUtils.notNull(modelInstance.getGroupAsName());
    } else {
      retval = modelInstance.getEffectiveName();
    }
    return retval;
  }

  @Override
  public String getItemBaseName() {
    return getInstance().getEffectiveName();
  }

  @Override
  public @NonNull TypeName getJavaItemType() {
    INamedModelInstance instance = getInstance();

    TypeName retval;
    if (instance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) instance;
      if (fieldInstance.isSimple()) {
        IDataTypeAdapter<?> dataType = fieldInstance.getDefinition().getJavaTypeAdapter();
        // this is a simple value
        retval = ObjectUtils.notNull(ClassName.get(dataType.getJavaClass()));
      } else {
        retval = getParentDefinitionTypeInfo().getTypeResolver().getClassName(fieldInstance.getDefinition());
      }
    } else if (instance instanceof IAssemblyInstance) {
      IAssemblyInstance assemblyInstance = (IAssemblyInstance) instance;
      retval = getParentDefinitionTypeInfo().getTypeResolver().getClassName(assemblyInstance.getDefinition());
    } else {
      String msg = String.format("Unknown model instance type: %s", instance.getClass().getCanonicalName());
      LOGGER.error(msg);
      throw new IllegalStateException(msg);
    }
    return retval;
  }

  @Override
  public @NonNull TypeName getJavaFieldType() {
    TypeName item = getJavaItemType();

    @NonNull TypeName retval;
    INamedModelInstance instance = getInstance();
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        retval = ObjectUtils.notNull(
            ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), item));
      } else {
        retval = ObjectUtils.notNull(ParameterizedTypeName.get(ClassName.get(List.class), item));
      }
    } else {
      retval = item;
    }

    return retval;
  }
}
