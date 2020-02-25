/**
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
package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupLine;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupString;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class MarkupLineAdapter extends AbstractMarkupAdapter<MarkupLine> {

  public MarkupLineAdapter() {
    super();
  }

  @Override
  public MarkupLine parse(String value) {
    return MarkupLine.fromMarkdown(value);
  }

  @Override
  public MarkupLine parse(XmlParsingContext parsingContext) throws BindingException {
    try {
      return getMarkupParser().parseMarkupline(parsingContext.getEventReader());
    } catch (XMLStreamException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  public MarkupLine parse(JsonParsingContext parsingContext) throws BindingException {
    try {
      JsonParser parser = parsingContext.getEventReader();
      MarkupLine retval = parse(parser.getValueAsString());
      // skip past value
      parser.nextToken();
      return retval;
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  protected void writeXmlElementInternal(Object value, StartElement parent, XmlWritingContext writingContext)
      throws BindingException {
    MarkupXmlWriter writingVisitor
        = new MarkupXmlWriter(parent.getName().getNamespaceURI(), writingContext.getXMLEventFactory());
    writingVisitor.process(((MarkupString<?>) value).getDocument(), writingContext.getEventWriter(), false);
  }

  @Override
  public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException {

    MarkupLine ml;
    try {
      ml = (MarkupLine) value;
    } catch (ClassCastException ex) {
      throw new BindingException(ex);
    }

    JsonGenerator generator = writingContext.getEventWriter();
    String jsonString;
    if (generator instanceof YAMLGenerator) {
      jsonString = ml.toMarkdownYaml().trim();
    } else {
      jsonString = ml.toMarkdown().trim();
    }
    try {
      generator.writeString(jsonString);
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  @Override
  public String getDefaultJsonFieldName() {
    return "RICHTEXT";
  }
}
