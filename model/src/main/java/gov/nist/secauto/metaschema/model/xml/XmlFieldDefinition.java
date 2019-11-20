package gov.nist.secauto.metaschema.model.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument.DefineField;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.model.DataType;
import gov.nist.secauto.metaschema.model.FieldDefinition;
import gov.nist.csrc.ns.oscal.metaschema.x10.FlagDocument;

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
	
}
