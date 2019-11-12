package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFlagDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractFlagDefinition;
import gov.nist.secauto.metaschema.schemagen.FlagDefinition;

public class XmlFlagDefinition extends AbstractFlagDefinition<XmlMetaschema> implements FlagDefinition {
	private final DefineFlagDocument.DefineFlag xFlag;

	public XmlFlagDefinition(DefineFlagDocument.DefineFlag xFlag, XmlMetaschema metaschema) {
		super(metaschema);
		this.xFlag = xFlag;
	}

	@Override
	public String getName() {
		return xFlag.getName();
	}
}
