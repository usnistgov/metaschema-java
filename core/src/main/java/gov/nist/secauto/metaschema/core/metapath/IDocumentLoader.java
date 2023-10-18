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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.model.IResourceResolver;
import gov.nist.secauto.metaschema.core.model.IUriResolver;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Supports loading documents referenced in Metapath expressions.
 */
public interface IDocumentLoader extends IResourceResolver {
  /**
   * Allows setting an {@link IUriResolver}, which will be used to map URIs prior
   * to loading the resource.
   *
   * @param resolver
   *          the resolver to set
   */
  void setUriResolver(@NonNull IUriResolver resolver);

  /**
   * Load a Metaschema-based document from a file resource.
   *
   * @param file
   *          the file to load
   * @return a document item representing the contents of the document.
   * @throws IOException
   *           if an error occurred while parsing the file
   */
  @NonNull
  default IDocumentNodeItem loadAsNodeItem(@NonNull File file) throws IOException {
    return loadAsNodeItem(ObjectUtils.notNull(file.toPath()));
  }

  /**
   * Load a Metaschema-based document from a file resource identified by a path.
   *
   * @param path
   *          the file to load
   * @return a document item representing the contents of the document.
   * @throws IOException
   *           if an error occurred while parsing the file
   */
  @NonNull
  default IDocumentNodeItem loadAsNodeItem(@NonNull Path path) throws IOException {
    return loadAsNodeItem(ObjectUtils.notNull(path.toUri()));
  }

  /**
   * Load a Metaschema-based document from a URL resource.
   *
   * @param url
   *          the resource to load
   * @return a document item representing the contents of the document.
   * @throws IOException
   *           if an error occurred while parsing the resource
   * @throws URISyntaxException
   *           if the URL is not a valid URI
   */
  @NonNull
  default IDocumentNodeItem loadAsNodeItem(@NonNull URL url) throws IOException, URISyntaxException {
    return loadAsNodeItem(ObjectUtils.notNull(url.toURI()));
  }

  /**
   * Load a Metaschema-based document from a URI resource.
   * <p>
   * This is the expected, primary entry point for implementations.
   *
   * @param uri
   *          the resource to load
   * @return a document item representing the contents of the document.
   * @throws IOException
   *           if an error occurred while parsing the resource
   */
  @NonNull
  IDocumentNodeItem loadAsNodeItem(@NonNull URI uri) throws IOException;
}
