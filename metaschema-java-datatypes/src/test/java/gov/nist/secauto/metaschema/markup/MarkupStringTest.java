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

package gov.nist.secauto.metaschema.markup;

import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.types.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatypes.types.markup.flexmark.AstCollectingVisitor;

import org.junit.jupiter.api.Test;

class MarkupStringTest {

  @Test
  void fromMarkdownLine() {
    MarkupLine ms = MarkupLine.fromMarkdown("Some \\**more* **text** and a param: {{ insert }}.");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());
    System.out.println(ms.toHtml());
    System.out.println(ms.toMarkdown());
  }

  @Test
  void fromMarkdown() {
    MarkupMultiline ms = MarkupMultiline.fromMarkdown("# Example\n\nSome \\**more* **text**\n\nA param: {{ insert }}.");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());
    System.out.println(ms.toHtml());
    System.out.println(ms.toMarkdown());
  }

  @Test
  void fromHTML() {
    MarkupMultiline ms = MarkupMultiline.fromHtml(
        "<h1>Example</h1><p><a href=\"link\">text</a></p><table><tr><th>Heading 1</th></tr><tr><td><q>data1</q> <insert param-id=\"insert\" /></td></tr></table><p>Some <em>more</em> <strong>text</strong><img alt=\"alt\" src=\"src\"/></p>");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());
    System.out.println(ms.toHtml());
    System.out.println(ms.toMarkdown());
  }

  @Test
  void preMarkdown() {
    MarkupMultiline ms = MarkupMultiline.fromHtml("<pre>Example **some** *code*</pre>");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());
    System.out.println(ms.toHtml());
    System.out.println(ms.toMarkdown());
  }

  @Test
  void pCodeMarkdown() {
    MarkupMultiline ms = MarkupMultiline.fromHtml("<p>Example<code>**some** *code*</code></p>");
    AstCollectingVisitor visitor = new AstCollectingVisitor();
    visitor.collect(ms.getDocument());
    System.out.println(visitor.getAst());
    System.out.println(ms.toHtml());
    System.out.println(ms.toMarkdown());
  }
}
