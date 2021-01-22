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
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonDeserializer;
import gov.nist.secauto.metaschema.binding.io.json.DefaultJsonSerializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlDeserializer;
import gov.nist.secauto.metaschema.binding.io.xml.DefaultXmlSerializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlDeserializer;
import gov.nist.secauto.metaschema.binding.io.yaml.DefaultYamlSerializer;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A basic implementation of a {@link BindingContext} used by this implementation.
 */
public class DefaultBindingContext implements BindingContext {

  private final Map<Class<?>, ClassBinding> classBindingsByClass = new HashMap<>();
  private final Map<Class<? extends JavaTypeAdapter<?>>, JavaTypeAdapter<?>> javaTypeAdapterMap = new HashMap<>();

  /**
   * Construct a new binding context.
   */
  protected DefaultBindingContext() {
  }

  @Override
  public synchronized ClassBinding getClassBinding(Class<?> clazz) {
    ClassBinding retval = classBindingsByClass.get(clazz);
    if (retval == null) {
      if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
        retval = DefaultAssemblyClassBinding.createInstance(clazz, this);
      } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
        retval = DefaultFieldClassBinding.createInstance(clazz, this);
      } else {
        throw new IllegalArgumentException(
            String.format(
                "Class '%s' does not represent a Metaschema definition" +
                    " since it is missing a '%s' or '%s' annotation.",
                clazz.getName(), MetaschemaAssembly.class.getName(), MetaschemaField.class.getName()));
      }
      if (retval != null) {
        classBindingsByClass.put(clazz, retval);
      }
    }
    return retval;
  }

  @Override
  public <TYPE extends JavaTypeAdapter<?>> JavaTypeAdapter<TYPE> getJavaTypeAdapterInstance(Class<TYPE> clazz) {
    synchronized (javaTypeAdapterMap) {
      @SuppressWarnings("unchecked")
      JavaTypeAdapter<TYPE> retval = (JavaTypeAdapter<TYPE>) javaTypeAdapterMap.get(clazz);
      if (retval == null) {
        Constructor<TYPE> constructor;
        try {
          constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
          throw new RuntimeException(e);
        }

        try {
          @SuppressWarnings("unchecked")
          JavaTypeAdapter<TYPE> instance = (JavaTypeAdapter<TYPE>) constructor.newInstance();
          retval = instance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
        javaTypeAdapterMap.put(clazz, retval);
      }
      return retval;
    }
  }

  @Override
  public <CLASS> Serializer<CLASS> newSerializer(Format format, Class<CLASS> clazz, Configuration configuration) {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding classBinding = (AssemblyClassBinding) getClassBinding(clazz);

    Serializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonSerializer<CLASS>(this, classBinding, configuration);
      break;
    case XML:
      retval = new DefaultXmlSerializer<CLASS>(this, classBinding, configuration);
      break;
    case YAML:
      retval = new DefaultYamlSerializer<CLASS>(this, classBinding, configuration);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, OutputStream out) throws
  // BindingException {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, null).serialize(data, out);
  // }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, File file)
  // throws BindingException, FileNotFoundException {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, null).serialize(data, file);
  // }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, Writer writer) throws BindingException
  // {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, null).serialize(data, writer);
  // }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, OutputStream out, Configuration
  // configuration)
  // throws BindingException {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, configuration).serialize(data, out);
  // }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, File file, Configuration configuration)
  // throws BindingException, FileNotFoundException {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, configuration).serialize(data, file);
  // }
  //
  // @Override
  // public void serializeToFormat(Format format, Object data, Writer writer, Configuration
  // configuration)
  // throws BindingException {
  // Class<?> clazz = data.getClass();
  // newSerializer(format, clazz, configuration).serialize(data, writer);
  // }

  @Override
  public <CLASS> Deserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz, Configuration configuration) {
    Objects.requireNonNull(format, "format");
    AssemblyClassBinding classBinding = (AssemblyClassBinding) getClassBinding(clazz);

    Deserializer<CLASS> retval;
    switch (format) {
    case JSON:
      retval = new DefaultJsonDeserializer<CLASS>(this, classBinding, configuration);
      break;
    case XML:
      retval = new DefaultXmlDeserializer<CLASS>(this, classBinding, configuration);
      break;
    case YAML:
      retval = new DefaultYamlDeserializer<CLASS>(this, classBinding, configuration);
      break;
    default:
      throw new UnsupportedOperationException(String.format("Unsupported format '%s'", format));
    }

    return retval;
  }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out)
  // throws BindingException {
  // return newDeserializer(format, clazz, null).deserialize(out);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file)
  // throws BindingException, FileNotFoundException {
  // return newDeserializer(format, clazz, null).deserialize(file);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url) throws
  // BindingException {
  // return newDeserializer(format, clazz, null).deserialize(url);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader)
  // throws BindingException {
  // return newDeserializer(format, clazz, null).deserialize(reader);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out,
  // Configuration configuration) throws BindingException {
  // return newDeserializer(format, clazz, configuration).deserialize(out);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file,
  // Configuration configuration)
  // throws BindingException, FileNotFoundException {
  // return newDeserializer(format, clazz, configuration).deserialize(file);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url,
  // Configuration configuration)
  // throws BindingException {
  // return newDeserializer(format, clazz, configuration).deserialize(url);
  // }
  //
  // @Override
  // public <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader,
  // Configuration configuration) throws BindingException {
  // return newDeserializer(format, clazz, configuration).deserialize(reader);
  // }
}
