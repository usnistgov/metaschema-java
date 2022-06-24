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

import gov.nist.secauto.metaschema.model.common.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementations of this interface are able to read structured data into a bound object instance
 * of the parameterized type.
 * 
 * @param <CLASS>
 *          the Java type into which data can be read
 */
public interface IDeserializer<CLASS> extends IMutableConfiguration<DeserializationFeature> {
  /**
   * Determine if the serializer is performing validation.
   * 
   * @return {@code true} if the serializer is performing content validation, or {@code false}
   *         otherwise
   */
  default boolean isValidating() {
    return isFeatureEnabled(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
  }

  @NotNull
  IConstraintValidationHandler getConstraintValidationHandler();

  void setConstraintValidationHandler(@NotNull IConstraintValidationHandler constraintValidationHandler);

  /**
   * Read data from the {@link InputStream} into a bound class instance.
   * 
   * @param is
   *          the input stream to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return the instance data
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NotNull
  default CLASS deserialize(@NotNull InputStream is, @NotNull URI documentUri) throws IOException {
    return deserialize(new InputStreamReader(is), documentUri);
  }

  /**
   * Read data from the {@link Path} into a bound class instance.
   * 
   * @param path
   *          the file to read from
   * @return the instance data
   * @throws IOException
   *           if an error occurred while writing data to the file indicated by the {@code path}
   *           parameter
   */
  @NotNull
  default CLASS deserialize(@NotNull Path path) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return deserialize(ObjectUtils.notNull(reader), ObjectUtils.notNull(path.toUri()));
    }
  }

  /**
   * Read data from the {@link File} into a bound class instance.
   * 
   * @param file
   *          the file to read from
   * @return the instance data
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NotNull
  default CLASS deserialize(@NotNull File file) throws IOException {
    return deserialize(ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Read data from the remote resource into a bound class instance.
   * 
   * 
   * @param url
   *          the remote resource to read from
   * @return the instance data
   * @throws IOException
   *           if an error occurred while reading data from the stream
   * @throws URISyntaxException
   *           if the provided URL is not formatted strictly according to to RFC2396 and cannot be
   *           converted to a URI.
   */
  @NotNull
  default CLASS deserialize(@NotNull URL url) throws IOException, URISyntaxException {
    try (InputStream in = url.openStream()) {
      return deserialize(ObjectUtils.notNull(in), ObjectUtils.notNull(url.toURI()));
    }
  }

  /**
   * Read data from the {@link Reader} into a bound class instance.
   * 
   * 
   * @param reader
   *          the reader to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return the instance data
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @SuppressWarnings({ "unchecked", "null" })
  @NotNull
  default CLASS deserialize(@NotNull Reader reader, @NotNull URI documentUri) throws IOException {
    INodeItem nodeItem = deserializeToNodeItem(reader, documentUri);
    return (CLASS) nodeItem.getValue();
  }

  /**
   * Read data from the {@link Reader} into a node item instance.
   * 
   * @param is
   *          the input stream to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return a new node item
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NotNull
  default INodeItem deserializeToNodeItem(@NotNull InputStream is, @NotNull URI documentUri)
      throws IOException {
    return deserializeToNodeItem(new InputStreamReader(is), documentUri);
  }

  /**
   * Read data from the {@link Reader} into a node item instance.
   * 
   * @param reader
   *          the reader to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return a new node item
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NotNull
  INodeItem deserializeToNodeItem(@NotNull Reader reader, @NotNull URI documentUri) throws IOException;
}
