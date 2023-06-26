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
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.IDeserializer;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;

import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides information supporting a binding between a set of Metaschema models and corresponding
 * Java classes.
 */
public interface IBindingContext extends IMetaschemaLoaderStrategy {

  /**
   * Get the singleton {@link IBindingContext} instance, which can be used to load information that
   * binds a model to a set of Java classes.
   *
   * @return a new binding context
   */
  @NonNull
  static IBindingContext instance() {
    return DefaultBindingContext.instance();
  }

  /**
   * Register a matcher used to identify a bound class by the content's root name.
   *
   * @param matcher
   *          the matcher implementation
   */
  void registerBindingMatcher(@NonNull IBindingMatcher matcher);

  /**
   * Determine the bound class for the provided XML {@link QName}.
   *
   * @param rootQName
   *          the root XML element's QName
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  @Nullable
  Class<?> getBoundClassForXmlQName(@NonNull QName rootQName);

  /**
   * Determine the bound class for the provided JSON/YAML property/item name using any registered
   * matchers.
   *
   * @param rootName
   *          the JSON/YAML property/item name
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  @Nullable
  Class<?> getBoundClassForJsonName(@NonNull String rootName);

  /**
   * Get's the {@link IDataTypeAdapter} associated with the specified Java class, which is used to
   * read and write XML, JSON, and YAML data to and from instances of that class. Thus, this adapter
   * supports a direct binding between the Java class and structured data in one of the supported
   * formats. Adapters are used to support bindings for simple data objects (e.g., {@link String},
   * {@link BigInteger}, {@link ZonedDateTime}, etc).
   *
   * @param <TYPE>
   *          the class type of the adapter
   * @param clazz
   *          the Java {@link Class} for the bound type
   * @return the adapter instance or {@code null} if the provided class is not bound
   */
  @Nullable
  <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NonNull Class<TYPE> clazz);

  // boolean hasClassBinding(Class<?> clazz) throws BindingException;

  // <TYPE> void registerSubclassType(@NonNull Class<TYPE> originalClass, @NonNull Class<? extends
  // TYPE> replacementClass);

  /**
   * Gets a data {@link ISerializer} which can be used to write Java instance data for the provided
   * class to the requested format. The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a
   * {@link IClassBinding} exists.
   *
   * @param <CLASS>
   *          the Java type this deserializer can write data from
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @return the serializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getClassBinding(Class)
   */
  @NonNull
  <CLASS> ISerializer<CLASS> newSerializer(@NonNull Format format, @NonNull Class<CLASS> clazz);

  /**
   * Gets a data {@link IDeserializer} which can be used to read Java instance data for the provided
   * class from the requested format. The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a
   * {@link IClassBinding} exists.
   *
   * @param <CLASS>
   *          the Java type this deserializer can read data into
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @return the deserializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getClassBinding(Class)
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
   *           if the provided class is not bound to a Metaschema assembly or field
   */
  @NonNull
  <CLASS> CLASS copyBoundObject(@NonNull CLASS other, Object parentInstance) throws BindingException;

  /**
   * Wraps a bound object in an {@link INodeItem} for use in the Metapath engine.
   *
   * @param boundObject
   *          the bound object to wrap
   * @param baseUri
   *          the base URI of the bound object, which may be used to resolve relative URIs
   * @return the wrapped node item
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @see MetapathExpression
   */
  // TODO: add method to IAssemblyInstance, etc to do this instead
  default INodeItem toNodeItem(@NonNull Object boundObject, @NonNull URI baseUri) {
    return toNodeItem(boundObject, baseUri, false);
  }

  /**
   * Wraps a bound object in an {@link INodeItem} for use in the Metapath engine.
   *
   * @param boundObject
   *          the bound object to wrap
   * @param baseUri
   *          the base URI of the bound object, which may be used to resolve relative URIs
   * @param rootNode
   *          if {@code true}, the bound object will be considered at the root of a node tree
   * @return the wrapped node item
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @see MetapathExpression
   */
  INodeItem toNodeItem(@NonNull Object boundObject, @NonNull URI baseUri, boolean rootNode);

  /**
   * Perform constraint validation on the provided bound object represented as an {@link INodeItem}.
   * The bound object can be turned into a {@link INodeItem} using {@link #toNodeItem(Object, URI)}.
   *
   * @param nodeItem
   *          the node item to validate
   * @return the validation result
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   */
  IValidationResult validate(@NonNull INodeItem nodeItem);

}
