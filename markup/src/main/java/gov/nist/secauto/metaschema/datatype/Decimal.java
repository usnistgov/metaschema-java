package gov.nist.secauto.metaschema.datatype;

import java.math.BigDecimal;

public class Decimal extends AbstractDatatype<Decimal, BigDecimal> {

	public Decimal(BigDecimal value) {
		super(value);
	}

	@Override
	public Decimal copy() {
		return new Decimal(getValue());
	}

}
