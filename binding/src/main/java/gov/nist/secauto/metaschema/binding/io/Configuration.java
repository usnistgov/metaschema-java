package gov.nist.secauto.metaschema.binding.io;

public interface Configuration {
	boolean isFeatureEnabled(Feature feature, boolean defaultState);
}
