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
import gov.nist.secauto.metaschema.binding.io.IDeserializer;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.net.URI;
import java.time.ZonedDateTime;

import javax.xml.namespace.QName;

/**
 * Provides information supporting a binding between a set of metaschema models and corresponding
 * Java classes.
 */
public interface IBindingContext {

  /**
   * Get a new {@link IBindingContext}, which can be used to load information that binds a model to a
   * set of Java classes.
   * 
   * @return a new binding context
   */
  @NotNull
  static IBindingContext newInstance() {
    return new DefaultBindingContext();
  }

  /**
   * Register a matcher used to identify a bound class by the content's root name.
   * 
   * @param matcher
   *          the matcher implementation
   */
  void registerBindingMatcher(@NotNull IBindingMatcher matcher);

  /**
   * Determine the bound class for the provided XML {@link QName}.
   * 
   * @param rootQName
   *          the root XML element's QName
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  Class<?> getBoundClassForXmlQName(@NotNull QName rootQName);

  /**
   * Determine the bound class for the provided JSON/YAML property/item name using any registered
   * matchers.
   * 
   * @param rootName
   *          the JSON/YAML property/item name
   * @return the bound class or {@code null} if not recognized
   * @see IBindingContext#registerBindingMatcher(IBindingMatcher)
   */
  Class<?> getBoundClassForJsonName(@NotNull String rootName);

  /**
   * Get's the {@link IJavaTypeAdapter} associated with the specified Java class, which is used to
   * read and write XML, JSON, and YAML data to and from instances of that class. Thus, this adapter
   * supports a direct binding between the Java class and structured data in one of the supported
   * formats. Adapters are used to support bindings for simple data objects (e.g., {@link String},
   * {@link BigInteger}, {@link ZonedDateTime}, etc).
   * 
   * @param <TYPE>
   *          the class type bound by the adapter
   * @param clazz
   *          the Java {@link Class} for the bound type
   * @return the adapter instance or {@code null} if the provided class is not bound
   */
  <TYPE extends IJavaTypeAdapter<?>> IJavaTypeAdapter<TYPE> getJavaTypeAdapterInstance(@NotNull Class<TYPE> clazz);

  // boolean hasClassBinding(Class<?> clazz) throws BindingException;

  // <TYPE> void registerSubclassType(@NotNull Class<TYPE> originalClass, @NotNull Class<? extends
  // TYPE> replacementClass);

  /**
   * Get the {@link IClassBinding} instance for a {@link MetaschemaAssembly} or
   * {@link MetaschemaField} associated with a Java class.
   * 
   * @param clazz
   *          the class binding to load
   * @return the associated class binding instance or {@code null} if the class is not bound
   * @throws NullPointerException
   *           if the provided class is {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   */
  IClassBinding getClassBinding(@NotNull Class<?> clazz) throws IllegalArgumentException, NullPointerException;

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
  @NotNull
  <CLASS> ISerializer<CLASS> newSerializer(@NotNull Format format, @NotNull Class<CLASS> clazz);

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
  @NotNull
  <CLASS> IDeserializer<CLASS> newDeserializer(@NotNull Format format, @NotNull Class<CLASS> clazz);

  /**
   * Get a new {@link IBoundLoader} instance.
   * 
   * @return the instance
   */
  @NotNull
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
  @NotNull
  <CLASS> CLASS copyBoundObject(@NotNull CLASS other, Object parentInstance) throws BindingException;

  default IBoundXdmNodeItem toNodeItem(@NotNull Object boundObject, URI baseUri) throws IllegalArgumentException {
    return toNodeItem(boundObject, baseUri, false);
  }

  IBoundXdmNodeItem toNodeItem(@NotNull Object boundObject, URI baseUri, boolean rootNode)
      throws IllegalArgumentException;

  void validate(@NotNull Object boundObject, URI baseUri, boolean rootNode, IConstraintValidationHandler handler)
      throws IllegalArgumentException;
}
