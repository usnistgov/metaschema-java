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

package gov.nist.secauto.metaschema.model.common.metapath.item.atomic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IBase64BinaryItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class IBase64BinaryItemTest {
  private static final long MIN_LONG = -9_223_372_036_854_775_808L;
  private static final long MAX_LONG = 9_223_372_036_854_775_807L;
  private static final String BASE_64 = "gAAAAAAAAAB//////////w==";

  @Test
  void testValueOf() {
    IBase64BinaryItem item = IBase64BinaryItem.valueOf(ObjectUtils.notNull(
        ByteBuffer.allocate(16).putLong(MIN_LONG).putLong(MAX_LONG)));
    assertEquals(BASE_64, item.asString());
  }

  @Test
  void testCastSame() {
    ByteBuffer buf
        = ObjectUtils.notNull(ByteBuffer.allocate(16).putLong(MIN_LONG).putLong(MAX_LONG));
    IBase64BinaryItem item = IBase64BinaryItem.valueOf(buf);
    assertEquals(IBase64BinaryItem.cast(item), item);
  }

  @Test
  void testCastString() {
    ByteBuffer buf
        = ObjectUtils.notNull(ByteBuffer.allocate(16).putLong(MIN_LONG).putLong(MAX_LONG));
    IBase64BinaryItem expected = IBase64BinaryItem.valueOf(buf);
    IBase64BinaryItem actual = IBase64BinaryItem.cast(IStringItem.valueOf(BASE_64));
    Assertions.assertAll(
        () -> assertArrayEquals(actual.getValue().array(), expected.getValue().array()),
        () -> assertEquals(actual.asString(), expected.asString()));
  }
}
