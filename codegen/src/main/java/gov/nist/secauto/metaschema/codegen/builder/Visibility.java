package gov.nist.secauto.metaschema.codegen.builder;

public enum Visibility {
	PRIVATE("private"),
	PACKAGE_PRIVATE(null),
	PROTECTED("protected"),
	PUBLIC("public");

	private final String modifier;

	private Visibility(String modifier) {
		this.modifier = modifier;
	}

	protected String getModifier() {
		return modifier;
	}
}
