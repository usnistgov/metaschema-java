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

import static gov.nist.secauto.metaschema.core.metapath.TestUtils.dayTimeDuration;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.decimal;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.string;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.yearMonthDuration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidArgumentFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IDayTimeDurationItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IYearMonthDurationItem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class FnAvgTest
    extends FunctionTestBase {

  private static Stream<Arguments> provideValuesForAvg() {
    IYearMonthDurationItem yearMonth1 = yearMonthDuration("P20Y");
    IYearMonthDurationItem yearMonth2 = yearMonthDuration("P10M");
    IDayTimeDurationItem dayTime1 = dayTimeDuration("P1DT12H");
    IDayTimeDurationItem dayTime2 = dayTimeDuration("P2D");

    return Stream.of(
        Arguments.of(decimal("4"), new IAnyAtomicItem[] { integer(3), integer(4), integer(5) }),
        Arguments.of(null, new IAnyAtomicItem[] { integer(3), integer(4), string("test") }),
        Arguments.of(dayTimeDuration("P1DT18H"), new IAnyAtomicItem[] { dayTime1, dayTime2 }),
        Arguments.of(null, new IAnyAtomicItem[] { dayTime1, dayTime2, integer(1) }),
        Arguments.of(yearMonthDuration("P10Y5M"), new IAnyAtomicItem[] { yearMonth1, yearMonth2 }),
        Arguments.of(null, new IAnyAtomicItem[] { yearMonth1, yearMonth2, integer(1) }));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForAvg")
  void testAvg(@Nullable IAnyAtomicItem expected, @NonNull IAnyAtomicItem... values) {
    try {
      assertFunctionResult(
          FnAvg.SIGNATURE,
          ISequence.of(expected),
          List.of(ISequence.of(values)));
    } catch (MetapathException ex) {
      if (expected == null) {
        assertAll(
            () -> assertInstanceOf(InvalidArgumentFunctionException.class, ex.getCause()));
      } else {
        throw ex;
      }
    }
  }

  @Test
  void testAvgNoOp() {
    assertFunctionResult(
        FnAvg.SIGNATURE,
        ISequence.empty(),
        List.of(ISequence.empty()));
  }
}
