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

import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.INamedInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

abstract class AbstractDefinitionTypeInfo<DEF extends IDefinition> implements IDefinitionTypeInfo {
  private static final Logger LOGGER = LogManager.getLogger(AbstractDefinitionTypeInfo.class);
  @NonNull
  private final DEF definition;
  @NonNull
  private final ITypeResolver typeResolver;
  @NonNull
  private final Map<String, ITypeInfo> propertyNameToInstanceTypeInfoMap = new LinkedHashMap<>();
  @NonNull
  private final Map<INamedInstance, IInstanceTypeInfo> instanceToInstanceTypeInfoMap = new LinkedHashMap<>();

  public AbstractDefinitionTypeInfo(@NonNull DEF definition, @NonNull ITypeResolver typeResolver) {
    this.definition = definition;
    this.typeResolver = typeResolver;
  }

  @Override
  public DEF getDefinition() {
    return definition;
  }

  @Override
  public ITypeResolver getTypeResolver() {
    return typeResolver;
  }

  /**
   * Lazy initialize instance information.
   * 
   * @return {@code true} if instance information was initialized or {@code false} otherwise
   */
  protected abstract boolean initInstanceTypeInfos();

  /**
   * Adds the provided property generator to this class generator.
   * 
   * @param typeInfo
   *          the instance type info to add
   */
  protected final void addPropertyTypeInfo(@NonNull ITypeInfo typeInfo) {
    String name = typeInfo.getPropertyName();
    ITypeInfo oldContext = propertyNameToInstanceTypeInfoMap.put(name, typeInfo);
    if (oldContext != null) {
      String msg = String.format("Unexpected duplicate property name '%s'", name);
      LOGGER.error(msg);
      throw new IllegalArgumentException(msg);
    }

    if (typeInfo instanceof IInstanceTypeInfo) {
      IInstanceTypeInfo instanceTypeInfo = (IInstanceTypeInfo) typeInfo;
      instanceToInstanceTypeInfoMap.put(instanceTypeInfo.getInstance(), instanceTypeInfo);
    }
  }

  @Override
  public boolean hasPropertyWithName(@NonNull String propertyName) {
    synchronized (this) {
      return propertyNameToInstanceTypeInfoMap.containsKey(propertyName);
    }
  }

  @SuppressWarnings("null")
  @Override
  public Collection<ITypeInfo> getPropertyTypeInfos() {
    initInstanceTypeInfos();
    return propertyNameToInstanceTypeInfoMap.values();
  }

  @Override
  @Nullable
  public IInstanceTypeInfo getInstanceTypeInfo(@NonNull INamedInstance instance) {
    initInstanceTypeInfos();
    return instanceToInstanceTypeInfoMap.get(instance);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<IInstanceTypeInfo> getInstanceTypeInfos() {
    initInstanceTypeInfos();
    return instanceToInstanceTypeInfoMap.values();
  }
}
