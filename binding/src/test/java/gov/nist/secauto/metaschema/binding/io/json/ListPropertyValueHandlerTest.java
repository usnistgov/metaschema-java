package gov.nist.secauto.metaschema.binding.io.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonUtil;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

class ListPropertyValueHandlerTest {

	@RegisterExtension
	JUnit5Mockery context = new JUnit5Mockery();

	@Mock
	ClassBinding<?> classBinding;
	@Mock
	JsonParsingContext parsingContext;
	@Mock
	PropertyBinding propertyBinding;
	@Mock
	PropertyInfo propertyInfo;

	PropertyItemHandler newPropertyItemHandler(PropertyBinding propertyBinding) {
		return new PropertyItemHandler() {
			private int count = 0;

			@Override
			public PropertyBinding getPropertyBinding() {
				return propertyBinding;
			}

			@Override
			public List<Object> parse(JsonParsingContext parsingContext, NamedPropertyBindingFilter filter)
					throws BindingException, IOException {
				JsonParser parser = parsingContext.getEventReader();
				assertEquals(JsonToken.START_OBJECT, parser.currentToken());
				JsonUtil.readNextToken(parser, JsonToken.FIELD_NAME);
				assertEquals("test", parser.currentName());
				JsonUtil.readNextToken(parser, JsonToken.VALUE_STRING);
				assertEquals("data", parser.getText());
				JsonUtil.readNextToken(parser, JsonToken.END_OBJECT);
				parser.nextToken();
				return Collections.singletonList(Integer.valueOf(count++));
			}

			@Override
			public void writeValue(Object value, JsonWritingContext writingContext, NamedPropertyBindingFilter filter)
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
		List<Integer> objects = (List<Integer>) propertyValueHandler.getObject();
		for (int i = 0; i < count; i++) {
			assertEquals(i, objects.get(i));
		}

		assertEquals(JsonToken.END_OBJECT, parser.currentToken());
		assertNull(parser.nextToken());
	}

	@Test
	void testSingleton() throws BindingException, IOException {

		JsonParser jsonParser = new JsonFactory().createParser(getClass().getResourceAsStream("list-singleton.json"));

		context.checking(new Expectations() {
			{
				allowing(parsingContext).getEventReader();
				will(returnValue(jsonParser));
				allowing(classBinding).getClazz();
				will(returnValue(Object.class));
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyInfo).getSimpleName();
				will(returnValue("_property"));
			}
		});

		PropertyItemHandler propertyItemHandler = newPropertyItemHandler(propertyBinding);
		ListPropertyValueHandler handler = new ListPropertyValueHandler(classBinding, propertyItemHandler, true);
		parseProperty(jsonParser, handler, 1);
		context.assertIsSatisfied();
	}

	@Test
	void testSingletonFail() throws IOException {

		JsonParser jsonParser = new JsonFactory().createParser(getClass().getResourceAsStream("list-singleton.json"));

		context.checking(new Expectations() {
			{
				allowing(parsingContext).getEventReader();
				will(returnValue(jsonParser));
				allowing(classBinding).getClazz();
				will(returnValue(Object.class));
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyInfo).getSimpleName();
				will(returnValue("_property"));
			}
		});

		PropertyItemHandler propertyItemHandler = newPropertyItemHandler(propertyBinding);
		ListPropertyValueHandler handler = new ListPropertyValueHandler(classBinding, propertyItemHandler, false);
		Assertions.assertThrows(BindingException.class, () -> { parseProperty(jsonParser, handler, 1); }, "Found unexpected 'START_OBJECT' token when parsing property '_property' on class 'Object' at location '2:15'. This list doesn't allow a singleton object.");
		context.assertIsSatisfied();
	}

	@Test
	void testSequence() throws BindingException, IOException {

		JsonParser jsonParser = new JsonFactory().createParser(getClass().getResourceAsStream("list-sequence.json"));

		context.checking(new Expectations() {
			{
				allowing(parsingContext).getEventReader();
				will(returnValue(jsonParser));
				allowing(classBinding).getClazz();
				will(returnValue(Object.class));
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyInfo).getSimpleName();
				will(returnValue("_property"));
			}
		});

		PropertyItemHandler propertyItemHandler = newPropertyItemHandler(propertyBinding);
		ListPropertyValueHandler handler = new ListPropertyValueHandler(classBinding, propertyItemHandler, false);
		parseProperty(jsonParser, handler, 2);
		context.assertIsSatisfied();
	}

}
