package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.csrc.ns.oscal.metaschema.x10.FlagDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractFlagInstance;
import gov.nist.secauto.metaschema.schemagen.FlagDefinition;
import gov.nist.secauto.metaschema.schemagen.InfoElementDefinition;
import gov.nist.secauto.metaschema.schemagen.Metaschema;
import gov.nist.secauto.metaschema.schemagen.Type;

public class XmlFlagInstance extends AbstractFlagInstance {
	private final FlagDocument.Flag xFlag;
	private final LocalFlagDefinition localFlagDefinition;

	public XmlFlagInstance(FlagDocument.Flag xFlag, InfoElementDefinition parent) {
		super(parent);
		this.xFlag = xFlag;

		if (xFlag.isSetName()) {
			localFlagDefinition = new LocalFlagDefinition();
		} else {
			localFlagDefinition = null;
		}
	}

	@Override
	protected FlagDefinition getLocalFlagDefinition() {
		return localFlagDefinition;
	}

	@Override
	public String getName() {
		return xFlag.isSetRef() ? xFlag.getRef() : xFlag.getName();
	}

	private class LocalFlagDefinition implements FlagDefinition {

		public LocalFlagDefinition() {
		}

		@Override
		public String getName() {
			return XmlFlagInstance.this.getName();
		}

		@Override
		public Type getType() {
			return Type.FLAG;
		}

		@Override
		public Metaschema getContainingMetaschema() {
			return XmlFlagInstance.this.getContainingMetaschema();
		}
		
	}
}
