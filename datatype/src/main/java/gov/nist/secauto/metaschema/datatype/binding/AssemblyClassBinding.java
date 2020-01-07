package gov.nist.secauto.metaschema.datatype.binding;

import java.util.List;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.datatype.annotations.RootWrapper;
import gov.nist.secauto.metaschema.datatype.binding.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.datatype.binding.property.ModelUtil;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.AssemblyXmlParsePlan;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.datatype.writer.xml.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.datatype.writer.xml.XmlWriter;

public class AssemblyClassBinding<CLASS> extends AbstractClassBinding<CLASS> {
	private final List<ModelItemPropertyBinding> modelItemPropertyBindings;
	private final RootWrapper rootWrapper;
	private QName rootName;

	public AssemblyClassBinding(Class<CLASS> clazz) throws BindingException {
		super(clazz);
		this.modelItemPropertyBindings = ClassIntrospector.getModelItemBindings(clazz);
		this.rootWrapper = clazz.getAnnotation(RootWrapper.class);
	}

	public List<ModelItemPropertyBinding> getModelItemPropertyBindings() {
		return modelItemPropertyBindings;
	}

	public boolean isRootElement() {
		return rootWrapper != null;
	}

	public QName getRootQName() throws BindingException {
		synchronized (this) {
			if (rootWrapper!= null && rootName == null) {
				String namespace = ModelUtil.resolveNamespace(rootWrapper.namespace(), getClazz());
				String localName = ModelUtil.resolveLocalName(rootWrapper.name(), getClazz().getSimpleName());
				rootName = new QName(namespace,localName);
			}
			return rootName;
		}
	}

	@Override
	public XmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
		return new AssemblyXmlParsePlan<CLASS>(this, bindingContext);
	}

	@Override
	public XmlWriter<CLASS> newXmlWriter(BindingContext bindingContext) {
		return new AssemblyXmlWriter<CLASS>(this, bindingContext);
	}

}
