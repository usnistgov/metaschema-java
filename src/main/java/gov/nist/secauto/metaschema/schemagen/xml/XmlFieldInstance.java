package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractFieldInstance;
import gov.nist.secauto.metaschema.schemagen.InfoElementDefinition;

public class XmlFieldInstance extends AbstractFieldInstance {
	private final FieldDocument.Field xField;

	public XmlFieldInstance(FieldDocument.Field xField, InfoElementDefinition parent) {
		super(parent);
		this.xField = xField;
	}

	@Override
	public String getName() {
		return xField.getRef();
	}
}
