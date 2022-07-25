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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

class AbstractModelDefinitionTypeInfo<DEF extends INamedModelDefinition>
    extends AbstractDefinitionTypeInfo<DEF>
    implements INamedModelDefinitionTypeInfo {
  @NotNull
  private final ClassName className;
  @Nullable
  private final ClassName baseClassName;
  private Map<@NotNull String, IFlagInstanceTypeInfo> flagTypeInfos;

  public AbstractModelDefinitionTypeInfo(@NotNull DEF definition,
      @NotNull ITypeResolver typeResolver) {
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
        flagTypeInfos = getDefinition().getFlagInstances().stream()
            .map(instance -> {
              return newFlagTypeInfo(instance);
            })
            .collect(Collectors.toUnmodifiableMap(IFlagInstanceTypeInfo::getPropertyName, Function.identity()));
        retval = true;
      } else {
        retval = false;
      }
      return retval;
    }
  }

  @Override
  public IFlagInstanceTypeInfo getFlagInstanceTypeInfo(@NotNull IFlagInstance instance) {
    initInstanceTypeInfos();
    return (IFlagInstanceTypeInfo) getInstanceTypeInfo(instance);
  }

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull IFlagInstanceTypeInfo> getFlagInstanceTypeInfos() {
    initInstanceTypeInfos();
    return flagTypeInfos.values();
  }

  protected IFlagInstanceTypeInfo newFlagTypeInfo(@NotNull IFlagInstance instance) {
    IFlagInstanceTypeInfo retval = new FlagInstanceTypeInfoImpl(instance, this);
    addPropertyTypeInfo(retval);
    return retval;
  }

  @Override
  public TypeSpec generateChildClass() throws IOException {
    return generateClass(getClassName(), true);
  }

  protected void buildCommonProperties(@NotNull AnnotationSpec.Builder annotation) {
    IDefinition definition = getDefinition();

    if (definition.getFormalName() != null) {
      annotation.addMember("formalName", "$S", definition.getFormalName());
    }

    if (definition.getDescription() != null) {
      annotation.addMember("description", "$S", definition.getDescription().toMarkdown());
    }

    annotation.addMember("name", "$S", definition.getName());
    IMetaschema metaschema = definition.getContainingMetaschema();
    annotation.addMember("metaschema", "$T.class", getTypeResolver().getClassName(metaschema));
  }

  protected void buildConstraints(@NotNull AnnotationSpec.Builder annotation) {
    INamedModelDefinition definition = getDefinition();
    AnnotationUtils.applyAllowedValuesConstraints(annotation, definition.getAllowedValuesConstraints());
    AnnotationUtils.applyIndexHasKeyConstraints(annotation, definition.getIndexHasKeyConstraints());
    AnnotationUtils.applyMatchesConstraints(annotation, definition.getMatchesConstraints());
    AnnotationUtils.applyExpectConstraints(annotation, definition.getExpectConstraints());
  }

  @Override
  public DefaultGeneratedDefinitionClass generateClass(Path outputDir) throws IOException {
    ClassName className = getClassName();

    TypeSpec classSpec = generateClass(className, false);

    JavaFile javaFile = JavaFile.builder(className.packageName(), classSpec).build();
    Path classFile = ObjectUtils.notNull(javaFile.writeToPath(outputDir));

    return new DefaultGeneratedDefinitionClass(classFile, className, getDefinition());
  }

  /**
   * Supports the building of Java classes.
   * 
   * @param className
   *          the type info for the class
   * @param isChild
   *          {@code true} if the class to be generated is a child class, or {@code false} otherwise
   * @return the class definition
   * @throws IOException
   *           if a building error occurred while generating the Java class
   */
  @NotNull
  protected TypeSpec generateClass(@NotNull ClassName className, boolean isChild) throws IOException {
    // create the class
    TypeSpec.Builder builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
    if (isChild) {
      builder.addModifiers(Modifier.STATIC);
    }

    ClassName baseClassName = getBaseClassName();
    if (baseClassName != null) {
      builder.superclass(baseClassName);
    }

    Set<@NotNull INamedModelDefinition> additionalChildClasses = buildClass(builder, className);

    ITypeResolver typeResolver = getTypeResolver();

    for (INamedModelDefinition definition : additionalChildClasses) {
      INamedModelDefinitionTypeInfo typeInfo = typeResolver.getTypeInfo(definition);
      TypeSpec childClass = typeInfo.generateChildClass();
      builder.addType(childClass);
    }
    return ObjectUtils.notNull(builder.build());
  }

  /**
   * Generate the class contents.
   * 
   * @param builder
   *          the builder to use for generating the class
   * @param className
   *          the name info for the class to build
   * @return the set of additional definitions for which child classes need to be generated
   * @throws IOException
   *           if an error occurred while building the class
   */
  @NotNull
  protected Set<@NotNull INamedModelDefinition> buildClass(@NotNull TypeSpec.Builder builder,
      @NotNull ClassName className)
      throws IOException {
    MarkupLine description = getDefinition().getDescription();
    if (description != null) {
      builder.addJavadoc(description.toHtml());
    }

    Set<@NotNull INamedModelDefinition> additionalChildClasses = new HashSet<>();

    // generate a no-arg constructor
    builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

    // // generate a copy constructor
    // MethodSpec.Builder copyBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    // copyBuilder.addParameter(className, "that", Modifier.FINAL);
    // for (IPropertyGenerator property : getPropertyGenerators()) {
    // additionalChildClasses.addAll(property.buildCopyStatements(copyBuilder, getTypeResolver()));
    // }
    // builder.addMethod(copyBuilder.build());

    // generate all the properties and access methods
    for (ITypeInfo property : getPropertyTypeInfos()) {
      additionalChildClasses.addAll(property.build(builder, getTypeResolver()));
    }

    // generate a toString method that will help with debugging
    MethodSpec.Builder toString = MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC)
        .returns(String.class).addAnnotation(Override.class);
    toString.addStatement("return new $T(this, $T.MULTI_LINE_STYLE).toString()", ReflectionToStringBuilder.class,
        MultilineRecursiveToStringStyle.class);
    builder.addMethod(toString.build());
    return CollectionUtil.unmodifiableSet(additionalChildClasses);
  }
}
