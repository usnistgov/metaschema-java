package gov.nist.secauto.metaschema.model.xml;

import java.util.Collections;
import java.util.Map;

import gov.nist.csrc.ns.oscal.metaschema.x10.DefineFlagDocument;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractFlagDefinition;
import gov.nist.secauto.metaschema.model.DataType;
import gov.nist.secauto.metaschema.model.FlagDefinition;
import gov.nist.secauto.metaschema.model.FlagInstance;

public class XmlFlagDefinition extends AbstractFlagDefinition<XmlMetaschema> implements FlagDefinition {
	private final DefineFlagDocument.DefineFlag xFlag;

	public XmlFlagDefinition(DefineFlagDocument.DefineFlag xFlag, XmlMetaschema metaschema) {
		super(metaschema);
		this.xFlag = xFlag;
	}

	@Override
	public String getName() {
		return getXmlFlag().getName();
	}

	@Override
	public String getFormalName() {
		return getXmlFlag().getFormalName();
	}

	@Override
	public MarkupString getDescription() {
		return MarkupStringConverter.toMarkupString(getXmlFlag().getDescription());
	}

	@Override
	public DataType getDatatype() {
		DataType retval;
		if (getXmlFlag().isSetAsType()) {
			retval = DataType.lookup(getXmlFlag().getAsType());
		} else {
			// the default
			retval = DataType.STRING;
		}
		return retval;
	}

	protected DefineFlagDocument.DefineFlag getXmlFlag() {
		return xFlag;
	}

	@Override
	public FlagInstance getFlagInstanceByName(String name) {
		return null;
	}

	@Override
	public Map<String, ? extends FlagInstance> getFlagInstances() {
		return Collections.emptyMap();
	}
}
