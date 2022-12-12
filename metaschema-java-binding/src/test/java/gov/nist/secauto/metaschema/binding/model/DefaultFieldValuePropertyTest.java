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

package gov.nist.secauto.metaschema.binding.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.Metaschema;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaFieldValue;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;

class DefaultFieldValuePropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  private final IFieldClassBinding classBinding = context.mock(IFieldClassBinding.class);
  private final IBindingContext bindingContext = context.mock(IBindingContext.class);
  private final IJsonParsingContext jsonParsingContext = context.mock(IJsonParsingContext.class);
  private final IXmlParsingContext xmlParsingContext = context.mock(IXmlParsingContext.class);

  @Test
  void testJsonRead()
      throws JsonParseException, IOException, NoSuchFieldException {
    String json = "{ \"a-value\": \"theValue\" }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      Class<?> theClass = SimpleField.class;

      Field field = theClass.getDeclaredField("_value");

      context.checking(new Expectations() {
        { // NOPMD - intentional
          atMost(1).of(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
          will(returnValue(MetaschemaDataTypeProvider.STRING));

          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getBindingContext();
          will(returnValue(bindingContext));
          allowing(classBinding).getJsonValueKeyFlagInstance();
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
  }

  @Test
  void testXmlRead()
      throws JsonParseException, IOException, NoSuchFieldException, XMLStreamException {
    String xml = "<field xmlns='http://example.com/ns'>theValue</field>";
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));
    Class<?> theClass = SimpleField.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        atMost(1).of(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(MetaschemaDataTypeProvider.STRING));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(xmlParsingContext).getReader();
        will(returnValue(eventReader));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    SimpleField obj = new SimpleField();
    assert start != null;
    assert xmlParsingContext != null;
    idProperty.read(obj, start, xmlParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @Metaschema
  public static class TestMetaschema
      extends AbstractBoundMetaschema {

    public TestMetaschema(@NonNull List<? extends IMetaschema> importedMetaschema,
        @NonNull IBindingContext bindingContext) {
      super(importedMetaschema, bindingContext);
    }

    @Override
    public MarkupLine getName() {
      return MarkupLine.fromMarkdown("Test Metaschema");
    }

    @Override
    public String getVersion() {
      return "1.0";
    }

    @Override
    public MarkupMultiline getRemarks() {
      return null;
    }

    @Override
    public String getShortName() {
      return "test-metaschema";
    }

    @Override
    public URI getXmlNamespace() {
      return ObjectUtils.notNull(URI.create("https://csrc.nist.gov/ns/test/xml"));
    }

    @Override
    public URI getJsonBaseUri() {
      return ObjectUtils.notNull(URI.create("https://csrc.nist.gov/ns/test/json"));
    }

  }

  @SuppressWarnings("PMD")
  @MetaschemaField(
      name = "simple-field",
      metaschema = TestMetaschema.class,
      isCollapsible = true)
  public static class SimpleField {
    @MetaschemaFieldValue(valueKeyName = "a-value")
    private String _value;

    public SimpleField() {
    }

    public String getValue() {
      return _value;
    }
  }

  @Test
  void testJsonDefaultNameRead() throws JsonParseException, IOException, NoSuchFieldException {
    String json = "{ \"STRVALUE\": \"theValue\" }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      Class<?> theClass = SimpleField2.class;

      Field field = theClass.getDeclaredField("_value");

      context.checking(new Expectations() {
        { // NOPMD - intentional
          atMost(1).of(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
          will(returnValue(MetaschemaDataTypeProvider.STRING));

          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getJsonValueKeyFlagInstance();
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
  }

  @Test
  void testXmlDefaultNameRead()
      throws JsonParseException, IOException, NoSuchFieldException, XMLStreamException {
    String xml = "<field xmlns='http://example.com/ns'>theValue</field>";
    XMLInputFactory factory = XMLInputFactory.newInstance();
    assert factory instanceof WstxInputFactory;
    XMLEventReader2 eventReader = (XMLEventReader2) factory.createXMLEventReader(new StringReader(xml));
    Class<?> theClass = SimpleField2.class;

    Field field = theClass.getDeclaredField("_value");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        atMost(1).of(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(MetaschemaDataTypeProvider.STRING));

        allowing(classBinding).getBoundClass();
        will(returnValue(theClass));
        allowing(classBinding).getBindingContext();
        will(returnValue(bindingContext));

        allowing(xmlParsingContext).getReader();
        will(returnValue(eventReader));
      }
    });

    DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(classBinding, field);

    assertEquals(XMLStreamConstants.START_DOCUMENT, eventReader.nextEvent().getEventType());
    XMLEvent event = eventReader.nextEvent();
    assertEquals(XMLStreamConstants.START_ELEMENT, event.getEventType());
    StartElement start = event.asStartElement();
    // assertEquals("test", jsonParser.nextFieldName());
    // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
    // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());

    SimpleField2 obj = new SimpleField2();
    assert start != null;
    assert xmlParsingContext != null;
    idProperty.read(obj, start, xmlParsingContext);

    assertEquals("theValue", obj.getValue());
  }

  @SuppressWarnings("PMD")
  @MetaschemaField(
      name = "simple-field2",
      metaschema = TestMetaschema.class,
      isCollapsible = true)
  public static class SimpleField2 {
    @MetaschemaFieldValue
    private String _value;

    public SimpleField2() {
    }

    public String getValue() {
      return _value;
    }
  }

}
