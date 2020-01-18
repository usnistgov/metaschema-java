package gov.nist.secauto.metaschema.binding.writer.json;

import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;

@FunctionalInterface
public interface FlagPropertyBindingFilter {
	boolean filter(FlagPropertyBinding flag);
}