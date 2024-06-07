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

package gov.nist.secauto.metaschema.core.metapath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adelean.inject.resources.junit.jupiter.GivenTextResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;

import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@TestWithResources
class MetapathExpressionTest {

  @GivenTextResource(from = "/correct-examples.txt", charset = "UTF-8")
  String correctMetapathInstances;

  // @GivenTextResource(from = "/incorrect-examples.txt", charset = "UTF-8")
  // String incorrectMetapathInstances;

  @Test
  @Disabled
  void testCorrect() {
    for (String line : correctMetapathInstances.split("\\r?\\n")) {
      if (line.startsWith("# ")) {
        continue;
      }
      // System.out.println(line);
      MetapathExpression.compile(line);
    }
  }
  //
  // @Test
  // @Disabled
  // void testIncorrect() {
  // for (String line : incorrectMetapathInstances.split("\\r?\\n")) {
  // if (line.startsWith("# ")) {
  // continue;
  // }
  // // System.out.println(line);
  // try {
  // MetapathExpression.compile(line);
  // } catch (ParseCancellationException ex) {
  // // ex.printStackTrace();
  // }
  // }
  // }

  @Test
  void testSyntaxError() {
    assertThrows(MetapathException.class, () -> {
      MetapathExpression.compile("**");
    });
  }

  @Test
  void test() {
    MetapathExpression path = MetapathExpression.compile("2 eq 1 + 1");
    ISequence<?> result = path.evaluate(null);
    assertNotNull(result, "null result");
    assertTrue(!result.isEmpty(), "result was empty");
    assertEquals(1, result.size(), "unexpected size");
    assertEquals(true, ((IBooleanItem) result.getValue().iterator().next()).toBoolean(), "unexpected result");
  }
}
