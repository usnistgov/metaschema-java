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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import static gov.nist.secauto.metaschema.model.common.metapath.TestUtils.decimal;
import static gov.nist.secauto.metaschema.model.common.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.model.common.metapath.TestUtils.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidValueForCastFunctionException;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class INumericItemTest {

  private static Stream<Arguments> provideValuesForAbs() {
    return Stream.of(
        Arguments.of(integer(10), integer(10)),
        Arguments.of(integer(-10), integer(10)),
        Arguments.of(decimal(10.5), decimal(10.5)),
        Arguments.of(decimal(-10.5), decimal(10.5)));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForAbs")
  void testAbs(INumericItem arg, INumericItem expected) {
    INumericItem result = arg.abs();
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideValuesForCeiling() {
    return Stream.of(
        Arguments.of(integer(10), integer(10)),
        Arguments.of(integer(-10), integer(-10)),
        Arguments.of(decimal(10.5), integer(11)),
        Arguments.of(decimal(-10.5), integer(-10)));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForCeiling")
  void testCeiling(@NotNull INumericItem arg, @NotNull INumericItem expected) {
    INumericItem result = arg.ceiling();
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideValuesForFloor() {
    return Stream.of(
        Arguments.of(integer(10), integer(10)),
        Arguments.of(integer(-10), integer(-10)),
        Arguments.of(decimal(10.5), integer(10)),
        Arguments.of(decimal(-10.5), integer(-11)));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForFloor")
  void testFloor(@NotNull INumericItem arg, @NotNull INumericItem expected) {
    INumericItem result = arg.floor();
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideValuesForRound() {
    return Stream.of(
        Arguments.of(integer(-100), integer(-3), integer(0)),
        Arguments.of(integer(-153), integer(-2), integer(-200)),
        Arguments.of(integer(-153), integer(-1), integer(-150)),
        Arguments.of(integer(654321), integer(-6), integer(0)),
        Arguments.of(integer(654321), integer(-5), integer(700000)),
        Arguments.of(integer(654321), integer(-4), integer(650000)),
        Arguments.of(integer(654321), integer(0), integer(654321)),
        Arguments.of(integer(654321), integer(2), integer(654321)),
        Arguments.of(decimal(2.5), integer(0), decimal(3.0)),
        Arguments.of(decimal(2.4999), integer(0), decimal(2.0)),
        Arguments.of(decimal(-2.5), integer(0), decimal(-2.0)),
        Arguments.of(decimal(1.125), integer(2), decimal("1.13")),
        Arguments.of(integer(8452), integer(-2), integer(8500)),
        Arguments.of(decimal("3.1415e0"), integer(2), decimal("3.14")),
        Arguments.of(decimal(35.425e0d), integer(2), decimal("35.42")));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForRound")
  void testRound(@NotNull INumericItem arg, @NotNull IIntegerItem precision, @NotNull INumericItem expected) {
    INumericItem result = arg.round(precision);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideValuesForCast() {
    return Stream.of(
        Arguments.of(integer(-100), integer(-100)),
        Arguments.of(integer(654321), integer(654321)),
        Arguments.of(decimal("2.4999"), decimal("2.4999")),
        Arguments.of(decimal("3.1415e0"), decimal("3.1415e0")),
        Arguments.of(string("-100"), decimal(-100)),
        Arguments.of(string("654321"), decimal(654321)),
        Arguments.of(string("2.5"), decimal(2.5)),
        Arguments.of(string("2.4999"), decimal("2.4999")),
        Arguments.of(string("-2.5"), decimal(-2.5)),
        Arguments.of(string("1.125"), decimal(1.125)),
        Arguments.of(string("3.1415e0"), decimal("3.1415e0")),
        Arguments.of(string("35.425e0"), decimal("35.425e0")));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForCast")
  void testCast(@NotNull IAnyAtomicItem item, @NotNull INumericItem expected) {
    INumericItem result = INumericItem.cast(item);
    assertEquals(expected, result);
  }


  private static Stream<Arguments> provideValuesForCastFail() {
    return Stream.of(
        Arguments.of(string("x123")),
        Arguments.of(string("abc")),
        Arguments.of(string("")));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForCastFail")
  void testCastFail(@NotNull IAnyAtomicItem item) {
    Assertions.assertThrows(InvalidValueForCastFunctionException.class, () -> {
      INumericItem.cast(item);
    });
  }
}
