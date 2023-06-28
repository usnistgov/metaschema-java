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
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.model.common.constraint.FindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.model.common.datatype.DataTypeService;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.DefaultNodeItemFactory;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;

import java.net.URI;
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
  @NonNull
  private final IMetaschemaLoaderStrategy metaschemaLoaderStrategy;
  @NonNull
  private final List<IBindingMatcher> bindingMatchers = new LinkedList<>();

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
  protected DefaultBindingContext() {
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
  public <CLASS> IDeserializer<CLASS> newDeserializer(@NonNull Format format, @NonNull Class<CLASS> clazz) {
    IAssemblyClassBinding classBinding = (IAssemblyClassBinding) getClassBinding(clazz);
    if (classBinding == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", clazz.getClass().getName()));
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
  public void registerBindingMatcher(@NonNull IBindingMatcher matcher) {
    synchronized (this) {
      bindingMatchers.add(matcher);
    }
  }

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

  @Override
  public INodeItem toNodeItem(@NonNull Object boundObject, URI baseUri, boolean rootNode) {
    IClassBinding classBinding = getClassBinding(boundObject.getClass());
    if (classBinding == null) {
      throw new IllegalStateException(String.format("Class '%s' is not bound", boundObject.getClass().getName()));
    }
    return DefaultNodeItemFactory.instance().newNodeItem(classBinding, boundObject, baseUri, rootNode);
  }

}
