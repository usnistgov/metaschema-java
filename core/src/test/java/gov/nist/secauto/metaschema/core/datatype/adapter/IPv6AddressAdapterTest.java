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

package gov.nist.secauto.metaschema.core.datatype.adapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
class IPv6AddressAdapterTest {
  @ParameterizedTest
  @ValueSource(strings = {
      // Disallow empty
      "",
      // Disallow IPv4
      "127.0.0.1",
      // Disallow binary notation
      "10000000000001:110110111000:1000010110100011:0:0:1000101000101110:1101110000:1110",
      // Disallow wildcard separator
      "2001:0000:4136:\\*:\\*:\\*:\\*:\\*",
      // Disallow prefixes beyond address size
      "baba:baba:baba:baba:baba:baba:baba:/64"
  })
  void testIPv6AddressThrowsWithInvalid(@NonNull String addr) {
    assertThrows(IllegalArgumentException.class, () -> {
      new IPv6AddressAdapter().parse(addr);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "::",
      "::1",
      "::/128",
      "::1/128",
      "fe80::/64",
      "::ffff:192.0.2.47",
      "fdf8:f53b:82e4::53",
      "fe80::200:5aee:feaa:20a2",
      "2001:10:240:ab::a",
      "2001:0000:4136:e378:8000:63bf:3fff:fdd2",
      // Regression test for usnistgov/metaschema-java#156
      "2001:0000:0000:0000:0000:ffff:0a02:0202",
  })
  void testIPv6AddressAllowsCommonIPv6Addresses(@NonNull String addr) {
    assertDoesNotThrow(() -> {
      new IPv6AddressAdapter().parse(addr);
    });
  }
}
