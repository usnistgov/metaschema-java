package gov.nist.secauto.metaschema.binding;

import com.fasterxml.jackson.core.JsonFactory;

class JsonFactoryFactory {
	private static final JsonFactory singletonInstance = newJsonFactoryInstance();

	private JsonFactoryFactory() {
		// disable construction
	}

	public static JsonFactory newJsonFactoryInstance() {
		return new JsonFactory();
	}

	public static JsonFactory singletonInstance() {
		return singletonInstance;
	}

}
