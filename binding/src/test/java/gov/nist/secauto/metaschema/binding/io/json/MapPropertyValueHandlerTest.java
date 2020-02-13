package gov.nist.secauto.metaschema.binding.io.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
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
			public List<Object> parse(JsonParsingContext parsingContext, PropertyBindingFilter filter)
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
			public void writeValue(Object value, JsonWritingContext writingContext, PropertyBindingFilter filter)
					throws BindingException, IOException {
				throw new UnsupportedOperationException();
			}

		};
	}

	private void parseProperty(JsonParser parser, PropertyValueHandler propertyValueHandler, int count)
			throws IOException, BindingException {
		JsonUtil.readNextToken(parser, JsonToken.START_OBJECT);
		JsonUtil.readNextToken(parser, JsonToken.FIELD_NAME);
		assertEquals("property", parser.currentName());

		// advance to value
		parser.nextToken();

		for (int i = 0; i < count; i++) {
			assertEquals(count != i + 1, propertyValueHandler.parseNextFieldValue(parsingContext), "when parsing item #"+i);
		}

		@SuppressWarnings("unchecked")
		LinkedHashMap<String, Integer> objects = (LinkedHashMap<String, Integer>) propertyValueHandler.getObjectSupplier().get();
		List<Integer> values = new ArrayList<>(objects.values());
		for (int i = 0; i < count; i++) {
			assertEquals(i, values.get(i));
		}

		assertEquals(JsonToken.END_OBJECT, parser.currentToken());
		assertNull(parser.nextToken());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSingleton() throws BindingException, IOException {

		JsonParser jsonParser = new JsonFactory().createParser(getClass().getResourceAsStream("map-singleton.json"));

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

		JsonParser jsonParser = new JsonFactory().createParser(getClass().getResourceAsStream("map-sequence.json"));

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
