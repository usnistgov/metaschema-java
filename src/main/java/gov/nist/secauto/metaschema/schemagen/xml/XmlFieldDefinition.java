package gov.nist.secauto.metaschema.schemagen.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFieldDocument.DefineField;
import gov.nist.csrc.ns.oscal.metaschema.x10.FlagDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractFieldDefinition;
import gov.nist.secauto.metaschema.schemagen.FieldDefinition;

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
		return xField.getName();
	}

	@Override
	public Map<String, XmlFlagInstance> getFlagInstances() {
		return flagInstances;
	}
}
