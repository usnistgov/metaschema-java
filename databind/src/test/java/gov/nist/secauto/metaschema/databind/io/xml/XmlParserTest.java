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

package gov.nist.secauto.metaschema.databind.io.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonParseException;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundModelTestSupport;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.test.FlaggedAssembly;
import gov.nist.secauto.metaschema.databind.model.test.MultiFieldAssembly;
import gov.nist.secauto.metaschema.databind.model.test.ValueKeyField;

import org.codehaus.stax2.XMLEventReader2;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class XmlParserTest
    extends AbstractBoundModelTestSupport {

  @Test
  void testXmlRead() throws IOException, XMLStreamException {
    String xml = "<test xmlns='https://csrc.nist.gov/ns/test/xml'>"
        + "  <field1>field1value</field1>" + "</test>";
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    assert start != null;

    MetaschemaXmlReader parser = new MetaschemaXmlReader(eventReader);

    IBindingContext bindingContext = getBindingContext();

    IAssemblyClassBinding assembly
        = ObjectUtils.requireNonNull((IAssemblyClassBinding) bindingContext.getClassBinding(MultiFieldAssembly.class));

    IBoundFieldInstance field1Instance
        = ObjectUtils.requireNonNull((IBoundFieldInstance) assembly.getModelInstanceByName("field1"));

    IBoundFieldInstance field2Instance
        = ObjectUtils.requireNonNull((IBoundFieldInstance) assembly.getModelInstanceByName("field2"));

    MultiFieldAssembly obj = new MultiFieldAssembly();

    assertTrue(parser.readModelInstanceItems(field1Instance, obj, start));
    assertFalse(parser.readModelInstanceItems(field2Instance, obj, start));

    assertEquals("field1value", obj.getField1());
    assertEquals(null, obj.getField2());
  }

  @Test
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
  void testXmlReadFlagProperty() throws JsonParseException, IOException, XMLStreamException {
    String xml = "<flagged-assembly xmlns='https://csrc.nist.gov/ns/test/xml' id='theId' number='1'/>";
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));

    IBindingContext bindingContext = getBindingContext();
    IAssemblyClassBinding assembly
        = ObjectUtils.requireNonNull((IAssemblyClassBinding) bindingContext.getClassBinding(FlaggedAssembly.class));

    IBoundFlagInstance idProperty = assembly.getFlagInstanceByName("id");
    assert idProperty != null;

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    assert start != null;

    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    MetaschemaXmlReader parser = new MetaschemaXmlReader(eventReader);
    FlaggedAssembly obj = parser.readDefinitionValue(assembly, null, start);

    assertEquals("theId", obj.getId());
  }

  @Test
  void testXmlReadGroupedField() throws JsonParseException, IOException, XMLStreamException {
    String xml = new StringBuilder()
        .append("<test xmlns='https://csrc.nist.gov/ns/test/xml'>\n")
        .append(" <fields2>\n")
        .append("   <field2>field2value</field2>\n")
        .append(" </fields2>\n")
        .append("</test>")
        .toString();
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT,
        event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    assert start != null;

    MetaschemaXmlReader parser = new MetaschemaXmlReader(eventReader);

    IBindingContext bindingContext = getBindingContext();

    IAssemblyClassBinding assembly
        = ObjectUtils.requireNonNull((IAssemblyClassBinding) bindingContext.getClassBinding(MultiFieldAssembly.class));

    IBoundFieldInstance field1Instance
        = ObjectUtils.requireNonNull(assembly.getFieldInstanceByName("field1"));

    IBoundFieldInstance field2Instance
        = ObjectUtils.requireNonNull(assembly.getFieldInstanceByName("field2"));

    MultiFieldAssembly obj = new MultiFieldAssembly();

    assertFalse(parser.readModelInstanceItems(field1Instance, obj, start));
    assertTrue(parser.readModelInstanceItems(field2Instance, obj, start));

    assertEquals(null, obj.getField1());
    assertIterableEquals(Collections.singleton("field2value"),
        obj.getField2());
  }

  @Test
  void testReadField() throws JsonParseException, IOException, XMLStreamException {
    String xml = "<simple-field xmlns='http://example.com/ns'>theValue</simple-field>";
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
    assert start != null;

    MetaschemaXmlReader parser = new MetaschemaXmlReader(eventReader);

    IBindingContext bindingContext = getBindingContext();

    IFieldClassBinding field
        = ObjectUtils.requireNonNull((IFieldClassBinding) bindingContext.getClassBinding(ValueKeyField.class));

    ValueKeyField obj = (ValueKeyField) parser.readDefinitionValue(field, null, start);

    assertNotNull(obj);
    assertEquals("theValue", obj.getValue());
  }
}
