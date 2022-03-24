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

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.binding.model.annotations.Ignore;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.property.DefaultFlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.common.util.XmlEventUtil;

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
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractClassBinding implements IClassBinding {
  // private static final Logger logger = LogManager.getLogger(AbstractClassBinding.class);

  @NotNull
  private final IBindingContext bindingContext;
  @NotNull
  private final Class<?> clazz;
  private final Method beforeDeserializeMethod;
  private final Method afterDeserializeMethod;
  private Map<@NotNull String, IBoundFlagInstance> flagInstances;
  private IBoundFlagInstance jsonKeyFlag;

  /**
   * Construct a new class binding for the provided class.
   * 
   * @param clazz
   *          the bound class
   * @param bindingContext
   *          the class binding context for which this class is participating
   */
  public AbstractClassBinding(@NotNull Class<?> clazz, @NotNull IBindingContext bindingContext) {
    this.bindingContext = ObjectUtils.requireNonNull(bindingContext, "bindingContext");
    this.clazz = ObjectUtils.requireNonNull(clazz, "clazz");
    this.beforeDeserializeMethod = ClassIntrospector.getMatchingMethod(clazz, "beforeDeserialize", Object.class);
    this.afterDeserializeMethod = ClassIntrospector.getMatchingMethod(clazz, "afterDeserialize", Object.class);
  }

  @Override
  public boolean isInline() {
    return getBoundClass().getEnclosingClass() != null;
  }

  @Override
  public Class<?> getBoundClass() {
    return clazz;
  }

  @Override
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public String getName() {
    // there is not a provided name, but we need to have one. This will always be provided on the
    // instance side as a use name
    return ObjectUtils.notNull(getBoundClass().getName());
  }

  @Override
  public String getUseName() { // NOPMD
    // a use name is never provided
    return null;
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s IClassBinding(%s): %s", getModelType().name().toLowerCase(Locale.ROOT), getName(),
        getBoundClass().getName());
  }

  @Override
  public String getFormalName() { // NOPMD - remove after implementation
    // TODO: implement
    return null;
  }

  @Override
  public MarkupLine getDescription() { // NOPMD - remove after implementation
    // TODO: implement
    return null;
  }

  @Override
  public @NotNull ModuleScopeEnum getModuleScope() {
    // TODO: is this the right value?
    return ModuleScopeEnum.INHERITED;
  }

  @Override
  public IMetaschema getContainingMetaschema() { // NOPMD - remove after implementation
    // TODO: implement
    return null;
  }

  @Override
  public MarkupMultiline getRemarks() { // NOPMD - remove after implementation
    // TODO: implement
    return null;
  }

  /**
   * Collect all fields that are flag instances on this class.
   * 
   * @param clazz
   *          the class
   * @return an immutable collection of flag instances
   */
  @NotNull
  protected Collection<@NotNull Field> getFlagInstanceFields(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();

    List<@NotNull Field> retval = new LinkedList<>();

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null) {
      // get flags from superclass
      retval.addAll(getFlagInstanceFields(superClass));
    }

    for (Field field : fields) {
      if (!field.isAnnotationPresent(BoundFlag.class)) {
        // skip non-flag fields
        continue;
      }

      if (field.isAnnotationPresent(Ignore.class)) {
        // skip this field, since it is ignored
        continue;
      }

      retval.add(field);
    }
    return ObjectUtils.notNull(Collections.unmodifiableCollection(retval));
  }

  /**
   * Initialize the flag instances for this class.
   * 
   * @return the initialized flag instances
   */
  @NotNull
  protected Map<@NotNull String, IBoundFlagInstance> initalizeFlagInstances() {
    synchronized (this) {
      if (this.flagInstances == null) {
        Map<@NotNull String, IBoundFlagInstance> flags = new LinkedHashMap<>(); // NOPMD - intentional use
        for (Field field : getFlagInstanceFields(clazz)) {

          if (field.isAnnotationPresent(BoundFlag.class)) {
            IBoundFlagInstance flagBinding
                = new DefaultFlagProperty(field, this, bindingContext); // NOPMD - intentional
            initializeFlagInstance(flagBinding);
            flags.put(flagBinding.getEffectiveName(), flagBinding);
          }
        }
        this.flagInstances = flags.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(flags);
      }
    }
    return ObjectUtils.notNull(this.flagInstances);
  }

  /**
   * Used to delegate flag instance initialization to subclasses.
   * 
   * @param instance
   *          the flag instance to process
   */
  protected void initializeFlagInstance(IBoundFlagInstance instance) {
    Field field = instance.getField();
    if (field.isAnnotationPresent(JsonKey.class)) {
      this.jsonKeyFlag = instance;
    }
  }

  @Override
  public Map<@NotNull String, IBoundFlagInstance> getFlagInstanceMap() {
    // check that the flag instances are lazy loaded
    return initalizeFlagInstances();
  }

  @Override
  public boolean hasJsonKey() {
    return getJsonKeyFlagInstance() != null;
  }

  @Override
  public IBoundFlagInstance getJsonKeyFlagInstance() {
    initalizeFlagInstances();
    return jsonKeyFlag;
  }

  @Override
  public Map<@NotNull String, ? extends IBoundNamedInstance> getNamedInstances(Predicate<IBoundFlagInstance> filter) {
    Map<@NotNull String, ? extends IBoundFlagInstance> retval;
    if (filter == null) {
      retval = getFlagInstanceMap();
    } else {
      retval = ObjectUtils.notNull(getFlagInstances().stream().filter(filter)
          .collect(Collectors.toMap(IFlagInstance::getJsonName, Function.identity())));
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
  @NotNull
  protected <CLASS> CLASS newInstance() throws BindingException {
    Class<?> clazz = getBoundClass();
    try {
      @SuppressWarnings("unchecked")
      Constructor<CLASS> constructor = (Constructor<CLASS>) clazz.getDeclaredConstructor();
      return ObjectUtils.notNull(constructor.newInstance());
    } catch (NoSuchMethodException ex) {
      String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
      throw new BindingException(msg, ex);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException ex) {
      throw new BindingException(ex);
    }
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
    if (beforeDeserializeMethod != null) {
      try {
        beforeDeserializeMethod.invoke(objectInstance, parentInstance);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
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
    if (afterDeserializeMethod != null) {
      try {
        afterDeserializeMethod.invoke(objectInstance, parentInstance);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
        throw new BindingException(ex);
      }
    }
  }

  @Override
  public Object readItem(Object parentInstance, StartElement start,
      IXmlParsingContext context) throws IOException, XMLStreamException {

    try {
      Object instance = newInstance();
      callBeforeDeserialize(instance, parentInstance);
      readInternal(instance, start, context);
      callAfterDeserialize(instance, parentInstance);
      return instance;
    } catch (BindingException ex) {
      throw new IOException(ex);
    }
  }

  protected void readInternal(@NotNull Object instance, @NotNull StartElement start,
      @NotNull IXmlParsingContext context) throws IOException, XMLStreamException {
    for (IBoundFlagInstance flag : getFlagInstances()) {
      flag.read(instance, start, context);
    }
    readBody(instance, start, context);

    XmlEventUtil.assertNext(context.getReader(), XMLEvent.END_ELEMENT, start.getName());
  }

  protected abstract void readBody(@NotNull Object instance, @NotNull StartElement start,
      @NotNull IXmlParsingContext context)
      throws IOException, XMLStreamException;

  @Override
  public void writeItem(Object instance, QName parentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    writeInternal(instance, parentName, context);
  }

  protected void writeInternal(@NotNull Object instance, @NotNull QName parentName, @NotNull IXmlWritingContext context)
      throws IOException, XMLStreamException {
    // write flags
    for (IBoundFlagInstance flag : getFlagInstances()) {
      flag.write(instance, parentName, context);
    }
    writeBody(instance, parentName, context);
  }

  protected abstract void writeBody(@NotNull Object instance, @NotNull QName parentName,
      @NotNull IXmlWritingContext context)
      throws XMLStreamException, IOException;

  @Override
  public Object copyBoundObject(Object item, Object parentInstance) throws BindingException {
    Object instance = newInstance();

    callBeforeDeserialize(instance, parentInstance);

    copyBoundObjectInternal(item, instance);

    callAfterDeserialize(instance, parentInstance);

    return instance;
  }

  protected void copyBoundObjectInternal(@NotNull Object fromInstance, @NotNull Object toInstance)
      throws BindingException {
    for (IBoundFlagInstance property : getFlagInstances()) {
      property.copyBoundObject(fromInstance, toInstance);
    }
  }

}
