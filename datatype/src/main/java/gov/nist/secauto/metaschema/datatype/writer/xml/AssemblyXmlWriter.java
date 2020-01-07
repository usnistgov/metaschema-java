package gov.nist.secauto.metaschema.datatype.writer.xml;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.binding.AssemblyClassBinding;
import gov.nist.secauto.metaschema.datatype.binding.BindingContext;
import gov.nist.secauto.metaschema.datatype.binding.property.ModelUtil;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class AssemblyXmlWriter<CLASS> extends AbstractXmlWriter<CLASS, AssemblyClassBinding<CLASS>> {
	public AssemblyXmlWriter(AssemblyClassBinding<CLASS> classBinding, BindingContext bindingContext) {
		super(classBinding);
	}

	@Override
	public void writeXml(CLASS obj, QName name, XmlWritingContext writingContext) throws BindingException {
		if (name == null && getClassBinding().isRootElement()) {
			name = getClassBinding().getRootQName();
		}
		super.writeXml(obj, name, writingContext);
	}

	@Override
	protected void writeBody(CLASS obj, XmlWritingContext writingContext) {
		// TODO Auto-generated method stub
		
	}
}
