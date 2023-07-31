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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.adapter.StringAdapter;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.model.test.MultiFieldAssembly;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;

class DefaultFieldPropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  @Mock
  private IMetaschema metaschema; // NOPMD - it's injected
  @Mock
  private IAssemblyClassBinding classBinding; // NOPMD - it's injected
  @Mock
  private IBindingContext bindingContext; // NOPMD - it's injected
  @Mock
  private IJsonParsingContext jsonParsingContext; // NOPMD - it's injected
  @Mock
  private IXmlParsingContext xmlParsingContext; // NOPMD - it's injected

  @SuppressWarnings("resource") // mocked
  @Test
  void testJsonRead()
      throws JsonParseException, IOException, NoSuchFieldException {

    String json = "{ \"field1\": \"field1value\", \"fields2\": [ \"field2value\" ] } }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      Class<?> theClass = MultiFieldAssembly.class;

      context.checking(new Expectations() {
        { // NOPMD - intentional
          allowing(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
          will(returnValue(MetaschemaDataTypeProvider.STRING));
          allowing(bindingContext).getClassBinding(String.class);
          will(returnValue(null));

          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getName();
          will(returnValue(null));
          allowing(classBinding).getBindingContext();
          will(returnValue(bindingContext));
          allowing(classBinding).getContainingMetaschema();
          will(returnValue(metaschema));

          allowing(jsonParsingContext).getReader();
          will(returnValue(jsonParser));

          allowing(metaschema).getLocation();
          will(returnValue(URI.create("relativeLocation")));
        }
      });

      java.lang.reflect.Field field1 = ObjectUtils.requireNonNull(theClass.getDeclaredField("field1"));
      IBoundFieldInstance field1Property = IBoundFieldInstance.newInstance(
          field1,
          ObjectUtils.notNull(classBinding));

      java.lang.reflect.Field field2 = ObjectUtils.requireNonNull(theClass.getDeclaredField("_field2"));
      IBoundFieldInstance field2Property = IBoundFieldInstance.newInstance(
          field2,
          ObjectUtils.notNull(classBinding));

      MultiFieldAssembly obj = new MultiFieldAssembly();

      assertAll(
          () -> assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken()),
          () -> assertEquals("field1", jsonParser.nextFieldName()),
          () -> assertTrue(field1Property.read(obj, ObjectUtils.notNull(jsonParsingContext))),
          () -> assertEquals("field1value", obj.getField1()),

          () -> assertEquals(JsonToken.FIELD_NAME, jsonParser.currentToken()),
          () -> assertEquals("fields2", jsonParser.currentName()),

          () -> assertTrue(field2Property.read(obj, ObjectUtils.notNull(jsonParsingContext))),
          () -> assertTrue(obj.getField2() instanceof LinkedList),
          () -> assertIterableEquals(Collections.singleton("field2value"), obj.getField2()));

      // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
      // assertEquals("id", jsonParser.currentName());
    }
  }

  @SuppressWarnings("resource") // mocked
  @Test
  void testJsonReadMissingFieldValue()
      throws JsonParseException, IOException, NoSuchFieldException {
    String json = "{ \"test\":\n" + "  { \"fields2\": [\n" + "    \"field2value\"\n" + "    ]\n" + "  }\n" + "}\n";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      Class<?> theClass = MultiFieldAssembly.class;

      context.checking(new Expectations() {
        { // NOPMD - intentional
          allowing(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
          will(returnValue(MetaschemaDataTypeProvider.STRING));
          allowing(bindingContext).getClassBinding(String.class);
          will(returnValue(null));

          allowing(classBinding).getBoundClass();
          will(returnValue(theClass));
          allowing(classBinding).getName();
          will(returnValue(null));
          allowing(classBinding).getBindingContext();
          will(returnValue(bindingContext));
          allowing(classBinding).getContainingMetaschema();
          will(returnValue(metaschema));

          allowing(jsonParsingContext).getReader();
          will(returnValue(jsonParser));

          allowing(metaschema).getLocation();
          will(returnValue(URI.create("relativeLocation")));
        }
      });

      java.lang.reflect.Field field1 = theClass.getDeclaredField("field1");
      IBoundFieldInstance field1Property = IBoundFieldInstance.newInstance(
          ObjectUtils.notNull(field1),
          ObjectUtils.notNull(classBinding));
      java.lang.reflect.Field field2 = theClass.getDeclaredField("_field2");
      IBoundFieldInstance field2Property = IBoundFieldInstance.newInstance(
          ObjectUtils.notNull(field2),
          ObjectUtils.notNull(classBinding));

      MultiFieldAssembly obj = new MultiFieldAssembly();

      // Advance to first property to parse
      assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      assertEquals("test", jsonParser.nextFieldName());
      assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      assertEquals("fields2", jsonParser.nextFieldName());

      // attempt to parse the missing property
      assertFalse(field1Property.read(obj, ObjectUtils.notNull(jsonParsingContext)));
      assertEquals(null, obj.getField1());

      // attempt to parse the existing property
      assertEquals(JsonToken.FIELD_NAME, jsonParser.currentToken());
      assertEquals("fields2", jsonParser.currentName());

      assertTrue(field2Property.read(obj, ObjectUtils.notNull(jsonParsingContext)));
      assertTrue(obj.getField2() instanceof LinkedList);
      assertIterableEquals(Collections.singleton("field2value"), obj.getField2());
    }
  }

}
