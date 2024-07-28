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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class UriUtilsTest {
  private static final boolean VALID = true;
  private static final boolean INVALID = false;

  private static Stream<Arguments> provideValuesTestToUri() {
    return Stream.of(
        Arguments.of("http://example.org/valid", VALID),
        Arguments.of("https://example.org/valid", VALID),
        Arguments.of("http://example.org/valid", VALID),
        Arguments.of("ftp://example.org/valid", VALID),
        Arguments.of("ssh://example.org/valid", VALID),
        Arguments.of("example.org/good", VALID),
        Arguments.of("bad.txt", VALID),
        Arguments.of("relative\\windows\\path\\resource.txt", VALID),
        Arguments.of("C:\\absolute\\valid.txt", VALID),
        Arguments.of("local/relative/path/is/invalid.txt", VALID),
        Arguments.of("/absolute/local/path/is/invalid.txt", VALID),
        Arguments.of("1;", VALID));
  }

  @ParameterizedTest
  @MethodSource("provideValuesTestToUri")
  void testToUri(@NonNull String location, boolean expectedResult) throws URISyntaxException {
    boolean result = INVALID;
    Path cwd = Paths.get("");
    URI uri = UriUtils.toUri(location, cwd.toAbsolutePath().toUri());
    result = VALID;
    // System.out.println(String.format("%s -> %s", location, uri.toASCIIString()));
    assertEquals(result, expectedResult);
  }

  private static Stream<Arguments> provideArgumentsTestRelativize() {
    return Stream.of(
        Arguments.of(
            "http://example.com/this/file1.txt",
            "http://example.com/this/file2.txt",
            true,
            "file2.txt"),
        Arguments.of(
            "http://example.com/this",
            "http://example.com/this/that",
            true,
            "that"),
        Arguments.of(
            "http://example.com/this/",
            "http://example.com/this/that",
            true,
            "that"),
        Arguments.of(
            "http://example.com/this/that",
            "http://example.com/this/new",
            true,
            "new"),
        Arguments.of(
            "http://example.com/this/that/A",
            "http://example.com/this/new/B",
            true,
            "../new/B"),
        Arguments.of(
            "http://example.com/this/that/",
            "http://example.com/this/new/",
            true,
            "../new/"),
        Arguments.of(
            "http://example.com/this/that/A/",
            "http://example.com/this/new/B",
            true,
            "../../new/B"),
        Arguments.of(
            "http://example.com/this/that/A/X/file1,text",
            "http://example.com/this/that/A/file2.txt",
            true,
            "../file2.txt"),
        Arguments.of(
            "http://example.com/this/that/A/",
            "http://example.org/this/new/B",
            true,
            "http://example.org/this/new/B"));
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsTestRelativize")
  void testRelativize(@NonNull String uri1, @NonNull String uri2, boolean prepend, @NonNull String expected)
      throws URISyntaxException {
    URI thisUri = URI.create(uri1);
    URI thatUri = URI.create(uri2);

    URI result = UriUtils.relativize(thisUri, thatUri, prepend);
    assertEquals(expected, result.toASCIIString());
  }
}
