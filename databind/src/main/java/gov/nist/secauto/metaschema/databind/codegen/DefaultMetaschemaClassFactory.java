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

package gov.nist.secauto.metaschema.databind.codegen;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.core.model.MetaschemaModelConstants;
import gov.nist.secauto.metaschema.core.model.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IFieldDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IFieldValueTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IFlagInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IModelDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.IModelInstanceTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.ITypeResolver;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonFieldValueKeyFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaFieldValue;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaPackage;
import gov.nist.secauto.metaschema.databind.model.annotations.Module;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlSchema;

import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings({
    "PMD.CouplingBetweenObjects", // ok
    "PMD.GodClass", // ok
    "PMD.CyclomaticComplexity" // ok
})
class DefaultMetaschemaClassFactory implements IMetaschemaClassFactory {
  @NonNull
  private final ITypeResolver typeResolver;

  /**
   * Get a new instance of the this class generation factory that uses the
   * provided {@code typeResolver}.
   *
   * @param typeResolver
   *          the resolver used to generate type information for Metasschema
   *          constructs
   * @return the new class factory
   */
  @NonNull
  public static DefaultMetaschemaClassFactory newInstance(@NonNull ITypeResolver typeResolver) {
    return new DefaultMetaschemaClassFactory(typeResolver);
  }

  /**
   * Construct a new instance of the this class ganeration factory that uses the
   * provided {@code typeResolver}.
   *
   * @param typeResolver
   *          the resolver used to generate type information for Metasschema
   *          constructs
   */
  protected DefaultMetaschemaClassFactory(@NonNull ITypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  @NonNull
  public ITypeResolver getTypeResolver() {
    return typeResolver;
  }

  @Override
  public IGeneratedModuleClass generateClass(
      IModule module,
      Path targetDirectory) throws IOException {

    // Generate the Module module class
    ClassName className = getTypeResolver().getClassName(module);

    TypeSpec.Builder classSpec = newClassBuilder(module, className);

    JavaFile javaFile = JavaFile.builder(className.packageName(), classSpec.build()).build();
    Path classFile = ObjectUtils.notNull(javaFile.writeToPath(targetDirectory));

    // now generate all related definition classes
    Stream<? extends IFlagContainer> globalDefinitions = Stream.concat(
        module.getAssemblyDefinitions().stream(),
        module.getFieldDefinitions().stream());

    Set<String> classNames = new HashSet<>();

    @SuppressWarnings("PMD.UseConcurrentHashMap") // map is unmodifiable
    Map<IFlagContainer, IGeneratedDefinitionClass> definitionProductions
        = ObjectUtils.notNull(globalDefinitions
            // Get type information for assembly and field definitions.
            // Avoid field definitions without flags that don't require a generated class
            .flatMap(definition -> {
              IModelDefinitionTypeInfo typeInfo = null;
              if (definition instanceof IAssemblyDefinition) {
                typeInfo = IAssemblyDefinitionTypeInfo.newTypeInfo((IAssemblyDefinition) definition, typeResolver);
              } else if (definition instanceof IFieldDefinition
                  && !((IFieldDefinition) definition).getFlagInstances().isEmpty()) {
                typeInfo = IFieldDefinitionTypeInfo.newTypeInfo((IFieldDefinition) definition, typeResolver);
              } // otherwise field is just a simple data value, then no class is needed
              return typeInfo == null ? null : Stream.of(typeInfo);
            })
            // generate the class for each type information
            .map(typeInfo -> {
              IFlagContainer definition = typeInfo.getDefinition();
              IGeneratedDefinitionClass generatedClass;
              try {
                generatedClass = generateClass(typeInfo, targetDirectory);
              } catch (RuntimeException ex) { // NOPMD - intended
                throw new IllegalStateException(
                    String.format("Unable to generate class for definition '%s' in Module '%s'",
                        definition.getName(),
                        module.getLocation()),
                    ex);
              } catch (IOException ex) {
                throw new IllegalStateException(ex);
              }
              String defClassName = generatedClass.getClassName().canonicalName();
              if (classNames.contains(defClassName)) {
                throw new IllegalStateException(String.format(
                    "Found duplicate class '%s' in metaschema '%s'."
                        + " All class names must be unique within the same namespace.",
                    defClassName, module.getLocation()));
              }
              classNames.add(defClassName);
              return generatedClass;
            })
            // collect the generated class information
            .collect(Collectors.toUnmodifiableMap(
                IGeneratedDefinitionClass::getDefinition,
                Function.identity())));
    String packageName = typeResolver.getPackageName(module);
    return new DefaultGeneratedModuleClass(module, className, classFile, definitionProductions, packageName);

  }

  @Override
  public IGeneratedDefinitionClass generateClass(
      IModelDefinitionTypeInfo typeInfo,
      Path targetDirectory)
      throws IOException {
    ClassName className = typeInfo.getClassName();

    TypeSpec.Builder classSpec = newClassBuilder(typeInfo, false);

    JavaFile javaFile = JavaFile.builder(className.packageName(), classSpec.build()).build();
    Path classFile = ObjectUtils.notNull(javaFile.writeToPath(targetDirectory));

    return new DefaultGeneratedDefinitionClass(classFile, className, typeInfo.getDefinition());
  }

  @Override
  public IGeneratedClass generatePackageInfoClass(
      String javaPackage,
      URI xmlNamespace,
      Collection<IGeneratedModuleClass> moduleProductions,
      Path targetDirectory) throws IOException {

    String packagePath = javaPackage.replace(".", "/");
    Path packageInfo = ObjectUtils.notNull(targetDirectory.resolve(packagePath + "/package-info.java"));

    try (PrintWriter writer = new PrintWriter(
        Files.newBufferedWriter(packageInfo, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING))) {
      writer.format("@%1$s(moduleClass = {%n", MetaschemaPackage.class.getName());

      boolean first = true;
      for (IGeneratedModuleClass moduleProduction : moduleProductions) {
        if (first) {
          first = false;
        } else {
          writer.format(",%n");
        }
        writer.format("  %1$s.class", moduleProduction.getClassName().canonicalName());
      }

      writer.format("})%n");

      writer.format(
          "@%1$s(namespace = \"%2$s\", xmlns = {@%3$s(prefix = \"\", namespace = \"%2$s\")},"
              + " xmlElementFormDefault = %4$s.QUALIFIED)%n",
          XmlSchema.class.getName(), xmlNamespace.toString(), XmlNs.class.getName(), XmlNsForm.class.getName());
      writer.format("package %s;%n", javaPackage);
    }

    return new DefaultGeneratedClass(packageInfo, ObjectUtils.notNull(ClassName.get(javaPackage, "package-info")));
  }

  /**
   * Creates and configures a builder, for a Module module, that can be used to
   * generate a Java class.
   *
   * @param module
   *          a parsed Module module
   * @param className
   *          the name of the class to create for the Module module
   * @return the class builder
   */
  @NonNull
  protected TypeSpec.Builder newClassBuilder(
      @NonNull IModule module,
      @NonNull ClassName className) { // NOPMD - long, but readable

    // create the class
    TypeSpec.Builder builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    builder.superclass(AbstractBoundModule.class);

    AnnotationSpec.Builder moduleAnnotation = AnnotationSpec.builder(Module.class);

    ITypeResolver typeResolver = getTypeResolver();
    for (IFieldDefinition definition : module.getFieldDefinitions()) {
      if (!definition.isSimple()) {
        moduleAnnotation.addMember("fields", "$T.class", typeResolver.getClassName(definition));
      }
    }

    for (IAssemblyDefinition definition : module.getAssemblyDefinitions()) {
      moduleAnnotation.addMember(
          "assemblies",
          "$T.class",
          typeResolver.getClassName(ObjectUtils.notNull(definition)));
    }

    for (IModule moduleImport : module.getImportedModules()) {
      moduleAnnotation.addMember(
          "imports",
          "$T.class",
          typeResolver.getClassName(ObjectUtils.notNull(moduleImport)));
    }

    {
      MarkupMultiline remarks = module.getRemarks();
      if (remarks != null) {
        moduleAnnotation.addMember("remarks", "$S", remarks.toMarkdown());
      }
    }

    builder.addAnnotation(moduleAnnotation.build());

    builder.addField(
        FieldSpec.builder(MarkupLine.class, "NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.fromMarkdown($S)", MarkupLine.class, module.getName().toMarkdown())
            .build());

    builder.addField(
        FieldSpec.builder(String.class, "SHORT_NAME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", module.getShortName())
            .build());

    builder.addField(
        FieldSpec.builder(String.class, "VERSION", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", module.getVersion())
            .build());

    builder.addField(
        FieldSpec.builder(URI.class, "XML_NAMESPACE", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.create($S)", URI.class, module.getXmlNamespace())
            .build());

    builder.addField(
        FieldSpec.builder(URI.class, "JSON_BASE_URI", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.create($S)", URI.class, module.getJsonBaseUri())
            .build());

    MarkupMultiline remarks = module.getRemarks();
    if (remarks != null) {
      builder.addField(
          FieldSpec.builder(MarkupMultiline.class, "REMARKS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
              .initializer("$T.fromMarkdown($S)", MarkupMultiline.class, remarks.toMarkdown())
              .build());
    }

    builder.addMethod(
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                ParameterizedTypeName.get(ClassName.get(List.class),
                    WildcardTypeName.subtypeOf(IModule.class).box()),
                "importedModules")
            .addParameter(IBindingContext.class, "bindingContext")
            .addStatement("super($N, $N)", "importedModules", "bindingContext")
            .build());
    builder.addMethod(
        MethodSpec.methodBuilder("getName")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(MarkupLine.class)
            .addStatement("return NAME")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getShortName")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return SHORT_NAME")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getVersion")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return VERSION")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getXmlNamespace")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(URI.class)
            .addStatement("return XML_NAMESPACE")
            .build());

    builder.addMethod(
        MethodSpec.methodBuilder("getJsonBaseUri")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(URI.class)
            .addStatement("return JSON_BASE_URI")
            .build());

    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getRemarks")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(MarkupMultiline.class);

    if (remarks == null) {
      methodBuilder.addStatement("return null");
    } else {
      methodBuilder.addStatement("return REMARKS");
    }

    builder.addMethod(methodBuilder.build());

    return builder;
  }

  /**
   * Creates and configures a builder, for a Module model definition, that can be
   * used to generate a Java class.
   *
   * @param typeInfo
   *          the type information for the class to generate
   * @param isChild
   *          {@code true} if the class to be generated is a child class, or
   *          {@code false} otherwise
   * @return the class builder
   * @throws IOException
   *           if an error occurred while building the Java class
   */
  @NonNull
  protected TypeSpec.Builder newClassBuilder(
      @NonNull IModelDefinitionTypeInfo typeInfo,
      boolean isChild) throws IOException {
    // create the class
    TypeSpec.Builder builder = TypeSpec.classBuilder(typeInfo.getClassName()).addModifiers(Modifier.PUBLIC);
    assert builder != null;
    if (isChild) {
      builder.addModifiers(Modifier.STATIC);
    }

    ClassName baseClassName = typeInfo.getBaseClassName();
    if (baseClassName != null) {
      builder.superclass(baseClassName);
    }

    Set<IFlagContainer> additionalChildClasses;
    if (typeInfo instanceof IAssemblyDefinitionTypeInfo) {
      additionalChildClasses = buildClass((IAssemblyDefinitionTypeInfo) typeInfo, builder);
    } else if (typeInfo instanceof IFieldDefinitionTypeInfo) {
      additionalChildClasses = buildClass((IFieldDefinitionTypeInfo) typeInfo, builder);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported type: %s", typeInfo.getClass().getName()));
    }

    ITypeResolver typeResolver = getTypeResolver();

    for (IFlagContainer definition : additionalChildClasses) {
      assert definition != null;
      IModelDefinitionTypeInfo childTypeInfo = typeResolver.getTypeInfo(definition);
      TypeSpec childClass = newClassBuilder(childTypeInfo, true).build();
      builder.addType(childClass);
    }
    return ObjectUtils.notNull(builder);
  }

  /**
   * Generate the contents of the class represented by the provided
   * {@code builder}.
   *
   * @param typeInfo
   *          the type information for the class to build
   * @param builder
   *          the builder to use for generating the class content
   * @return the set of additional definitions for which child classes need to be
   *         generated
   */
  protected Set<IFlagContainer> buildClass(
      @NonNull IAssemblyDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    Set<IFlagContainer> retval = new HashSet<>();

    retval.addAll(buildClass((IModelDefinitionTypeInfo) typeInfo, builder));

    AnnotationSpec.Builder metaschemaAssembly = ObjectUtils.notNull(AnnotationSpec.builder(MetaschemaAssembly.class));

    buildCommonProperties(typeInfo, metaschemaAssembly);

    IAssemblyDefinition definition = typeInfo.getDefinition();
    if (definition.isRoot()) {
      metaschemaAssembly.addMember("rootName", "$S", definition.getRootName());
    }

    MarkupMultiline remarks = definition.getRemarks();
    if (remarks != null) {
      metaschemaAssembly.addMember("remarks", "$S", remarks.toMarkdown());
    }

    builder.addAnnotation(metaschemaAssembly.build());

    AnnotationGenerator.buildValueConstraints(builder, definition);
    AnnotationGenerator.buildAssemblyConstraints(builder, definition);
    return retval;
  }

  /**
   * Generate the contents of the class represented by the provided
   * {@code builder}.
   *
   * @param typeInfo
   *          the type information for the class to build
   * @param builder
   *          the builder to use for generating the class content
   * @return the set of additional definitions for which child classes need to be
   *         generated
   */
  protected Set<IFlagContainer> buildClass(
      @NonNull IFieldDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    Set<IFlagContainer> retval = new HashSet<>();
    retval.addAll(buildClass((IModelDefinitionTypeInfo) typeInfo, builder));

    AnnotationSpec.Builder metaschemaField = ObjectUtils.notNull(AnnotationSpec.builder(MetaschemaField.class));

    buildCommonProperties(typeInfo, metaschemaField);

    builder.addAnnotation(metaschemaField.build());

    IFieldDefinition definition = typeInfo.getDefinition();
    AnnotationGenerator.buildValueConstraints(builder, definition);
    return retval;
  }

  /**
   * Generate the contents of the class represented by the provided
   * {@code builder}.
   *
   * @param typeInfo
   *          the type information for the class to build
   * @param builder
   *          the builder to use for generating the class content
   * @return the set of additional definitions for which child classes need to be
   *         generated
   */
  @NonNull
  protected Set<IFlagContainer> buildClass(
      @NonNull IModelDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    MarkupLine description = typeInfo.getDefinition().getDescription();
    if (description != null) {
      builder.addJavadoc(description.toHtml());
    }

    Set<IFlagContainer> additionalChildClasses = new HashSet<>();

    // generate a no-arg constructor
    builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

    // // generate a copy constructor
    // MethodSpec.Builder copyBuilder =
    // MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
    // copyBuilder.addParameter(className, "that", Modifier.FINAL);
    // for (IPropertyGenerator property : getPropertyGenerators()) {
    // additionalChildClasses.addAll(property.buildCopyStatements(copyBuilder,
    // getTypeResolver()));
    // }
    // builder.addMethod(copyBuilder.build());

    // generate all the properties and access methods
    for (ITypeInfo property : typeInfo.getPropertyTypeInfos()) {
      assert property != null;
      additionalChildClasses.addAll(buildClass(property, builder));
    }

    // generate a toString method that will help with debugging
    MethodSpec.Builder toString = MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC)
        .returns(String.class).addAnnotation(Override.class);
    toString.addStatement("return new $T(this, $T.MULTI_LINE_STYLE).toString()", ReflectionToStringBuilder.class,
        MultilineRecursiveToStringStyle.class);
    builder.addMethod(toString.build());
    return CollectionUtil.unmodifiableSet(additionalChildClasses);
  }

  /**
   * Build the Java class data for the property.
   *
   * @param typeInfo
   *          the type information for the Java property to build
   * @param builder
   *          the class builder
   * @return the set of additional child definitions that need to be built
   */
  @NonNull
  protected Set<IFlagContainer> buildClass(
      @NonNull ITypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {

    TypeName javaFieldType = typeInfo.getJavaFieldType();
    FieldSpec.Builder field = FieldSpec.builder(javaFieldType, typeInfo.getJavaFieldName())
        .addModifiers(Modifier.PRIVATE);
    assert field != null;

    final Set<IFlagContainer> retval = buildField(typeInfo, field);

    FieldSpec valueField = ObjectUtils.notNull(field.build());
    builder.addField(valueField);

    String propertyName = typeInfo.getPropertyName();
    {
      MethodSpec.Builder method = MethodSpec.methodBuilder("get" + propertyName)
          .returns(javaFieldType)
          .addModifiers(Modifier.PUBLIC);
      assert method != null;
      method.addStatement("return $N", valueField);
      builder.addMethod(method.build());
    }

    {
      ParameterSpec valueParam = ParameterSpec.builder(javaFieldType, "value").build();
      MethodSpec.Builder method = MethodSpec.methodBuilder("set" + propertyName)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(valueParam);
      assert method != null;
      method.addStatement("$N = $N", valueField, valueParam);
      builder.addMethod(method.build());
    }

    if (typeInfo instanceof IModelInstanceTypeInfo) {
      buildExtraMethods((IModelInstanceTypeInfo) typeInfo, builder, valueField);
    }
    return retval;
  }

  /**
   * Build the core property annotations that are common to all Module classes.
   *
   * @param typeInfo
   *          the type information for the Java property to build
   * @param builder
   *          the class builder
   */
  protected void buildCommonProperties(
      @NonNull IModelDefinitionTypeInfo typeInfo,
      @NonNull AnnotationSpec.Builder builder) {
    IDefinition definition = typeInfo.getDefinition();

    String formalName = definition.getEffectiveFormalName();
    if (formalName != null) {
      builder.addMember("formalName", "$S", formalName);
    }

    MarkupLine description = definition.getEffectiveDescription();
    if (description != null) {
      builder.addMember("description", "$S", description.toMarkdown());
    }

    builder.addMember("name", "$S", definition.getName());
    IModule module = definition.getContainingModule();
    builder.addMember("moduleClass", "$T.class", getTypeResolver().getClassName(module));
  }

  /**
   * Generate the Java field associated with this property.
   *
   * @param typeInfo
   *          the type information for the Java property to build
   * @param builder
   *          the field builder
   * @return the set of definitions used by this field
   */
  @NonNull
  protected Set<IFlagContainer> buildField(
      @NonNull ITypeInfo typeInfo,
      @NonNull FieldSpec.Builder builder) {
    Set<IFlagContainer> retval = null;
    if (typeInfo instanceof IFlagInstanceTypeInfo) {
      buildFieldForFlag((IFlagInstanceTypeInfo) typeInfo, builder);
    } else if (typeInfo instanceof IModelInstanceTypeInfo) {
      retval = buildFieldForModelInstance((IModelInstanceTypeInfo) typeInfo, builder);
    } else if (typeInfo instanceof IFieldValueTypeInfo) {
      buildFieldForFieldValue((IFieldValueTypeInfo) typeInfo, builder);
    }
    return retval == null ? CollectionUtil.emptySet() : retval;
  }

  protected void buildFieldForInstance(
      @NonNull IInstanceTypeInfo typeInfo,
      @NonNull FieldSpec.Builder builder) {
    MarkupLine description = typeInfo.getInstance().getDescription();
    if (description != null) {
      builder.addJavadoc("$S", description.toHtml());
    }
  }

  protected void buildFieldForFieldValue(
      @NonNull IFieldValueTypeInfo typeInfo,
      @NonNull FieldSpec.Builder builder) {
    IFieldDefinition definition = typeInfo.getParentDefinitionTypeInfo().getDefinition();
    AnnotationSpec.Builder fieldValue = AnnotationSpec.builder(MetaschemaFieldValue.class);

    IDataTypeAdapter<?> valueDataType = definition.getJavaTypeAdapter();

    // a field object always has a single value
    if (!definition.hasJsonValueKeyFlagInstance()) {
      fieldValue.addMember("valueKeyName", "$S", definition.getJsonValueKeyName());
    } // else do nothing, the annotation will be on the flag

    if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType)) {
      fieldValue.addMember("typeAdapter", "$T.class", valueDataType.getClass());
    }

    Object defaultValue = definition.getDefaultValue();
    if (defaultValue != null) {
      fieldValue.addMember("defaultValue", "$S", valueDataType.asString(defaultValue));
    }

    builder.addAnnotation(fieldValue.build());
  }

  @SuppressWarnings("PMD.CyclomaticComplexity") // acceptable
  protected void buildFieldForFlag(
      @NonNull IFlagInstanceTypeInfo typeInfo,
      @NonNull FieldSpec.Builder builder) {
    IFlagInstance instance = typeInfo.getInstance();

    AnnotationSpec.Builder annotation
        = AnnotationSpec.builder(BoundFlag.class);

    String formalName = instance.getEffectiveFormalName();
    if (formalName != null) {
      annotation.addMember("formalName", "$S", formalName);
    }

    MarkupLine description = instance.getEffectiveDescription();
    if (description != null) {
      annotation.addMember("description", "$S", description.toMarkdown());
    }

    annotation.addMember("useName", "$S", instance.getEffectiveName());

    if (instance.isRequired()) {
      annotation.addMember("required", "$L", true);
    }

    IFlagDefinition definition = instance.getDefinition();

    IDataTypeAdapter<?> valueDataType = definition.getJavaTypeAdapter();
    annotation.addMember("typeAdapter", "$T.class", valueDataType.getClass());

    MarkupMultiline remarks = instance.getRemarks();
    if (remarks != null) {
      annotation.addMember("remarks", "$S", remarks.toMarkdown());
    }

    builder.addAnnotation(annotation.build());

    AnnotationGenerator.buildValueConstraints(builder, definition);

    IFlagContainer parent = instance.getContainingDefinition();
    if (parent.hasJsonKey() && instance.equals(parent.getJsonKeyFlagInstance())) {
      builder.addAnnotation(JsonKey.class);
    }

    if (parent instanceof IFieldDefinition) {
      IFieldDefinition parentField = (IFieldDefinition) parent;

      if (parentField.hasJsonValueKeyFlagInstance() && instance.equals(parentField.getJsonValueKeyFlagInstance())) {
        builder.addAnnotation(JsonFieldValueKeyFlag.class);
      }
    }
  }

  @SuppressWarnings("PMD.NPathComplexity")
  @NonNull
  protected AnnotationSpec.Builder generateBindingAnnotation(
      @NonNull IModelInstanceTypeInfo typeInfo) {
    // determine which annotation to apply
    AnnotationSpec.Builder retval;
    INamedModelInstance modelInstance = typeInfo.getInstance();
    if (modelInstance instanceof IFieldInstance) {
      retval = AnnotationSpec.builder(BoundField.class);
    } else if (modelInstance instanceof IAssemblyInstance) {
      retval = AnnotationSpec.builder(BoundAssembly.class);
    } else {
      throw new UnsupportedOperationException(
          String.format("ModelContainer instance '%s' of type '%s' is not supported.",
              modelInstance.getName(), modelInstance.getClass().getName()));
    }

    String formalName = modelInstance.getEffectiveFormalName();
    if (formalName != null) {
      retval.addMember("formalName", "$S", formalName);
    }

    MarkupLine description = modelInstance.getEffectiveDescription();
    if (description != null) {
      retval.addMember("description", "$S", description.toMarkdown());
    }

    retval.addMember("useName", "$S", modelInstance.getEffectiveName());

    String namespace = modelInstance.getXmlNamespace();
    if (namespace == null) {
      retval.addMember("namespace", "$S", "##none");
    } else if (!modelInstance.getContainingModule().getXmlNamespace().toASCIIString().equals(namespace)) {
      retval.addMember("namespace", "$S", namespace);
    } // otherwise use the ##default

    int minOccurs = modelInstance.getMinOccurs();
    if (minOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MIN_OCCURS) {
      retval.addMember("minOccurs", "$L", minOccurs);
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    if (maxOccurs != MetaschemaModelConstants.DEFAULT_GROUP_AS_MAX_OCCURS) {
      retval.addMember("maxOccurs", "$L", maxOccurs);
    }

    MarkupMultiline remarks = modelInstance.getRemarks();
    if (remarks != null) {
      retval.addMember("remarks", "$S", remarks.toMarkdown());
    }

    if (modelInstance instanceof IFieldInstance) {
      IFieldInstance fieldInstance = (IFieldInstance) modelInstance;

      if (MetaschemaModelConstants.DEFAULT_FIELD_IN_XML_WRAPPED != fieldInstance.isInXmlWrapped()) {
        retval.addMember("inXmlWrapped", "$L", fieldInstance.isInXmlWrapped());
      }
    }
    return retval;
  }

  @NonNull
  protected AnnotationSpec.Builder generateGroupAsAnnotation(
      @NonNull IModelInstanceTypeInfo typeInfo) {
    AnnotationSpec.Builder groupAsAnnoation = AnnotationSpec.builder(GroupAs.class);

    INamedModelInstance modelInstance = typeInfo.getInstance();

    groupAsAnnoation.addMember("name", "$S",
        ObjectUtils.requireNonNull(modelInstance.getGroupAsName(), "The grouping name must be non-null"));

    String groupAsNamespace = modelInstance.getGroupAsXmlNamespace();
    if (groupAsNamespace == null) {
      groupAsAnnoation.addMember("namespace", "$S", "##none");
    } else if (!modelInstance.getContainingModule().getXmlNamespace().toASCIIString().equals(groupAsNamespace)) {
      groupAsAnnoation.addMember("namespace", "$S", groupAsNamespace);
    } // otherwise use the ##default

    JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
    assert jsonGroupAsBehavior != null;
    if (!MetaschemaModelConstants.DEFAULT_JSON_GROUP_AS_BEHAVIOR.equals(jsonGroupAsBehavior)) {
      groupAsAnnoation.addMember("inJson", "$T.$L",
          JsonGroupAsBehavior.class, jsonGroupAsBehavior.toString());
    }

    XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
    assert xmlGroupAsBehavior != null;
    if (!MetaschemaModelConstants.DEFAULT_XML_GROUP_AS_BEHAVIOR.equals(xmlGroupAsBehavior)) {
      groupAsAnnoation.addMember("inXml", "$T.$L",
          XmlGroupAsBehavior.class, xmlGroupAsBehavior.toString());
    }
    return groupAsAnnoation;
  }

  @SuppressWarnings("PMD.CognitiveComplexity")
  public Set<IFlagContainer> buildFieldForModelInstance(
      @NonNull IModelInstanceTypeInfo typeInfo,
      @NonNull FieldSpec.Builder builder) { // NOPMD - intentional
    buildFieldForInstance(typeInfo, builder);

    builder.addAnnotation(generateBindingAnnotation(typeInfo).build());

    INamedModelInstance modelInstance = typeInfo.getInstance();
    IFlagContainer definition = modelInstance.getDefinition();
    if (modelInstance instanceof IFieldInstance) {
      // handle the field value related info
      IFieldDefinition fieldDefinition = (IFieldDefinition) definition;
      if (fieldDefinition.isSimple()) {
        // this is a simple field, without flags
        // we need to add the BoundFieldValue annotation to the property
        // fieldAnnoation.addMember("valueName", "$S",
        // fieldDefinition.getJsonValueKeyName());
        IDataTypeAdapter<?> valueDataType = fieldDefinition.getJavaTypeAdapter();

        Object defaultValue = fieldDefinition.getDefaultValue();

        if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType) || defaultValue != null) {
          AnnotationSpec.Builder boundFieldValueAnnotation = AnnotationSpec.builder(BoundFieldValue.class);

          if (!MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE.equals(valueDataType)) {
            boundFieldValueAnnotation.addMember("typeAdapter", "$T.class", valueDataType.getClass());
          }

          if (defaultValue != null) {
            boundFieldValueAnnotation.addMember("defaultValue", "$S", valueDataType.asString(defaultValue));
          }
          builder.addAnnotation(boundFieldValueAnnotation.build());
        }

        AnnotationGenerator.buildValueConstraints(builder, fieldDefinition);
      }
    }

    int maxOccurs = modelInstance.getMaxOccurs();
    if (maxOccurs == -1 || maxOccurs > 1) {
      // requires a group-as
      builder.addAnnotation(generateGroupAsAnnotation(typeInfo).build());
    }

    Set<IFlagContainer> retval = new HashSet<>();
    if (definition.isInline() && !(definition instanceof IFieldDefinition && definition.isSimple())) {
      // this is an inline definition that must be built as a child class
      retval.add(definition);
    }
    return retval.isEmpty() ? CollectionUtil.emptySet() : CollectionUtil.unmodifiableSet(retval);
  }

  /**
   * This method can be implemented by subclasses to create additional methods.
   *
   * @param typeInfo
   *          the type information for the Java property to build
   * @param builder
   *          the class builder
   * @param valueField
   *          the field corresponding to this property
   */
  @SuppressWarnings("PMD.LooseCoupling") // need implementation classes
  protected void buildExtraMethods( // NOPMD - intentional
      @NonNull IModelInstanceTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder,
      @NonNull FieldSpec valueField) {
    INamedModelInstance instance = typeInfo.getInstance();
    int maxOccurance = instance.getMaxOccurs();
    if (maxOccurance == -1 || maxOccurance > 1) {
      TypeName itemType = typeInfo.getJavaItemType();
      ParameterSpec valueParam = ParameterSpec.builder(itemType, "item").build();

      String itemPropertyName = ClassUtils.toPropertyName(typeInfo.getItemBaseName());

      if (JsonGroupAsBehavior.KEYED.equals(instance.getJsonGroupAsBehavior())) {
        IFlagInstance jsonKey = instance.getDefinition().getJsonKeyFlagInstance();
        if (jsonKey == null) {
          throw new IllegalStateException(
              String.format("JSON key not defined for property: %s", instance.toCoordinates()));
        }

        // get the json key property on the instance's definition
        IModelDefinitionTypeInfo instanceTypeInfo = typeResolver.getTypeInfo(instance.getDefinition());
        IFlagInstanceTypeInfo jsonKeyTypeInfo = instanceTypeInfo.getFlagInstanceTypeInfo(jsonKey);

        if (jsonKeyTypeInfo == null) {
          throw new IllegalStateException(
              String.format("Unable to identify JSON key for property: %s", instance.toCoordinates()));
        }

        {
          // create add method
          MethodSpec.Builder method = MethodSpec.methodBuilder("add" + itemPropertyName)
              .addParameter(valueParam)
              .returns(itemType)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Add a new {@link $T} item to the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to add\n")
              .addJavadoc("@return the existing {@link $T} item in the collection or {@code null} if not item exists\n",
                  itemType)
              .addStatement("$1T value = $2T.requireNonNull($3N,\"$3N value cannot be null\")",
                  itemType, ObjectUtils.class, valueParam)
              .addStatement("$1T key = $2T.requireNonNull($3N.$4N(),\"$3N key cannot be null\")",
                  String.class, ObjectUtils.class, valueParam, "get" + jsonKeyTypeInfo.getPropertyName())
              .beginControlFlow("if ($N == null)", valueField)
              .addStatement("$N = new $T<>()", valueField, LinkedHashMap.class)
              .endControlFlow()
              .addStatement("return $N.put(key, value)", valueField);

          builder.addMethod(method.build());
        }
        {
          // create remove method
          MethodSpec.Builder method = MethodSpec.methodBuilder("remove" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Remove the {@link $T} item from the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to remove\n")
              .addJavadoc("@return {@code true} if the item was removed or {@code false} otherwise\n")
              .addStatement("$1T value = $2T.requireNonNull($3N,\"$3N value cannot be null\")",
                  itemType, ObjectUtils.class, valueParam)
              .addStatement("$1T key = $2T.requireNonNull($3N.$4N(),\"$3N key cannot be null\")",
                  String.class, ObjectUtils.class, valueParam, "get" + jsonKeyTypeInfo.getPropertyName())
              .addStatement("return $1N == null ? false : $1N.remove(key, value)", valueField);
          builder.addMethod(method.build());
        }
      } else {
        {
          // create add method
          MethodSpec.Builder method = MethodSpec.methodBuilder("add" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Add a new {@link $T} item to the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to add\n")
              .addJavadoc("@return {@code true}\n")
              .addStatement("$T value = $T.requireNonNull($N,\"$N cannot be null\")",
                  itemType, ObjectUtils.class, valueParam, valueParam)
              .beginControlFlow("if ($N == null)", valueField)
              .addStatement("$N = new $T<>()", valueField, LinkedList.class)
              .endControlFlow()
              .addStatement("return $N.add(value)", valueField);

          builder.addMethod(method.build());
        }

        {
          // create remove method
          MethodSpec.Builder method = MethodSpec.methodBuilder("remove" + itemPropertyName)
              .addParameter(valueParam)
              .returns(TypeName.BOOLEAN)
              .addModifiers(Modifier.PUBLIC)
              .addJavadoc("Remove the first matching {@link $T} item from the underlying collection.\n", itemType)
              .addJavadoc("@param item the item to remove\n")
              .addJavadoc("@return {@code true} if the item was removed or {@code false} otherwise\n")
              .addStatement("$T value = $T.requireNonNull($N,\"$N cannot be null\")",
                  itemType, ObjectUtils.class, valueParam, valueParam)
              .addStatement("return $1N == null ? false : $1N.remove(value)", valueField);
          builder.addMethod(method.build());
        }
      }
    }
  }
}
