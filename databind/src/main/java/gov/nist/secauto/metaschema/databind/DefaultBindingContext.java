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

import gov.nist.secauto.metaschema.core.datatype.DataTypeService;
import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.DefaultBoundLoader;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.IDeserializer;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.databind.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.databind.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.databind.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The implementation of a {@link IBindingContext} provided by this library.
 * <p>
 * This implementation caches Metaschema information, which can dramatically
 * improve read and write performance at the cost of some memory use. Thus,
 * using the same singleton of this class across multiple I/O operations will
 * improve overall read and write performance when processing the same types of
 * data.
 * <p>
 * Serializers and deserializers provided by this class using the
 * {@link #newSerializer(Format, Class)} and
 * {@link #newDeserializer(Format, Class)} methods will
 * <p>
 * This class is synchronized and is thread-safe.
 */
public class DefaultBindingContext implements IBindingContext {
  private static DefaultBindingContext singleton;
  @NonNull
  private final IMetaschemaLoaderStrategy metaschemaLoaderStrategy;
  @NonNull
  private final List<IBindingMatcher> bindingMatchers = new LinkedList<>();

  /**
   * Get the singleton instance of this binding context.
   *
   * @return the binding context
   */
  @NonNull
  public static DefaultBindingContext instance() {
    synchronized (DefaultBindingContext.class) {
      if (singleton == null) {
        singleton = new DefaultBindingContext();
      }
    }
    return ObjectUtils.notNull(singleton);
  }

  /**
   * Construct a new binding context.
   *
   * @param externalConstraintSets
   *          the set of external constraints to configure this binding to use
   */
  public DefaultBindingContext(@NonNull Set<IConstraintSet> externalConstraintSets) {
    // only allow extended classes
    metaschemaLoaderStrategy = new ExternalConstraintsMetaschemaLoaderStrategy(this, externalConstraintSets);
  }

  /**
   * Construct a new binding context.
   */
  public DefaultBindingContext() {
    // only allow extended classes
    metaschemaLoaderStrategy = new SimpleMetaschemaLoaderStrategy(this);
  }

  @Override
  public IMetaschema getMetaschemaInstanceByClass(@NonNull Class<? extends IMetaschema> clazz) {
    return metaschemaLoaderStrategy.getMetaschemaInstanceByClass(clazz);
  }

  @Override
  public IClassBinding getClassBinding(@NonNull Class<?> clazz) {
    return metaschemaLoaderStrategy.getClassBinding(clazz);
  }

  @Override
  public Map<Class<?>, IClassBinding> getClassBindingsByClass() {
    return metaschemaLoaderStrategy.getClassBindingsByClass();
  }

  @Override
  public <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NonNull Class<TYPE> clazz) {
    return DataTypeService.getInstance().getJavaTypeAdapterByClass(clazz);
  }

  /**
   * {@inheritDoc}
   * <p>
   * A serializer returned by this method is thread-safe.
   */
  @Override
  public <CLASS> ISerializer<CLASS> newSerializer(@NonNull Format format, @NonNull Class<CLASS> clazz) {
    Objects.requireNonNull(format, "format");
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);
    if (classBinding == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getClass().getName()));
    }
    ISerializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonSerializer<>(classBinding);
      break;
    case XML:
      retval = new DefaultXmlSerializer<>(classBinding);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<>(classBinding);
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
  public <CLASS> IDeserializer<CLASS> newDeserializer(@NonNull Format format, @NonNull Class<CLASS> clazz) {
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);
    if (classBinding == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getName()));
    }
    IDeserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonDeserializer<>(classBinding);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<>(classBinding);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<>(classBinding);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  public DefaultBindingContext registerBindingMatcher(@NonNull IBindingMatcher matcher) {
    synchronized (this) {
      bindingMatchers.add(matcher);
    }
    return this;
  }

  /**
   * Get the binding matchers that are associated with this class.
   *
   * @return the list of matchers
   * @see #registerBindingMatcher(IBindingMatcher)
   */
  @NonNull
  protected List<? extends IBindingMatcher> getBindingMatchers() {
    synchronized (this) {
      return CollectionUtil.unmodifiableList(bindingMatchers);
    }
  }

  @Override
  public Class<?> getBoundClassForXmlQName(@NonNull QName rootQName) {
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
  public Class<?> getBoundClassForJsonName(@NonNull String rootName) {
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
  public <CLASS> CLASS copyBoundObject(@NonNull CLASS other, Object parentInstance) throws BindingException {
    IClassBinding classBinding = getClassBinding(other.getClass());
    if (classBinding == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", other.getClass().getName()));
    }
    @SuppressWarnings("unchecked") CLASS retval = (CLASS) classBinding.copyBoundObject(other, parentInstance);
    return retval;
  }
}
