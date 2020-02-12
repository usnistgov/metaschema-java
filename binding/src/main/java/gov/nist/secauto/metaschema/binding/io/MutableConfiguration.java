package gov.nist.secauto.metaschema.binding.io;

import java.util.EnumMap;

public class MutableConfiguration implements Configuration {
	private final EnumMap<Feature, Boolean> features = new EnumMap<>(Feature.class);

	public MutableConfiguration enableFeature(Feature feature) {
		features.put(feature, Boolean.TRUE);
		return this;
	}

	public MutableConfiguration disableFeature(Feature feature) {
		features.put(feature, Boolean.FALSE);
		return this;
	}

	@Override
	public boolean isFeatureEnabled(Feature feature, boolean defaultState) {
		Boolean state = features.get(feature);

		boolean retval;
		if (state == null) {
			retval = defaultState;
		} else {
			retval = state;
		}
		return retval;
	}
}
