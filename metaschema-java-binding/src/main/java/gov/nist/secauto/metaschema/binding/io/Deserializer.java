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

package gov.nist.secauto.metaschema.binding.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * Implementations of this interface are able to read structured data into a Java class instance of
 * the parameterized type.
 * 
 * @param <CLASS>
 *          the Java type into which data can be read
 */
public interface Deserializer<CLASS> {
  // Format supportedFromat();
  //
  /**
   * Read data from the {@link InputStream} into a bound class instance.
   * 
   * @param is
   *          the input stream to read from
   * @return the instance data
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  CLASS deserialize(InputStream is) throws BindingException;

  /**
   * Read data from the {@link File} into a bound class instance.
   * 
   * @param file
   *          the file to read from
   * @return the instance data
   * @throws FileNotFoundException
   *           if the provided file does not exist
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  CLASS deserialize(File file) throws FileNotFoundException, BindingException;

  /**
   * Read data from the remote resource into a bound class instance.
   * 
   * 
   * @param url
   *          the remote resource to read from
   * @return the instance data
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  CLASS deserialize(URL url) throws BindingException;

  /**
   * Read data from the {@link Reader} into a bound class instance.
   * 
   * 
   * @param reader
   *          the reader to read from
   * @return the instance data
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  CLASS deserialize(Reader reader) throws BindingException;
}
