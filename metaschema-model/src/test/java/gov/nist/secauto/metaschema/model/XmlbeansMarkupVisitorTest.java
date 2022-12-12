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

package gov.nist.secauto.metaschema.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.xmlbeans.METASCHEMADocument;
import gov.nist.secauto.metaschema.model.xmlbeans.METASCHEMADocument.METASCHEMA;
import gov.nist.secauto.metaschema.model.xmlbeans.MarkupLineDatatype;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import edu.umd.cs.findbugs.annotations.NonNull;

class XmlbeansMarkupVisitorTest {

  @Test
  void testText() throws IOException {
    String html = testMarkupLine("this is some basic text");
    assertNotNull(html, "not null");
    // System.out.println(html);
  }

  @Test
  void testQuote() throws IOException {
    String html = testMarkupLine("this is some \"basic text\"");
    assertNotNull(html, "not null");
    // System.out.println(html);
  }

  @Test
  void testLink() throws IOException {
    String html = testMarkupLine("this is some basic text with a [link](url/).");
    assertNotNull(html, "not null");
    // System.out.println(html);
  }

  @Test
  void testComplex() throws IOException {
    String html = testMarkupLine(
        "this is some \"quoted *basic text*\" with a [**bold** link](url/). <span id=\"id\">test</span>");
    assertNotNull(html, "not null");
    // System.out.println(html);
  }

  @NonNull
  private static String testMarkupLine(@NonNull String markdown) throws IOException {
    MarkupLine markup = MarkupLine.fromMarkdown(markdown);

    XmlOptions options = new XmlOptions();
    options.setSaveAggressiveNamespaces(true);
    options.setUseDefaultNamespace(true);

    METASCHEMADocument metaschemaDocument = METASCHEMADocument.Factory.newInstance();
    METASCHEMA metaschema = metaschemaDocument.addNewMETASCHEMA();
    MarkupLineDatatype xmlData = metaschema.addNewSchemaName();

    try (XmlCursor cursor = xmlData.newCursor()) {
      cursor.toEndToken();

      new XmlbeansMarkupVisitor("http://csrc.nist.gov/ns/oscal/metaschema/1.0", false).visitDocument(
          markup.getDocument(),
          cursor);

      try (StringWriter writer = new StringWriter()) {
        metaschemaDocument.save(writer, options);
        return ObjectUtils.notNull(writer.toString());
      }
    }
  }
}
