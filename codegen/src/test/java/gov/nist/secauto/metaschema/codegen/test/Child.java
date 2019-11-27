package gov.nist.secauto.metaschema.codegen.test;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Child {
	private String id;

	@JsonProperty(value = "STRVALUE")
	public String value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
