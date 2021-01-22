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

package gov.nist.secauto.metaschema.binding.model.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.datatypes.adapter.types.IntegerAdapter;
import gov.nist.secauto.metaschema.datatypes.adapter.types.StringAdapter;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigInteger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class DefaultFlagPropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  private ClassBinding classBinding = context.mock(ClassBinding.class);
  private BindingContext bindingContext = context.mock(BindingContext.class);
  private JsonParsingContext jsonParsingContext = context.mock(JsonParsingContext.class);
  private XmlParsingContext xmlParsingContext = context.mock(XmlParsingContext.class);

  @Test
  void testJsonRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, BindingException {
    String json = "{ \"test\": { \"id\": \"theId\", \"number\": 1 } }";
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(json);

    Field field = SimpleAssembly.class.getDeclaredField("_id");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(SimpleAssembly.class));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(jsonParsingContext).getReader();
        will(returnValue(jsonParser));
      }
    });

    DefaultFlagProperty idProperty = new DefaultFlagProperty(classBinding, field, bindingContext);

    assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    assertEquals("test", jsonParser.nextFieldName());
    assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
    assertEquals("id", jsonParser.currentName());

    SimpleAssembly obj = new SimpleAssembly();

    assertTrue(idProperty.read(obj, jsonParsingContext));

    assertEquals("theId", obj.getId());
  }

  @Test
  void testXmlRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, XMLStreamException {
    String xml = "<test xmlns='http://example.com/ns' id='theId' number='1'/>";
    XMLInputFactory factory = WstxInputFactory.newInstance();
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));

    Field field = SimpleAssembly.class.getDeclaredField("_id");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(SimpleAssembly.class));

        allowing(xmlParsingContext).getReader();
        will(returnValue(eventReader));
      }
    });

    DefaultFlagProperty idProperty = new DefaultFlagProperty(classBinding, field, bindingContext);

    assertEquals(XMLEvent.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLEvent.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    SimpleAssembly obj = new SimpleAssembly();
    assertTrue(idProperty.read(obj, start, xmlParsingContext));

    assertEquals("theId", obj.getId());
  }

  @MetaschemaAssembly(
      rootName = "test",
      rootNamespace = "http://example.com/ns")
  private static class SimpleAssembly {
    @Flag(
        name = "id",
        typeAdapter = StringAdapter.class)
    private String _id;

    @Flag(
        name = "number",
        typeAdapter = IntegerAdapter.class)
    private BigInteger _number;

    public SimpleAssembly() {
    }

    public String getId() {
      return _id;
    }

    public BigInteger getNumber() {
      return _number;
    }
  }
}
