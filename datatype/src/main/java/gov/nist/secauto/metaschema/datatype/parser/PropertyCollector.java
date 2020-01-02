package gov.nist.secauto.metaschema.datatype.parser;

public interface PropertyCollector {
	void add(Object item) throws BindingException;
	void applyCollection(Object obj) throws BindingException;

}
