package gov.nist.secauto.metaschema.model.xml;

import java.math.BigInteger;

import gov.nist.csrc.ns.oscal.metaschema.x10.AssemblyDocument;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.model.InfoElementDefinition;

public class XmlAssemblyInstance extends AbstractAssemblyInstance {
//	private static final Logger logger = LogManager.getLogger(XmlAssemblyInstance.class);

	private final AssemblyDocument.Assembly xAssembly;


	public XmlAssemblyInstance(AssemblyDocument.Assembly xAssembly, InfoElementDefinition parent) {
		super(parent);
		this.xAssembly = xAssembly;
	}

	@Override
	public String getName() {
		return getXmlAssembly().getRef();
	}

	@Override
	public String getFormalName() {
		return getAssemblyDefinition().getFormalName();
	}

	@Override
	public MarkupString getDescription() {
		MarkupString retval = null;
		if (getXmlAssembly().isSetDescription()) {
			retval = MarkupStringConverter.toMarkupString(getXmlAssembly().getDescription());
		} else if (isReference()) {
			retval = getAssemblyDefinition().getDescription();
		}
		return retval;
	}

	@Override
	public int getMinOccurs() {
		int retval = 0;
		if (getXmlAssembly().isSetMinOccurs()) {
			retval = getXmlAssembly().getMinOccurs().intValueExact();
		}
		return retval;
	}

	@Override
	public int getMaxOccurs() {
		int retval = 1;
		if (getXmlAssembly().isSetMaxOccurs()) {
			Object value = getXmlAssembly().getMaxOccurs();
			if (value instanceof String) {
				// unbounded
				retval = -1;
			} else if (value instanceof BigInteger) {
				retval = ((BigInteger)value).intValueExact();
			}
		}
		return retval;
	}

	@Override
	public String getGroupAsName() {
		return getXmlAssembly().isSetGroupAs() ? getXmlAssembly().getGroupAs().getName() : null;
	}

	protected AssemblyDocument.Assembly getXmlAssembly() {
		return xAssembly;
	}
}
