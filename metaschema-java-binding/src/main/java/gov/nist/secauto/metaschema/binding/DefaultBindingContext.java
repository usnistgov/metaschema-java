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

package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.IDeserializer;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;

/**
 * The implementation of a {@link IBindingContext} provided by this library.
 * <p>
 * This implementation caches Metaschema information, which can dramatically improve read and write
 * performance at the cost of some memory use. Thus, using the same instance of this class across
 * multiple I/O operations will improve overall read and write performance when processing the same
 * types of data.
 * <p>
 * Serializers and deserializers provided by this class using the
 * {@link #newSerializer(Format, Class)} and {@link #newDeserializer(Format, Class)} methods will
 * <p>
 * This class is synchronized and is thread-safe.
 */
public class DefaultBindingContext implements IBindingContext {
  private static DefaultBindingContext instance;

  public static synchronized DefaultBindingContext instance() {
    if (instance == null) {
      instance = new DefaultBindingContext();
    }
    return instance;
  }

  private final Map<Class<?>, IClassBinding> classBindingsByClass = new HashMap<>();
  private final Map<Class<? extends IJavaTypeAdapter<?>>, IJavaTypeAdapter<?>> javaTypeAdapterMap = new HashMap<>();
  private final List<IBindingMatcher> bindingMatchers = new LinkedList<>();

  /**
   * Construct a new binding context.
   */
  protected DefaultBindingContext() {
  }

  @Override
  public synchronized IClassBinding getClassBinding(@NotNull Class<?> clazz) throws IllegalArgumentException {
    IClassBinding retval = classBindingsByClass.get(clazz);
    if (retval == null) {
      if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
        retval = DefaultAssemblyClassBinding.createInstance(clazz, this);
      } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
        retval = DefaultFieldClassBinding.createInstance(clazz, this);
      } else {
        throw new IllegalArgumentException(String.format(
            "Class '%s' does not represent a Metaschema definition" + " since it is missing a '%s' or '%s' annotation.",
            clazz.getName(), MetaschemaAssembly.class.getName(), MetaschemaField.class.getName()));
      }
      if (retval != null) {
        classBindingsByClass.put(clazz, retval);
      }
    }
    return retval;
  }

  @Override
  public synchronized <TYPE extends IJavaTypeAdapter<?>> IJavaTypeAdapter<TYPE>
      getJavaTypeAdapterInstance(@NotNull Class<TYPE> clazz) {
    @SuppressWarnings("unchecked")
    IJavaTypeAdapter<TYPE> retval
        = (IJavaTypeAdapter<TYPE>) javaTypeAdapterMap.get(clazz);
    if (retval == null) {
      Constructor<TYPE> constructor;
      try {
        constructor = clazz.getDeclaredConstructor();
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
      }

      try {
        @SuppressWarnings("unchecked")
        IJavaTypeAdapter<TYPE> instance
            = (IJavaTypeAdapter<TYPE>) constructor.newInstance();
        retval = instance;
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
      javaTypeAdapterMap.put(clazz, retval);
    }
    return retval;
  }

  /**
   * {@inheritDoc}
   * <p>
   * A serializer returned by this method is thread-safe.
   */
  @Override
  public <CLASS> ISerializer<CLASS> newSerializer(@NotNull Format format, @NotNull Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);

    ISerializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonSerializer<CLASS>(this, classBinding);
      break;
    case XML:
      retval = new DefaultXmlSerializer<CLASS>(this, classBinding);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<CLASS>(this, classBinding);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  /**
   * {@inheritDoc}
   * <p>
   * A deserializer returned by this method is thread-safe.
   */
  @Override
  public <CLASS> IDeserializer<CLASS> newDeserializer(@NotNull Format format, @NotNull Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);

    IDeserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonDeserializer<CLASS>(this, classBinding);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<CLASS>(this, classBinding);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<CLASS>(this, classBinding);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  public synchronized void registerBindingMatcher(@NotNull IBindingMatcher matcher) {
    bindingMatchers.add(matcher);
  }

  protected synchronized List<IBindingMatcher> getBindingMatchers() {
    return Collections.unmodifiableList(bindingMatchers);
  }

  public synchronized Map<Class<?>, IClassBinding> getClassBindingsByClass() {
    return Collections.unmodifiableMap(classBindingsByClass);
  }

  public Map<Class<? extends IJavaTypeAdapter<?>>, IJavaTypeAdapter<?>> getJavaTypeAdaptersByClass() {
    return Collections.unmodifiableMap(javaTypeAdapterMap);
  }

  @Override
  public Class<?> getBoundClassForXmlQName(@NotNull QName rootQName) {
    Class<?> retval = null;
    for (IBindingMatcher matcher : getBindingMatchers()) {
      retval = matcher.getBoundClassForXmlQName(rootQName);
      if (retval != null) {
        break;
      }
    }
    return retval;
  }

  @Override
  public Class<?> getBoundClassForJsonName(@NotNull String rootName) {
    Class<?> retval = null;
    for (IBindingMatcher matcher : getBindingMatchers()) {
      retval = matcher.getBoundClassForJsonName(rootName);
      if (retval != null) {
        break;
      }
    }
    return retval;
  }

  @Override
  public IBoundLoader newBoundLoader() {
    return new DefaultBoundLoader(this);
  }

  @Override
  public <CLASS> CLASS copyBoundObject(@NotNull CLASS other, Object parentInstance) throws BindingException {
    IClassBinding classBinding = getClassBinding(other.getClass());
    @SuppressWarnings("unchecked")
    CLASS retval = (CLASS) classBinding.copyBoundObject(other, parentInstance);
    return retval;
  }

  @Override
  public IBoundXdmNodeItem toNodeItem(@NotNull Object boundObject, URI baseUri, boolean rootNode)
      throws IllegalArgumentException {
    IClassBinding binding = getClassBinding(boundObject.getClass());
    return IXdmFactory.INSTANCE.newNodeItem(binding, boundObject, baseUri, rootNode);
  }

  @Override
  public void validate(@NotNull Object boundObject, URI baseUri, boolean rootNode, IConstraintValidationHandler handler)
      throws IllegalArgumentException {
    IBoundXdmNodeItem nodeItem = toNodeItem(boundObject, baseUri, rootNode);

    StaticContext staticContext = new StaticContext();
    DynamicContext dynamicContext = staticContext.newDynamicContext();
    dynamicContext.setDocumentLoader(newBoundLoader());
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext);
    if (handler != null) {
      validator.setConstraintValidationHandler(handler);
    }
    nodeItem.validate(validator);
    validator.finalizeValidation();
  }

}
