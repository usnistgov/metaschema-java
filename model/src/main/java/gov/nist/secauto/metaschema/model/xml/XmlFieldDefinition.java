package gov.nist.secauto.metaschema.model.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument.DefineField;
import gov.nist.csrc.ns.oscal.metaschema.x10.FlagDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.JsonValueKeyDocument.JsonValueKey;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.model.DataType;
import gov.nist.secauto.metaschema.model.FieldDefinition;
import gov.nist.secauto.metaschema.model.FlagInstance;

public class XmlFieldDefinition extends AbstractFieldDefinition<XmlMetaschema> implements FieldDefinition {
	private final DefineFieldDocument.DefineField xField;
	private final Map<String, XmlFlagInstance> flagInstances;

	public XmlFieldDefinition(DefineField xField, XmlMetaschema metaschema) {
		super(metaschema);
		this.xField = xField;

		int numFlags = xField.sizeOfFlagArray();
		if (numFlags > 0) {
			Map<String, XmlFlagInstance> flagInstances = new LinkedHashMap<>();
			for (FlagDocument.Flag xFlag : xField.getFlagList()) {
				XmlFlagInstance flagInstance = new XmlFlagInstance(xFlag, this);
				flagInstances.put(flagInstance.getName(), flagInstance);
			}
			this.flagInstances = Collections.unmodifiableMap(flagInstances);
		} else {
			flagInstances = Collections.emptyMap();
		}
	}

	@Override
	public String getName() {
		return getXmlField().getName();
	}

	@Override
	public String getFormalName() {
		return getXmlField().getFormalName();
	}

	@Override
	public MarkupString getDescription() {
		return MarkupStringConverter.toMarkupString(getXmlField().getDescription());
	}

	@Override
	public Map<String, XmlFlagInstance> getFlagInstances() {
		return flagInstances;
	}

	@Override
	public DataType getDatatype() {
		DataType retval;
		if (getXmlField().isSetAsType()) {
			retval = DataType.lookup(getXmlField().getAsType());
		} else {
			// the default
			retval = DataType.STRING;
		}
		return retval;
	}

	protected DefineFieldDocument.DefineField getXmlField() {
		return xField;
	}

	@Override
	public boolean hasJsonValueKey() {
		return getXmlField().isSetJsonValueKey();
	}

	@Override
	public Object getJsonValueKey() {
		Object retval = null;
		if (getXmlField().isSetJsonValueKey()) {
			JsonValueKey jvk = getXmlField().getJsonValueKey();
			if (jvk.isSetFlagName()) {
				retval = getFlagInstances().get(jvk.getFlagName());
			} else {
				retval = jvk.getStringValue();
			}
		}
		return retval;
	}

	@Override
	public FlagInstance getJsonValueKeyFlagInstance() {
		FlagInstance retval = null;
		if (getXmlField().isSetJsonValueKey()) {
			retval = getFlagInstanceByName(getXmlField().getJsonValueKey().getFlagName());
		}
		return retval;
	}

	@Override
	public String getJsonValueKeyName() {
		String retval = null;
		
		if (getXmlField().isSetJsonValueKey()) {
			retval = getXmlField().getJsonValueKey().getStringValue();
		}

		if (retval == null || retval.isEmpty()) {
			switch (getDatatype()) {
			case MARKUP_LINE:
				retval = "RICHTEXT";
				break;
			case MARKUP_MULTILINE:
				retval = "PROSE";
				break;
			default:
				retval = "STRVALUE";
			}
		}
		return retval;
	}

	@Override
	public boolean hasJsonKey() {
		return getXmlField().isSetJsonKey();
	}
	
	@Override
	public FlagInstance getJsonKeyFlagInstance() {
		FlagInstance retval = null;
		if (hasJsonKey()) {
			retval = getFlagInstanceByName(getXmlField().getJsonKey().getFlagName());
		}
		return retval;
	}

	@Override
	public boolean isCollapsible() {
		// default value
		boolean retval = true;
		if (getXmlField().isSetCollapsible()) {
			retval = gov.nist.csrc.ns.oscal.metaschema.x10.Boolean.YES.equals(getXmlField().getCollapsible());
		}
		return retval;
	}
}
