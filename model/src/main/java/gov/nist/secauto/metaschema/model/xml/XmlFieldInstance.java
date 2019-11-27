package gov.nist.secauto.metaschema.model.xml;

import java.math.BigInteger;

import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument;
import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument.Field.InXml;
import gov.nist.csrc.ns.oscal.metaschema.x10.XmlGroupBehavior;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.DataType;
import gov.nist.secauto.metaschema.model.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.JsonGroupBehavior;

public class XmlFieldInstance extends AbstractFieldInstance {
//	private static final Logger logger = LogManager.getLogger(XmlFieldInstance.class);

	private final FieldDocument.Field xField;

	public XmlFieldInstance(FieldDocument.Field xField, InfoElementDefinition parent) {
		super(parent);
		this.xField = xField;
	}

	@Override
	public String getName() {
		return getXmlField().getRef();
	}

	@Override
	public String getFormalName() {
		return getDefinition().getFormalName();
	}

	@Override
	public MarkupString getDescription() {
		MarkupString retval = null;
		if (getXmlField().isSetDescription()) {
			retval = MarkupStringConverter.toMarkupString(getXmlField().getDescription());
		} else if (isReference()) {
			retval = getDefinition().getDescription();
		}
		return retval;
	}

	@Override
	public DataType getDatatype() {
		return getDefinition().getDatatype();
	}

	@Override
	public int getMinOccurs() {
		int retval = 0;
		if (getXmlField().isSetMinOccurs()) {
			retval = getXmlField().getMinOccurs().intValueExact();
		}
		return retval;
	}

	@Override
	public int getMaxOccurs() {
		int retval = 1;
		if (getXmlField().isSetMaxOccurs()) {
			Object value = getXmlField().getMaxOccurs();
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
	public String getInstanceName() {
		return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : getName();
	}

	protected FieldDocument.Field getXmlField() {
		return xField;
	}

	@Override
	public boolean hasXmlWrapper() {
		// default value
		boolean retval = true;
		if (getXmlField().isSetInXml()) {
			retval = InXml.WITH_WRAPPER.equals(getXmlField().getInXml());
		}
		return retval;
	}

	@Override
	public JsonGroupBehavior getGroupBehaviorJson() {
		JsonGroupBehavior retval = JsonGroupBehavior.SINGLETON_OR_LIST;
		if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInJson()) {
			retval = JsonGroupBehavior.lookup(getXmlField().getGroupAs().getInJson());
		}
		return retval;
	}

	@Override
	public boolean isGroupBehaviorXmlGrouped() {
		// the default
		boolean retval = true;
		if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInXml()) {
			retval = XmlGroupBehavior.GROUPED.equals(getXmlField().getGroupAs().getInXml());
		}
		return retval;
	}

}
