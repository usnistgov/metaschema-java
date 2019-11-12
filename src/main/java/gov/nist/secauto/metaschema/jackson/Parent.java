package gov.nist.secauto.metaschema.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonRootName(value = "parent")
public class Parent {

	@JsonProperty
    @JsonDeserialize(contentUsing = Deser.class)
	public Map<String, Child> props;

	public Map<String, Child> getProps() {
		return props;
	}

	public static class Deser extends JsonDeserializer<Child> {

	    @Override
	    public Child deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
	        String id = ctxt.getParser().getCurrentName();

	        Child child = p.readValueAs(Child.class);

	        child.setId(id);  // This copies the key name to the value object

	        return child;
	    }
	}
}
