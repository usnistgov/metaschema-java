package gov.nist.secauto.metaschema.datatype.jaxb;

import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.AttributeTransformer;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

import gov.nist.secauto.metaschema.datatype.MarkupString;

public class MarkupStringReadTransformer implements AttributeTransformer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AbstractTransformationMapping mapping;

	@Override
	public void initialize(AbstractTransformationMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public MarkupString buildAttributeValue(Record record, Object object, Session session) {
		return null;
	}

}
