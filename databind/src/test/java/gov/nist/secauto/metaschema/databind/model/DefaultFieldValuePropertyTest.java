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

package gov.nist.secauto.metaschema.databind.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.model.test.SimpleField2;
import gov.nist.secauto.metaschema.databind.model.test.TestSimpleField;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.lang.reflect.Field;

class DefaultFieldValuePropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  private final IFieldClassBinding classBinding = context.mock(IFieldClassBinding.class);
  private final IBindingContext bindingContext = context.mock(IBindingContext.class);
  private final IJsonParsingContext jsonParsingContext = context.mock(IJsonParsingContext.class);
  private final IXmlParsingContext xmlParsingContext = context.mock(IXmlParsingContext.class);

  @SuppressWarnings("resource") // mocked
  @Test
  void testJsonRead()
      throws JsonParseException, IOException, NoSuchFieldException {
    String json = "{ \"a-value\": \"theValue\" }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      Class<?> theClass = TestSimpleField.class;

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

      DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(
          ObjectUtils.notNull(classBinding),
          ObjectUtils.notNull(field));

      assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      assertEquals("a-value", jsonParser.nextFieldName());
      // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
      // assertEquals("id", jsonParser.currentName());

      TestSimpleField obj = new TestSimpleField();

      idProperty.read(obj, ObjectUtils.notNull(jsonParsingContext));

      assertEquals("theValue", obj.getValue());
    }
  }

  @SuppressWarnings("resource") // mocked
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

      DefaultFieldValueProperty idProperty = new DefaultFieldValueProperty(
          ObjectUtils.notNull(classBinding),
          ObjectUtils.notNull(field));

      assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      assertEquals("STRVALUE", jsonParser.nextFieldName());
      // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
      // assertEquals("id", jsonParser.currentName());

      SimpleField2 obj = new SimpleField2();

      idProperty.read(obj, ObjectUtils.notNull(jsonParsingContext));

      assertEquals("theValue", obj.getValue());
    }
  }

}
