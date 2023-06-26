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

package gov.nist.secauto.metaschema.binding.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.AssemblyConstraints;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundField;
import gov.nist.secauto.metaschema.binding.model.annotations.Ignore;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.ValueConstraints;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.constraint.IAssemblyConstraintSupport;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.InternalModelSource;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.CustomCollectors;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public class DefaultAssemblyClassBinding // NOPMD - ok
    extends AbstractClassBinding
    implements IAssemblyClassBinding, IAssemblyConstraintFeature {
  private static final Logger LOGGER = LogManager.getLogger(DefaultAssemblyClassBinding.class);

  private final MetaschemaAssembly metaschemaAssembly;
  private Map<String, IBoundNamedModelInstance> modelInstances;
  private final QName xmlRootQName;
  private final Lazy<IAssemblyConstraintSupport> constraints;

  /**
   * Create a new {@link IClassBinding} for a Java bean annotated with the {@link BoundAssembly}
   * annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the Metaschema binding environment context
   * @return the Metaschema assembly binding for the class
   */
  @NonNull
  public static DefaultAssemblyClassBinding createInstance(@NonNull Class<?> clazz,
      @NonNull IBindingContext bindingContext) {
    return new DefaultAssemblyClassBinding(clazz, bindingContext);
  }

  /**
   * Construct a new {@link IClassBinding} for a Java bean annotated with the {@link BoundAssembly}
   * annotation.
   *
   * @param clazz
   *          the Java bean class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  protected DefaultAssemblyClassBinding(@NonNull Class<?> clazz, @NonNull IBindingContext bindingContext) {
    super(clazz, bindingContext);
    Objects.requireNonNull(clazz, "clazz");
    if (!clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      throw new IllegalArgumentException(
          String.format("Class '%s' is missing the '%s' annotation.",
              clazz.getName(),
              MetaschemaAssembly.class.getName())); // NOPMD
    }
    this.metaschemaAssembly = ObjectUtils.notNull(clazz.getAnnotation(MetaschemaAssembly.class));
    String namespace = ObjectUtils.notNull(ModelUtil.resolveNamespace(this.metaschemaAssembly.rootNamespace(), this));
    String localName = ModelUtil.resolveLocalName(this.metaschemaAssembly.rootName(), null);

    this.xmlRootQName = localName == null ? null : new QName(namespace, localName);

    this.constraints = Lazy.lazy(() -> new AssemblyConstraintSupport(
        clazz.getAnnotation(ValueConstraints.class),
        clazz.getAnnotation(AssemblyConstraints.class),
        InternalModelSource.instance()));
  }

  /**
   * Get the {@link MetaschemaAssembly} annotation associated with this class. This annotation
   * provides information used by this class binding to control binding behavior.
   *
   * @return the annotation
   */
  public MetaschemaAssembly getMetaschemaAssemblyAnnotation() {
    return metaschemaAssembly;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveToString(getMetaschemaAssemblyAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getMetaschemaAssemblyAnnotation().description());
  }

  @Override
  public @Nullable MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getMetaschemaAssemblyAnnotation().description());
  }

  @Override
  public String getName() {
    return getMetaschemaAssemblyAnnotation().name();
  }

  @Override
  public boolean isInline() {
    return false;
  }

  @Override
  public IBoundAssemblyInstance getInlineInstance() {
    return null;
  }

  @Override
  public boolean isRoot() {
    // Overriding this is more efficient, since the root name is derived from the XML QName
    return getRootXmlQName() != null;
  }

  @Override
  public String getRootName() {
    QName qname = getRootXmlQName();
    return qname == null ? null : qname.getLocalPart();
  }

  @Override
  public QName getRootXmlQName() {
    // Overriding this is more efficient, since it is already built
    return xmlRootQName;
  }

  protected Stream<IBoundNamedModelInstance> getModelInstanceFieldStream(Class<?> clazz) {
    Stream<IBoundNamedModelInstance> superInstances;
    Class<?> superClass = clazz.getSuperclass();
    if (superClass == null) {
      superInstances = Stream.empty();
    } else {
      // get instances from superclass
      superInstances = getModelInstanceFieldStream(superClass);
    }

    return Stream.concat(superInstances, Arrays.stream(clazz.getDeclaredFields())
        // skip this field, since it is ignored
        .filter(field -> !field.isAnnotationPresent(Ignore.class))
        // skip fields that aren't a Metaschema field or assembly instance
        .filter(field -> field.isAnnotationPresent(BoundField.class) || field.isAnnotationPresent(BoundAssembly.class))
        .map(field -> {
          assert field != null;
          return newModelInstance(clazz, field);
        })
        .filter(Objects::nonNull)
        .map(ObjectUtils::notNull));
  }

  protected IBoundNamedModelInstance newModelInstance(@NonNull Class<?> clazz, @NonNull java.lang.reflect.Field field) {
    IBoundNamedModelInstance retval;
    if (field.isAnnotationPresent(BoundAssembly.class)
        && getBindingContext().getClassBinding(IBoundNamedModelInstance.getItemType(field)) != null) {
      retval = IBoundAssemblyInstance.newInstance(field, this);
    } else if (field.isAnnotationPresent(BoundField.class)) {
      retval = IBoundFieldInstance.newInstance(field, this);
    } else {
      throw new IllegalStateException(
          String.format("The field '%s' on class '%s' is not bound", field.getName(), clazz.getName()));
    }
    // TODO: handle choice
    return retval;
  }

  /**
   * Initialize the flag instances for this class.
   */
  protected void initalizeModelInstances() {
    synchronized (this) {
      if (this.modelInstances == null) {
        this.modelInstances = getModelInstanceFieldStream(getBoundClass())
            .collect(Collectors.toMap(instance -> instance.getEffectiveName(), Function.identity(),
                CustomCollectors.useLastMapper(),
                LinkedHashMap::new));
      }
    }
  }

  @Override
  public Collection<? extends IBoundNamedModelInstance> getModelInstances() {
    return getNamedModelInstances();
  }

  @Override
  public IBoundNamedModelInstance getModelInstanceByName(String name) {
    return getNamedModelInstanceMap().get(name);
  }

  @SuppressWarnings("null")
  @NonNull
  private Map<String, ? extends IBoundNamedModelInstance> getNamedModelInstanceMap() {
    initalizeModelInstances();
    return modelInstances;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IBoundNamedModelInstance> getNamedModelInstances() {
    return getNamedModelInstanceMap().values();
  }

  @Override
  public Map<String, ? extends IBoundNamedInstance>
      getNamedInstances(Predicate<IBoundFlagInstance> flagFilter) {
    return ObjectUtils.notNull(Stream.concat(
        super.getNamedInstances(flagFilter).values().stream()
            .map(ObjectUtils::notNull),
        getNamedModelInstances().stream())
        .collect(
            Collectors.toMap(instance -> instance.getJsonName(), Function.identity(), CustomCollectors.useLastMapper(),
                LinkedHashMap::new)));
  }

  @NonNull
  private Map<String, ? extends IBoundFieldInstance> getFieldInstanceMap() {
    return ObjectUtils.notNull(getNamedModelInstances().stream()
        .filter(instance -> instance instanceof IBoundFieldInstance)
        .map(instance -> (IBoundFieldInstance) instance)
        .map(ObjectUtils::notNull)
        .collect(Collectors.toMap(IBoundFieldInstance::getEffectiveName, Function.identity(),
            CustomCollectors.useLastMapper(),
            LinkedHashMap::new)));
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends IBoundFieldInstance> getFieldInstances() {
    return getFieldInstanceMap().values();
  }

  @Override
  public IBoundFieldInstance getFieldInstanceByName(String name) {
    return getFieldInstanceMap().get(name);
  }

  @NonNull
  private Map<String, ? extends IBoundAssemblyInstance> getAssemblyInstanceMap() {
    return ObjectUtils.notNull(getNamedModelInstances().stream()
        .filter(instance -> instance instanceof IBoundAssemblyInstance)
        .map(instance -> (IBoundAssemblyInstance) instance)
        .map(ObjectUtils::notNull)
        .collect(Collectors.toMap(IBoundAssemblyInstance::getEffectiveName, Function.identity(),
            CustomCollectors.useLastMapper(),
            LinkedHashMap::new)));
  }

  @SuppressWarnings("null")
  @Override
  public @NonNull Collection<? extends IBoundAssemblyInstance> getAssemblyInstances() {
    return getAssemblyInstanceMap().values();
  }

  @Override
  public IBoundAssemblyInstance getAssemblyInstanceByName(String name) {
    return getAssemblyInstanceMap().get(name);
  }

  @Override
  public List<? extends IChoiceInstance> getChoiceInstances() {
    // choices are not exposed by this API
    return CollectionUtil.emptyList();
  }

  @Override
  public IAssemblyConstraintSupport getConstraintSupport() {
    return constraints.get();
  }

  @Override
  public Object readObject(IJsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional

    JsonUtil.assertAndAdvance(parser, JsonToken.START_OBJECT);

    try {
      Object instance = newInstance();

      readInternal(instance, null, context);

      // advance past the end object
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
      return instance;
    } catch (BindingException ex) {
      throw new IOException(String.format("Failed to parse JSON object for '%s'", getBoundClass().getName()), ex);
    }
  }

  @Override
  protected void readBody(Object instance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    Set<IBoundNamedModelInstance> unhandledProperties = new HashSet<>();
    for (IBoundNamedModelInstance modelProperty : getModelInstances()) {
      if (!modelProperty.read(instance, start, context)) {
        unhandledProperties.add(modelProperty);
      }
    }

    // process all properties that did not get a value
    for (IBoundNamedModelInstance property : unhandledProperties) {
      // use the default value of the collector
      property.setValue(instance, property.newPropertyCollector().getValue());
    }
  }

  @Override
  public List<Object> readItem(Object parentInstance, boolean requiresJsonKey, IJsonParsingContext context)
      throws IOException {

    JsonUtil.assertCurrent(context.getReader(), JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    try {
      Object instance = newInstance();

      readInternal(instance, parentInstance, context);

      return CollectionUtil.singletonList(instance);
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Parses the JSON field contents related to the bound assembly.
   * <p>
   * This method expects the parser's current token to be at the first field name to parse.
   * <p>
   * After parsing the current token will be the token at the end object immediately after all the
   * fields and values.
   *
   * @param instance
   *          the bound object to read data into
   * @param parentInstance
   *          the parent object used for deserialization callbacks
   * @param context
   *          the JSON parser
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  protected void readInternal( // NOPMD - ok
      @NonNull Object instance,
      @Nullable Object parentInstance,
      @NonNull IJsonParsingContext context) throws IOException {
    JsonParser parser = context.getReader(); // NOPMD - intentional

    JsonUtil.assertCurrent(parser, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);

    try {
      callBeforeDeserialize(instance, parentInstance);
    } catch (BindingException ex) {
      throw new IOException("an error occured calling the beforeDeserialize() method", ex);
    }

    IBoundFlagInstance jsonKey = getJsonKeyFlagInstance();
    Map<String, ? extends IBoundNamedInstance> properties;
    if (jsonKey == null) {
      properties = getNamedInstances(null);
    } else {
      properties = getNamedInstances((flag) -> {
        return !jsonKey.equals(flag);
      });

      // if there is a json key, the first field will be the key
      String key = ObjectUtils.notNull(parser.getCurrentName());

      Object value = jsonKey.readValueFromString(key);
      jsonKey.setValue(instance, value.toString());

      // advance past the FIELD_NAME
      // next the value will be a start object
      JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
      //
      // JsonUtil.assertAndAdvance(jsonParser, JsonToken.START_OBJECT);
    }

    Set<String> handledProperties = new HashSet<>();
    while (!JsonToken.END_OBJECT.equals(parser.currentToken())) {
      String propertyName = parser.getCurrentName();
      IBoundNamedInstance property = properties.get(propertyName);

      boolean handled = false;
      if (property != null) {
        handled = property.read(instance, context);
      }

      if (handled) {
        handledProperties.add(propertyName);
      } else {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Unrecognized property named '{}' at '{}'", propertyName,
              JsonUtil.toString(parser.getCurrentLocation()));
        }
        JsonUtil.assertAndAdvance(parser, JsonToken.FIELD_NAME);
        JsonUtil.skipNextValue(parser);
      }
    }

    // set undefined properties
    // TODO: re-implement this by removing the parsed properties from the properties map to speed up
    for (Map.Entry<String, ? extends IBoundNamedInstance> entry : properties.entrySet()) {
      if (!handledProperties.contains(entry.getKey())) {
        // use the default value of the collector
        IBoundNamedInstance property = ObjectUtils.notNull(entry.getValue());
        property.setValue(instance, property.newPropertyCollector().getValue());
      }
    }

    if (jsonKey != null) {
      // read the END_OBJECT for the JSON key value
      JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
    }

    try {
      callAfterDeserialize(instance, parentInstance);
    } catch (BindingException ex) {
      throw new IOException("an error occured calling the afterDeserialize() method", ex);
    }

    JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
  }

  /**
   * Serializes the provided instance in JSON.
   *
   * @param instance
   *          the instance to serialize
   * @param writeObjectWrapper
   *          {@code true} if the start and end object should be written, or {@code false} otherwise
   * @param context
   *          the JSON writing context used to generate output
   * @throws IOException
   *           if an error occurs while writing to the output context
   * @throws NullPointerException
   *           if there is a JSON key configured and the key property's value is {@code null}
   */
  protected void writeInternal(@NonNull Object instance, boolean writeObjectWrapper,
      @NonNull IJsonWritingContext context)
      throws IOException {
    JsonGenerator writer = context.getWriter(); // NOPMD - intentional

    if (writeObjectWrapper) {
      writer.writeStartObject();
    }

    IBoundFlagInstance jsonKey = getJsonKeyFlagInstance();
    Map<String, ? extends IBoundNamedInstance> properties;
    if (jsonKey == null) {
      properties = getNamedInstances(null);
    } else {
      properties = getNamedInstances((flag) -> {
        return !jsonKey.equals(flag);
      });

      // if there is a json key, the first field will be the key
      Object flagValue = jsonKey.getValue(instance);
      String key = jsonKey.getValueAsString(flagValue);
      if (key == null) {
        throw new IOException(new NullPointerException("Null key value"));
      }
      writer.writeFieldName(key);

      // next the value will be a start object
      writer.writeStartObject();
    }

    for (IBoundNamedInstance property : properties.values()) {
      ObjectUtils.notNull(property).write(instance, context);
    }

    if (jsonKey != null) {
      // write the END_OBJECT for the JSON key value
      writer.writeEndObject();
    }

    if (writeObjectWrapper) {
      writer.writeEndObject();
    }
  }

  @Override
  protected void writeBody(Object instance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException {
    for (IBoundNamedModelInstance modelProperty : getModelInstances()) {
      modelProperty.write(instance, parentName, context);
    }
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper,
      IJsonWritingContext context)
      throws IOException {
    for (Object item : items) {
      assert item != null;
      writeInternal(item, writeObjectWrapper, context);
    }
  }

  @Override
  protected void copyBoundObjectInternal(@NonNull Object fromInstance, @NonNull Object toInstance)
      throws BindingException {
    super.copyBoundObjectInternal(fromInstance, toInstance);

    for (IBoundNamedModelInstance property : getModelInstances()) {
      property.copyBoundObject(fromInstance, toInstance);
    }
  }

  @Override
  protected Class<? extends IMetaschema> getMetaschemaClass() {
    return getMetaschemaAssemblyAnnotation().metaschema();
  }
}
