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

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.Deserializer;
import gov.nist.secauto.metaschema.binding.io.Serializer;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

public interface BindingContext {

  public static BindingContext newInstance() {
    return new DefaultBindingContext();
  }

  boolean hasClassBinding(Class<?> clazz) throws BindingException;

  <CLASS> ClassBinding<CLASS> getClassBinding(Class<CLASS> clazz) throws BindingException;

  <TYPE> JavaTypeAdapter<TYPE> getJavaTypeAdapter(Class<TYPE> itemType) throws BindingException;

  <CLASS> Serializer<CLASS> newSerializer(Format format, Class<CLASS> clazz, Configuration configuration)
      throws BindingException;

  <CLASS> void serializeToFormat(Format format, CLASS data, OutputStream out) throws BindingException;

  <CLASS> void serializeToFormat(Format format, CLASS data, File file) throws BindingException, FileNotFoundException;

  <CLASS> void serializeToFormat(Format format, CLASS data, Writer writer) throws BindingException;

  <CLASS> void serializeToFormat(Format format, CLASS data, OutputStream out, Configuration configuration)
      throws BindingException;

  <CLASS> void serializeToFormat(Format format, CLASS data, File file, Configuration configuration)
      throws BindingException, FileNotFoundException;

  <CLASS> void serializeToFormat(Format format, CLASS data, Writer writer, Configuration configuration)
      throws BindingException;

  <CLASS> Deserializer<CLASS> newDeserializer(Format format, Class<CLASS> clazz, Configuration configuration)
      throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out) throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file)
      throws BindingException, FileNotFoundException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url) throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader) throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, InputStream out, Configuration configuration)
      throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, File file, Configuration configuration)
      throws BindingException, FileNotFoundException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, URL url, Configuration configuration)
      throws BindingException;

  <CLASS> CLASS deserializeFromFormat(Format format, Class<CLASS> clazz, Reader reader, Configuration configuration)
      throws BindingException;
}
