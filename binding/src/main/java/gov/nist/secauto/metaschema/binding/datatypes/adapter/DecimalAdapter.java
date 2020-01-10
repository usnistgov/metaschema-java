package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.math.BigDecimal;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.Decimal;

public class DecimalAdapter extends SimpleJavaTypeAdapter<Decimal> {

	@Override
	public Decimal parse(String value) {
		return new Decimal(new BigDecimal(value));
	}

}
