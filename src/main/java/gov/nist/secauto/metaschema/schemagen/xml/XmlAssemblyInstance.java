package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.csrc.ns.oscal.metaschema.x10.AssemblyDocument;
import gov.nist.secauto.metaschema.schemagen.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.schemagen.InfoElementDefinition;

public class XmlAssemblyInstance extends AbstractAssemblyInstance {
	private final AssemblyDocument.Assembly xAssembly;


	public XmlAssemblyInstance(AssemblyDocument.Assembly xAssembly, InfoElementDefinition parent) {
		super(parent);
		this.xAssembly = xAssembly;
	}

	@Override
	public String getName() {
		return xAssembly.getRef();
	}

}
