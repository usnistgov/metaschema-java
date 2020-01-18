package gov.nist.secauto.metaschema.binding;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.parser.xml.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.writer.xml.XmlWriter;

public abstract class AbstractClassBinding<CLASS, XML_PARSE_PLAN extends XmlParsePlan<CLASS>, XML_WRITER extends XmlWriter> implements ClassBinding<CLASS> {
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
}
