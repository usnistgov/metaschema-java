/**
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

import gov.nist.secauto.metaschema.binding.datatypes.adapter.DataTypes;
import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassIntrospector;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaModel;
import gov.nist.secauto.metaschema.datatypes.Datatype;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class DefaultBindingContext implements BindingContext {
  private static final Logger logger = LogManager.getLogger(DefaultBindingContext.class);

  private final Map<Class<?>, ClassBinding<?>> classBindingsByClass = new HashMap<>();
  // private final Map<Class<?>, XmlParsePlan<?>> xmlParsePlansByClass = new HashMap<>();
  // private final Map<Class<?>, XmlWriter> xmlWriterByClass = new HashMap<>();
  // private final Map<Class<?>, JsonWriter> jsonWriterByClass = new HashMap<>();
  private final Map<Type, JavaTypeAdapter<?>> xmlJavaTypeAdapters = new HashMap<>();

  public DefaultBindingContext() {
    // register all known types
    for (DataTypes dts : DataTypes.values()) {
      JavaTypeAdapter<?> adapter = dts.getJavaTypeAdapter();
      if (adapter != null) {
        xmlJavaTypeAdapters.put(dts.getJavaClass(), adapter);
      }
    }
  }

  public <TYPE extends Datatype<TYPE>> JavaTypeAdapter<TYPE> registerJavaTypeAdapter(Class<TYPE> clazz,
      JavaTypeAdapter<TYPE> adapter) {
    Objects.requireNonNull(clazz, "clazz");
    Objects.requireNonNull(adapter, "adapter");

    synchronized (adapter) {
      @SuppressWarnings("unchecked")
      JavaTypeAdapter<TYPE> retval = (JavaTypeAdapter<TYPE>) xmlJavaTypeAdapters.put(clazz, adapter);
      return retval;
    }
  }

  protected <CLASS> ClassBinding<CLASS> getBoundClassBinding(Class<CLASS> clazz) throws BindingException {
    ClassBinding<CLASS> classBinding = getClassBinding(clazz);
    if (classBinding == null) {
      throw new BindingException(String.format("Class '%s' is not a bound Java class.", clazz.getName()));
    }
    return classBinding;
  }

  @Override
  public boolean hasClassBinding(Class<?> clazz) throws BindingException {
    return getClassBinding(clazz) != null;
  }

  @Override
  public <CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException {
    synchronized (this) {
      @SuppressWarnings("unchecked")
      ClassBinding<CLASS> retval = (ClassBinding<CLASS>) classBindingsByClass.get(clazz);
      if (retval == null) {
        if (ClassIntrospector.hasClassAnnotation(clazz, MetaschemaModel.class)) {
          retval = ClassBinding.newClassBinding(clazz);
          if (retval != null) {
            classBindingsByClass.put(clazz, retval);
          } else {
            logger.warn("Unable to bind class: {}", clazz.getName());
          }
        }
      }
      return retval;
    }
  }

  @Override
  public <TYPE> JavaTypeAdapter<TYPE> getJavaTypeAdapter(Class<TYPE> clazz) throws BindingException {
    synchronized (this) {
      // try to find a simple data binding
      @SuppressWarnings("unchecked")
      JavaTypeAdapter<TYPE> retval = (JavaTypeAdapter<TYPE>) xmlJavaTypeAdapters.get(clazz);
      // if (retval == null) {
      // // no simple binding exists, try to bind to the object
      //
      // // TODO: handle binding exception, which may be caused if the class cannot be
      // // bound for any reason
      // ClassBinding<TYPE> classBinding = getClassBinding(clazz);
      // if (classBinding == null) {
      // throw new BindingException(String.format("Unable to bind to Java type '%s'.", clazz.getName()));
      // }
      // retval = new ObjectJavaTypeAdapter<TYPE>(classBinding);
      // xmlJavaTypeAdapters.put(clazz, retval);
      // }
      if (retval == null) {
        logger.warn("Unable to load Java type adapter for class: {}", clazz.getName());
      } else if (logger.isDebugEnabled()) {
        logger.debug("Loaded Java type adapter for class: {}", clazz.getName());
      }
      return retval;
    }
  }

  @Override
  public <CLASS> Serializer<CLASS> newSerializer(Format format, Class<CLASS> clazz, Configuration configuration)
      throws BindingException {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);

    Serializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new JsonSerializerImpl<CLASS>(this, classBinding, configuration);
      break;
    case XML:
      retval = new XmlSerializerImpl<CLASS>(this, classBinding, configuration);
      break;
    case YAML:
      retval = new YamlSerializerImpl<CLASS>(this, classBinding, configuration);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, OutputStream out) throws BindingException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, null).serialize(data, out);
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, File file)
      throws BindingException, FileNotFoundException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, null).serialize(data, file);
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, Writer writer) throws BindingException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, null).serialize(data, writer);
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, OutputStream out, Configuration configuration)
      throws BindingException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, configuration).serialize(data, out);
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, File file, Configuration configuration)
      throws BindingException, FileNotFoundException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, configuration).serialize(data, file);
  }

  @Override
  public <CLASS> void serializeToFormat(Format format, CLASS data, Writer writer, Configuration configuration)
      throws BindingException {
    @SuppressWarnings("unchecked")
    Class<CLASS> clazz = (Class<CLASS>) data.getClass();
    newSerializer(format, clazz, configuration).serialize(data, writer);
  }

  @Override
  public <CLASS> Deserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz, Configuration configuration)
      throws BindingException {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding<CLASS> classBinding = (AssemblyClassBinding<CLASS>) getBoundClassBinding(clazz);

    Deserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new JsonDeserializerImpl<CLASS>(this, classBinding, configuration);
      break;
    case XML:
      retval = new XmlDeserializerImpl<CLASS>(this, classBinding, configuration);
      break;
    case YAML:
      retval = new YamlDeserializerImpl<CLASS>(this, classBinding, configuration);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out)
      throws BindingException {
    return newDeserializer(format, clazz, null).deserialize(out);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file)
      throws BindingException, FileNotFoundException {
    return newDeserializer(format, clazz, null).deserialize(file);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url) throws BindingException {
    return newDeserializer(format, clazz, null).deserialize(url);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader) throws BindingException {
    return newDeserializer(format, clazz, null).deserialize(reader);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out,
      Configuration configuration) throws BindingException {
    return newDeserializer(format, clazz, configuration).deserialize(out);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file, Configuration configuration)
      throws BindingException, FileNotFoundException {
    return newDeserializer(format, clazz, configuration).deserialize(file);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url, Configuration configuration)
      throws BindingException {
    return newDeserializer(format, clazz, configuration).deserialize(url);
  }

  @Override
  public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader,
      Configuration configuration) throws BindingException {
    return newDeserializer(format, clazz, configuration).deserialize(reader);
  }
}
