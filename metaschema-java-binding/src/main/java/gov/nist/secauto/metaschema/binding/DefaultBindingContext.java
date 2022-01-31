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
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.binding.model.constraint.ValidatingXdmVisitor;
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
 * A basic implementation of a {@link BindingContext} used by this implementation.
 */
public class DefaultBindingContext implements BindingContext {
  private static DefaultBindingContext instance;

  public static synchronized DefaultBindingContext instance() {
    if (instance == null) {
      instance = new DefaultBindingContext();
    }
    return instance;
  }

  private final Map<Class<?>, ClassBinding> classBindingsByClass = new HashMap<>();
  private final Map<Class<? extends IJavaTypeAdapter<?>>, IJavaTypeAdapter<?>> javaTypeAdapterMap = new HashMap<>();
  private final List<IBindingMatcher> bindingMatchers = new LinkedList<>();

  /**
   * Construct a new binding context.
   */
  protected DefaultBindingContext() {
  }

  @Override
  public synchronized ClassBinding getClassBinding(Class<?> clazz) throws IllegalArgumentException {
    ClassBinding retval = classBindingsByClass.get(clazz);
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
      getJavaTypeAdapterInstance(Class<TYPE> clazz) {
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

  @Override
  public <CLASS> Serializer<CLASS> newSerializer(Format format, Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding classBinding = (AssemblyClassBinding) getClassBinding(clazz);

    Serializer<CLASS> retval;
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

  @Override
  public <CLASS> Deserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding classBinding = (AssemblyClassBinding) getClassBinding(clazz);

    Deserializer<CLASS> retval;
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
  public synchronized void registerBindingMatcher(IBindingMatcher matcher) {
    bindingMatchers.add(matcher);
  }

  protected List<IBindingMatcher> getBindingMatchers() {
    return Collections.unmodifiableList(bindingMatchers);
  }

  public synchronized Map<Class<?>, ClassBinding> getClassBindingsByClass() {
    return Collections.unmodifiableMap(classBindingsByClass);
  }

  public Map<Class<? extends IJavaTypeAdapter<?>>, IJavaTypeAdapter<?>> getJavaTypeAdaptersByClass() {
    return Collections.unmodifiableMap(javaTypeAdapterMap);
  }

  @Override
  public Class<?> getBoundClassForXmlQName(QName rootQName) {
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
  public Class<?> getBoundClassForJsonName(String rootName) {
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
    ClassBinding classBinding = getClassBinding(other.getClass());
    @SuppressWarnings("unchecked")
    CLASS retval = (CLASS) classBinding.copyBoundObject(other, parentInstance);
    return retval;
  }

  @Override
  public IBoundXdmNodeItem toNodeItem(@NotNull Object boundObject, URI baseUri, boolean rootNode)
      throws IllegalArgumentException {
    ClassBinding binding = getClassBinding(boundObject.getClass());
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
    new ValidatingXdmVisitor().visit(nodeItem, validator);
    validator.finalizeValidation();
  }

}
