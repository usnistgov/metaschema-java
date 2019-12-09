package gov.nist.secauto.metaschema.codegen.support;

import java.io.StringWriter;

public class PaddedStringWriter extends StringWriter {
	private final String linePadding;

	public PaddedStringWriter(String linePadding) {
		this.linePadding = linePadding;
	}

	public PaddedStringWriter(String linePadding, int initialSize) {
		super(initialSize);
		this.linePadding = linePadding;
	}

	protected String getLinePadding() {
		return linePadding;
	}

}
