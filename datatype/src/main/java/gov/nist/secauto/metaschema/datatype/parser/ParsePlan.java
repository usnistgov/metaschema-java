package gov.nist.secauto.metaschema.datatype.parser;

public interface ParsePlan<READER, CLASS> {

	CLASS parse(READER reader) throws BindingException;
}
