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

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagContainerSupport;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.FlagReference;
import gov.nist.secauto.metaschema.databind.model.metaschema.binding.InlineDefineFlag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class FlagContainerSupport implements IFlagContainerSupport<IFlagInstance> {
  private static final Logger LOGGER = LogManager.getLogger(FlagContainerSupport.class);
  @NonNull
  private final Map<String, IFlagInstance> flagInstances;
  @Nullable
  private IFlagInstance jsonKeyFlag;

  public FlagContainerSupport(
      @Nullable List<Object> flags,
      @NonNull IFlagContainer definition) {
    if (flags == null) {
      this.flagInstances = CollectionUtil.emptyMap();
    } else {
      this.flagInstances = ObjectUtils.notNull(flags.stream()
          .map(obj -> {
            IFlagInstance retval;
            if (obj instanceof InlineDefineFlag) {
              retval = newFlagInstance((InlineDefineFlag) obj, definition);
            } else if (obj instanceof FlagReference) {
              retval = newFlagInstance((FlagReference) obj, definition);
            } else {
              throw new UnsupportedOperationException(String.format("Unknown flag instance class: %s", obj.getClass()));
            }
            return retval;
          })
          .peek((instance) -> {
            if (instance.isJsonKey()) {
              jsonKeyFlag = instance;
            }
          })
          .collect(CustomCollectors.toMap(
              (instance) -> instance.getEffectiveName(),
              (instance) -> instance,
              (key, v1, v2) -> {
                if (LOGGER.isErrorEnabled()) {
                  IFlagContainer owningDefinition = v1.getContainingDefinition();
                  LOGGER.error(
                      String.format("Unexpected duplicate flag instance name '%s' in definition '%s' in module '%s'",
                          key,
                          owningDefinition.getName(),
                          owningDefinition.getContainingModule().getShortName()));
                }
                return ObjectUtils.notNull(v2);
              },
              LinkedHashMap::new)));
    }
  }

  @Override
  public Map<String, IFlagInstance> getFlagInstanceMap() {
    return flagInstances;
  }

  @Override
  public IFlagInstance getJsonKeyFlagInstance() {
    return jsonKeyFlag;
  }

  protected IFlagInstance newFlagInstance(
      @NonNull InlineDefineFlag obj,
      @NonNull IFlagContainer parent) {
    return new InstanceFlagInline(obj, parent);
  }

  protected IFlagInstance newFlagInstance(
      @NonNull FlagReference obj,
      @NonNull IFlagContainer parent) {
    String flagName = ObjectUtils.requireNonNull(obj.getRef());
    IFlagDefinition definition = parent.getContainingModule().getScopedFlagDefinitionByName(flagName);
    if (definition == null) {
      IFlagContainer owningDefinition = parent.getOwningDefinition();
      throw new IllegalStateException(
          String.format("Unable to resolve flag reference '%s' in definition '%s' in module '%s'",
              flagName,
              owningDefinition.getName(),
              owningDefinition.getContainingModule().getShortName()));
    }
    return new InstanceFlagReference(obj, definition, parent);
  }
}
