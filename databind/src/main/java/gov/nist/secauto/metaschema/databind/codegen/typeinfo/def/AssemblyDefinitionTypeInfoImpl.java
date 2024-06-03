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

package gov.nist.secauto.metaschema.databind.codegen.typeinfo.def;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IContainerModelAbsolute;
import gov.nist.secauto.metaschema.core.model.IInstance;
import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IModelInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IPropertyTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

class AssemblyDefinitionTypeInfoImpl
    extends AbstractModelDefinitionTypeInfo<IAssemblyDefinition>
    implements IAssemblyDefinitionTypeInfo {
  private static final Logger LOGGER = LogManager.getLogger(AssemblyDefinitionTypeInfoImpl.class);

  @NonNull
  private final Lazy<Map<String, IPropertyTypeInfo>> propertyNameToTypeInfoMap;
  @NonNull
  private final Lazy<Map<IInstance, IInstanceTypeInfo>> instanceToTypeInfoMap;

  public AssemblyDefinitionTypeInfoImpl(@NonNull IAssemblyDefinition definition, @NonNull ITypeResolver typeResolver) {
    super(definition, typeResolver);
    this.instanceToTypeInfoMap = ObjectUtils.notNull(Lazy.lazy(() -> Stream.concat(
        getFlagInstanceTypeInfos().stream(),
        processModel(definition))
        .collect(CustomCollectors.toMap(
            IInstanceTypeInfo::getInstance,
            CustomCollectors.identity(),
            (key, v1, v2) -> {
              if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Unexpected duplicate property name '%s'", key));
              }
              return ObjectUtils.notNull(v2);
            },
            LinkedHashMap::new))));
    this.propertyNameToTypeInfoMap = ObjectUtils.notNull(Lazy.lazy(() -> getInstanceTypeInfoMap().values().stream()
        .collect(Collectors.toMap(
            IInstanceTypeInfo::getPropertyName,
            Function.identity(),
            (v1, v2) -> v2,
            LinkedHashMap::new))));
  }

  @Override
  protected Map<String, IPropertyTypeInfo> getPropertyTypeInfoMap() {
    return ObjectUtils.notNull(propertyNameToTypeInfoMap.get());
  }

  @Override
  protected Map<IInstance, IInstanceTypeInfo> getInstanceTypeInfoMap() {
    return ObjectUtils.notNull(instanceToTypeInfoMap.get());
  }

  private Stream<? extends IModelInstanceTypeInfo> processModel(
      @NonNull IContainerModelAbsolute model) {
    Stream<IModelInstanceTypeInfo> modelInstances = Stream.empty();
    // create model instances for the model
    for (IModelInstanceAbsolute instance : model.getModelInstances()) {
      assert instance != null;

      if (instance instanceof IChoiceGroupInstance) {
        modelInstances = Stream.concat(
            modelInstances,
            Stream.of(getTypeResolver().getTypeInfo((IChoiceGroupInstance) instance, this)));
      } else if (instance instanceof IChoiceInstance) {
        modelInstances = Stream.concat(
            modelInstances,
            processModel((IChoiceInstance) instance));
      } else if (instance instanceof INamedModelInstanceAbsolute) {
        // else the instance is an object model instance with a name
        modelInstances = Stream.concat(
            modelInstances,
            Stream.of(getTypeResolver().getTypeInfo((INamedModelInstanceAbsolute) instance, this)));
      }
    }
    return modelInstances;
  }
}
