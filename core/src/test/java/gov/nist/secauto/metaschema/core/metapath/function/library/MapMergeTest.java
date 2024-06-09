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

package gov.nist.secauto.metaschema.core.metapath.function.library;

import static gov.nist.secauto.metaschema.core.metapath.TestUtils.entry;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.map;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.sequence;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.metapath.ExpressionTestBase;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.LookupTest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class MapMergeTest
    extends ExpressionTestBase {
  private static Stream<Arguments> provideValues() { // NOPMD - false positive
    return Stream.of(
        Arguments.of(
            map(),
            "map:merge(())"),
        Arguments.of(
            map(entry(integer(0), string("no")), entry(integer(1), string("yes"))),
            "map:merge((map:entry(0, \"no\"), map:entry(1, \"yes\")))"),
        Arguments.of(
            map(
                entry(integer(0), string("Sonntag")),
                entry(integer(1), string("Montag")),
                entry(integer(2), string("Dienstag")),
                entry(integer(3), string("Mittwoch")),
                entry(integer(4), string("Donnerstag")),
                entry(integer(5), string("Freitag")),
                entry(integer(6), string("Samstag")),
                entry(integer(7), string("Unbekannt"))),
            "let $week :=  " + LookupTest.WEEKDAYS_GERMAN + " return map:merge(($week, map{7:\"Unbekannt\"}))"),
        Arguments.of(
            map(
                entry(integer(0), string("Sonntag")),
                entry(integer(1), string("Montag")),
                entry(integer(2), string("Dienstag")),
                entry(integer(3), string("Mittwoch")),
                entry(integer(4), string("Donnerstag")),
                entry(integer(5), string("Freitag")),
                entry(integer(6), string("Sonnabend"))),
            "let $week :=  " + LookupTest.WEEKDAYS_GERMAN
                + " return map:merge(($week, map{6:\"Sonnabend\"}), map{\"duplicates\":\"use-last\"})"),
        Arguments.of(
            map(
                entry(integer(0), string("Sonntag")),
                entry(integer(1), string("Montag")),
                entry(integer(2), string("Dienstag")),
                entry(integer(3), string("Mittwoch")),
                entry(integer(4), string("Donnerstag")),
                entry(integer(5), string("Freitag")),
                entry(integer(6), string("Samstag"))),
            "let $week :=  " + LookupTest.WEEKDAYS_GERMAN
                + " return map:merge(($week, map{6:\"Sonnabend\"}), map{\"duplicates\":\"use-first\"})"),
        Arguments.of(
            map(
                entry(integer(0), string("Sonntag")),
                entry(integer(1), string("Montag")),
                entry(integer(2), string("Dienstag")),
                entry(integer(3), string("Mittwoch")),
                entry(integer(4), string("Donnerstag")),
                entry(integer(5), string("Freitag")),
                entry(integer(6), sequence(string("Samstag"), string("Sonnabend")))),
            "let $week :=  " + LookupTest.WEEKDAYS_GERMAN
                + " return map:merge(($week, map{6:\"Sonnabend\"}), map{\"duplicates\":\"combine\"})"));
  }

  @ParameterizedTest
  @MethodSource("provideValues")
  void testExpression(@NonNull IItem expected, @NonNull String metapath) {

    IItem result = MetapathExpression.compile(metapath)
        .evaluateAs(null, MetapathExpression.ResultType.NODE, newDynamicContext());
    assertEquals(expected, result);
  }
}
