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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.codegen.IGeneratedClass;
import gov.nist.secauto.metaschema.databind.codegen.IGeneratedDefinitionClass;
import gov.nist.secauto.metaschema.databind.codegen.IGeneratedModuleClass;
import gov.nist.secauto.metaschema.databind.codegen.impl.AnnotationGenerator;
import gov.nist.secauto.metaschema.databind.codegen.impl.DefaultGeneratedClass;
import gov.nist.secauto.metaschema.databind.codegen.impl.DefaultGeneratedDefinitionClass;
import gov.nist.secauto.metaschema.databind.codegen.impl.DefaultGeneratedModuleClass;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IAssemblyDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IFieldDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.codegen.typeinfo.def.IModelDefinitionTypeInfo;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundModule;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaPackage;
import gov.nist.secauto.metaschema.databind.model.annotations.Module;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.databind.model.annotations.XmlSchema;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
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
public class DefaultMetaschemaClassFactory implements IMetaschemaClassFactory {
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
    Stream<? extends IModelDefinition> globalDefinitions = Stream.concat(
        module.getAssemblyDefinitions().stream(),
        module.getFieldDefinitions().stream());

    Set<String> classNames = new HashSet<>();

    @SuppressWarnings("PMD.UseConcurrentHashMap") // map is unmodifiable
    Map<IModelDefinition, IGeneratedDefinitionClass> definitionProductions
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
              IModelDefinition definition = typeInfo.getDefinition();
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
      if (definition.hasChildren()) {
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
                    WildcardTypeName.subtypeOf(IBoundModule.class).box()),
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

    Set<IModelDefinition> additionalChildClasses;
    if (typeInfo instanceof IAssemblyDefinitionTypeInfo) {
      additionalChildClasses = buildClass((IAssemblyDefinitionTypeInfo) typeInfo, builder);
    } else if (typeInfo instanceof IFieldDefinitionTypeInfo) {
      additionalChildClasses = buildClass((IFieldDefinitionTypeInfo) typeInfo, builder);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported type: %s", typeInfo.getClass().getName()));
    }

    ITypeResolver typeResolver = getTypeResolver();

    for (IModelDefinition definition : additionalChildClasses) {
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
  protected Set<IModelDefinition> buildClass(
      @NonNull IAssemblyDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    Set<IModelDefinition> retval = new HashSet<>(buildClass((IModelDefinitionTypeInfo) typeInfo, builder));

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

    AnnotationGenerator.buildValueConstraints(metaschemaAssembly, definition);
    AnnotationGenerator.buildAssemblyConstraints(metaschemaAssembly, definition);

    builder.addAnnotation(metaschemaAssembly.build());
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
  protected Set<IModelDefinition> buildClass(
      @NonNull IFieldDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    Set<IModelDefinition> retval = new HashSet<>(buildClass((IModelDefinitionTypeInfo) typeInfo, builder));
    AnnotationSpec.Builder metaschemaField = ObjectUtils.notNull(AnnotationSpec.builder(MetaschemaField.class));

    buildCommonProperties(typeInfo, metaschemaField);

    IFieldDefinition definition = typeInfo.getDefinition();
    AnnotationGenerator.buildValueConstraints(metaschemaField, definition);

    builder.addAnnotation(metaschemaField.build());
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
  protected Set<IModelDefinition> buildClass(
      @NonNull IModelDefinitionTypeInfo typeInfo,
      @NonNull TypeSpec.Builder builder) {
    MarkupLine description = typeInfo.getDefinition().getDescription();
    if (description != null) {
      builder.addJavadoc(description.toHtml());
    }

    Set<IModelDefinition> additionalChildClasses = new HashSet<>();

    // // generate a no-arg constructor
    // builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

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
    for (IPropertyTypeInfo property : typeInfo.getPropertyTypeInfos()) {
      assert property != null;
      additionalChildClasses.addAll(property.build(builder));
    }

    // generate a toString method that will help with debugging
    MethodSpec.Builder toString = MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC)
        .returns(String.class).addAnnotation(Override.class);
    toString.addStatement("return new $T(this, $T.MULTI_LINE_STYLE).toString()", ReflectionToStringBuilder.class,
        ToStringStyle.class);
    builder.addMethod(toString.build());
    return CollectionUtil.unmodifiableSet(additionalChildClasses);
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
}
