package gov.nist.secauto.metaschema.binding.parser.xml;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.binding.parser.ParsePlan;

public interface XmlParsePlan<CLASS> extends ParsePlan<XmlParsingContext, XMLEventReader2, CLASS> {

}
