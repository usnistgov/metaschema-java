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

import gov.nist.secauto.metaschema.codegen.property.FlagPropertyGenerator;
import gov.nist.secauto.metaschema.codegen.property.PropertyGenerator;
import gov.nist.secauto.metaschema.codegen.support.AnnotationUtils;
import gov.nist.secauto.metaschema.codegen.type.TypeResolver;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedModelDefinition;
import gov.nist.secauto.metaschema.model.common.explode.FlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;

import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Modifier;

public abstract class AbstractJavaClassGenerator<DEFINITION extends INamedModelDefinition>
    implements JavaClassGenerator {
  private static final Logger logger = LogManager.getLogger(AbstractJavaClassGenerator.class);

  @NotNull
  private final DEFINITION definition;
  @NotNull
  private final TypeResolver typeResolver;
  @NotNull
  private final Map<String, PropertyGenerator> propertyNameToPropertyGeneratorMap = new LinkedHashMap<>();
  private boolean hasJsonKeyFlag = false;

  /**
   * Constructs a new class generator based on the provided definition.
   * 
   * @param definition
   *          the Metaschema field or assembly definition for which a Java class will be generated
   * @param typeResolver
   *          the resolver to use to lookup Java type information for Metaschema objects
   */
  public AbstractJavaClassGenerator(@NotNull DEFINITION definition, @NotNull TypeResolver typeResolver) {
    Objects.requireNonNull(definition, "definition");
    Objects.requireNonNull(typeResolver, "typeResolver");
    this.definition = definition;
    this.typeResolver = typeResolver;
    this.hasJsonKeyFlag = definition.hasJsonKey();

    // create Java properties for the definition's flags
    for (IFlagInstance instance : definition.getFlagInstances()) {
      newFlagPropertyGenerator(instance);
    }
  }

  /**
   * Gets the Metaschema field or assembly definition for which a Java class will be generated.
   * 
   * @return the definition
   */
  @Override
  public DEFINITION getDefinition() {
    return definition;
  }

  @Override
  public ClassName getClassName() {
    return getTypeResolver().getClassName(getDefinition());
  }

  /**
   * Gets the resolver which can be used to lookup Java type information for Metaschema objects.
   * 
   * @return the type resolver
   */
  @NotNull
  public TypeResolver getTypeResolver() {
    return typeResolver;
  }

  protected void applyConstraints(AnnotationSpec.Builder annotation) {
    AnnotationUtils.applyAllowedValuesConstraints(annotation, getDefinition().getAllowedValuesContraints());
    AnnotationUtils.applyIndexHasKeyConstraints(annotation, getDefinition().getIndexHasKeyConstraints());
    AnnotationUtils.applyMatchesConstraints(annotation, getDefinition().getMatchesConstraints());
    AnnotationUtils.applyExpectConstraints(annotation, getDefinition().getExpectConstraints());
  }

  @Override
  public TypeSpec.Builder generateChildClass() throws IOException {
    ClassName className = getClassName();

    TypeSpec.Builder builder = generateClass(className, true);
    return builder;
  }

  @Override
  public JavaGenerator.GeneratedClass generateClass(File outputDir) throws IOException {
    ClassName className = getClassName();

    TypeSpec.Builder builder = generateClass(className, false);

    JavaFile javaFile = JavaFile.builder(className.packageName(), builder.build()).build();
    File classFile = javaFile.writeToFile(outputDir);

    return new JavaGenerator.GeneratedClass(classFile, className, isRootClass());
  }

  /**
   * Supports the building of Java lasses.
   * 
   * @param className
   *          the type info for the class
   * @param isChild
   *          {@code true} if the class to be generated is a child class, or {@code false} otherwise
   * @return the class builder
   * @throws IOException
   *           if a building error occurred while generating the Java class
   */
  protected TypeSpec.Builder generateClass(ClassName className, boolean isChild) throws IOException {

    TypeSpec.Builder builder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
    if (isChild) {
      builder.addModifiers(Modifier.STATIC);
    }

    Set<INamedModelDefinition> additionalChildClasses = buildClass(builder);

    for (INamedModelDefinition definition : additionalChildClasses) {
      TypeSpec.Builder childBuilder;
      switch (definition.getModelType()) {
      case ASSEMBLY:
        childBuilder
            = new AssemblyJavaClassGenerator((IAssemblyDefinition) definition, getTypeResolver()).generateChildClass();
        break;
      case FIELD:
        childBuilder
            = new FieldJavaClassGenerator((IFieldDefinition) definition, getTypeResolver()).generateChildClass();
        break;
      default:
        throw new UnsupportedOperationException(String
            .format("Generation of child classes for %s definitions is unsupported", definition.getModelType().name()));
      }
      builder.addType(childBuilder.build());
    }
    return builder;
  }

  /**
   * Identifies if this class to generate is a root data element in the bound (.i.e., XML, JSON, etc.)
   * model. This provides a convenient way to query the root status of the underlying object
   * definition based on what is known in the subclasses of this class.
   * 
   * @return {@code true} if the class to be generated is a possible model root, or {@code false}
   *         otherwise
   */
  protected abstract boolean isRootClass();

  /**
   * Adds the provided property generator to this class generator.
   * 
   * @param property
   *          the property generator to add
   */
  protected void addPropertyGenerator(PropertyGenerator property) {
    String name = property.getPropertyName();
    PropertyGenerator oldContext = propertyNameToPropertyGeneratorMap.put(name, property);
    if (oldContext != null) {
      logger.error("Unexpected duplicate Java property name '{}'", name);
      throw new RuntimeException(String.format("Unexpected duplicate property name '%s'", name));
    }
  }

  /**
   * Creates a new {@link PropertyGenerator} for the provided {@link FlagInstance} and registers it
   * with this class generator.
   * 
   * @param instance
   *          the flag instance to generate the property for
   * @return the new property generator
   */
  public FlagPropertyGenerator newFlagPropertyGenerator(@NotNull IFlagInstance instance) {
    FlagPropertyGenerator context = new FlagPropertyGenerator(instance, this);
    addPropertyGenerator(context);
    return context;
  }

  @Override
  public boolean hasPropertyWithName(String newName) {
    return propertyNameToPropertyGeneratorMap.containsKey(newName);
  }

  /**
   * Gets the collection of properties to generate when generating this class.
   * 
   * @return an unmodifiable collection of property generators
   */
  protected Collection<PropertyGenerator> getPropertyGenerators() {
    return Collections.unmodifiableCollection(propertyNameToPropertyGeneratorMap.values());
  }

  /**
   * Indicates if the Metaschema field or assembly associated with this generator has a JSON key
   * binding.
   * 
   * @return {@code true} if the JSON key binding is configured or {@code false} otherwise
   */
  public boolean hasJsonKeyFlag() {
    return hasJsonKeyFlag;
  }

  /**
   * Generate the class contents.
   * 
   * @param builder
   *          the builder to use for generating the class
   * @return the set of additional definitions for which child classes need to be generated
   * @throws IOException
   *           if an error occurred while building the class
   */
  protected Set<INamedModelDefinition> buildClass(TypeSpec.Builder builder) throws IOException {
    builder.addJavadoc(getDefinition().getDescription().toHtml());

    // generate a no-arg constructor
    builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

    // generate all the properties and access methods
    Set<INamedModelDefinition> additionalChildClasses = new HashSet<>();
    for (PropertyGenerator property : getPropertyGenerators()) {
      additionalChildClasses.addAll(property.build(builder, getTypeResolver()));
    }

    // generate a toString method that will help with debugging
    MethodSpec.Builder toString = MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC)
        .returns(String.class).addAnnotation(Override.class);
    toString.addStatement("return new $T(this, $T.MULTI_LINE_STYLE).toString()", ReflectionToStringBuilder.class,
        MultilineRecursiveToStringStyle.class);
    builder.addMethod(toString.build());
    return Collections.unmodifiableSet(additionalChildClasses);
  }
}
