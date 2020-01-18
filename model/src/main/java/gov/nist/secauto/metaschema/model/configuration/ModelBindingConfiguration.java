package gov.nist.secauto.metaschema.model.configuration;

public class ModelBindingConfiguration implements BindingConfiguration {

	public static final ModelBindingConfiguration NULL_CONFIG;
	static {
		NULL_CONFIG = new ModelBindingConfiguration(null);
	}

	private final String packageName;

	public ModelBindingConfiguration(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}
}
