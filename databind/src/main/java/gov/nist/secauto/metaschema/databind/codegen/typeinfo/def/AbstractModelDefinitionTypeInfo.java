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

import com.squareup.javapoet.ClassName;

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.FlagInstanceTypeInfoImpl;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IFlagInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class AbstractModelDefinitionTypeInfo<DEF extends IFlagContainer>
    extends AbstractDefinitionTypeInfo<DEF>
    implements IModelDefinitionTypeInfo {
  @NonNull
  private final ClassName className;
  @Nullable
  private final ClassName baseClassName;
  private Map<String, IFlagInstanceTypeInfo> flagTypeInfos;

  public AbstractModelDefinitionTypeInfo(@NonNull DEF definition,
      @NonNull ITypeResolver typeResolver) {
    super(definition, typeResolver);
    this.className = typeResolver.getClassName(definition);
    this.baseClassName = typeResolver.getBaseClassName(definition);
  }

  @Override
  public ClassName getClassName() {
    return className;
  }

  @Override
  public ClassName getBaseClassName() {
    return baseClassName;
  }

  @Override
  protected boolean initInstanceTypeInfos() {
    synchronized (this) {
      boolean retval;
      if (flagTypeInfos == null) {
        // create Java properties for the definition's flags
        flagTypeInfos = Collections.unmodifiableMap(getDefinition().getFlagInstances().stream()
            .map(instance -> {
              assert instance != null;
              return newFlagTypeInfo(instance);
            })
            .collect(Collectors.toMap(
                IFlagInstanceTypeInfo::getPropertyName,
                Function.identity(),
                (v1, v2) -> v2,
                LinkedHashMap::new)));
        retval = true;
      } else {
        retval = false;
      }
      return retval;
    }
  }

  @Override
  public IFlagInstanceTypeInfo getFlagInstanceTypeInfo(@NonNull IFlagInstance instance) {
    initInstanceTypeInfos();
    return (IFlagInstanceTypeInfo) getInstanceTypeInfo(instance);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFlagInstanceTypeInfo> getFlagInstanceTypeInfos() {
    initInstanceTypeInfos();
    return flagTypeInfos.values();
  }

  protected IFlagInstanceTypeInfo newFlagTypeInfo(@NonNull IFlagInstance instance) {
    IFlagInstanceTypeInfo retval = new FlagInstanceTypeInfoImpl(instance, this);
    addPropertyTypeInfo(retval);
    return retval;
  }
}
