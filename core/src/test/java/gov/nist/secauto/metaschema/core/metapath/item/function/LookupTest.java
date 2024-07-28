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

package gov.nist.secauto.metaschema.core.metapath.item.function;

import static gov.nist.secauto.metaschema.core.metapath.TestUtils.array;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.entry;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.map;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.sequence;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.nist.secauto.metaschema.core.metapath.ExpressionTestBase;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class LookupTest
    extends ExpressionTestBase {

  public static final String WEEKDAYS = "map { \"Su\" : \"Sunday\",\"Mo\" : \"Monday\", \"Tu\" : \"Tuesday\","
      + " \"We\" : \"Wednesday\", \"Th\" : \"Thursday\", \"Fr\" : \"Friday\", \"Sa\" : \"Saturday\"}";
  public static final String WEEKDAYS_GERMAN = "map{0:\"Sonntag\", 1:\"Montag\", 2:\"Dienstag\", 3:\"Mittwoch\","
      + " 4:\"Donnerstag\", 5:\"Freitag\", 6:\"Samstag\"}";
  private static final String BOOKS = "map {" +
      "\"book\": map {" +
      "    \"title\": \"Data on the Web\"," +
      "    \"year\": 2000," +
      "    \"author\": [" +
      "        map {" +
      "            \"last\": \"Abiteboul\"," +
      "            \"first\": \"Serge\"" +
      "        }," +
      "        map {" +
      "            \"last\": \"Buneman\"," +
      "            \"first\": \"Peter\"" +
      "        }," +
      "        map {" +
      "            \"last\": \"Suciu\"," +
      "            \"first\": \"Dan\"" +
      "        }" +
      "    ]," +
      "    \"publisher\": \"Morgan Kaufmann Publishers\"," +
      "    \"price\": 39.95" +
      "}" +
      "}";

  private static Stream<Arguments> functionCallLookupValues() { // NOPMD - false positive
    return Stream.of(
        // function call lookup
        Arguments.of(
            sequence(integer(7)),
            "[ 1, 2, 5, 7 ](4)"),
        Arguments.of(
            sequence(array(integer(4), integer(5), integer(6))),
            "[ [1, 2, 3], [4, 5, 6]](2)"),
        Arguments.of(
            sequence(integer(5)),
            "[ [1, 2, 3], [4, 5, 6]](2)(2)"),
        Arguments.of(
            sequence(string("Robert Johnson")),
            "[ 'a', 123, \"Robert Johnson\" ](3)"),
        Arguments.of(
            sequence(integer(27)),
            "array { (), (27, 17, 0) }(1)"),
        Arguments.of(
            sequence(integer(7)),
            "[ 1, 2, 5, 7 ](4)"),
        Arguments.of(
            sequence(string("Donnerstag")),
            "let $week :=  " + WEEKDAYS_GERMAN + " return $week(4)"),
        Arguments.of(
            sequence(string("Sunday")),
            "let $weekdays := " + WEEKDAYS + " return $weekdays(\"Su\")"),
        Arguments.of(
            sequence(string("Data on the Web")),
            "let $b := " + BOOKS + " return $b(\"book\")(\"title\")"),
        Arguments.of(
            sequence(array(
                map(
                    entry(string("last"), string("Abiteboul")),
                    entry(string("first"), string("Serge"))),
                map(
                    entry(string("last"), string("Buneman")),
                    entry(string("first"), string("Peter"))),
                map(
                    entry(string("last"), string("Suciu")),
                    entry(string("first"), string("Dan"))))),
            "let $b := " + BOOKS + " return $b(\"book\")(\"author\")"),
        Arguments.of(
            sequence(string("Abiteboul")),
            "let $b := " + BOOKS + " return $b(\"book\")(\"author\")(1)(\"last\")"));
  }

  @ParameterizedTest
  @MethodSource("functionCallLookupValues")
  void testFunctionCallLookup(@NonNull ISequence<?> expected, @NonNull String metapath) {
    ISequence<?> result = MetapathExpression.compile(metapath)
        .evaluateAs(null, MetapathExpression.ResultType.SEQUENCE, newDynamicContext());
    assertEquals(expected, result);
  }

  private static Stream<Arguments> postfixLookupValues() { // NOPMD - false positive
    return Stream.of(
        // postfix lookup
        Arguments.of(
            sequence(string("Jenna")),
            "map { \"first\" : \"Jenna\", \"last\" : \"Scott\" }?first"),
        Arguments.of(
            sequence(integer(5)),
            "[4, 5, 6]?2"),
        Arguments.of(
            sequence(string("Tom"), string("Dick"), string("Harry")),
            "(map {\"first\": \"Tom\"}, map {\"first\": \"Dick\"}, map {\"first\":\"Harry\"})?first"),
        Arguments.of(
            sequence(string("Donnerstag")),
            "let $week :=  " + WEEKDAYS_GERMAN + " return $week?4"),
        Arguments.of(
            sequence(integer(2), integer(5)),
            "([1,2,3], [4,5,6])?2"),
        Arguments.of(
            sequence(integer(1), integer(2), integer(5), integer(7)),
            "[1, 2, 5, 7]?*"),
        Arguments.of(
            sequence(array(integer(1), integer(2), integer(3)), array(integer(4), integer(5), integer(6))),
            "[[1, 2, 3], [4, 5, 6]]?*"));
  }

  @ParameterizedTest
  @MethodSource("postfixLookupValues")
  void testPostfixLookup(@NonNull ISequence<?> expected, @NonNull String metapath) {
    ISequence<?> result = MetapathExpression.compile(metapath)
        .evaluateAs(null, MetapathExpression.ResultType.SEQUENCE, newDynamicContext());
    assertEquals(expected, result);
  }

  @Test
  void testUnaryLookupMissingMember() {
    ArrayException thrown = assertThrows(
        ArrayException.class,
        () -> {
          ISequence<?> result = MetapathExpression.compile("([1,2,3], [1,2,5], [1,2])[?3 = 5]")
              .evaluateAs(null, MetapathExpression.ResultType.SEQUENCE, newDynamicContext());
          assertNotNull(result);
          result.safeStream();
        });
    assertEquals(ArrayException.INDEX_OUT_OF_BOUNDS, thrown.getCode());
  }
}
