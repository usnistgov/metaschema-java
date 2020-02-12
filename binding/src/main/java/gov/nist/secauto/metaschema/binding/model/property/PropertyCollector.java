package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface PropertyCollector {
	void add(Object item) throws BindingException;
	void applyCollection(Object obj) throws BindingException;

}
