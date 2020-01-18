package gov.nist.secauto.metaschema.model.xml;

import java.math.BigInteger;

import gov.nist.itl.metaschema.model.xml.FieldDocument;
import gov.nist.itl.metaschema.model.xml.FieldDocument.Field.InXml;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.model.info.definitions.DataType;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.info.instances.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.info.instances.XmlGroupAsBehavior;

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
	public JsonGroupAsBehavior getJsonGroupAsBehavior() {
		JsonGroupAsBehavior retval = JsonGroupAsBehavior.SINGLETON_OR_LIST;
		if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInJson()) {
			retval = JsonGroupAsBehavior.lookup(getXmlField().getGroupAs().getInJson());
		}
		return retval;
	}

	@Override
	public XmlGroupAsBehavior getXmlGroupAsBehavior() {
		XmlGroupAsBehavior retval = XmlGroupAsBehavior.UNGROUPED;
		if (getXmlField().isSetGroupAs() && getXmlField().getGroupAs().isSetInXml()) {
			retval = XmlGroupAsBehavior.lookup(getXmlField().getGroupAs().getInXml());
		}
		return retval;
	}

}
