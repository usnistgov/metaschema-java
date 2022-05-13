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

import gov.nist.secauto.metaschema.binding.DefaultBindingContext;
import gov.nist.secauto.metaschema.model.common.metapath.IDocumentLoader;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A common interface for loading Metaschema based instance resources.
 */
public interface IBoundLoader extends IDocumentLoader, IMutableConfiguration {
  /**
   * Determine the format of the provided resource.
   * 
   * @param url
   *          the resource
   * @return the format of the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   * @throws URISyntaxException
   */
  @NotNull
  default Format detectFormat(@NotNull URL url) throws IOException, URISyntaxException {
    return detectFormat(toInputSource(ObjectUtils.notNull(url.toURI())));
  }

  /**
   * Determine the format of the provided resource.
   * 
   * @param path
   *          the resource
   * @return the format of the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NotNull
  default Format detectFormat(@NotNull Path path) throws IOException {
    return detectFormat(toInputSource(ObjectUtils.notNull(path.toUri())));
  }

  /**
   * Determine the format of the provided resource.
   * 
   * @param file
   *          the resource
   * @return the format of the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NotNull
  default Format detectFormat(@NotNull File file) throws IOException {
    return detectFormat(ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Determine the format of the provided resource.
   * <p>
   * This method will consume data from any {@link InputStream} provided by the {@link InputSource}.
   * If the caller of this method intends to read data from the stream after determining the format,
   * the caller should pass in a stream that can be reset.
   * <p>
   * This method will not close any {@link InputStream} provided by the {@link InputSource}, since it
   * does not own the stream.
   * 
   * @param source
   *          information about how to access the resource
   * @return the format of the provided resource
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NotNull
  Format detectFormat(@NotNull InputSource source) throws IOException;

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
   * @see #detectFormat(URL)
   */
  @NotNull
  default <CLASS> CLASS load(@NotNull URL url) throws IOException, URISyntaxException {
    return loadAsNodeItem(url).toBoundObject();
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
  @NotNull
  default <CLASS> CLASS load(@NotNull Path path) throws IOException {
    return loadAsNodeItem(path).toBoundObject();
  }

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
  @NotNull
  default <CLASS> CLASS load(@NotNull File file) throws IOException {
    return loadAsNodeItem(file).toBoundObject();
  }

  /**
   * Load data from the provided resource into a bound object.
   * <p>
   * This method will auto-detect the format of the provided resource.
   * <p>
   * This method will not close any {@link InputStream} provided by the {@link InputSource}, since it
   * does not own the stream.
   * 
   * @param <CLASS>
   *          the type of the bound object to return
   * @param source
   *          information about how to access the resource
   * @return a bound object containing the loaded data
   * @throws IOException
   *           if an error occurred while reading the resource
   * @see #detectFormat(InputSource)
   */
  @NotNull
  default <CLASS> CLASS load(@NotNull InputSource source) throws IOException {
    return loadAsNodeItem(source).toBoundObject();
  }

  /**
   * Load data from the specified resource into a bound object with the type of the specified Java
   * class.
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
  @NotNull
  default <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull Path path) throws IOException {
    try (InputStream is = Files.newInputStream(path)) {
      return load(clazz, toInputSource(ObjectUtils.notNull(path.toUri())));
    }
  }

  /**
   * Load data from the specified resource into a bound object with the type of the specified Java
   * class.
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
  @NotNull
  default <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull File file) throws IOException {
    return load(clazz, ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Load data from the specified resource into a bound object with the type of the specified Java
   * class.
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
   */
  @NotNull
  default <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull URL url) throws IOException, URISyntaxException {
    return load(clazz, toInputSource(ObjectUtils.notNull(url.toURI())));
  }

  /**
   * Load data from the specified resource into a bound object with the type of the specified Java
   * class.
   * <p>
   * This method will not close the provided {@link InputStream}, since it does not own the stream.
   * <p>
   * Implementations of this method will do format detection. This process might leave the provided
   * {@link InputStream} at a position beyond the last parsed location. If you want to avoid this
   * possibility, use and implementation of {@link IDeserializer#deserialize(InputStream, URI)}
   * instead, such as what is provided by
   * {@link DefaultBindingContext#newDeserializer(Format, Class)}.
   * 
   * @param <CLASS>
   *          the Java type to load data into
   * @param clazz
   *          the class for the java type
   * @param source
   *          information about how to access the resource
   * @return the loaded instance data
   * @throws IOException
   *           if an error occurred while loading the data in the specified file
   */
  @NotNull
  <CLASS> CLASS load(@NotNull Class<CLASS> clazz, @NotNull InputSource source) throws IOException;
}
