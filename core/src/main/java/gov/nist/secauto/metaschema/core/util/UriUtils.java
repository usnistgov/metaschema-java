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
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class UriUtils {
  private static final Pattern URI_SEPERATOR_PATTERN = Pattern.compile("\\/");
  private static final String URI_SEPERATOR = "/";

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
  @SuppressWarnings("PMD.PreserveStackTrace")
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

  /**
   * This function extends the functionality of {@link URI#relativize(URI)} by
   * supporting relative reference pathing (e.g., ..), when the {@code prepend}
   * parameter is set to {@code true}.
   *
   * @param base
   *          the URI to relativize against
   * @param other
   *          the URI to make relative
   * @param prepend
   *          if {@code true}, then prepend relative pathing
   * @return a new relative URI
   * @throws URISyntaxException
   *           if any of the URIs are malformed
   */
  public static URI relativize(URI base, URI other, boolean prepend) throws URISyntaxException {
    URI normBase = Objects.requireNonNull(base).normalize();
    URI normOther = Objects.requireNonNull(other).normalize();
    URI retval = normBase.relativize(normOther);

    if (prepend && !normBase.isOpaque() && !retval.isOpaque() && hasSameSchemeAndAuthority(normBase, retval)) {
      // the URIs are not opaque and they share the same scheme and authority
      String basePath = normBase.getPath();
      String targetPath = normOther.getPath();
      String newPath = prependRelativePath(basePath, targetPath);

      retval = new URI(null, null, newPath, normOther.getQuery(), normOther.getFragment());
    }

    return retval;
  }

  private static boolean hasSameSchemeAndAuthority(URI base, URI other) {
    String baseScheme = base.getScheme();
    boolean retval = (baseScheme == null && other.getScheme() == null)
        || (baseScheme != null && baseScheme.equals(other.getScheme()));
    String baseAuthority = base.getAuthority();
    retval = retval && ((baseAuthority == null && other.getAuthority() == null)
        || (baseAuthority != null && baseAuthority.equals(other.getAuthority())));
    return retval;
  }

  /**
   * Based on code from
   * http://stackoverflow.com/questions/10801283/get-relative-path-of-two-uris-in-java
   *
   * @param base
   *          the base path to resolve against
   * @param target
   *          the URI to relativize against the base
   * @return the relativized URI
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static String prependRelativePath(String base, String target) {

    // Split paths into segments
    String[] baseSegments = URI_SEPERATOR_PATTERN.split(base);
    String[] targetSegments = URI_SEPERATOR_PATTERN.split(target, -1);

    // Discard trailing segment of base path, since this resource doesn't matter
    if (baseSegments.length > 0 && !base.endsWith(URI_SEPERATOR)) {
      baseSegments = Arrays.copyOf(baseSegments, baseSegments.length - 1);
    }

    // Remove common prefix segments
    int segmentIndex = 0;
    while (segmentIndex < baseSegments.length && segmentIndex < targetSegments.length
        && baseSegments[segmentIndex].equals(targetSegments[segmentIndex])) {
      segmentIndex++;
    }

    // Construct the relative path
    StringBuilder retval = new StringBuilder();
    for (int j = 0; j < (baseSegments.length - segmentIndex); j++) {
      retval.append("..");
      if (retval.length() != 0) {
        retval.append(URI_SEPERATOR);
      }
    }

    for (int j = segmentIndex; j < targetSegments.length; j++) {
      retval.append(targetSegments[j]);
      if (retval.length() != 0 && j < targetSegments.length - 1) {
        retval.append(URI_SEPERATOR);
      }
    }
    return retval.toString();
  }
}
