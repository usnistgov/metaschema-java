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

package gov.nist.secauto.metaschema.databind.model.metaschema.impl;

import gov.nist.secauto.metaschema.core.model.IContainerFlagSupport;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.metaschema.IBindingModule;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FlagReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineFlag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class FlagContainerSupport implements IContainerFlagSupport<IBindingInstanceFlag> {
  private static final Logger LOGGER = LogManager.getLogger(FlagContainerSupport.class);
  @NonNull
  private final Map<String, IBindingInstanceFlag> flagInstances;

  @SuppressWarnings("PMD.ShortMethodName")
  public static IContainerFlagSupport<IBindingInstanceFlag> of(
      @Nullable List<Object> flags,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      @NonNull IBindingDefinitionModel parent) {
    return flags == null || flags.isEmpty()
        ? IContainerFlagSupport.empty()
        : new FlagContainerSupport(
            flags,
            bindingInstance,
            parent);
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  public FlagContainerSupport(
      @NonNull List<Object> flags,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      @NonNull IBindingDefinitionModel parent) {
    // create temporary collections to store the child binding objects
    final Map<String, IBindingInstanceFlag> flagInstances = new LinkedHashMap<>();

    // create counter to track child positions
    int flagReferencePosition = 0;
    int flagInlineDefinitionPosition = 0;

    IBoundInstanceModelChoiceGroup instance = ObjectUtils.requireNonNull(
        bindingInstance.getDefinition().getChoiceGroupInstanceByName("flags"));
    for (Object obj : flags) {
      IBoundInstanceModelGroupedAssembly objInstance
          = (IBoundInstanceModelGroupedAssembly) instance.getItemInstance(obj);

      IBindingInstanceFlag flag;
      if (obj instanceof InlineDefineFlag) {
        flag = newFlagInstance(
            (InlineDefineFlag) obj,
            objInstance,
            flagInlineDefinitionPosition++,
            parent);
      } else if (obj instanceof FlagReference) {
        flag = newFlagInstance(
            (FlagReference) obj,
            objInstance,
            flagReferencePosition++,
            parent);
      } else {
        throw new UnsupportedOperationException(String.format("Unknown flag instance class: %s", obj.getClass()));
      }

      String key = flag.getEffectiveName();
      flagInstances.merge(flag.getEffectiveName(), flag, (v1, v2) -> {
        if (LOGGER.isErrorEnabled()) {
          IModelDefinition owningDefinition = v1.getContainingDefinition();
          IModule module = owningDefinition.getContainingModule();
          LOGGER.error(
              String.format(
                  "Unexpected duplicate flag instance name '%s' in definition '%s' in module name '%s' at '%s'",
                  key,
                  owningDefinition.getName(),
                  module.getShortName(),
                  module.getLocation()));
        }
        return ObjectUtils.notNull(v2);
      });
    }

    this.flagInstances = flagInstances.isEmpty()
        ? CollectionUtil.emptyMap()
        : CollectionUtil.unmodifiableMap(flagInstances);
  }

  @Override
  public Map<String, IBindingInstanceFlag> getFlagInstanceMap() {
    return flagInstances;
  }

  protected static final IBindingInstanceFlag newFlagInstance(
      @NonNull InlineDefineFlag obj,
      @NonNull IBoundInstanceModelGroupedAssembly objInstance,
      int position,
      @NonNull IBindingDefinitionModel parent) {
    return new InstanceFlagInline(obj, objInstance, position, parent);
  }

  protected static final IBindingInstanceFlag newFlagInstance(
      @NonNull FlagReference obj,
      @NonNull IBoundInstanceModelGroupedAssembly objInstance,
      int position,
      @NonNull IBindingDefinitionModel parent) {
    String flagName = ObjectUtils.requireNonNull(obj.getRef());
    IBindingModule module = parent.getContainingModule();
    IBindingDefinitionFlag definition = module.getScopedFlagDefinitionByName(flagName);
    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve flag reference '%s' in definition '%s' in module '%s'",
              flagName,
              parent.getName(),
              module.getShortName()));
    }
    return new InstanceFlagReference(obj, objInstance, position, definition, parent);
  }
}
