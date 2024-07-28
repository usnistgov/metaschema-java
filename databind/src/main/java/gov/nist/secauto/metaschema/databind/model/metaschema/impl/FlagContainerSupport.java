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
import gov.nist.secauto.metaschema.core.model.IFlagContainerBuilder;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.FlagReference;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.InlineDefineFlag;

import java.util.List;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("PMD.OnlyOneReturn")
public final class FlagContainerSupport {
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  @NonNull
  public static IContainerFlagSupport<IFlagInstance> newFlagContainer(
      @Nullable List<Object> flags,
      @NonNull IBoundInstanceModelGroupedAssembly bindingInstance,
      @NonNull IModelDefinition parent,
      @Nullable String jsonKeyName) {
    if (flags == null || flags.isEmpty()) {
      return IContainerFlagSupport.empty();
    }

    // create temporary collections to store the child binding objects
    IFlagContainerBuilder<IFlagInstance> builder = jsonKeyName == null
        ? IContainerFlagSupport.builder()
        : IContainerFlagSupport.builder(parent.getContainingModule().toFlagQName(jsonKeyName));

    // create counter to track child positions
    int flagReferencePosition = 0;
    int flagInlineDefinitionPosition = 0;

    IBoundInstanceModelChoiceGroup instance = ObjectUtils.requireNonNull(
        bindingInstance.getDefinition().getChoiceGroupInstanceByName("flags"));
    for (Object obj : flags) {
      IBoundInstanceModelGroupedAssembly objInstance
          = (IBoundInstanceModelGroupedAssembly) instance.getItemInstance(obj);

      IFlagInstance flag;
      if (obj instanceof InlineDefineFlag) {
        flag = new InstanceFlagInline(
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

      builder.flag(flag);
    }

    return builder.build();
  }

  @NonNull
  private static IFlagInstance newFlagInstance(
      @NonNull FlagReference obj,
      @NonNull IBoundInstanceModelGroupedAssembly objInstance,
      int position,
      @NonNull IModelDefinition parent) {
    IModule module = parent.getContainingModule();

    QName qname = module.toFlagQName(ObjectUtils.requireNonNull(obj.getRef()));
    IFlagDefinition definition = module.getScopedFlagDefinitionByName(qname);
    if (definition == null) {
      throw new IllegalStateException(
          String.format("Unable to resolve flag reference '%s' in definition '%s' in module '%s'",
              qname,
              parent.getName(),
              module.getShortName()));
    }
    return new InstanceFlagReference(obj, objInstance, position, definition, parent);
  }

  private FlagContainerSupport() {
    // disable construction
  }

}
