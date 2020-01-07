package gov.nist.secauto.metaschema.datatype.binding.property;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public interface PropertyCollector {
	void add(Object item) throws BindingException;
	void applyCollection(Object obj) throws BindingException;

}
