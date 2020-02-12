package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingException;

public interface XmlParsePlan<CLASS> {

	CLASS parse(XmlParsingContext parsingContext) throws BindingException;

}
