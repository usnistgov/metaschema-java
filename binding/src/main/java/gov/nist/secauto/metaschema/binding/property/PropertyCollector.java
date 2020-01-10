package gov.nist.secauto.metaschema.binding.property;

import gov.nist.secauto.metaschema.binding.parser.BindingException;

public interface PropertyCollector {
	void add(Object item) throws BindingException;
	void applyCollection(Object obj) throws BindingException;

}
