package gov.nist.secauto.metaschema.datatype.parser;

public interface ParsePlan<CONTEXT extends ParsingContext<READER>, READER, CLASS> {

	CLASS parse(CONTEXT parser) throws BindingException;
}
