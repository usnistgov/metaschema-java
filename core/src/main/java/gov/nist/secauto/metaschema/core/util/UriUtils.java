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

package gov.nist.secauto.metaschema.core.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class UriUtils {

  private UriUtils() {
    // disable construction
  }

  /**
   * Process a string to a local file path or remote location. If the location is
   * convertible to a URI, return the {@link URI}. Normalize the resulting URI
   * with the base URI, if provided.
   *
   * @param location
   *          a string defining a remote or local file-based location
   * @param baseUri
   *          the base URI to use for URI normalization
   * @return a new URI
   * @throws URISyntaxException
   *           an error if the location string is not convertible to URI
   */
  public static URI toUri(@NonNull String location, @NonNull URI baseUri) throws URISyntaxException {
    URI asUri;
    try {
      asUri = new URI(location);
    } catch (URISyntaxException ex) {
      // the location is not a valid URI
      try {
        // try to parse the location as a local file path
        Path path = Paths.get(location);
        asUri = path.toUri();
      } catch (InvalidPathException ex2) {
        // not a local file path, so rethrow the original URI expection
        throw ex;
      }
    }
    return baseUri.resolve(asUri.normalize());
  }
}
