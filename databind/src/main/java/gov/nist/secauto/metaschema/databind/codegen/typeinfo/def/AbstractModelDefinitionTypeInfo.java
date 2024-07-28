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

import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IInstance;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.FlagInstanceTypeInfoImpl;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IFlagInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IPropertyTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public abstract class AbstractModelDefinitionTypeInfo<DEF extends IModelDefinition>
    implements IModelDefinitionTypeInfo {
  private static final Logger LOGGER = LogManager.getLogger(AbstractModelDefinitionTypeInfo.class);
  @NonNull
  private final DEF definition;
  @NonNull
  private final ITypeResolver typeResolver;
  @NonNull
  private final ClassName className;
  @Nullable
  private final ClassName baseClassName;
  @NonNull
  private final List<ClassName> superinterfaces;
  @NonNull
  private final Lazy<Map<String, IFlagInstanceTypeInfo>> flagTypeInfos;

  public AbstractModelDefinitionTypeInfo(
      @NonNull DEF definition,
      @NonNull ITypeResolver typeResolver) {
    this.definition = definition;
    this.typeResolver = typeResolver;
    this.className = typeResolver.getClassName(definition);
    this.baseClassName = typeResolver.getBaseClassName(definition);
    this.superinterfaces = typeResolver.getSuperinterfaces(definition);
    this.flagTypeInfos = ObjectUtils.notNull(Lazy.lazy(() -> flags()
        .collect(CustomCollectors.toMap(
            ITypeInfo::getPropertyName,
            CustomCollectors.identity(),
            (key, v1, v2) -> {
              if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Unexpected duplicate flag property name '%s'", key));
              }
              return ObjectUtils.notNull(v2);
            },
            LinkedHashMap::new))));
  }

  @Override
  public DEF getDefinition() {
    return definition;
  }

  @Override
  public ITypeResolver getTypeResolver() {
    return typeResolver;
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
  public List<ClassName> getSuperinterfaces() {
    return superinterfaces;
  }

  private Stream<IFlagInstanceTypeInfo> flags() {
    return getDefinition().getFlagInstances().stream()
        .map(instance -> {
          assert instance != null;
          return new FlagInstanceTypeInfoImpl(instance, this);
        });
  }

  @NonNull
  protected abstract Map<String, IPropertyTypeInfo> getPropertyTypeInfoMap();

  @NonNull
  protected abstract Map<IInstance, IInstanceTypeInfo> getInstanceTypeInfoMap();

  @NonNull
  protected Map<String, IFlagInstanceTypeInfo> getFlagInstanceTypeInfoMap() {
    return ObjectUtils.notNull(flagTypeInfos.get());
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IPropertyTypeInfo> getPropertyTypeInfos() {
    return getPropertyTypeInfoMap().values();
  }

  @Override
  @Nullable
  public IInstanceTypeInfo getInstanceTypeInfo(@NonNull IInstance instance) {
    return getInstanceTypeInfoMap().get(instance);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IInstanceTypeInfo> getInstanceTypeInfos() {
    return getInstanceTypeInfoMap().values();
  }

  @Override
  public IFlagInstanceTypeInfo getFlagInstanceTypeInfo(@NonNull IFlagInstance instance) {
    return (IFlagInstanceTypeInfo) getInstanceTypeInfo(instance);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IFlagInstanceTypeInfo> getFlagInstanceTypeInfos() {
    return getFlagInstanceTypeInfoMap().values();
  }
}
