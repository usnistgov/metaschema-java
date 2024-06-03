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
import gov.nist.secauto.metaschema.core.metapath.IDocumentLoader;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A common interface for loading Module based instance resources.
 */
public interface IBoundLoader extends IDocumentLoader, IMutableConfiguration<DeserializationFeature<?>> {

  @Override
  default IBoundLoader enableFeature(DeserializationFeature<?> feature) {
    return set(feature, true);
  }

  @Override
  default IBoundLoader disableFeature(DeserializationFeature<?> feature) {
    return set(feature, false);
  }

  @Override
  IBoundLoader applyConfiguration(IConfiguration<DeserializationFeature<?>> other);

  @Override
  IBoundLoader set(DeserializationFeature<?> feature, Object value);

  /**
   * Determine the format of the provided resource.
   *
   * @param file
   *          the resource
   * @return the format information for the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  default FormatDetector.Result detectFormat(@NonNull File file) throws IOException {
    return detectFormat(ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Determine the format of the provided resource.
   *
   * @param path
   *          the resource
   * @return the format information for the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  default FormatDetector.Result detectFormat(@NonNull Path path) throws IOException {
    return detectFormat(ObjectUtils.notNull(path.toUri()));
  }

  /**
   * Determine the format of the provided resource.
   *
   * @param url
   *          the resource
   * @return the format information for the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  default FormatDetector.Result detectFormat(@NonNull URL url) throws IOException {
    try {
      return detectFormat(ObjectUtils.notNull(url.toURI()));
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Determine the format of the resource identified by the provided {@code uri}.
   *
   * @param uri
   *          the resource
   * @return the format information for the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  FormatDetector.Result detectFormat(@NonNull URI uri) throws IOException;

  /**
   * Determine the format of the provided resource.
   * <p>
   * This method will consume data from the provided {@link InputStream}. If the
   * caller of this method intends to read data from the stream after determining
   * the format, the caller should pass in a stream that can be reset.
   * <p>
   * This method will not close the provided {@link InputStream}, since it does
   * not own the stream.
   *
   * @param is
   *          an input stream for the resource
   * @return the format information for the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  FormatDetector.Result detectFormat(@NonNull InputStream is) throws IOException;

  //
  // /**
  // * Determine the format of the provided resource.
  // * <p>
  // * This method will consume data from any {@link InputStream} provided by the
  // * {@link InputSource}. If the caller of this method intends to read data from
  // * the stream after determining the format, the caller should pass in a stream
  // * that can be reset.
  // * <p>
  // * This method will not close any {@link InputStream} provided by the
  // * {@link InputSource}, since it does not own the stream.
  // *
  // * @param source
  // * information about how to access the resource
  // * @return the format of the provided resource
  // * @throws IOException
  // * if an error occurred while reading the resource
  // */
  // @NonNull
  // Format detectFormat(@NonNull InputSource source) throws IOException;

  /**
   * Load data from the provided resource into a bound object.
   * <p>
   * This method will auto-detect the format of the provided resource.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param file
   *          the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @see #detectFormat(File)
   */
  @NonNull
  default <CLASS> CLASS load(@NonNull File file) throws IOException {
    return load(ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Load data from the provided resource into a bound object.
   * <p>
   * This method will auto-detect the format of the provided resource.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param path
   *          the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @see #detectFormat(File)
   */
  @NonNull
  default <CLASS> CLASS load(@NonNull Path path) throws IOException {
    return load(ObjectUtils.notNull(path.toUri()));
  }

  /**
   * Load data from the provided resource into a bound object.
   * <p>
   * This method will auto-detect the format of the provided resource.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param url
   *          the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @throws URISyntaxException
   *           if the provided {@code url} is malformed
   * @see #detectFormat(URL)
   */
  @NonNull
  default <CLASS> CLASS load(@NonNull URL url) throws IOException, URISyntaxException {
    return load(ObjectUtils.notNull(url.toURI()));
  }

  /**
   * Load data from the resource identified by the provided {@code uri} into a
   * bound object.
   * <p>
   * This method will auto-detect the format of the provided resource.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param uri
   *          the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @see #detectFormat(URL)
   */
  @NonNull
  <CLASS> CLASS load(@NonNull URI uri) throws IOException;

  /**
   * Load data from the provided resource into a bound object.
   * <p>
   * This method should auto-detect the format of the provided resource.
   * <p>
   * This method will not close the provided {@link InputStream}, since it does
   * not own the stream.
   *
   * @param <CLASS>
   *          the type of the bound object to return
   * @param is
   *          the resource stream
   * @param documentUri
   *          the URI of the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @see #detectFormat(InputStream)
   */
  @NonNull
  <CLASS> CLASS load(@NonNull InputStream is, @NonNull URI documentUri) throws IOException;

  /**
   * Load data from the specified resource into a bound object with the type of
   * the specified Java class.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param file
   *          the resource to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   */
  @NonNull
  default <CLASS> CLASS load(
      @NonNull Class<CLASS> clazz,
      @NonNull File file) throws IOException {
    return load(clazz, ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Load data from the specified resource into a bound object with the type of
   * the specified Java class.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param path
   *          the resource to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   */
  @NonNull
  default <CLASS> CLASS load(
      @NonNull Class<CLASS> clazz,
      @NonNull Path path) throws IOException {
    return load(clazz, ObjectUtils.notNull(path.toUri()));
  }

  /**
   * Load data from the specified resource into a bound object with the type of
   * the specified Java class.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param url
   *          the resource to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   * @throws URISyntaxException
   *           if the provided {@code url} is malformed
   */
  @NonNull
  default <CLASS> CLASS load(
      @NonNull Class<CLASS> clazz,
      @NonNull URL url) throws IOException, URISyntaxException {
    return load(clazz, ObjectUtils.notNull(url.toURI()));
  }

  /**
   * Load data from the specified resource into a bound object with the type of
   * the specified Java class.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param uri
   *          the resource to load
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   */
  @NonNull
  <CLASS> CLASS load(
      @NonNull Class<CLASS> clazz,
      @NonNull URI uri) throws IOException;

  /**
   * Load data from the specified resource into a bound object with the type of
   * the specified Java class.
   * <p>
   * This method will not close the provided {@link InputStream}, since it does
   * not own the stream.
   * <p>
   * Implementations of this method will do format detection. This process might
   * leave the provided {@link InputStream} at a position beyond the last parsed
   * location. If you want to avoid this possibility, use and implementation of
   * {@link IDeserializer#deserialize(InputStream, URI)} instead, such as what is
   * provided by {@link DefaultBindingContext#newDeserializer(Format, Class)}.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param is
   *          the resource stream
   * @param documentUri
   *          the URI of the resource
   * @return the loaded data
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource
   */
  @NonNull
  <CLASS> CLASS load(
      @NonNull Class<CLASS> clazz,
      @NonNull InputStream is,
      @NonNull URI documentUri) throws IOException;

  /**
   * Load data expressed using the provided {@code format} and return that data as
   * a Metapath node item.
   * <p>
   * The specific Module model is auto-detected by analyzing the source. The class
   * reported is implementation specific.
   *
   * @param format
   *          the expected format of the data to parse
   * @param path
   *          the resource
   * @return the Metapath node item for the parsed data
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource
   */
  @NonNull
  default IDocumentNodeItem loadAsNodeItem(
      @NonNull Format format,
      @NonNull Path path) throws IOException {
    return loadAsNodeItem(format, ObjectUtils.notNull(path.toUri()));
  }

  /**
   * Load data expressed using the provided {@code format} and return that data as
   * a Metapath node item.
   * <p>
   * The specific Module model is auto-detected by analyzing the source. The class
   * reported is implementation specific.
   *
   * @param format
   *          the expected format of the data to parse
   * @param uri
   *          the resource
   * @return the Metapath node item for the parsed data
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource
   */
  @NonNull
  IDocumentNodeItem loadAsNodeItem(
      @NonNull Format format,
      @NonNull URI uri) throws IOException;

  /**
   * Load data expressed using the provided {@code format} and return that data as
   * a Metapath node item.
   * <p>
   * The specific Module model is auto-detected by analyzing the source. The class
   * reported is implementation specific.
   *
   * @param format
   *          the expected format of the data to parse
   * @param is
   *          the resource stream
   * @param documentUri
   *          the URI of the resource
   * @return the Metapath node item for the parsed data
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource
   */
  @NonNull
  IDocumentNodeItem loadAsNodeItem(
      @NonNull Format format,
      @NonNull InputStream is,
      @NonNull URI documentUri) throws IOException;

  /**
   * Get the configured Module binding context to use to load Java types.
   *
   * @return the binding context
   */
  @NonNull
  IBindingContext getBindingContext();

  /**
   * Auto convert the provided {@code source} to the provided {@code toFormat}.
   * Write the converted content to the provided {@code destination}.
   * <p>
   * The format of the source is expected to be auto detected using
   * {@link #detectFormat(Path)}.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param source
   *          the resource to convert
   * @param destination
   *          the resource to write converted content to
   * @param toFormat
   *          the format to convert to
   * @param rootClass
   *          the class for the Java type to load data into
   * @throws FileNotFoundException
   *           the the provided source file was not found
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource or writing the converted data to the specified destination
   */
  default <CLASS> void convert(
      @NonNull Path source,
      @NonNull Path destination,
      @NonNull Format toFormat,
      @NonNull Class<CLASS> rootClass) throws FileNotFoundException, IOException {
    CLASS object = load(rootClass, source);

    ISerializer<CLASS> serializer = getBindingContext().newSerializer(toFormat, rootClass);
    serializer.serialize(object, destination);
  }

  /**
   * Auto convert the provided {@code source} to the provided {@code toFormat}.
   * Write the converted content to the provided {@code destination}.
   * <p>
   * The format of the source is expected to be auto detected using
   * {@link #detectFormat(Path)}.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param source
   *          the resource to convert
   * @param os
   *          the output stream to write converted content to
   * @param toFormat
   *          the format to convert to
   * @param rootClass
   *          the class for the Java type to load data into
   * @throws FileNotFoundException
   *           the the provided source file was not found
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource or writing the converted data to the specified destination
   */
  default <CLASS> void convert(
      @NonNull Path source,
      @NonNull OutputStream os,
      @NonNull Format toFormat,
      @NonNull Class<CLASS> rootClass) throws FileNotFoundException, IOException {
    CLASS object = load(rootClass, source);

    ISerializer<CLASS> serializer = getBindingContext().newSerializer(toFormat, rootClass);
    serializer.serialize(object, os);
  }

  /**
   * Auto convert the provided {@code source} to the provided {@code toFormat}.
   * Write the converted content to the provided {@code destination}.
   * <p>
   * The format of the source is expected to be auto detected using
   * {@link #detectFormat(Path)}.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param source
   *          the resource to convert
   * @param destination
   *          the resource to write converted content to
   * @param toFormat
   *          the format to convert to
   * @param rootClass
   *          the class for the Java type to load data into
   * @throws FileNotFoundException
   *           the the provided source file was not found
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource or writing the converted data to the specified destination
   */
  default <CLASS> void convert(
      @NonNull URI source,
      @NonNull Path destination,
      @NonNull Format toFormat,
      @NonNull Class<CLASS> rootClass) throws FileNotFoundException, IOException {
    CLASS object = load(rootClass, source);

    ISerializer<CLASS> serializer = getBindingContext().newSerializer(toFormat, rootClass);
    serializer.serialize(object, destination);
  }

  /**
   * Auto convert the provided {@code source} to the provided {@code toFormat}.
   * Write the converted content to the provided {@code destination}.
   * <p>
   * The format of the source is expected to be auto detected using
   * {@link #detectFormat(Path)}.
   *
   * @param <CLASS>
   *          the Java type to load data into
   * @param source
   *          the resource to convert
   * @param os
   *          the output stream to write converted content to
   * @param toFormat
   *          the format to convert to
   * @param rootClass
   *          the class for the Java type to load data into
   * @throws FileNotFoundException
   *           the the provided source file was not found
   * @throws IOException
   *           if an error occurred while loading the data from the specified
   *           resource or writing the converted data to the specified destination
   */
  default <CLASS> void convert(
      @NonNull URI source,
      @NonNull OutputStream os,
      @NonNull Format toFormat,
      @NonNull Class<CLASS> rootClass) throws FileNotFoundException, IOException {
    CLASS object = load(rootClass, source);

    ISerializer<CLASS> serializer = getBindingContext().newSerializer(toFormat, rootClass);
    serializer.serialize(object, os);
  }
}
