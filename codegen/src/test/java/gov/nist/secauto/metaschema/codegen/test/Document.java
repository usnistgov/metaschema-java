package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Document {
	public Parent parent;

	public Parent getParent() {
		return parent;
	}

	public void setParent(Parent parent) {
		this.parent = parent;
	}

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
//		mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
//		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
		Parent parent = mapper.readValue(new File(
				"/home/davidwal/github/david-waltermire-nist/OSCAL/build/metaschema/unit-testing/group-as/group-as-by-key_test_valid_PASS.json"),
				Document.class).getParent();
		for (Map.Entry<String, Child> entry : parent.getProps().entrySet()) {
			System.out.println(String.format("%s = (%s)%s", entry.getKey(), entry.getValue().getId(), entry.getValue().getValue()));
		}
	}

}
