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

package gov.nist.secauto.metaschema.core.model.util;

import gov.nist.secauto.metaschema.core.model.xml.XmlObjectParser;
import gov.nist.secauto.metaschema.core.model.xml.XmlObjectParser.Handler;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

class XmlObjectParserTest {
  private static final String TEST_NS = "https://example.com/ns/test";

  @Test
  void test() {
    XmlObject obj = XmlObject.Factory.newInstance();
    try (XmlCursor cursor = obj.newCursor()) {

      cursor.toNextToken();

      cursor.beginElement(new QName(TEST_NS, "A"));
      cursor.toEndToken();
      cursor.toNextToken();

      cursor.beginElement(new QName(TEST_NS, "B"));
      cursor.toEndToken();
      cursor.toNextToken();

      cursor.beginElement(new QName(TEST_NS, "A"));
      cursor.toEndToken();
      cursor.toNextToken();

      cursor.beginElement(new QName(TEST_NS, "C"));
      cursor.toEndToken();
      cursor.toNextToken();
    }

    obj.dump();

    Procesor processor = new Procesor();
    Map<QName, Handler<Void>> objMapping = ObjectUtils.notNull(Map.ofEntries(
        Map.entry(new QName(TEST_NS, "A"), processor::handleA),
        Map.entry(new QName(TEST_NS, "B"), processor::handleB),
        Map.entry(new QName(TEST_NS, "C"), processor::handleC)));

    new XmlObjectParser<Void>(objMapping).parse(obj, null);
  }

  @SuppressWarnings("unused")
  private static class Procesor {
    void handleA(@NonNull XmlObject obj, Void state) {
      try {
        obj.save(System.out);
        System.out.println();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    void handleB(@NonNull XmlObject obj, Void state) {
      try {
        obj.save(System.out);
        System.out.println();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    void handleC(@NonNull XmlObject obj, Void state) {
      try {
        obj.save(System.out);
        System.out.println();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

}
