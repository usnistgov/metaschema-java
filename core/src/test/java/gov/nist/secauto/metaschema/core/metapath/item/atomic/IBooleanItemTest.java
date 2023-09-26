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

package gov.nist.secauto.metaschema.core.metapath.item.atomic;

import static gov.nist.secauto.metaschema.core.metapath.TestUtils.decimal;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class IBooleanItemTest {
  @Test
  void testValueOf() {
    Assertions.assertAll(
        () -> assertEquals(IBooleanItem.valueOf(true), IBooleanItem.TRUE),
        () -> assertEquals(IBooleanItem.valueOf(false), IBooleanItem.FALSE),
        () -> assertEquals(IBooleanItem.valueOf(ObjectUtils.notNull(Boolean.TRUE)), IBooleanItem.TRUE),
        () -> assertEquals(IBooleanItem.valueOf(ObjectUtils.notNull(Boolean.FALSE)), IBooleanItem.FALSE),
        () -> assertEquals(IBooleanItem.valueOf("1"), IBooleanItem.TRUE, "1"),
        () -> assertEquals(IBooleanItem.valueOf("0"), IBooleanItem.FALSE, "0"),
        () -> assertEquals(IBooleanItem.valueOf(""), IBooleanItem.FALSE, ""),
        () -> assertEquals(IBooleanItem.valueOf("true"), IBooleanItem.TRUE),
        () -> assertEquals(IBooleanItem.valueOf("false"), IBooleanItem.FALSE));
  }

  private static Stream<Arguments> provideValuesForCast() {
    return Stream.of(
        Arguments.of(IBooleanItem.TRUE, IBooleanItem.TRUE),
        Arguments.of(IBooleanItem.FALSE, IBooleanItem.FALSE),
        Arguments.of(integer(1), IBooleanItem.TRUE),
        Arguments.of(integer(0), IBooleanItem.FALSE),
        Arguments.of(decimal("1"), IBooleanItem.TRUE),
        Arguments.of(decimal("0"), IBooleanItem.FALSE),
        Arguments.of(string("1"), IBooleanItem.TRUE),
        Arguments.of(string("654321"), IBooleanItem.TRUE),
        Arguments.of(string("0"), IBooleanItem.FALSE),
        Arguments.of(string(""), IBooleanItem.FALSE),
        Arguments.of(string("true"), IBooleanItem.TRUE),
        Arguments.of(string("false"), IBooleanItem.FALSE));
  }

  @ParameterizedTest
  @MethodSource("provideValuesForCast")
  void testCast(@NonNull IAnyAtomicItem item, @NonNull IBooleanItem expected) {
    IBooleanItem result = IBooleanItem.cast(item);
    assertEquals(expected, result);
  }

}
