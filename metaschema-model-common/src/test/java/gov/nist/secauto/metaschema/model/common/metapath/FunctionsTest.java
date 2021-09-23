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

package gov.nist.secauto.metaschema.model.common.metapath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.model.common.metapath.function.Functions;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.ItemFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.stream.Stream;

class FunctionsTest {
  private static IDecimalItem decimal(String value) {
    return IDecimalItem.valueOf(new BigDecimal(value,MathContext.DECIMAL64));
  }
  private static IDecimalItem decimal(double value) {
    return IDecimalItem.valueOf(new BigDecimal(value,MathContext.DECIMAL64));
  }
  private static IIntegerItem integer(int value) {
    return IIntegerItem.valueOf(BigInteger.valueOf(value));
  }

  private static Stream<Arguments> provideValuesForIntegerDivide() {
    return Stream.of(
        Arguments.of(integer(10), integer(3), integer(3)),
        Arguments.of(integer(3), integer(-2), integer(-1)),
        Arguments.of(integer(-3), integer(2), integer(-1)),
        Arguments.of(integer(-3), integer(-2), integer(1)),
        Arguments.of(decimal("9.0"), integer(3), integer(3)),
        Arguments.of(decimal("-3.5"), integer(3), integer(-1)),
        Arguments.of(decimal("3.0"), integer(4), integer(0)),
        Arguments.of(decimal("3.1E1"), integer(6), integer(5)),
        Arguments.of(decimal("3.1E1"), integer(7), integer(4)));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForIntegerDivide")
  void testIntegerDivide(INumericItem dividend, INumericItem divisor, IIntegerItem expected) {
    INumericItem result = Functions.opNumericIntegerDivide(dividend, divisor);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideValuesForMod() {
    return Stream.of(
        Arguments.of(integer(5), integer(3), decimal(2)),
        Arguments.of(integer(6), integer(-2), decimal(0)),
        Arguments.of(decimal("4.5"), decimal("1.2"), decimal("0.9")),
        Arguments.of(integer(123), integer(6), decimal(3)));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForMod")
  void test(INumericItem dividend, INumericItem divisor, INumericItem expected) {
    INumericItem result = Functions.opNumericMod(dividend, divisor);
    assertEquals(expected, result);
  }

}
