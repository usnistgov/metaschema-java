package gov.nist.secauto.metaschema.binding.io.json.parser;

import java.util.List;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public interface BoundObjectParser<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> {

	List<CLASS> parseObjects() throws BindingException;

	JsonParsingContext getParsingContext();

}
