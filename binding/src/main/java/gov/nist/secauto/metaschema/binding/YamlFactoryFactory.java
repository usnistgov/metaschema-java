package gov.nist.secauto.metaschema.binding;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class YamlFactoryFactory {
	private static final YAMLFactory singletonInstance = newYamlFactoryInstance();

	private YamlFactoryFactory() {
		// disable construction
	}

	public static YAMLFactory newYamlFactoryInstance() {
		return new YAMLFactory();
	}

	public static YAMLFactory singletonInstance() {
		return singletonInstance;
	}

}
