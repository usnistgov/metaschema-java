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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.Ignore;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedProperty;
import gov.nist.secauto.metaschema.binding.model.property.Property;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public abstract class AbstractClassBinding implements ClassBinding {
  // private static final Logger logger = LogManager.getLogger(AbstractClassBinding.class);

  private final BindingContext bindingContext;
  private final Class<?> clazz;
  private final List<Method> beforeDeserializeMethods;
  private final List<Method> afterDeserializeMethods;
  private Map<String, FlagProperty> flagProperties;
  private FlagProperty jsonKeyFlag;

  /**
   * Construct a new class binding for the provided class.
   * 
   * @param clazz
   *          the bound class
   * @param bindingContext
   * @throws BindingException
   *           if an error occurred while processing the class binding
   */
  public AbstractClassBinding(Class<?> clazz, BindingContext bindingContext) {
    Objects.requireNonNull(bindingContext, "bindingContext");
    Objects.requireNonNull(clazz, "clazz");
    this.bindingContext = bindingContext;
    this.clazz = clazz;
    this.beforeDeserializeMethods = ClassIntrospector.getMatchingMethods(clazz, "beforeDeserialize", Object.class);
    this.afterDeserializeMethods = ClassIntrospector.getMatchingMethods(clazz, "afterDeserialize", Object.class);
  }

  /**
   * Initialize the content model of the class.
   * 
   * @throws BindingException
   *           if an error occurred while processing the class binding
   */
  protected void initialize() {

    Map<String, FlagProperty> flags = new LinkedHashMap<>();
    for (Field field : clazz.getDeclaredFields()) {

      boolean handled = false;

      Ignore ignore = field.getAnnotation(Ignore.class);
      if (ignore != null) {
        handled = true;
      }

      if (!handled) {
        Flag flag = field.getAnnotation(Flag.class);
        if (flag != null) {
          FlagProperty binding = new DefaultFlagProperty(this, field, bindingContext);
          flags.put(binding.getJavaPropertyName(), binding);
          handled = true;
        }
      }

      if (!handled) {
        handled = initializeField(field);
      }

      if (!handled) {
        throw new IllegalArgumentException(
            String.format("Class '%s' has unbound field '%s'", getBoundClass().getName(), field.getName()));
      }
    }
    this.flagProperties = flags.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flags);

    FlagProperty jsonKey = null;
    for (FlagProperty flag : getFlagProperties().values()) {

      if (flag.isJsonKey()) {
        jsonKey = flag;
      }

      initializeFlag(flag);
    }
    this.jsonKeyFlag = jsonKey;
  }

  /**
   * Used to delegate flag processing to subclasses.
   * 
   * @param flag
   *          the flag to process
   */
  protected void initializeFlag(FlagProperty flag) {
    // do nothing
  }

  /**
   * Used to delegate field processing to subclasses. Subclasses should not call this method.
   * 
   * @param field
   *          the Java class field to process
   * @return {@code true} if the field was handled, or {@code false} otherwise
   */
  protected boolean initializeField(Field field) {
    return false;
  }

  @Override
  public Class<?> getBoundClass() {
    return clazz;
  }

  @Override
  public BindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public Map<String, FlagProperty> getFlagProperties() {
    return flagProperties;
  }

  @Override
  public FlagProperty getJsonKey() {
    return jsonKeyFlag;
  }

  @Override
  public Map<String, ? extends NamedProperty> getProperties(Predicate<FlagProperty> filter) {
    Map<String, ? extends NamedProperty> retval;
    if (filter == null) {
      retval = getFlagProperties();
    } else {
      retval = getFlagProperties().values().stream().filter(filter)
          .collect(Collectors.toMap(NamedProperty::getJsonPropertyName, Function.identity()));
    }
    return retval;
  }

  @Override
  public Map<String, ? extends Property> getProperties() {
    return getFlagProperties();
  }

  /**
   * Gets a new instance of the bound class.
   * 
   * @param <CLASS>
   *          the type of the bound class
   * @return a Java object for the class
   * @throws BindingException
   *           if the instance cannot be created due to a binding error
   */
  protected <CLASS> CLASS newInstance() throws BindingException {
    Class<?> clazz = getBoundClass();
    CLASS retval;
    try {
      @SuppressWarnings("unchecked")
      Constructor<CLASS> constructor = (Constructor<CLASS>) clazz.getDeclaredConstructor();
      retval = constructor.newInstance();
    } catch (NoSuchMethodException e) {
      String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
      throw new BindingException(msg);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new BindingException(e);
    }
    return retval;
  }

  /**
   * Calls the method named "beforeDeserialize" on each class in the class's hierarchy if the method
   * exists. These methods can be used to set the initial state of a bound class before data is
   * applied during deserialization.
   * 
   * @param objectInstance
   *          the object instance
   * @param parentInstance
   *          the object's parent object instance
   * @throws BindingException
   *           if an error occurs while calling a deserialization method
   */
  protected void callBeforeDeserialize(Object objectInstance, Object parentInstance) throws BindingException {
    if (!beforeDeserializeMethods.isEmpty()) {
      for (Method method : beforeDeserializeMethods) {
        try {
          method.invoke(objectInstance, parentInstance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          throw new BindingException(ex);
        }
      }
    }
  }

  /**
   * Calls the method named "afterDeserialize" on each class in the class's hierarchy if the method
   * exists. These methods can be used to modify the state of a bound class after data is applied
   * during deserialization.
   * 
   * @param objectInstance
   *          the object instance
   * @param parentInstance
   *          the object's parent object instance
   * @throws BindingException
   *           if an error occurs while calling a deserialization method
   */
  protected void callAfterDeserialize(Object objectInstance, Object parentInstance) throws BindingException {
    if (!afterDeserializeMethods.isEmpty()) {
      for (Method method : afterDeserializeMethods) {
        try {
          method.invoke(objectInstance, parentInstance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          throw new BindingException(ex);
        }
      }
    }
  }

  @Override
  public boolean readItem(PropertyCollector collector, Object parentInstance, StartElement start,
      XmlParsingContext context) throws BindingException, IOException, XMLStreamException {
    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    readInternal(parentInstance, instance, start, context);

    callAfterDeserialize(instance, parentInstance);

    collector.add(instance);
    return true;
  }

  protected void readInternal(@SuppressWarnings("unused") Object parentInstance, Object instance, StartElement start,
      XmlParsingContext context) throws IOException, XMLStreamException, BindingException {
    for (FlagProperty flag : getFlagProperties().values()) {
      flag.read(instance, start, context);
    }
    readBody(instance, start, context);

    // TODO: should I check for the END_ELEMENT here?
  }

  protected abstract void readBody(Object instance, StartElement start, XmlParsingContext context)
      throws IOException, XMLStreamException, BindingException;

  @Override
  public void writeItem(Object instance, QName parentName, XmlWritingContext context)
      throws IOException, XMLStreamException {
    writeInternal(instance, parentName, context);
  }

  protected void writeInternal(Object instance, QName parentName, XmlWritingContext context)
      throws IOException, XMLStreamException {
    // write flags
    for (FlagProperty flag : getFlagProperties().values()) {
      flag.write(instance, parentName, context);
    }
    writeBody(instance, parentName, context);
  }

  protected abstract void writeBody(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException;
}
