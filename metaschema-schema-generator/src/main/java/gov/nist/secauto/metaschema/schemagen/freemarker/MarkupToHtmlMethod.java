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

package gov.nist.secauto.metaschema.schemagen.freemarker;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupText;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupXmlStreamWriter;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.evt.MergedNsContext;
import org.codehaus.stax2.ri.evt.NamespaceEventImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

public class MarkupToHtmlMethod implements TemplateMethodModelEx {

  @Override
  public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {

    if (arguments.isEmpty() || arguments.size() < 2 || arguments.size() > 3) {
      throw new TemplateModelException(String.format(
          "This method requires a %s typed object argument, a namspace string argument, and may optionally have a prefix string argument.",
          MarkupText.class.getName()));
    }

    String namespace = DeepUnwrap.unwrap((TemplateModel) arguments.get(1)).toString();

    String prefix = null;
    if (arguments.size() == 3) {
      prefix = DeepUnwrap.unwrap((TemplateModel) arguments.get(2)).toString();
    }

    Object markupObject = DeepUnwrap.unwrap((TemplateModel) arguments.get(0));

    if (!(markupObject instanceof MarkupText)) {
      throw new TemplateModelException(String.format("The first argument must be of type %s. The type %s is invalid.",
          MarkupText.class.getName(), markupObject.getClass().getName()));
    }

    MarkupText text = (MarkupText) markupObject;

    MarkupXmlStreamWriter writingVisitor
        = new MarkupXmlStreamWriter(namespace, text instanceof MarkupMultiline);

    XMLOutputFactory2 factory = (XMLOutputFactory2) WstxOutputFactory.newInstance();
    factory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      XMLStreamWriter2 xmlStreamWriter = (XMLStreamWriter2) factory.createXMLStreamWriter(os);
      NamespaceContext nsContext = MergedNsContext.construct(xmlStreamWriter.getNamespaceContext(),
          List.of(NamespaceEventImpl.constructNamespace(null, prefix != null ? prefix : "", namespace)));
      xmlStreamWriter.setNamespaceContext(nsContext);
      writingVisitor.visitChildren(text.getDocument(), xmlStreamWriter);
      xmlStreamWriter.flush();
      return os.toString();
    } catch (XMLStreamException | IOException ex) {
      throw new TemplateModelException(ex);
    }
  }

}
