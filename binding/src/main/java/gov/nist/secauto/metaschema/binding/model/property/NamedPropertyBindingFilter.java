package gov.nist.secauto.metaschema.binding.model.property;

@FunctionalInterface
public interface NamedPropertyBindingFilter {
	boolean filter(NamedPropertyBinding binding);
}