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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.IChoiceGroupInstance;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.ClassUtils;
import gov.nist.secauto.metaschema.databind.codegen.config.IBindingConfiguration;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IFieldDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IModelDefinitionTypeInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

class DefaultTypeResolver implements ITypeResolver {
  private static final Logger LOGGER = LogManager.getLogger(DefaultTypeResolver.class);

  private final Map<String, Set<String>> packageToClassNamesMap = new ConcurrentHashMap<>();
  private final Map<IModelDefinition, ClassName> definitionToTypeMap = new ConcurrentHashMap<>();
  private final Map<IModule, ClassName> moduleToTypeMap = new ConcurrentHashMap<>();
  private final Map<IAssemblyDefinition, IAssemblyDefinitionTypeInfo> assemblyDefinitionToTypeInfoMap
      = new ConcurrentHashMap<>();
  private final Map<IFieldDefinition, IFieldDefinitionTypeInfo> fieldDefinitionToTypeInfoMap
      = new ConcurrentHashMap<>();
  private final Map<IDefinitionTypeInfo, Set<String>> typeInfoToPropertyNameMap = new ConcurrentHashMap<>();

  @NonNull
  private final IBindingConfiguration bindingConfiguration;

  public DefaultTypeResolver(@NonNull IBindingConfiguration bindingConfiguration) {
    this.bindingConfiguration = bindingConfiguration;
  }

  protected IBindingConfiguration getBindingConfiguration() {
    return bindingConfiguration;
  }

  @Override
  public IAssemblyDefinitionTypeInfo getTypeInfo(@NonNull IAssemblyDefinition definition) {
    return ObjectUtils.notNull(assemblyDefinitionToTypeInfoMap.computeIfAbsent(
        definition,
        def -> IAssemblyDefinitionTypeInfo.newTypeInfo(ObjectUtils.notNull(def),
            this)));
  }

  @Override
  public IFieldDefinitionTypeInfo getTypeInfo(@NonNull IFieldDefinition definition) {
    return ObjectUtils.notNull(fieldDefinitionToTypeInfoMap.computeIfAbsent(
        definition,
        def -> IFieldDefinitionTypeInfo.newTypeInfo(ObjectUtils.notNull(def),
            this)));
  }

  @Override
  public IModelDefinitionTypeInfo getTypeInfo(@NonNull IModelDefinition definition) {
    IModelDefinitionTypeInfo retval;
    if (definition instanceof IAssemblyDefinition) {
      retval = getTypeInfo((IAssemblyDefinition) definition);
    } else if (definition instanceof IFieldDefinition) {
      retval = getTypeInfo((IFieldDefinition) definition);
    } else {
      throw new IllegalStateException(String.format("Unknown type '%s'",
          definition.getClass().getName()));
    }
    return retval;
  }

  @Override
  public IGroupedNamedModelInstanceTypeInfo getTypeInfo(
      @NonNull INamedModelInstanceGrouped modelInstance,
      @NonNull IChoiceGroupTypeInfo choiceGroupTypeInfo) {
    IGroupedNamedModelInstanceTypeInfo retval;
    if (modelInstance instanceof IAssemblyInstanceGrouped) {
      retval = getTypeInfo((IAssemblyInstanceGrouped) modelInstance, choiceGroupTypeInfo);
    } else if (modelInstance instanceof IFieldInstanceGrouped) {
      retval = getTypeInfo((IFieldInstanceGrouped) modelInstance, choiceGroupTypeInfo);
    } else {
      throw new IllegalStateException(String.format("Unknown type '%s'",
          modelInstance.getClass().getName()));
    }
    return retval;
  }

  @NonNull
  private static IGroupedAssemblyInstanceTypeInfo getTypeInfo(
      @NonNull IAssemblyInstanceGrouped modelInstance,
      @NonNull IChoiceGroupTypeInfo choiceGroupTypeInfo) {
    return new GroupedAssemblyInstanceTypeInfo(modelInstance, choiceGroupTypeInfo);
  }

  @NonNull
  private static IGroupedFieldInstanceTypeInfo getTypeInfo(
      @NonNull IFieldInstanceGrouped modelInstance,
      @NonNull IChoiceGroupTypeInfo choiceGroupTypeInfo) {
    return new GroupedFieldInstanceTypeInfo(modelInstance, choiceGroupTypeInfo);
  }

  @NonNull
  private ClassName getFlagContainerClassName(
      @NonNull IModelDefinition definition,
      @NonNull String packageName,
      @NonNull String suggestedClassName) {
    ClassName retval;
    if (definition.isInline()) {
      // this is a local definition, which means a child class needs to be generated
      INamedInstance inlineInstance = definition.getInlineInstance();
      IModelDefinition parentDefinition = inlineInstance.getContainingDefinition();
      ClassName parentClassName = getClassName(parentDefinition);
      retval = getSubclassName(parentClassName, suggestedClassName, definition);
    } else {
      String className = generateClassName(packageName, suggestedClassName, definition);
      retval = ObjectUtils.notNull(ClassName.get(packageName, className));
    }
    return retval;
  }

  @Override
  public ClassName getSubclassName(
      @NonNull ClassName parentClass,
      @NonNull String suggestedClassName,
      @NonNull IModelDefinition definition) {
    String name = generateClassName(
        ObjectUtils.notNull(parentClass.canonicalName()),
        ClassUtils.toClassName(suggestedClassName),
        definition);
    return ObjectUtils.notNull(parentClass.nestedClass(name));
  }

  @Override
  @NonNull
  public ClassName getClassName(@NonNull IModelDefinition definition) {
    return ObjectUtils.notNull(definitionToTypeMap.computeIfAbsent(
        definition,
        def -> {
          String packageName = getBindingConfiguration().getPackageNameForModule(def.getContainingModule());
          String suggestedClassName = getBindingConfiguration().getClassName(definition);
          return getFlagContainerClassName(def, packageName, suggestedClassName);
        }));
  }

  @Override
  public ClassName getClassName(@NonNull INamedModelInstanceTypeInfo typeInfo) {
    return getClassName(typeInfo.getInstance().getDefinition());
  }

  @Override
  public ClassName getClassName(IChoiceGroupInstance instance) {
    // TODO: Support some form of binding override for a common interface type
    return ObjectUtils.notNull(ClassName.get(Object.class));
  }

  @Override
  public ClassName getClassName(IModule module) {
    return ObjectUtils.notNull(moduleToTypeMap.computeIfAbsent(
        module,
        mod -> {
          assert mod != null;
          String packageName = getBindingConfiguration().getPackageNameForModule(mod);
          String className = getBindingConfiguration().getClassName(mod);
          String classNameBase = className;
          int index = 1;
          while (isClassNameClash(packageName, className)) {
            className = classNameBase + Integer.toString(index);
          }
          addClassName(packageName, className);
          return ClassName.get(packageName, className);
        }));
  }

  @NonNull
  protected Set<String> getClassNamesFor(@NonNull String packageOrTypeName) {
    return ObjectUtils.notNull(packageToClassNamesMap.computeIfAbsent(
        packageOrTypeName,
        pkg -> Collections.synchronizedSet(new LinkedHashSet<>())));
  }

  protected boolean isClassNameClash(@NonNull String packageOrTypeName, @NonNull String className) {
    return getClassNamesFor(packageOrTypeName).contains(className);
  }

  protected boolean addClassName(@NonNull String packageOrTypeName, @NonNull String className) {
    return getClassNamesFor(packageOrTypeName).add(className);
  }

  private String generateClassName(
      @NonNull String packageOrTypeName,
      @NonNull String suggestedClassName,
      @NonNull IModelDefinition definition) {
    @NonNull String retval = suggestedClassName;
    Set<String> classNames = getClassNamesFor(packageOrTypeName);
    synchronized (classNames) {
      boolean clash = false;
      if (classNames.contains(suggestedClassName)) {
        clash = true;
        // first try to append the metaschema's short name
        String metaschemaShortName = definition.getContainingModule().getShortName();
        retval = ClassUtils.toClassName(suggestedClassName + StringUtils.capitalize(metaschemaShortName));
      }

      String classNameBase = retval;
      int index = 1;
      while (classNames.contains(retval)) {
        retval = classNameBase + Integer.toString(index++);
      }
      classNames.add(retval);

      if (clash && LOGGER.isWarnEnabled()) {
        LOGGER.warn(String.format(
            "Class name '%s', based on '%s' in '%s', clashes with another bound class. Using '%s' instead.",
            suggestedClassName,
            definition.getName(),
            definition.getContainingModule().getLocation(),
            retval));
      }
    }
    return retval;
  }

  @Override
  public ClassName getBaseClassName(IModelDefinition definition) {
    String className = bindingConfiguration.getQualifiedBaseClassName(definition);
    ClassName retval = null;
    if (className != null) {
      retval = ClassName.bestGuess(className);
    }
    return retval;
  }

  @Override
  public List<ClassName> getSuperinterfaces(IModelDefinition definition) {
    List<String> classNames = bindingConfiguration.getQualifiedSuperinterfaceClassNames(definition);
    return classNames.stream()
        .map(ClassName::bestGuess)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public String getPackageName(@NonNull IModule module) {
    return bindingConfiguration.getPackageNameForModule(module);
  }

  @Override
  @NonNull
  public String getPropertyName(IDefinitionTypeInfo parent, String name) {
    synchronized (typeInfoToPropertyNameMap) {
      Set<String> propertyNames = typeInfoToPropertyNameMap.get(parent);
      if (propertyNames == null) {
        propertyNames = new HashSet<>();
      }

      String retval = name;
      int index = 0;
      while (propertyNames.contains(retval)) {
        // append an integer value to make the name unique
        retval = ClassUtils.toPropertyName(name + Integer.toString(++index));
      }
      propertyNames.add(retval);
      return retval;
    }
  }

}
