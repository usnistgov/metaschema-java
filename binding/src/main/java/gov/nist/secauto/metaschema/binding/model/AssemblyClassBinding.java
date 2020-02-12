package gov.nist.secauto.metaschema.binding.model;

import java.util.List;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.writer.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;

public interface AssemblyClassBinding<CLASS> extends ClassBinding<CLASS> {

	List<ModelItemPropertyBinding> getModelItemPropertyBindings();

	boolean isRootElement();

	QName getRootQName();

	@Override
	AssemblyXmlWriter<CLASS> getXmlWriter() throws BindingException;
	
}
