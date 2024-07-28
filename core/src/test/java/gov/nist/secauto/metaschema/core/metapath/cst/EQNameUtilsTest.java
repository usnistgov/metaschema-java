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

package gov.nist.secauto.metaschema.core.metapath.cst;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.metapath.EQNameUtils;
import gov.nist.secauto.metaschema.core.metapath.EQNameUtils.IEQNamePrefixResolver;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

class EQNameUtilsTest {
  private static final StaticContext STATIC_CONTEXT = StaticContext.builder()
      .namespace("prefix", "http://example.com/ns/prefix")
      .defaultFunctionNamespace("http://example.com/ns/function")
      .defaultModelNamespace("http://example.com/ns/model")
      .build();

  static Stream<Arguments> provideValues() {
    return Stream.of(
        Arguments.of(
            "prefix:local-name",
            new QName("http://example.com/ns/prefix", "local-name"),
            STATIC_CONTEXT.getFunctionPrefixResolver()),
        Arguments.of(
            "prefix:local-name",
            new QName("http://example.com/ns/prefix", "local-name"),
            STATIC_CONTEXT.getFlagPrefixResolver()),
        Arguments.of(
            "prefix:local-name",
            new QName("http://example.com/ns/prefix", "local-name"),
            STATIC_CONTEXT.getVariablePrefixResolver()),
        Arguments.of(
            "prefix:local-name",
            new QName("http://example.com/ns/prefix", "local-name"),
            STATIC_CONTEXT.getModelPrefixResolver()),
        Arguments.of(
            "local-name",
            new QName("http://example.com/ns/function", "local-name"),
            STATIC_CONTEXT.getFunctionPrefixResolver()),
        Arguments.of(
            "local-name",
            new QName("local-name"),
            STATIC_CONTEXT.getFlagPrefixResolver()),
        Arguments.of(
            "local-name",
            new QName("local-name"),
            STATIC_CONTEXT.getVariablePrefixResolver()),
        Arguments.of(
            "local-name",
            new QName("http://example.com/ns/model", "local-name"),
            STATIC_CONTEXT.getModelPrefixResolver()),
        Arguments.of("Q{http://example.com/ns}local-name",
            new QName("http://example.com/ns", "local-name"),
            STATIC_CONTEXT.getFunctionPrefixResolver()),
        Arguments.of("Q{http://example.com/ns}local-name",
            new QName("http://example.com/ns", "local-name"),
            STATIC_CONTEXT.getFlagPrefixResolver()),
        Arguments.of("Q{http://example.com/ns}local-name",
            new QName("http://example.com/ns", "local-name"),
            STATIC_CONTEXT.getVariablePrefixResolver()),
        Arguments.of("Q{http://example.com/ns}local-name",
            new QName("http://example.com/ns", "local-name"),
            STATIC_CONTEXT.getModelPrefixResolver()));
  }

  @ParameterizedTest
  @MethodSource("provideValues")
  void test(
      @NonNull String eqname,
      @NonNull QName expected,
      @NonNull IEQNamePrefixResolver resolver) {

    QName actual = EQNameUtils.parseName(eqname, resolver);
    assertEquals(expected, actual);
  }
}
