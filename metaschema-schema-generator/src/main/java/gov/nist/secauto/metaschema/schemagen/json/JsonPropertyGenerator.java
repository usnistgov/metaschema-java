/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.schemagen.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.common.ModelType;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonPropertyGenerator {
  private JsonPropertyGenerator() {
    // disable construction
  }

  public static void
      generateFlagProperty(
          @NotNull IFlagInstance flag,
          @NotNull DatatypeManager datatypeManager,
          @NotNull InstanceProperties properties) {
    String propertyName = flag.getUseName();
    properties.addProperty(propertyName,
        JsonNodeFactory.instance.objectNode()
            .put("$ref", datatypeManager.getJsonDefinitionRefForDefinition(flag.getDefinition()).toString()));
    if (flag.isRequired()) {
      properties.addRequired(propertyName);
    }
  }

  public static void generateSimpleFieldValue(
      @NotNull IFieldDefinition definition,
      @NotNull DatatypeManager datatypeManager,
      @NotNull InstanceProperties properties) {
    String propertyName = definition.getJsonValueKeyName();
    properties.addProperty(propertyName,
        JsonNodeFactory.instance.objectNode()
            .put("$ref", datatypeManager.getJsonDefinitionRefForDatatype(definition.getDatatype()).toString()));
    properties.addRequired(propertyName);
  }

  public static void generateInstanceProperty(
      INamedModelInstance instance,
      @NotNull DatatypeManager datatypeManager,
      @NotNull InstanceProperties properties) {
    String propertyName = instance.getJsonName();
    String definitionRef = datatypeManager.getJsonDefinitionRefForDefinition(instance.getDefinition()).toString();

    ObjectNode instanceJsonObject = JsonNodeFactory.instance.objectNode();
    int maxOccurs = instance.getMaxOccurs();
    int minOccurs = instance.getMinOccurs();
    if (maxOccurs > 1 || maxOccurs == -1) {
      switch (instance.getJsonGroupAsBehavior()) {
      case LIST:
        instanceJsonObject.put("type", "array");
        instanceJsonObject.putObject("items")
            .put("$ref", definitionRef);
        instanceJsonObject.put("minItems", Math.max(1, minOccurs));
        if (maxOccurs != -1) {
          instanceJsonObject.put("maxItems", maxOccurs);
        }
        break;
      case SINGLETON_OR_LIST:
        ArrayNode oneOf = instanceJsonObject.putArray("oneOf");
        oneOf.addObject()
            .put("$ref", definitionRef);
        ObjectNode arrayObject = oneOf.addObject();
        arrayObject.put("type", "array");
        arrayObject.putObject("items")
            .put("$ref", definitionRef);
        arrayObject.put("minItems", Math.max(2, minOccurs));
        if (maxOccurs != -1) {
          arrayObject.put("maxItems", maxOccurs);
        }
        break;
      case KEYED:
        instanceJsonObject.put("type", "object");
        instanceJsonObject.put("minProperties", 1);

        IFlagInstance jsonKey = instance.getDefinition().getJsonKeyFlagInstance();
        if (jsonKey == null) {
          throw new IllegalStateException();
        }

        instanceJsonObject.putObject("propertyNames")
            .put("$ref",
                datatypeManager.getJsonDefinitionRefForDatatype(jsonKey.getDefinition().getDatatype()).toString());
        instanceJsonObject.putObject("additionalProperties")
            .put("$ref", definitionRef);
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported group-as in-json binding '%s'.", instance.getJsonGroupAsBehavior()));
      }
    } else {
      instanceJsonObject.put("$ref", definitionRef);
    }

    properties.addProperty(propertyName, instanceJsonObject);
    
    if (minOccurs > 0) {
      properties.addRequired(propertyName);
    }
  }

  @NotNull
  public static void generateChoices(
      @NotNull Collection<? extends IChoiceInstance> choices,
      @NotNull DatatypeManager datatypeManager,
      @NotNull InstanceProperties properties,
      @NotNull JsonGenerator jsonGenerator) throws IOException {

    List<InstanceProperties> propertyChoices = Collections.singletonList(properties);
    propertyChoices = explodeChoices(choices, datatypeManager, propertyChoices);

    if (propertyChoices.size() == 1) {
      propertyChoices.iterator().next().generate(jsonGenerator);
    } else if (propertyChoices.size() > 1) {
      jsonGenerator.writeFieldName("oneOf");
      jsonGenerator.writeStartArray();
      for (InstanceProperties propertyChoice : propertyChoices) {
        jsonGenerator.writeStartObject();
        propertyChoice.generate(jsonGenerator);
        jsonGenerator.writeEndObject();
      }
      jsonGenerator.writeEndArray();
    }
  }
  
  protected static List<InstanceProperties> explodeChoices(
      @NotNull Collection<? extends IChoiceInstance> choices,
      @NotNull DatatypeManager datatypeManager,
      @NotNull List<InstanceProperties> propertyChoices) {
    
    List<InstanceProperties> retval = propertyChoices;

    for (IChoiceInstance choice : choices) {
      List<InstanceProperties> newRetval = new LinkedList<>();
      for (IModelInstance optionInstance : choice.getModelInstances()) {
        if (ModelType.CHOICE.equals(optionInstance.getModelType())) {
          // recurse
          newRetval.addAll(explodeChoices(
              Collections.singleton((IChoiceInstance) optionInstance),
              datatypeManager,
              retval));
        } else {
          // interate over the old array of choices and append new choice
          for (InstanceProperties oldInstanceProperties : retval) {
            InstanceProperties newInstanceProperties = oldInstanceProperties.copy();

            // add the choice
            generateInstanceProperty(
                (INamedModelInstance) optionInstance,
                datatypeManager,
                newInstanceProperties);
            newRetval.add(newInstanceProperties);
          }
        }
      }
      retval = newRetval;
    }
    return retval;
  }

  public static class InstanceProperties {
    private final Map<String, ObjectNode> properties;
    private final Set<String> required;

    public InstanceProperties() {
      this(new LinkedHashMap<>(), new LinkedHashSet<>());
    }

    protected InstanceProperties(@NotNull Map<String, ObjectNode> properties, @NotNull Set<String> required) {
      this.properties = properties;
      this.required = required;
    }

    public Map<String, ObjectNode> getProperties() {
      return Collections.unmodifiableMap(properties);
    }

    public Set<String> getRequired() {
      return Collections.unmodifiableSet(required);
    }

    public void addProperty(@NotNull String name, @NotNull ObjectNode def) {
      properties.put(name, def);
    }

    public void addRequired(@NotNull String name) {
      required.add(name);
    }

    public InstanceProperties copy() {
      return new InstanceProperties(new LinkedHashMap<>(properties), new LinkedHashSet<>(required));
    }

    public void generate(JsonGenerator jsonGenerator) throws IOException {
      if (!properties.isEmpty()) {
        jsonGenerator.writeFieldName("properties");
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, ObjectNode> entry : properties.entrySet()) {
          jsonGenerator.writeFieldName(entry.getKey());
          jsonGenerator.writeTree(entry.getValue());
        }
        jsonGenerator.writeEndObject();

        if (!required.isEmpty()) {
          jsonGenerator.writeFieldName("required");
          jsonGenerator.writeStartArray();
          for (String requiredProperty : required) {
            jsonGenerator.writeString(requiredProperty);
          }
          jsonGenerator.writeEndArray();
        }
      }
    }
  }
}
