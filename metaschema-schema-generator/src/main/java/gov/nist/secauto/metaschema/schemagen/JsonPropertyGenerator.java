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

package gov.nist.secauto.metaschema.schemagen;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IInstance;
import gov.nist.secauto.metaschema.model.common.IModelInstance;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.ModelType;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.JsonSchemaGenerator.GenerationState;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class JsonPropertyGenerator {
  private JsonPropertyGenerator() {
    // disable construction
  }

  public static void generateDescription(IInstance instance, @NonNull ObjectNode propertyNode) {
    MarkupMultiline remarks = instance.getRemarks();
    if (remarks != null) {
      propertyNode.put("description", remarks.toMarkdown());
    }
  }

  public static void generateFlagProperty(
      @NonNull IFlagInstance flag,
      @NonNull InstanceProperties properties,
      @NonNull GenerationState state) throws IOException {
    String propertyName = flag.getJsonName();
    ObjectNode type = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
    generateInstancePropertyDefinitionOrRef(flag, type, state);
    properties.addProperty(propertyName, type);
    if (flag.isRequired()) {
      properties.addRequired(propertyName);
    }
  }

  public static void generateSimpleFieldValueInstance(
      @NonNull IFieldDefinition definition,
      @NonNull InstanceProperties properties,
      @NonNull GenerationState state) {
    String propertyName = definition.getJsonValueKeyName();
    properties.addProperty(propertyName,
        ObjectUtils.notNull(JsonNodeFactory.instance.objectNode()
            .put("$ref", state.getDatatypeManager().getJsonDefinitionRefForDatatype(definition.getJavaTypeAdapter()))));
    properties.addRequired(propertyName);
  }

  public static void generateCollapsibleFieldValueInstance(
      @NonNull IFieldDefinition definition,
      @NonNull InstanceProperties properties,
      @NonNull GenerationState state) {
    String propertyName = definition.getJsonValueKeyName();

    properties.addProperty(propertyName, generateCollapsibleFieldValueType(definition, state));
    properties.addRequired(propertyName);
  }

  @NonNull
  public static ObjectNode generateCollapsibleFieldValueType(
      @NonNull IFieldDefinition definition,
      @NonNull GenerationState state) {
    String definitionRef = state.getDatatypeManager().getJsonDefinitionRefForDatatype(definition.getJavaTypeAdapter());

    ObjectNode retval = JsonNodeFactory.instance.objectNode();
    ArrayNode oneOf = retval.putArray("oneOf");
    oneOf.addObject()
        .put("$ref", definitionRef);
    oneOf.addObject()
        .put("type", "array")
        .put("minItems", 1)
        .putObject("items")
        .put("$ref", definitionRef);
    return retval;
  }

  public static void generateInstanceProperty(
      @NonNull INamedModelInstance instance,
      @NonNull InstanceProperties properties,
      @NonNull GenerationState state) throws IOException {
    JsonDatatypeManager datatypeManager = state.getDatatypeManager();

    @SuppressWarnings("null")
    @NonNull
    ObjectNode instanceJsonObject = JsonNodeFactory.instance.objectNode();
    int maxOccurs = instance.getMaxOccurs();
    int minOccurs = instance.getMinOccurs();
    if (maxOccurs > 1 || maxOccurs == -1) {
      switch (instance.getJsonGroupAsBehavior()) {
      case LIST: {
        instanceJsonObject.put("type", "array");

        @SuppressWarnings("null")
        @NonNull
        ObjectNode items = instanceJsonObject.putObject("items");
        generateInstancePropertyDefinitionOrRef(instance, items, state);
        instanceJsonObject.put("minItems", Math.max(1, minOccurs));
        if (maxOccurs != -1) {
          instanceJsonObject.put("maxItems", maxOccurs);
        }
        break;
      }
      case SINGLETON_OR_LIST: {
        ArrayNode oneOf = instanceJsonObject.putArray("oneOf");

        @SuppressWarnings("null")
        @NonNull
        ObjectNode singleton = oneOf.addObject();
        generateInstancePropertyDefinitionOrRef(instance, singleton, state);
        ObjectNode arrayObject = oneOf.addObject();
        arrayObject.put("type", "array");

        @SuppressWarnings("null")
        @NonNull
        ObjectNode items = arrayObject.putObject("items");
        generateInstancePropertyDefinitionOrRef(instance, items, state);
        arrayObject.put("minItems", Math.max(2, minOccurs));
        if (maxOccurs != -1) {
          arrayObject.put("maxItems", maxOccurs);
        }
        break;
      }
      case KEYED: {
        instanceJsonObject.put("type", "object");
        instanceJsonObject.put("minProperties", 1);

        IFlagInstance jsonKey = instance.getDefinition().getJsonKeyFlagInstance();
        if (jsonKey == null) {
          throw new IllegalStateException();
        }

        instanceJsonObject.putObject("propertyNames")
            .put("$ref",
                datatypeManager.getJsonDefinitionRefForDatatype(jsonKey.getDefinition().getJavaTypeAdapter()));
        // TODO: is this correct?
        @SuppressWarnings("null")
        @NonNull
        ObjectNode additional = instanceJsonObject.putObject("additionalProperties");
        generateInstancePropertyDefinitionOrRef(instance, additional, state);
        break;
      }
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported group-as in-json binding '%s'.", instance.getJsonGroupAsBehavior()));
      }
    } else {
      generateInstancePropertyDefinitionOrRef(instance, instanceJsonObject, state);
    }

    String propertyName = instance.getJsonName();
    properties.addProperty(propertyName, instanceJsonObject);

    if (minOccurs > 0) {
      properties.addRequired(propertyName);
    }
  }

  public static void generateInstancePropertyDefinitionOrRef(
      @NonNull INamedInstance instance,
      @NonNull ObjectNode instanceNode,
      @NonNull GenerationState state) throws IOException {

    if (state.isInline(instance.getDefinition())) {
      JsonDefinitionGenerator.generateDefinition(instance.getDefinition(), instanceNode, state);
    } else {
      String definitionRef
          = state.getDatatypeManager().getJsonDefinitionRefForDefinition(instance.getDefinition(), state);
      instanceNode.put("$ref", definitionRef);
    }
  }

  public static void generateChoices(
      @NonNull Collection<? extends IChoiceInstance> choices,
      @NonNull InstanceProperties properties,
      @NonNull ObjectNode definitionNode,
      @NonNull GenerationState state) throws IOException {
    List<InstanceProperties> propertyChoices = CollectionUtil.singletonList(properties);
    propertyChoices = explodeChoices(choices, propertyChoices, state);

    if (propertyChoices.size() == 1) {
      propertyChoices.iterator().next().generate(definitionNode);
    } else if (propertyChoices.size() > 1) {
      ArrayNode anyOfdNode = ObjectUtils.notNull(JsonNodeFactory.instance.arrayNode());
      for (InstanceProperties propertyChoice : propertyChoices) {
        ObjectNode choiceDefinitionNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
        propertyChoice.generate(choiceDefinitionNode);
        choiceDefinitionNode.put("additionalProperties", false);
        anyOfdNode.add(choiceDefinitionNode);
      }
      definitionNode.set("anyOf", anyOfdNode);
    }
  }

  private static List<InstanceProperties> explodeChoices(
      @NonNull Collection<? extends IChoiceInstance> choices,
      @NonNull List<InstanceProperties> propertyChoices,
      @NonNull GenerationState state) throws IOException {

    List<InstanceProperties> retval = propertyChoices;

    for (IChoiceInstance choice : choices) {
      List<InstanceProperties> newRetval = new LinkedList<>(); // NOPMD - intentional
      for (IModelInstance optionInstance : choice.getModelInstances()) {
        if (ModelType.CHOICE.equals(optionInstance.getModelType())) {
          // recurse
          newRetval.addAll(explodeChoices(
              CollectionUtil.singleton((IChoiceInstance) optionInstance),
              retval,
              state));
        } else {
          // iterate over the old array of choices and append new choice
          for (InstanceProperties oldInstanceProperties : retval) {
            @SuppressWarnings("null")
            @NonNull
            InstanceProperties newInstanceProperties = oldInstanceProperties.copy();

            // add the choice
            generateInstanceProperty(
                (INamedModelInstance) optionInstance,
                newInstanceProperties,
                state);
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

    protected InstanceProperties(@NonNull Map<String, ObjectNode> properties, @NonNull Set<String> required) {
      this.properties = properties;
      this.required = required;
    }

    public Map<String, ObjectNode> getProperties() {
      return Collections.unmodifiableMap(properties);
    }

    public Set<String> getRequired() {
      return Collections.unmodifiableSet(required);
    }

    public void addProperty(@NonNull String name, @NonNull ObjectNode def) {
      properties.put(name, def);
    }

    public void addRequired(@NonNull String name) {
      required.add(name);
    }

    public InstanceProperties copy() {
      return new InstanceProperties(new LinkedHashMap<>(properties), new LinkedHashSet<>(required));
    }

    public void generate(@NonNull ObjectNode definitionNode) {
      if (!properties.isEmpty()) {
        ObjectNode propertiesNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
        for (Map.Entry<String, ObjectNode> entry : properties.entrySet()) {
          propertiesNode.set(entry.getKey(), entry.getValue());
        }
        definitionNode.set("properties", propertiesNode);

        if (!required.isEmpty()) {
          ArrayNode requiredNode = ObjectUtils.notNull(JsonNodeFactory.instance.arrayNode());
          for (String requiredProperty : required) {
            requiredNode.add(requiredProperty);
          }
          definitionNode.set("required", requiredNode);
        }
      }
    }
  }
}
