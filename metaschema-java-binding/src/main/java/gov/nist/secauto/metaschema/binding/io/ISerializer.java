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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Implementations of this interface are able to write data in a bound object instance of the
 * parameterized type to a structured data format.
 * 
 * @param <CLASS>
 *          the Java type from which data can be written
 */
public interface ISerializer<CLASS> extends IMutableConfiguration {
  /**
   * Write data from a bound class instance to the {@link OutputStream}.
   * 
   * @param data
   *          the instance data
   * @param os
   *          the output stream to write to
   * @throws BindingException
   *           if an error occurred while writing data to the stream
   */
  default void serialize(CLASS data, OutputStream os) throws BindingException {
    serialize(data, new OutputStreamWriter(os));
  }

  /**
   * Write data from a bound class instance to the {@link File}.
   * 
   * @param data
   *          the instance data
   * @param file
   *          the file to write to
   * @throws BindingException
   *           if an error occurred while writing data to the stream
   * @throws FileNotFoundException
   *           if the provided file is not a regular file or if there was an error creating the file
   */
  default void serialize(CLASS data, File file) throws BindingException, FileNotFoundException {
    try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
        StandardOpenOption.WRITE)) {
      serialize(data, writer);
      writer.close();
    } catch (IOException ex) {
      throw new BindingException("Unable to open file: " + file == null ? "{null}" : file.getPath(), ex);
    }
  }

  /**
   * Write data from a bound class instance to the {@link Writer}.
   * 
   * @param data
   *          the instance data
   * @param writer
   *          the writer to write to
   * @throws BindingException
   *           if an error occurred while writing data to the stream
   */
  void serialize(CLASS data, Writer writer) throws BindingException;
}
