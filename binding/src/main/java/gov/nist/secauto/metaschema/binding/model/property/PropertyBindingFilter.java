package gov.nist.secauto.metaschema.binding.model.property;

@FunctionalInterface
public interface PropertyBindingFilter {
	boolean filter(PropertyBinding binding);
}