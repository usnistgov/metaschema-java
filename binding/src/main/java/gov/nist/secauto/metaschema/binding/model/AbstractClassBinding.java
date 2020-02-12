package gov.nist.secauto.metaschema.binding.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

abstract class AbstractClassBinding<CLASS, XML_PARSE_PLAN extends XmlParsePlan<CLASS>, XML_WRITER extends XmlWriter> implements ClassBinding<CLASS> {
	private final Class<CLASS> clazz;
	private final List<FlagPropertyBinding> flagPropertyBindings;
	private final FlagPropertyBinding jsonKeyFlagPropertyBinding;
	private XML_PARSE_PLAN xmlParsePlan;
	private XML_WRITER xmlWriter;

	public AbstractClassBinding(Class<CLASS> clazz) throws BindingException {
		Objects.requireNonNull(clazz, "clazz");
		this.clazz = clazz;
		this.flagPropertyBindings = Collections.unmodifiableList(ClassIntrospector.getFlagPropertyBindings(clazz));

		FlagPropertyBinding jsonKey = null;
		for (FlagPropertyBinding flag : flagPropertyBindings) {
			if (flag.isJsonKey()) {
				jsonKey = flag;
				break;
			}
		}
		this.jsonKeyFlagPropertyBinding = jsonKey;
	}

	@Override
	public XML_PARSE_PLAN getXmlParsePlan(BindingContext bindingContext) throws BindingException {
		synchronized (this) {
			if (xmlParsePlan == null) {
				xmlParsePlan = newXmlParsePlan(bindingContext);
			}
			return xmlParsePlan;
		}
	}

	protected abstract XML_PARSE_PLAN newXmlParsePlan(BindingContext bindingContext) throws BindingException;

	@Override
	public XML_WRITER getXmlWriter() throws BindingException {
		synchronized (this) {
			if (xmlWriter == null) {
				xmlWriter = newXmlWriter();
			}
			return xmlWriter;
		}
	}

	protected abstract XML_WRITER newXmlWriter();

	@Override
	public Class<CLASS> getClazz() {
		return clazz;
	}

	@Override
	public List<FlagPropertyBinding> getFlagPropertyBindings() {
		return flagPropertyBindings;
	}

	@Override
	public FlagPropertyBinding getJsonKeyFlagPropertyBinding() {
		return jsonKeyFlagPropertyBinding;
	}

	@Override
	public Map<String, NamedPropertyBinding> getNamedPropertyBindings(BindingContext bindingContext, NamedPropertyBindingFilter filter) throws BindingException {
		Map<String, NamedPropertyBinding> retval = new HashMap<>();
		List<FlagPropertyBinding> flags = getFlagPropertyBindings();
		
		
		if (!flags.isEmpty()) {
			for (NamedPropertyBinding binding : flags) {
				String jsonFieldName = binding.getJsonFieldName(bindingContext);
				if (jsonFieldName != null) {
					if (retval.put(jsonFieldName, binding) != null) {
						throw new BindingException(String.format("The same field name '%s' is used on multiple properties.", jsonFieldName));
					}
				}
			}
		}
		return retval;
	}

	@Override
	public boolean hasRootWrapper() {
		return getRootWrapper() != null;
	}

	@Override
	public CLASS newInstance() throws BindingException {
		Class<CLASS> clazz = getClazz();
		CLASS retval;
		try {
			Constructor<CLASS> constructor = (Constructor<CLASS>) clazz.getDeclaredConstructor();
			retval = constructor.newInstance();
		} catch (NoSuchMethodException e) {
			String msg = String.format("Class '%s' does not have a required no-arg constructor.", clazz.getName());
			throw new BindingException(msg);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new BindingException(e);
		}
		return retval;
	}

}
