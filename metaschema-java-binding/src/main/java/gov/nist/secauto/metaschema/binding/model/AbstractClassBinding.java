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
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedProperty;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
  private Map<String, FlagProperty> flagInstances;
  private FlagProperty jsonKeyFlag;

  /**
   * Construct a new class binding for the provided class.
   * 
   * @param clazz
   *          the bound class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  public AbstractClassBinding(Class<?> clazz, BindingContext bindingContext) {
    Objects.requireNonNull(bindingContext, "bindingContext");
    Objects.requireNonNull(clazz, "clazz");
    this.bindingContext = bindingContext;
    this.clazz = clazz;
    this.beforeDeserializeMethods = ClassIntrospector.getMatchingMethods(clazz, "beforeDeserialize", Object.class);
    this.afterDeserializeMethods = ClassIntrospector.getMatchingMethods(clazz, "afterDeserialize", Object.class);
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
  public String getName() {
    // there is not a provided name, but we need to have one. This will always be provided on the
    // instance side as a use name
    return getBoundClass().getName();
  }

  @Override
  public String getUseName() {
    // a use name is never provided
    return null;
  }

  @Override
  public String getXmlNamespace() {
    // a namespace is never provided. This will always be defined on the instance side
    return null;
  }

  @Override
  public String toCoordinates() {
    return String.format("%s ClassBinding(%s): %s", getModelType().name().toLowerCase(), getName(),
        getBoundClass().getName());
  }

  @Override
  public String getFormalName() {
    // TODO: implement
    return null;
  }

  @Override
  public MarkupLine getDescription() {
    // TODO: implement
    return null;
  }

  @Override
  public @NotNull ModuleScopeEnum getModuleScope() {
    // TODO: is this the right value?
    return ModuleScopeEnum.INHERITED;
  }

  @Override
  public boolean isGlobal() {
    return getBoundClass().getEnclosingClass() == null;
  }

  @Override
  public IMetaschema getContainingMetaschema() {
    // TODO: implement
    return null;
  }

  @Override
  public MarkupMultiline getRemarks() {
    return null;
  }

  /**
   * Collect all fields that are flag instances on this class.
   * 
   * @param clazz
   *          the class
   * @return an immutable collection of flag instances
   */
  protected Collection<Field> getFlagInstanceFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();

    List<Field> retval = new LinkedList<>();

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      // get flags from superclass
      retval.addAll(getFlagInstanceFields(superClass));
    }

    for (Field field : fields) {
      if (!field.isAnnotationPresent(Flag.class)) {
        // skip non-flag fields
        continue;
      }

      if (field.isAnnotationPresent(Ignore.class)) {
        // skip this field, since it is ignored
        continue;
      }

      retval.add(field);
    }
    return Collections.unmodifiableCollection(retval);
  }

  /**
   * Initialize the flag instances for this class.
   */
  protected synchronized void initalizeFlagInstances() {
    if (this.flagInstances == null) {
      Map<String, FlagProperty> flags = new LinkedHashMap<>();
      for (Field field : getFlagInstanceFields(clazz)) {

        Flag flag = field.getAnnotation(Flag.class);
        if (flag != null) {
          FlagProperty flagBinding = new DefaultFlagProperty(this, field, bindingContext);
          initializeFlagInstance(flagBinding);
          flags.put(flagBinding.getEffectiveName(), flagBinding);
        }
      }
      this.flagInstances = flags.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flags);
    }
  }

  /**
   * Used to delegate flag instance initialization to subclasses.
   * 
   * @param instance
   *          the flag instance to process
   */
  protected void initializeFlagInstance(FlagProperty instance) {
    Field field = instance.getField();
    if (field.isAnnotationPresent(JsonKey.class)) {
      this.jsonKeyFlag = instance;
    }
  }

  @Override
  public synchronized Map<String, FlagProperty> getFlagInstanceMap() {
    // check that the flag instances are lazy loaded
    initalizeFlagInstances();
    return flagInstances;
  }

  @Override
  public boolean hasJsonKey() {
    return getJsonKeyFlagInstance() != null;
  }

  @Override
  public FlagProperty getJsonKeyFlagInstance() {
    initalizeFlagInstances();
    return jsonKeyFlag;
  }

  @Override
  public Map<String, ? extends NamedProperty> getNamedInstances(Predicate<FlagProperty> filter) {
    Map<String, ? extends NamedProperty> retval;
    if (filter == null) {
      retval = getFlagInstanceMap();
    } else {
      retval = getFlagInstances().stream().filter(filter)
          .collect(Collectors.toMap(IFlagInstance::getJsonName, Function.identity()));
    }
    return retval;
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
  public Object readItem(Object parentInstance, StartElement start,
      XmlParsingContext context) throws BindingException, IOException, XMLStreamException {
    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    readInternal(parentInstance, instance, start, context);

    callAfterDeserialize(instance, parentInstance);

    return instance;
  }

  protected void readInternal(@SuppressWarnings("unused") Object parentInstance, Object instance, StartElement start,
      XmlParsingContext context) throws IOException, XMLStreamException, BindingException {
    for (FlagProperty flag : getFlagInstances()) {
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
    for (FlagProperty flag : getFlagInstances()) {
      flag.write(instance, parentName, context);
    }
    writeBody(instance, parentName, context);
  }

  protected abstract void writeBody(Object instance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException;
}
