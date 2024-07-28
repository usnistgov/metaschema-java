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

package gov.nist.secauto.metaschema.databind.io;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Implementations of this interface are able to write data in a bound object
 * instance of the parameterized type to a structured data format.
 *
 * @param <CLASS>
 *          the Java type from which data can be written
 */
public interface ISerializer<CLASS extends IBoundObject> extends IMutableConfiguration<SerializationFeature<?>> {

  @Override
  ISerializer<CLASS> enableFeature(SerializationFeature<?> feature);

  @Override
  ISerializer<CLASS> disableFeature(SerializationFeature<?> feature);

  @Override
  ISerializer<CLASS> applyConfiguration(IConfiguration<SerializationFeature<?>> other);

  @Override
  ISerializer<CLASS> set(SerializationFeature<?> feature, Object value);

  /**
   * Write data from a bound class instance to the {@link OutputStream}.
   * <p>
   * This method does not have ownership of the the provided output stream and
   * will not close it.
   *
   * @param data
   *          the instance data
   * @param os
   *          the output stream to write to
   * @throws IOException
   *           if an error occurred while writing data to the stream
   */
  default void serialize(@NonNull IBoundObject data, @NonNull OutputStream os) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
    serialize(data, writer);
    writer.flush();
  }

  /**
   * Write data from a bound class instance to the {@link File}.
   *
   * @param data
   *          the instance data
   * @param path
   *          the file to write to
   * @param openOptions
   *          options specifying how the file is opened
   * @throws IOException
   *           if an error occurred while writing data to the file indicated by
   *           the {@code path} parameter
   */
  default void serialize(@NonNull IBoundObject data, @NonNull Path path, OpenOption... openOptions) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, openOptions)) {
      assert writer != null;
      serialize(data, writer);
    }
  }

  /**
   * Write data from a bound class instance to the {@link File}.
   *
   * @param data
   *          the instance data
   * @param file
   *          the file to write to
   * @throws IOException
   *           if an error occurred while writing data to the stream
   */
  default void serialize(@NonNull IBoundObject data, @NonNull File file) throws IOException {
    serialize(data, ObjectUtils.notNull(file.toPath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * Write data from a bound class instance to the {@link Writer}.
   *
   * @param data
   *          the instance data
   * @param writer
   *          the writer to write to
   * @throws IOException
   *           if an error occurred while writing data to the stream
   */
  void serialize(@NonNull IBoundObject data, @NonNull Writer writer) throws IOException;
}
