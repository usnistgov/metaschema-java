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

import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Implementations of this interface are able to read structured data into a bound object instance
 * of the parameterized type.
 * 
 * @param <CLASS>
 *          the Java type into which data can be read
 */
public interface IDeserializer<CLASS> extends IMutableConfiguration {
  default boolean isValidating() {
    return isFeatureEnabled(Feature.DESERIALIZE_VALIDATE);
  }

  /**
   * Read data from the {@link InputStream} into a bound class instance.
   * 
   * @param is
   *          the input stream to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return the instance data
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  default CLASS deserialize(InputStream is, @Nullable URI documentUri) throws BindingException {
    return deserialize(new InputStreamReader(is), documentUri);
  }

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
  default CLASS deserialize(File file) throws FileNotFoundException, BindingException {
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"))) {
      CLASS retval = deserialize(reader, file.toURI());
      reader.close();
      return retval;
    } catch (IOException ex) {
      throw new BindingException("Unable to open file: " + file.getPath(), ex);
    }
  }

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
  default CLASS deserialize(URL url) throws BindingException {
    try (InputStream in = url.openStream()) {
      CLASS retval = deserialize(in, url.toURI());
      in.close();
      return retval;
    } catch (IOException ex) {
      throw new BindingException("Unable to open url: " + url.toString(), ex);
    } catch (URISyntaxException ex) {
      throw new BindingException(ex);
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
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  default CLASS deserialize(Reader reader, @Nullable URI documentUri) throws BindingException {
    IBoundXdmNodeItem nodeItem = deserializeToNodeItem(reader, documentUri);
    return nodeItem.toBoundObject();
  }

  /**
   * Read data from the {@link Reader} into a node item instance.
   * 
   * @param is
   *          the input stream to read from
   * @param documentUri
   *          the URI of the document to read from
   * @return a new node item
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  default IBoundXdmNodeItem deserializeToNodeItem(InputStream is, @Nullable URI documentUri) throws BindingException {
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
   * @throws BindingException
   *           if an error occurred while reading data from the stream
   */
  IBoundXdmNodeItem deserializeToNodeItem(Reader reader, @Nullable URI documentUri) throws BindingException;
}
