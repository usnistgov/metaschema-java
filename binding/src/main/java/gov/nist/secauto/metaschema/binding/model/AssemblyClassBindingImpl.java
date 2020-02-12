package gov.nist.secauto.metaschema.binding.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.AssemblyJsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.AssemblyXmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.AssemblyXmlWriter;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

class AssemblyClassBindingImpl<CLASS> extends AbstractClassBinding<CLASS, AssemblyXmlParsePlan<CLASS>, AssemblyXmlWriter<CLASS>> implements AssemblyClassBinding<CLASS> {
	private final List<ModelItemPropertyBinding> modelItemPropertyBindings;
	private final RootWrapper rootWrapper;
	private QName rootName;
	private AssemblyJsonWriter<CLASS> assemblyJsonWriter;

	public AssemblyClassBindingImpl(Class<CLASS> clazz) throws BindingException {
		super(clazz);
		this.modelItemPropertyBindings = ClassIntrospector.getModelItemBindings(clazz);
		this.rootWrapper = clazz.getAnnotation(RootWrapper.class);
	}

	@Override
	public List<ModelItemPropertyBinding> getModelItemPropertyBindings() {
		return modelItemPropertyBindings;
	}

	@Override
	public Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext, PropertyBindingFilter filter) throws BindingException {
		Map<String, PropertyBinding> retval = super.getJsonPropertyBindings(bindingContext, filter);
		List<? extends NamedPropertyBinding> modelItems = getModelItemPropertyBindings();
		
		if (!modelItems.isEmpty()) {
			for (NamedPropertyBinding binding : modelItems) {
				String jsonFieldName = binding.getJsonFieldName(bindingContext);
				if (jsonFieldName != null) {
					if (retval.put(jsonFieldName, binding) != null) {
						throw new BindingException(String.format("The same field name '%s' is used on multiple properties.", jsonFieldName));
					}
				}
			}
		}
		return Collections.unmodifiableMap(retval);
	}

	@Override
	public boolean isRootElement() {
		return rootWrapper != null;
	}

	@Override
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

	@Override
	public RootWrapper getRootWrapper() {
		return rootWrapper;
	}

	@Override
	public AssemblyJsonReader<CLASS> getJsonReader(BindingContext bindingContext) throws BindingException {
		return new AssemblyJsonReader<CLASS>(this);
	}

}
