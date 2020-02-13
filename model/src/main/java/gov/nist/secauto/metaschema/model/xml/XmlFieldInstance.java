/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.metaschema.model.xml;

import java.math.BigInteger;

import gov.nist.itl.metaschema.model.xml.FieldDocument;
import gov.nist.itl.metaschema.model.xml.FieldDocument.Field.InXml;
import gov.nist.secauto.metaschema.datatype.markup.MarkupLine;
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
	public MarkupLine getDescription() {
		MarkupLine retval = null;
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
