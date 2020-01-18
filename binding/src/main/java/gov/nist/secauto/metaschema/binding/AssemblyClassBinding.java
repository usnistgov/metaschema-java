package gov.nist.secauto.metaschema.binding;

import java.util.List;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.AssemblyXmlParsePlan;
import gov.nist.secauto.metaschema.binding.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.property.ModelUtil;
import gov.nist.secauto.metaschema.binding.writer.json.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.writer.xml.AssemblyXmlWriter;

public class AssemblyClassBinding<CLASS> extends AbstractClassBinding<CLASS, AssemblyXmlParsePlan<CLASS>, AssemblyXmlWriter<CLASS>> {
	private final List<ModelItemPropertyBinding> modelItemPropertyBindings;
	private final RootWrapper rootWrapper;
	private QName rootName;
	private AssemblyJsonWriter<CLASS> assemblyJsonWriter;

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

	public QName getRootQName() {
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
	public AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException {
		synchronized (this) {
			if (assemblyJsonWriter == null) {
				assemblyJsonWriter = newAssemblyJsonWriter();
			}
			return assemblyJsonWriter;
		}
	}

	@Override
	public AssemblyXmlParsePlan<CLASS> newXmlParsePlan(BindingContext bindingContext) throws BindingException {
		return new AssemblyXmlParsePlan<CLASS>(this, bindingContext);
	}

	@Override
	public AssemblyXmlWriter<CLASS> newXmlWriter() {
		return new AssemblyXmlWriter<CLASS>(this);
	}

	public AssemblyJsonWriter<CLASS> newAssemblyJsonWriter() {
		return new AssemblyJsonWriter<CLASS>(this);
	}

}
