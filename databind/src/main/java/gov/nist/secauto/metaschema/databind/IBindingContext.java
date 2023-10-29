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
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IDefinition;
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
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.RootAssemblyBindingMatcher;

import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigInteger;
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
    return MetaschemaBindingContext.instance();
  }

  /**
   * Register a matcher used to identify a bound class by the content's root name.
   *
   * @param matcher
   *          the matcher implementation
   * @return this instance
   */
  @NonNull
  IBindingContext registerBindingMatcher(@NonNull IBindingMatcher matcher);

  /**
   * Register a class binding strategy for a given bound class.
   *
   * @param <T>
   *          the Java type of the bound definition
   * @param strategy
   *          the strategy to register
   * @param boundClass
   *          the class to be bound to the strategy
   * @return the old registered strategy or {@code null} if no stategy was
   *         registered for the bound class
   */
  @Nullable
  <T extends IDefinition> IClassBindingStrategy<?> registerClassBindingStrategy(
      @NonNull IClassBindingStrategy<T> strategy);

  /**
   * Get the {@link IClassBindingStrategy} instance associated with the provided
   * Java class.
   * <p>
   * Typically the class will have a {@link MetaschemaAssembly} or
   * {@link MetaschemaField} annotation.
   *
   * @param clazz
   *          the class binding to load
   * @return the associated class binding instance
   * @throws IllegalArgumentException
   *           if the class was not bound
   */
  IClassBindingStrategy<?> getClassBindingStrategy(@NonNull Class<?> clazz);

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
  IModule loadModule(@NonNull Class<? extends IModule> clazz);

  /**
   * Determine the bound class for the provided XML {@link QName}.
   *
   * @param rootQName
   *          the root XML element's QName
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  @Nullable
  IClassBindingStrategy<IAssemblyDefinition> getBoundClassForXmlQName(@NonNull QName rootQName);

  /**
   * Determine the bound class for the provided JSON/YAML property/item name using
   * any registered matchers.
   *
   * @param rootName
   *          the JSON/YAML property/item name
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  @Nullable
  IClassBindingStrategy<IAssemblyDefinition> getBoundClassForJsonName(@NonNull String rootName);

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
  IBindingContext registerModule(@NonNull IModule module, @NonNull Path compilePath) throws IOException;

  /**
   * Gets a data {@link ISerializer} which can be used to write Java instance data
   * for the provided class in the requested format.
   * <p>
   * The provided class must be a registered bound Java class.
   *
   * @param <CLASS>
   *          the Java type this deserializer can write data from
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @return the serializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are
   *           {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getClassBindingStrategy(Class)
   * @see #registerClassBindingStrategy(IClassBindingStrategy)
   */
  @NonNull
  <CLASS> ISerializer<CLASS> newSerializer(@NonNull Format format, @NonNull Class<CLASS> clazz);

  /**
   * Gets a data {@link IDeserializer} which can be used to read Java instance
   * data for the provided class from the requested format.
   * <p>
   * The provided class must be a registered bound Java class.
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
   * @see #getClassBindingStrategy(Class)
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
  <CLASS> CLASS copyBoundObject(@NonNull CLASS other, Object parentInstance) throws BindingException;

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

    DynamicContext context = StaticContext.instance().dynamicContext();
    context.setDocumentLoader(loader);

    return new DefaultConstraintValidator(handler);
  }

  /**
   * Perform constraint validation on the provided bound object represented as an
   * {@link INodeItem}.
   *
   * @param nodeItem
   *          the node item to validate
   * @return the validation result
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Module assembly or field
   */
  default IValidationResult validate(@NonNull INodeItem nodeItem) {
    FindingCollectingConstraintValidationHandler handler = new FindingCollectingConstraintValidationHandler();
    IConstraintValidator validator = newValidator(handler);
    DynamicContext dynamicContext = StaticContext.instance().dynamicContext();
    dynamicContext.setDocumentLoader(newBoundLoader());
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
      @NonNull Path target,
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
          .validate(json, ObjectUtils.notNull(target.toUri()));
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
  default IValidationResult validateWithConstraints(@NonNull Path target) throws IOException {
    IBoundLoader loader = newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    DynamicContext dynamicContext = StaticContext.instance().dynamicContext();
    dynamicContext.setDocumentLoader(loader);
    IDocumentNodeItem nodeItem = loader.loadAsNodeItem(target);

    return validate(nodeItem);
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
    IModule loadModule(@NonNull Class<? extends IModule> clazz);

    /**
     * Get the {@link IClassBindingStrategy} instance associated with the provided
     * Java class.
     * <p>
     * Typically the class will have a {@link MetaschemaAssembly} or
     * {@link MetaschemaField} annotation.
     *
     * @param clazz
     *          the class binding to load
     * @return the associated class binding instance
     * @throws IllegalArgumentException
     *           if the class was not bound
     */
    @NonNull
    IClassBindingStrategy<?> getClassBindingStrategy(@NonNull Class<?> clazz);
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
    /**
     * Construct a new root matcher from the provided class binding strategy.
     *
     * @param strategy
     *          an assembly class binding strategy for an assembly that is a root.
     * @return the new matcher
     * @see IAssemblyDefinition#isRoot()
     */
    @NonNull
    static IBindingMatcher rootMatcher(@NonNull IClassBindingStrategy<IAssemblyDefinition> strategy) {
      return new RootAssemblyBindingMatcher(strategy);
    }

    /**
     * Determine the bound class for the provided XML {@link QName}.
     *
     * @param rootQName
     *          the root XML element's QName
     * @return the class binding strategy for the class or {@code null} if not
     *         recognized
     */
    IClassBindingStrategy<IAssemblyDefinition> getBoundClassForXmlQName(QName rootQName);

    /**
     * Determine the bound class for the provided JSON/YAML property/item name.
     *
     * @param rootName
     *          the JSON/YAML property/item name
     * @return the class binding strategy for the class or {@code null} if not
     *         recognized
     */
    IClassBindingStrategy<IAssemblyDefinition> getBoundClassForJsonName(String rootName);
  }
}
