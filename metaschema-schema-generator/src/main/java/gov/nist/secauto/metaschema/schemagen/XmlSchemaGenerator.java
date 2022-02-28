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

package gov.nist.secauto.metaschema.schemagen;

import gov.nist.secauto.metaschema.model.common.IMetaschema;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

import org.codehaus.stax2.io.Stax2StringSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.stream.StreamSource;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class XmlSchemaGenerator
    extends AbstractSchemaGenerator {

  @Override
  protected Template getTemplate(Configuration cfg)
      throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
    return cfg.getTemplate("xml-schema.ftlx");
  }

  @Override
  public void generateFromMetaschema(@NotNull IMetaschema metaschema, @NotNull Writer out)
      throws TemplateNotFoundException, MalformedTemplateNameException, TemplateException, ParseException, IOException {

    StringWriter stringWriter = new StringWriter();
    try (PrintWriter writer = new PrintWriter(stringWriter)) {
      super.generateFromMetaschema(metaschema, writer);
      writer.flush();

      Processor processor = new Processor(false);
      XsltCompiler compiler = processor.newXsltCompiler();
      XsltExecutable stylesheet
          = compiler.compile(new StreamSource(getClass().getClassLoader().getResourceAsStream("identity.xsl")));
      Xslt30Transformer transformer = stylesheet.load30();
      Serializer serializer = processor.newSerializer(out);
      
      try (StringReader stringReader = new StringReader(stringWriter.toString())) {
        StreamSource source = new StreamSource(stringReader);
        transformer.transform(source, serializer);
      }
    } catch (SaxonApiException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
