package gov.nist.secauto.metaschema.binding.parser;

public interface ParsePlan<CONTEXT extends ParsingContext<READER>, READER, CLASS> {

	CLASS parse(CONTEXT parser) throws BindingException;
}
