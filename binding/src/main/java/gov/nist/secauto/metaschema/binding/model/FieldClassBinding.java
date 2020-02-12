package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.writer.FieldXmlWriter;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;

public interface FieldClassBinding<CLASS> extends ClassBinding<CLASS> {

	FieldValuePropertyBinding getFieldValuePropertyBinding();

	boolean isCollapsible();

	FlagPropertyBinding getJsonValueKeyFlagPropertyBinding();
	
	@Override
	FieldXmlWriter<CLASS> getXmlWriter() throws BindingException;

}
