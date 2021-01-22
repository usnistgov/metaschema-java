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

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.datatypes.adapter.types.StringAdapter;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class DefaultFieldValuePropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  private FieldClassBinding classBinding = context.mock(FieldClassBinding.class);
  private BindingContext bindingContext = context.mock(BindingContext.class);
  private JsonParsingContext jsonParsingContext = context.mock(JsonParsingContext.class);
  private XmlParsingContext xmlParsingContext = context.mock(XmlParsingContext.class);

  @Test
  void testJsonRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, BindingException {
    String json = "{ \"a-value\": \"theValue\" }";
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(json);
    Class<?> theClass = SimpleField.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));
        allowing(classBinding).getJsonValueKeyFlag();
        will(returnValue(null));

        allowing(jsonParsingContext).getReader();
        will(returnValue(jsonParser));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    assertEquals("a-value", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
    // assertEquals("id", jsonParser.currentName());

    SimpleField obj = new SimpleField();

    idProperty.read(obj, jsonParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @Test
  void testXmlRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, XMLStreamException {
    String xml = "<field xmlns='http://example.com/ns'>theValue</field>";
    XMLInputFactory factory = WstxInputFactory.newInstance();
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));
    Class<?> theClass = SimpleField.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(xmlParsingContext).getReader();
        will(returnValue(eventReader));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(XMLEvent.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLEvent.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    SimpleField obj = new SimpleField();
    idProperty.read(obj, start, xmlParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @MetaschemaField(
      isCollapsible = true)
  public static class SimpleField {
    @FieldValue(
        name = "a-value",
        typeAdapter = StringAdapter.class)
    private String _value;

    public SimpleField() {
    }

    public String getValue() {
      return _value;
    }
  }

  @Test
  void testJsonDefaultNameRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, BindingException {
    String json = "{ \"STRVALUE\": \"theValue\" }";
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(json);
    Class<?> theClass = SimpleField2.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getJsonValueKeyFlag();
        will(returnValue(null));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(jsonParsingContext).getReader();
        will(returnValue(jsonParser));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    assertEquals("STRVALUE", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
    // assertEquals("id", jsonParser.currentName());

    SimpleField2 obj = new SimpleField2();

    idProperty.read(obj, jsonParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @Test
  void testXmlDefaultNameRead()
      throws JsonParseException, IOException, NoSuchFieldException, SecurityException, XMLStreamException {
    String xml = "<field xmlns='http://example.com/ns'>theValue</field>";
    XMLInputFactory factory = WstxInputFactory.newInstance();
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));
    Class<?> theClass = SimpleField2.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      {
        oneOf(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(xmlParsingContext).getReader();
        will(returnValue(eventReader));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(XMLEvent.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLEvent.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    SimpleField2 obj = new SimpleField2();
    idProperty.read(obj, start, xmlParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @MetaschemaField(
      isCollapsible = true)
  public static class SimpleField2 {
    @FieldValue(
        typeAdapter = StringAdapter.class)
    private String _value;

    public SimpleField2() {
    }

    public String getValue() {
      return _value;
    }
  }

}
