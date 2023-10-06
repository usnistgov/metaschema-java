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

import static gov.nist.secauto.metaschema.core.metapath.TestUtils.bool;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.integer;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.string;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.uri;
import static gov.nist.secauto.metaschema.core.metapath.TestUtils.yearMonthDuration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidArgumentFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IUntypedAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class FnBooleanTest
    extends FunctionTestBase {
  static Stream<Arguments> provideValues() {
    return Stream.of(
        Arguments.of(null, new IItem[] { yearMonthDuration("P20Y") }),
        Arguments.of(bool(true), new IItem[] { IBooleanItem.TRUE }),
        Arguments.of(bool(false), new IItem[] { IBooleanItem.FALSE }),
        Arguments.of(bool(true), new IItem[] { string("non-blank") }),
        Arguments.of(bool(false), new IItem[] { string("") }),
        Arguments.of(bool(false), new IItem[] { IBooleanItem.TRUE, IBooleanItem.FALSE }),
        Arguments.of(bool(true), new IItem[] { integer(1) }),
        Arguments.of(bool(false), new IItem[] { integer(0) }),
        Arguments.of(bool(true), new IItem[] { integer(-1) }),
        Arguments.of(bool(true), new IItem[] { uri("path") }),
        Arguments.of(bool(false), new IItem[] { uri("") }),
        Arguments.of(bool(false), new IItem[] {}));
  }

  @ParameterizedTest
  @MethodSource("provideValues")
  void test(@Nullable IBooleanItem expected, @NonNull IItem... values) {
    List<IItem> valueList = ObjectUtils.notNull(Arrays.asList(values));
    try {
      assertFunctionResult(
          FnBoolean.SIGNATURE,
          ISequence.of(expected),
          List.of(ISequence.of(valueList)));
    } catch (MetapathException ex) {
      assertAll(
          () -> assertNull(expected),
          () -> assertInstanceOf(InvalidArgumentFunctionException.class, ex.getCause()));
    }
  }

  @Test
  void testNodeItem() {
    INodeItem item = getContext().mock(INodeItem.class, "nodeItem");
    assertFunctionResult(
        FnBoolean.SIGNATURE,
        ISequence.of(IBooleanItem.TRUE),
        List.of(ISequence.of(item)));
  }

  @Test
  void testUntypedAtomicItemBlank() {
    IUntypedAtomicItem item = getContext().mock(IUntypedAtomicItem.class, "untypedAtomicItem");
    assert item != null;

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(item).asString();
        will(returnValue(""));
      }
    });

    assertFunctionResult(
        FnBoolean.SIGNATURE,
        ISequence.of(IBooleanItem.FALSE),
        List.of(ISequence.of(item)));
  }

  @Test
  void testUntypedAtomicItemNonBlank() {
    IUntypedAtomicItem item = getContext().mock(IUntypedAtomicItem.class, "untypedAtomicItem");
    assert item != null;

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(item).asString();
        will(returnValue("non-blank"));
      }
    });

    assertFunctionResult(
        FnBoolean.SIGNATURE,
        ISequence.of(IBooleanItem.TRUE),
        List.of(ISequence.of(item)));
  }
}
