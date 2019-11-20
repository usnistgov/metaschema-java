package gov.nist.secauto.metaschema.model.xml;

import java.math.BigInteger;

import gov.nist.csrc.ns.oscal.metaschema.x10.FieldDocument;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.model.AbstractFieldInstance;
import gov.nist.secauto.metaschema.model.DataType;
import gov.nist.secauto.metaschema.model.InfoElementDefinition;

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
		return getFieldDefinition().getFormalName();
	}

	@Override
	public MarkupString getDescription() {
		MarkupString retval = null;
		if (getXmlField().isSetDescription()) {
			retval = MarkupStringConverter.toMarkupString(getXmlField().getDescription());
		} else if (isReference()) {
			retval = getFieldDefinition().getDescription();
		}
		return retval;
	}

	@Override
	public DataType getDatatype() {
		return getFieldDefinition().getDatatype();
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
	public String getGroupAsName() {
		return getXmlField().isSetGroupAs() ? getXmlField().getGroupAs().getName() : null;
	}

	protected FieldDocument.Field getXmlField() {
		return xField;
	}
}
