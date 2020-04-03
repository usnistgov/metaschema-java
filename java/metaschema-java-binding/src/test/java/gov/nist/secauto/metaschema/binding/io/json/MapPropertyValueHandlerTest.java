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

package gov.nist.secauto.metaschema.binding.io.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.property.PropertyItemHandler;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

class MapPropertyValueHandlerTest {

  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  @Mock
  BindingContext bindingContext;
  @Mock
  ClassBinding<?> classBinding;
  @Mock
  JsonParsingContext parsingContext;
  @Mock
  PropertyBinding propertyBinding;
  @Mock
  PropertyInfo propertyInfo;
  @Mock
  ClassBinding<?> itemClassBinding;
  @Mock
  FlagPropertyBinding jsonKeyPropertyBinding;
  @Mock
  PropertyInfo jsonKeyPropertyInfo;
  @Mock
  JavaTypeAdapter<String> stringJavaTypeAdapter;

  PropertyItemHandler newPropertyItemHandler(PropertyBinding propertyBinding) {
    return new PropertyItemHandler() {
      private int count = 0;

      @Override
      public PropertyBinding getPropertyBinding() {
        return propertyBinding;
      }

      @Override
      public List<Object> parse(PropertyBindingFilter filter, Object parent, JsonParsingContext parsingContext)
          throws BindingException, IOException {
        JsonParser parser = parsingContext.getEventReader();
        assertEquals(JsonToken.START_OBJECT, parser.currentToken());
        JsonUtil.readNextToken(parser, JsonToken.FIELD_NAME);
        assertEquals("data", parser.currentName());
        JsonUtil.readNextToken(parser, JsonToken.VALUE_STRING);
        assertEquals("value", parser.getText());
        JsonUtil.readNextToken(parser, JsonToken.END_OBJECT);
        parser.nextToken();
        return Collections.singletonList(Integer.valueOf(count++));
      }

      @Override
      public void writeValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
          throws BindingException, IOException {
        throw new UnsupportedOperationException();
      }

      @Override
      public Object parse(Object parent, XmlParsingContext parsingContext) throws BindingException {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isParsingXmlStartElement() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean canHandleQName(QName nextQName) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void writeXmlElement(Object child, QName itemWrapperQName, StartElement propertyParent,
          XmlWritingContext writingContext) throws BindingException {
        throw new UnsupportedOperationException();
      }

    };
  }

  private void parseProperty(JsonParser parser, PropertyValueHandler propertyValueHandler, int count)
      throws IOException, BindingException {
    JsonToken currentToken = JsonUtil.readNextToken(parser, JsonToken.START_OBJECT);
    currentToken = JsonUtil.readNextToken(parser, JsonToken.FIELD_NAME);
    assertEquals("property", parser.currentName());

    // advance to value
    currentToken = parser.nextToken();

    for (int i = 0; i < count; i++) {
      assertEquals(count != i + 1, propertyValueHandler.parseNextFieldValue(null, parsingContext), "when parsing item #" + i);
    }

    currentToken = parser.currentToken();

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Integer> objects
        = (LinkedHashMap<String, Integer>) propertyValueHandler.getObjectSupplier().get();
    List<Integer> values = new ArrayList<>(objects.values());
    for (int i = 0; i < count; i++) {
      assertEquals(i, values.get(i));
    }

    assertEquals(JsonToken.END_OBJECT, currentToken);
    assertNull(parser.nextToken());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSingleton() throws BindingException, IOException {

    InputStream is = MapPropertyValueHandlerTest.class.getResourceAsStream("map-singleton.json");
    JsonParser jsonParser = new JsonFactory().createParser(is);

    context.checking(new Expectations() {
      {
        allowing(bindingContext).getClassBinding(with(any(Class.class)));
        will(returnValue(itemClassBinding));
        allowing(bindingContext).getJavaTypeAdapter(with(any(Class.class)));
        will(returnValue(stringJavaTypeAdapter));

        allowing(itemClassBinding).getJsonKeyFlagPropertyBinding();
        will(returnValue(jsonKeyPropertyBinding));
        allowing(jsonKeyPropertyBinding).getPropertyInfo();
        will(returnValue(jsonKeyPropertyInfo));
        allowing(jsonKeyPropertyInfo).getSimpleName();
        will(returnValue("_json_key"));
        allowing(jsonKeyPropertyInfo).getItemType();
        will(returnValue(String.class));
        allowing(jsonKeyPropertyInfo).setValue(with(any(Object.class)), with(any(Object.class)));

        allowing(stringJavaTypeAdapter).parse(with(any(String.class)));

        allowing(parsingContext).getEventReader();
        will(returnValue(jsonParser));
        allowing(parsingContext).getBindingContext();
        will(returnValue(bindingContext));

        allowing(classBinding).getClazz();
        will(returnValue(Object.class));

        allowing(propertyBinding).getPropertyInfo();
        will(returnValue(propertyInfo));
        allowing(propertyInfo).getSimpleName();
        will(returnValue("_property"));
        allowing(propertyInfo).getItemType();
        will(returnValue(Integer.class));
      }
    });

    PropertyItemHandler propertyItemHandler = newPropertyItemHandler(propertyBinding);
    PropertyValueHandler handler = new MapPropertyValueHandler(classBinding, propertyItemHandler, bindingContext);
    parseProperty(jsonParser, handler, 1);
    context.assertIsSatisfied();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSequence() throws BindingException, IOException {

    JsonParser jsonParser
        = new JsonFactory().createParser(MapPropertyValueHandlerTest.class.getResourceAsStream("map-sequence.json"));

    context.checking(new Expectations() {
      {
        allowing(bindingContext).getClassBinding(with(any(Class.class)));
        will(returnValue(itemClassBinding));
        allowing(bindingContext).getJavaTypeAdapter(with(any(Class.class)));
        will(returnValue(stringJavaTypeAdapter));

        allowing(itemClassBinding).getJsonKeyFlagPropertyBinding();
        will(returnValue(jsonKeyPropertyBinding));
        allowing(jsonKeyPropertyBinding).getPropertyInfo();
        will(returnValue(jsonKeyPropertyInfo));
        allowing(jsonKeyPropertyInfo).getSimpleName();
        will(returnValue("_json_key"));
        allowing(jsonKeyPropertyInfo).getItemType();
        will(returnValue(String.class));
        allowing(jsonKeyPropertyInfo).setValue(with(any(Object.class)), with(any(Object.class)));

        allowing(stringJavaTypeAdapter).parse(with(any(String.class)));

        allowing(parsingContext).getEventReader();
        will(returnValue(jsonParser));
        allowing(parsingContext).getBindingContext();
        will(returnValue(bindingContext));

        allowing(classBinding).getClazz();
        will(returnValue(Object.class));

        allowing(propertyBinding).getPropertyInfo();
        will(returnValue(propertyInfo));
        allowing(propertyInfo).getSimpleName();
        will(returnValue("_property"));
        allowing(propertyInfo).getItemType();
        will(returnValue(Integer.class));
      }
    });

    PropertyItemHandler propertyItemHandler = newPropertyItemHandler(propertyBinding);
    PropertyValueHandler handler = new MapPropertyValueHandler(classBinding, propertyItemHandler, bindingContext);
    parseProperty(jsonParser, handler, 2);
    context.assertIsSatisfied();
  }

}
