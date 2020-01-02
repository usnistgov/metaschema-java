package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.stax2.XMLEventReader2;

import gov.nist.secauto.metaschema.datatype.annotations.RootWrapper;
import gov.nist.secauto.metaschema.datatype.binding.ClassIntrospector;
import gov.nist.secauto.metaschema.datatype.binding.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.datatype.binding.FlagPropertyBinding;
import gov.nist.secauto.metaschema.datatype.binding.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.ParsePlan;

public class XmlParsePlanBuilder<CLASS> {
	private final Class<CLASS> clazz;

	public XmlParsePlanBuilder(Class<CLASS> clazz) {
		this.clazz = clazz;
	}

	protected Class<CLASS> getClazz() {
		return clazz;
	}

	public ParsePlan<XMLEventReader2, CLASS> build(XmlParser parser) throws BindingException {
		Map<QName, XmlAttributePropertyParser> attributes = getAttributeParsers(parser);

		FieldValueXmlPropertyParser fieldValueParser = getFieldValueParser(parser);
		List<XmlObjectPropertyParser> model = getModelParsers(parser);
		if (fieldValueParser != null && !model.isEmpty()) {
			throw new BindingException(String.format(
					"Class '%s' contains a FieldValue annotation and Field and/or Assembly annotations. FieldValue can only be used with Flag annotations.",
					getClazz().getName()));
		}

		ParsePlan<XMLEventReader2, CLASS> retval;
		if (fieldValueParser != null) {
			retval = new FieldXmlParsePlan<CLASS>(parser, getClazz(), attributes, fieldValueParser);
		} else {
			retval = new AssemblyXmlParsePlan<CLASS>(parser, getClazz(), attributes, model);
		}
		return retval;
	}

	private FieldValueXmlPropertyParser getFieldValueParser(XmlParser parser) throws BindingException {
		FieldValuePropertyBinding binding = ClassIntrospector.getFieldValueBinding(getClazz());
		return binding != null ? binding.newXmlPropertyParser(parser) : null;
	}

	private List<XmlObjectPropertyParser> getModelParsers(XmlParser parser) throws BindingException {
		List<ModelItemPropertyBinding> bindings = ClassIntrospector.getModelItemBindings(getClazz());
		List<XmlObjectPropertyParser> retval;
		if (bindings.isEmpty()) {
			retval = Collections.emptyList();
		} else {
			retval = new ArrayList<>(bindings.size());
			for (ModelItemPropertyBinding binding : bindings) {
				retval.add(binding.newXmlPropertyParser(parser));
			}
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	protected Map<QName, XmlAttributePropertyParser> getAttributeParsers(XmlParser parser) throws BindingException {
		List<FlagPropertyBinding> bindings = ClassIntrospector.getFlagPropertyBindings(getClazz());
		Map<QName, XmlAttributePropertyParser> retval;
		if (bindings.isEmpty()) {
			retval = Collections.emptyMap();
		} else {
			retval = new LinkedHashMap<>();
			for (FlagPropertyBinding binding : bindings) {
				XmlAttributePropertyParser propertyParser = binding.newXmlPropertyParser(parser);
				// for an attribute, only a single QName should be handled
				QName handledQName = propertyParser.getHandledQName();
				retval.put(handledQName, propertyParser);
			}
			retval = Collections.unmodifiableMap(retval);
		}
		return retval;
	}

}
