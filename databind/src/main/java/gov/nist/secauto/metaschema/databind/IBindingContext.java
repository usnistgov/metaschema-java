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

package gov.nist.secauto.metaschema.databind;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.core.model.constraint.FindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintValidationHandler;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintValidator;
import gov.nist.secauto.metaschema.core.model.validation.AggregateValidationResult;
import gov.nist.secauto.metaschema.core.model.validation.IValidationResult;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.io.yaml.YamlOperations;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModel;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;

import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides information supporting a binding between a set of Module models and
 * corresponding Java classes.
 */
public interface IBindingContext {

  /**
   * Get the singleton {@link IBindingContext} instance, which can be used to load
   * information that binds a model to a set of Java classes.
   *
   * @return a new binding context
   */
  @NonNull
  static IBindingContext instance() {
    return DefaultBindingContext.instance();
  }

  /**
   * Register a matcher used to identify a bound class by the definition's root
   * name.
   *
   * @param definition
   *          the definition to match for
   * @return the matcher
   */
  @NonNull
  IBindingMatcher registerBindingMatcher(@NonNull IBoundDefinitionModelAssembly definition);

  /**
   * Register a matcher used to identify a bound class by the definition's root
   * name.
   *
   * @param clazz
   *          the definition class to match for, which must represent a root
   *          assembly definition
   * @return the matcher
   */
  @NonNull
  IBindingMatcher registerBindingMatcher(@NonNull Class<?> clazz);

  /**
   * Register a class binding for a given bound class.
   *
   * @param definition
   *          the bound class information to register
   * @return the old bound class information or {@code null} if no binding existed
   *         for the associated class
   */
  @Nullable
  IBoundDefinitionModelComplex registerClassBinding(@NonNull IBoundDefinitionModelComplex definition);

  /**
   * Get the {@link IBoundDefinitionModel} instance associated with the provided
   * Java class.
   * <p>
   * Typically the class will have a {@link MetaschemaAssembly} or
   * {@link MetaschemaField} annotation.
   *
   * @param clazz
   *          the class binding to load
   * @return the associated class binding instance or {@code null} if the class is
   *         not bound
   */
  @Nullable
  IBoundDefinitionModelComplex getBoundDefinitionForClass(@NonNull Class<?> clazz);

  /**
   * Determine the bound class for the provided XML {@link QName}.
   *
   * @param rootQName
   *          the root XML element's QName
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(Class)
   */
  @Nullable
  Class<?> getBoundClassForRootXmlQName(@NonNull QName rootQName);

  /**
   * Determine the bound class for the provided JSON/YAML property/item name using
   * any registered matchers.
   *
   * @param rootName
   *          the JSON/YAML property/item name
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(Class)
   */
  @Nullable
  Class<?> getBoundClassForRootJsonName(@NonNull String rootName);

  /**
   * Get's the {@link IDataTypeAdapter} associated with the specified Java class,
   * which is used to read and write XML, JSON, and YAML data to and from
   * instances of that class. Thus, this adapter supports a direct binding between
   * the Java class and structured data in one of the supported formats. Adapters
   * are used to support bindings for simple data objects (e.g., {@link String},
   * {@link BigInteger}, {@link ZonedDateTime}, etc).
   *
   * @param <TYPE>
   *          the class type of the adapter
   * @param clazz
   *          the Java {@link Class} for the bound type
   * @return the adapter instance or {@code null} if the provided class is not
   *         bound
   */
  @Nullable
  <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NonNull Class<TYPE> clazz);

  /**
   * Load a bound Metaschema module implemented by the provided class.
   * <p>
   * Also registers any associated bound classes.
   * <p>
   * Implementations are expected to return the same IModule instance for multiple
   * calls to this method with the same class argument.
   *
   * @param clazz
   *          the class implementing a bound Metaschema module
   * @return the loaded module
   */
  @NonNull
  IBoundModule registerModule(@NonNull Class<? extends IBoundModule> clazz);

  /**
   * Generate, compile, and load a set of generated Module annotated Java classes
   * based on the provided Module {@code module}.
   *
   * @param module
   *          the Module module to generate classes for
   * @param compilePath
   *          the path to the directory to generate classes in
   * @return this instance
   * @throws IOException
   *           if an error occurred while generating or loading the classes
   */
  @NonNull
  IBindingContext registerModule(
      @NonNull IModule module,
      @NonNull Path compilePath) throws IOException;

  /**
   * Gets a data {@link ISerializer} which can be used to write Java instance data
   * for the provided class in the requested format.
   * <p>
   * The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a
   * {@link IBoundDefinitionModel} exists.
   *
   * @param <CLASS>
   *          the Java type this serializer can write data from
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data object to serialize
   * @return the serializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are
   *           {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getBoundDefinitionForClass(Class)
   */
  @NonNull
  <CLASS> ISerializer<CLASS> newSerializer(@NonNull Format format, @NonNull Class<CLASS> clazz);

  /**
   * Gets a data {@link IDeserializer} which can be used to read Java instance
   * data for the provided class from the requested format.
   * <p>
   * The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a
   * {@link IBoundDefinitionModel} exists.
   *
   * @param <CLASS>
   *          the Java type this deserializer can read data into
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @return the deserializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are
   *           {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getBoundDefinitionForClass(Class)
   */
  @NonNull
  <CLASS> IDeserializer<CLASS> newDeserializer(@NonNull Format format, @NonNull Class<CLASS> clazz);

  /**
   * Get a new {@link IBoundLoader} instance.
   *
   * @return the instance
   */
  @NonNull
  IBoundLoader newBoundLoader();

  /**
   * Create a deep copy of the provided bound object.
   *
   * @param <CLASS>
   *          the bound object type
   * @param other
   *          the object to copy
   * @param parentInstance
   *          the object's parent or {@code null}
   * @return a deep copy of the provided object
   * @throws BindingException
   *           if an error occurred copying content between java instances
   * @throws NullPointerException
   *           if the provided object is {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   */
  @NonNull
  <CLASS> CLASS deepCopy(@NonNull CLASS other, Object parentInstance) throws BindingException;

  /**
   * Get a new single use constraint validator.
   *
   * @param handler
   *          the validation handler to use to process the validation results
   *
   * @return the validator
   */
  default IConstraintValidator newValidator(@NonNull IConstraintValidationHandler handler) {
    IBoundLoader loader = newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    DynamicContext context = new DynamicContext();
    context.setDocumentLoader(loader);

    return new DefaultConstraintValidator(handler);
  }

  /**
   * Perform constraint validation on the provided bound object represented as an
   * {@link INodeItem}.
   *
   * @param nodeItem
   *          the node item to validate
   * @param loader
   *          a module loader used to load and resolve referenced resources
   * @return the validation result
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   */
  default IValidationResult validate(@NonNull INodeItem nodeItem, @NonNull IBoundLoader loader) {
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    IConstraintValidator validator = newValidator(handler);

    StaticContext staticContext = StaticContext.builder()
        .defaultModelNamespace(nodeItem.getNamespace())
        .build();

    DynamicContext dynamicContext = new DynamicContext(staticContext);
    dynamicContext.setDocumentLoader(loader);
    validator.validate(nodeItem, dynamicContext);
    validator.finalizeValidation(dynamicContext);
    return handler;
  }

  /**
   * Load and perform schema and constraint validation on the target. The
   * constraint validation will only be performed if the schema validation passes.
   *
   * @param target
   *          the target to validate
   * @param asFormat
   *          the schema format to use to validate the target
   * @param schemaProvider
   *          provides callbacks to get the appropriate schemas
   * @return the validation result
   * @throws IOException
   *           if an error occurred while reading the target
   * @throws SAXException
   *           if an error occurred when parsing the target as XML
   */
  default IValidationResult validate(
      @NonNull URI target,
      @NonNull Format asFormat,
      @NonNull IValidationSchemaProvider schemaProvider) throws IOException, SAXException {
    IValidationResult retval;
    switch (asFormat) {
    case JSON:
      retval = new JsonSchemaContentValidator(schemaProvider.getJsonSchema()).validate(target);
      break;
    case XML:
      List<Source> schemaSources = schemaProvider.getXmlSchemas();
      retval = new XmlSchemaContentValidator(schemaSources).validate(target);
      break;
    case YAML:
      JSONObject json = YamlOperations.yamlToJson(YamlOperations.parseYaml(target));
      assert json != null;
      retval = new JsonSchemaContentValidator(schemaProvider.getJsonSchema())
          .validate(json, ObjectUtils.notNull(target));
      break;
    default:
      throw new UnsupportedOperationException("Unsupported format: " + asFormat.name());
    }

    if (retval.isPassing()) {
      IValidationResult constraintValidationResult = validateWithConstraints(target);
      retval = AggregateValidationResult.aggregate(retval, constraintValidationResult);
    }
    return retval;
  }

  /**
   * Load and validate the provided {@code target} using the associated Module
   * module constraints.
   *
   * @param target
   *          the file to load and validate
   * @return the validation results
   * @throws IOException
   *           if an error occurred while loading the document
   */
  default IValidationResult validateWithConstraints(@NonNull URI target) throws IOException {
    IBoundLoader loader = newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(target);

    return validate(nodeItem, loader);
  }

  interface IModuleLoaderStrategy {
    /**
     * Load the bound Metaschema module represented by the provided class.
     * <p>
     * Implementations are allowed to return a cached instance if the module has
     * already been loaded.
     *
     * @param clazz
     *          the Module class
     * @return the module
     * @throws IllegalStateException
     *           if an error occurred while processing the associated module
     *           information
     */
    @NonNull
    IBoundModule loadModule(@NonNull Class<? extends IBoundModule> clazz);

    /**
     * Get the {@link IBoundDefinitionModel} instance associated with the provided
     * Java class.
     * <p>
     * Typically the class will have a {@link MetaschemaAssembly} or
     * {@link MetaschemaField} annotation.
     *
     * @param clazz
     *          the class binding to load
     * @return the associated class binding instance or {@code null} if the class is
     *         not bound
     */
    @Nullable
    IBoundDefinitionModelComplex getBoundDefinitionForClass(@NonNull Class<?> clazz);
  }

  interface IValidationSchemaProvider {
    /**
     * Get a JSON schema to use for content validation.
     *
     * @return the JSON schema
     * @throws IOException
     *           if an error occurred while loading the schema
     */
    @NonNull
    JSONObject getJsonSchema() throws IOException;

    /**
     * Get a XML schema to use for content validation.
     *
     * @return the XML schema sources
     * @throws IOException
     *           if an error occurred while loading the schema
     */
    @NonNull
    List<Source> getXmlSchemas() throws IOException;
  }

  /**
   * Implementations of this interface provide a means by which a bound class can
   * be found that corresponds to an XML element, JSON property, or YAML item
   * name.
   */
  interface IBindingMatcher {
    @SuppressWarnings("PMD.ShortMethodName")
    @NonNull
    static IBindingMatcher of(IBoundDefinitionModelAssembly assembly) {
      if (!assembly.isRoot()) {
        throw new IllegalArgumentException(
            String.format("The provided class '%s' is not a root assembly.", assembly.getBoundClass().getName()));
      }
      return new RootAssemblyBindingMatcher(assembly);
    }

    /**
     * Determine the bound class for the provided XML {@link QName}.
     *
     * @param rootQName
     *          the root XML element's QName
     * @return the bound class for the XML qualified name or {@code null} if not
     *         recognized
     */
    Class<?> getBoundClassForXmlQName(QName rootQName);

    /**
     * Determine the bound class for the provided JSON/YAML property/item name.
     *
     * @param rootName
     *          the JSON/YAML property/item name
     * @return the bound class for the JSON property name or {@code null} if not
     *         recognized
     */
    Class<?> getBoundClassForJsonName(String rootName);
  }
}
