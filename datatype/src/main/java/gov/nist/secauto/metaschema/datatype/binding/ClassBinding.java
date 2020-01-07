package gov.nist.secauto.metaschema.datatype.binding;

import java.util.List;

import gov.nist.secauto.metaschema.datatype.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.writer.xml.XmlWriter;

public interface ClassBinding<CLASS> {
	Class<CLASS> getClazz();
	List<FlagPropertyBinding> getFlagPropertyBindings();
	XmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException;
	XmlWriter<CLASS> newXmlWriter(BindingContext bindingContext);
}
