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
import gov.nist.secauto.metaschema.binding.metapath.item.ConstraintContentValidator;
import gov.nist.secauto.metaschema.binding.metapath.item.IXdmFactory;
import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;

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
 * performance at the cost of some memory use. Thus, using the same singleton of this class across
 * multiple I/O operations will improve overall read and write performance when processing the same
 * types of data.
 * <p>
 * Serializers and deserializers provided by this class using the
 * {@link #newSerializer(Format, Class)} and {@link #newDeserializer(Format, Class)} methods will
 * <p>
 * This class is synchronized and is thread-safe.
 */
public class DefaultBindingContext implements IBindingContext {
  private static DefaultBindingContext singleton;

  @NotNull
  private final Map<Class<?>, IMetaschema> metaschemasByClass = new HashMap<>(); // NOPMD - intentional
  @NotNull
  private final Map<Class<?>, IClassBinding> classBindingsByClass = new HashMap<>(); // NOPMD - intentional
  @NotNull
  private final Map<Class<? extends IJavaTypeAdapter<?>>, IJavaTypeAdapter<?>> javaTypeAdapterMap // NOPMD - intentional
      = new HashMap<>();
  @NotNull
  private final List<@NotNull IBindingMatcher> bindingMatchers = new LinkedList<>();

  public static DefaultBindingContext instance() {
    synchronized (DefaultBindingContext.class) {
      if (singleton == null) {
        singleton = new DefaultBindingContext();
      }
    }
    return singleton;
  }

  /**
   * Construct a new binding context.
   */
  protected DefaultBindingContext() {
    // only allow extended classes
  }

  @Override
  public IMetaschema getMetaschemaInstanceByClass(Class<? extends AbstractBoundMetaschema> clazz) {
    IMetaschema retval;
    synchronized (this) {
      retval = metaschemasByClass.get(clazz);
      if (retval == null) {
        retval = AbstractBoundMetaschema.createInstance(clazz, this);
        metaschemasByClass.put(clazz, retval);
      }
    }
    return retval;
  }

  @Override
  public IClassBinding getClassBinding(@NotNull Class<?> clazz) {
    IClassBinding retval;
    synchronized (this) {
      retval = classBindingsByClass.get(clazz);
      if (retval == null) {
        if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
          retval = DefaultAssemblyClassBinding.createInstance(clazz, this);
        } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
          retval = DefaultFieldClassBinding.createInstance(clazz, this);
        } else {
          throw new IllegalArgumentException(String.format(
              "Class '%s' does not represent a Metaschema definition"
                  + " since it is missing a '%s' or '%s' annotation.",
              clazz.getName(), MetaschemaAssembly.class.getName(), MetaschemaField.class.getName()));
        }
        classBindingsByClass.put(clazz, retval);
      }
    }
    return retval;
  }

  @Override
  public <TYPE extends IJavaTypeAdapter<?>> TYPE
      getJavaTypeAdapterInstance(@NotNull Class<TYPE> clazz) {
    IJavaTypeAdapter<?> instance;
    synchronized (this) {
      instance = javaTypeAdapterMap.get(clazz);
      if (instance == null) {
        Constructor<TYPE> constructor;
        try {
          constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException ex) {
          throw new IllegalArgumentException(ex);
        }

        try {
          instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
          throw new IllegalArgumentException(ex);
        }
        javaTypeAdapterMap.put(clazz, instance);
      }
    }
    @SuppressWarnings("unchecked")
    TYPE retval = (TYPE) instance;
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
      retval = new DefaultJsonSerializer<>(this, classBinding);
      break;
    case XML:
      retval = new DefaultXmlSerializer<>(this, classBinding);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<>(this, classBinding);
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
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);
    if (classBinding == null) {
      throw new IllegalStateException(String.format("A binding for class '%s' was not found.", clazz.getName()));
    }

    IDeserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonDeserializer<>(this, classBinding);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<>(this, classBinding);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<>(this, classBinding);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  public void registerBindingMatcher(@NotNull IBindingMatcher matcher) {
    synchronized (this) {
      bindingMatchers.add(matcher);
    }
  }

  @NotNull
  protected List<@NotNull ? extends IBindingMatcher> getBindingMatchers() {
    synchronized (this) {
      return CollectionUtil.unmodifiableList(bindingMatchers);
    }
  }

  public Map<Class<?>, IClassBinding> getClassBindingsByClass() {
    synchronized (this) {
      return Collections.unmodifiableMap(classBindingsByClass);
    }
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
  public INodeItem toNodeItem(@NotNull Object boundObject, URI baseUri, boolean rootNode) {
    IClassBinding classBinding = getClassBinding(boundObject.getClass());
    if (classBinding == null) {
      throw new IllegalStateException(
          String.format("A binding for class '%s' was not found.", boundObject.getClass().getName()));
    }
    return IXdmFactory.INSTANCE.newNodeItem(classBinding, boundObject, baseUri, rootNode);
  }

  @Override
  public IValidationResult validate(@NotNull INodeItem nodeItem) {
    return new ConstraintContentValidator(this).validate(nodeItem);
  }

}
