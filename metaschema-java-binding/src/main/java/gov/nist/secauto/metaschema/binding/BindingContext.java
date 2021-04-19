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

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;

import java.math.BigInteger;
import java.time.ZonedDateTime;

/**
 * Represents information supporting a binding between a set of models and related Java classes.
 */
public interface BindingContext {

  /**
   * Get a new {@link BindingContext}, which can be used to load information that binds a model to a
   * set of Java classes.
   * 
   * @return a new binding context
   */
  static BindingContext newInstance() {
    return new DefaultBindingContext();
  }

  /**
   * Get's the {@link JavaTypeAdapter} associated with the specified Java class, which is used to read
   * and write XML, JSON, and YAML data to and from instances of that class. Thus, this adapter
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
  <TYPE extends JavaTypeAdapter<?>> JavaTypeAdapter<TYPE> getJavaTypeAdapterInstance(Class<TYPE> clazz);

  // boolean hasClassBinding(Class<?> clazz) throws BindingException;

  /**
   * Get the {@link ClassBinding} instance for a {@link MetaschemaAssembly} or {@link MetaschemaField}
   * associated with a Java class.
   * 
   * @param clazz
   *          the class binding to load
   * @return the associated class binding instance or {@code null} if the class is not bound
   * @throws NullPointerException
   *           if the provided class is {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   */
  ClassBinding getClassBinding(Class<?> clazz);

  /**
   * Gets a data {@link Serializer} which can be used to write Java instance data for the provided
   * class to the requested format. The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a {@link ClassBinding}
   * exists.
   * 
   * @param <CLASS>
   *          the Java type this deserializer can write data from
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @param configuration
   *          provides configuration parameters to customize the serializer's behavior
   * @return the serializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getClassBinding(Class)
   */
  <CLASS> Serializer<CLASS> newSerializer(Format format, Class<CLASS> clazz, Configuration configuration);
  //
  // void serializeToFormat(Format format, Object data, OutputStream out) throws BindingException;
  //
  // void serializeToFormat(Format format, Object data, File file) throws BindingException,
  // FileNotFoundException;
  //
  // void serializeToFormat(Format format, Object data, Writer writer) throws BindingException;
  //
  // void serializeToFormat(Format format, Object data, OutputStream out, Configuration configuration)
  // throws BindingException;
  //
  // void serializeToFormat(Format format, Object data, File file, Configuration configuration)
  // throws BindingException, FileNotFoundException;
  //
  // void serializeToFormat(Format format, Object data, Writer writer, Configuration configuration)
  // throws BindingException;

  /**
   * Gets a data {@link Deserializer} which can be used to read Java instance data for the provided
   * class from the requested format. The provided class must be a bound Java class with a
   * {@link MetaschemaAssembly} or {@link MetaschemaField} annotation for which a {@link ClassBinding}
   * exists.
   * 
   * @param <CLASS>
   *          the Java type this deserializer can read data into
   * @param format
   *          the format to serialize into
   * @param clazz
   *          the Java data type to serialize
   * @param configuration
   *          provides configuration parameters to customize the serializer's behavior
   * @return the deserializer instance
   * @throws NullPointerException
   *           if any of the provided arguments, except the configuration, are {@code null}
   * @throws IllegalArgumentException
   *           if the provided class is not bound to a Metaschema assembly or field
   * @throws UnsupportedOperationException
   *           if the requested format is not supported by the implementation
   * @see #getClassBinding(Class)
   */
  <CLASS> Deserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz, Configuration configuration);
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out) throws
  // BindingException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file)
  // throws BindingException, FileNotFoundException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url) throws
  // BindingException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader) throws
  // BindingException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out,
  // Configuration configuration)
  // throws BindingException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file, Configuration
  // configuration)
  // throws BindingException, FileNotFoundException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url, Configuration
  // configuration)
  // throws BindingException;
  //
  // <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader,
  // Configuration configuration)
  // throws BindingException;
}
