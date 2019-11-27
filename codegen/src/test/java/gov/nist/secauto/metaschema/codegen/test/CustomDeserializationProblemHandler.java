package gov.nist.secauto.metaschema.codegen.test;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

public class CustomDeserializationProblemHandler extends DeserializationProblemHandler {

	public CustomDeserializationProblemHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer,
			Object beanOrClass, String propertyName) throws IOException {
		// TODO Auto-generated method stub
		return super.handleUnknownProperty(ctxt, p, deserializer, beanOrClass, propertyName);
	}

	@Override
	public Object handleWeirdKey(DeserializationContext ctxt, Class<?> rawKeyType, String keyValue, String failureMsg)
			throws IOException {
		// TODO Auto-generated method stub
		return super.handleWeirdKey(ctxt, rawKeyType, keyValue, failureMsg);
	}

	@Override
	public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert,
			String failureMsg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleWeirdStringValue(ctxt, targetType, valueToConvert, failureMsg);
	}

	@Override
	public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert,
			String failureMsg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleWeirdNumberValue(ctxt, targetType, valueToConvert, failureMsg);
	}

	@Override
	public Object handleWeirdNativeValue(DeserializationContext ctxt, JavaType targetType, Object valueToConvert,
			JsonParser p) throws IOException {
		// TODO Auto-generated method stub
		return super.handleWeirdNativeValue(ctxt, targetType, valueToConvert, p);
	}

	@Override
	public Object handleUnexpectedToken(DeserializationContext ctxt, Class<?> targetType, JsonToken t, JsonParser p,
			String failureMsg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg);
	}

	@Override
	public Object handleInstantiationProblem(DeserializationContext ctxt, Class<?> instClass, Object argument,
			Throwable t) throws IOException {
		// TODO Auto-generated method stub
		return super.handleInstantiationProblem(ctxt, instClass, argument, t);
	}

	@Override
	public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass,
			ValueInstantiator valueInsta, JsonParser p, String msg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleMissingInstantiator(ctxt, instClass, valueInsta, p, msg);
	}

	@Override
	public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId,
			TypeIdResolver idResolver, String failureMsg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleUnknownTypeId(ctxt, baseType, subTypeId, idResolver, failureMsg);
	}

	@Override
	public JavaType handleMissingTypeId(DeserializationContext ctxt, JavaType baseType, TypeIdResolver idResolver,
			String failureMsg) throws IOException {
		// TODO Auto-generated method stub
		return super.handleMissingTypeId(ctxt, baseType, idResolver, failureMsg);
	}

	@Override
	public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass, JsonParser p, String msg)
			throws IOException {
		// TODO Auto-generated method stub
		return super.handleMissingInstantiator(ctxt, instClass, p, msg);
	}

}
