package gov.nist.secauto.metaschema.binding;

import java.util.List;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWriter;

public interface ClassBinding<CLASS> {
	Class<CLASS> getClazz();
	List<FlagPropertyBinding> getFlagPropertyBindings();
	XmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException;
	XmlWriter newXmlWriter(BindingContext bindingContext);
}
