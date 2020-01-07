package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.math.BigDecimal;

import gov.nist.secauto.metaschema.datatype.Decimal;

public class DecimalAdapter extends SimpleJavaTypeAdapter<Decimal> {

	@Override
	public Decimal parseValue(String value) {
		return new Decimal(new BigDecimal(value));
	}

}
