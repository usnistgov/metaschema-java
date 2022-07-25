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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.JsonSchemaGenerator.GenerationState;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

public final class JsonDefinitionGenerator {
  private JsonDefinitionGenerator() {
    // disable construction
  }

  public static void generateDescription(@NotNull IDefinition definition, @NotNull ObjectNode parentNode) {
    MarkupLine description = definition.getDescription();

    StringBuilder retval = null;
    if (description != null) {
      retval = new StringBuilder().append(description.toMarkdown());
    }

    MarkupMultiline remarks = definition.getRemarks();
    if (remarks != null) {
      if (retval == null) {
        retval = new StringBuilder();
      } else {
        retval.append("\n\n");
      }
      retval.append(remarks.toMarkdown());
    }
    if (retval != null) {
      parentNode.put("description", retval.toString());
    }
  }

  public static void generateTitle(@NotNull IDefinition definition, @NotNull ObjectNode parentNode) {
    String formalName = definition.getFormalName();
    if (formalName != null) {
      parentNode.put("title", formalName);
    }
  }

  public static void generateDefinition(@NotNull IDefinition definition, @NotNull ObjectNode parentNode,
      @NotNull GenerationState state)
      throws IOException {
    JsonDatatypeManager datatypeManager = state.getDatatypeManager();

    boolean inline = state.isInline(definition);

    ObjectNode definitionContextNode;
    if (inline) {
      definitionContextNode = parentNode;
    } else {
      definitionContextNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
      definitionContextNode.put("$id", datatypeManager.getJsonDefinitionRefForDefinition(definition, state));
    }

    generateTitle(definition, definitionContextNode);
    generateDescription(definition, definitionContextNode);

    switch (definition.getModelType()) {
    case ASSEMBLY:
      generateAssemblyDefinition((IAssemblyDefinition) definition, definitionContextNode, state);
      break;
    case FIELD:
      generateFieldDefinition((IFieldDefinition) definition, definitionContextNode, state);
      break;
    case FLAG:
      generateFlagDefinition((IFlagDefinition) definition, definitionContextNode, state);
      break;
    default:
      break;
    }

    if (!inline) {
      String name = datatypeManager.getTypeNameForDefinition(definition, state).toString();
      parentNode.set(name, definitionContextNode);
    }
  }

  public static void generateAssemblyDefinition(
      @NotNull IAssemblyDefinition definition,
      @NotNull ObjectNode definitionNode,
      @NotNull GenerationState state) throws IOException {

    definitionNode.put("type", "object");

    // determine the flag instances to generate
    IFlagInstance jsonKeyFlag = definition.getJsonKeyFlagInstance();
    Collection<@NotNull ? extends IFlagInstance> flags
        = FlagInstanceFilter.filterFlags(definition.getFlagInstances(), jsonKeyFlag);

    JsonPropertyGenerator.InstanceProperties properties = new JsonPropertyGenerator.InstanceProperties();

    // generate flag properties
    for (IFlagInstance flag : flags) {
      JsonPropertyGenerator.generateFlagProperty(flag, properties, state);
    }
    // generate model properties
    Collection<@NotNull ? extends INamedModelInstance> instances = definition.getNamedModelInstances();
    for (INamedModelInstance instance : instances) {
      JsonPropertyGenerator.generateInstanceProperty(instance, properties, state);
    }

    Collection<@NotNull ? extends IChoiceInstance> choices = definition.getChoiceInstances();
    if (choices.isEmpty()) {
      properties.generate(definitionNode);

      definitionNode.put("additionalProperties", false);
    } else {
      JsonPropertyGenerator.generateChoices(choices, properties, definitionNode, state);
    }
  }

  public static void generateFieldDefinition( // NOPMD - ok
      @NotNull IFieldDefinition definition,
      @NotNull ObjectNode definitionNode,
      @NotNull GenerationState state) throws IOException {
    JsonDatatypeManager datatypeManager = state.getDatatypeManager();

    Collection<@NotNull ? extends IFlagInstance> flags = definition.getFlagInstances();
    IFlagInstance jsonKeyFlag = definition.getJsonKeyFlagInstance();
    if (flags.isEmpty() || (jsonKeyFlag != null && flags.size() == 1)) { // NOPMD - readability
      // field is a simple value if there are no flags or if the only flag is a JSON key
      definitionNode.put("$ref",
          datatypeManager.getJsonDefinitionRefForDatatype(definition.getJavaTypeAdapter()));
    } else {
      definitionNode.put("type", "object");

      // determine the flag instances to generate
      IFlagInstance jsonValueKeyFlag = definition.getJsonValueKeyFlagInstance();
      flags = FlagInstanceFilter.filterFlags(flags, jsonKeyFlag, jsonValueKeyFlag);

      JsonPropertyGenerator.InstanceProperties properties = new JsonPropertyGenerator.InstanceProperties();

      // generate flag properties
      for (IFlagInstance flag : flags) {
        JsonPropertyGenerator.generateFlagProperty(flag, properties, state);
      }

      // generate value property
      if (jsonValueKeyFlag == null) {
        if (definition.isCollapsible()) {
          JsonPropertyGenerator.generateCollapsibleFieldValueInstance(definition, properties, state);
        } else {
          JsonPropertyGenerator.generateSimpleFieldValueInstance(definition, properties, state);
        }
      }

      properties.generate(definitionNode);

      if (jsonValueKeyFlag == null) {
        definitionNode.put("additionalProperties", false);
      } else {
        ObjectNode additionalPropertiesTypeNode;

        if (definition.isCollapsible()) {
          additionalPropertiesTypeNode = JsonPropertyGenerator.generateCollapsibleFieldValueType(definition, state);
        } else {
          additionalPropertiesTypeNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
          // the type of the additional properties must be the datatype of the field value
          additionalPropertiesTypeNode.put("$ref",
              datatypeManager.getJsonDefinitionRefForDatatype(definition.getJavaTypeAdapter()));
        }

        ObjectNode additionalPropertiesNode = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
        ArrayNode allOf = additionalPropertiesNode.putArray("allOf");
        allOf.add(additionalPropertiesTypeNode);
        allOf.addObject()
            .put("minProperties", properties.getRequired().size() + 1)
            .put("maxProperties", properties.getProperties().size() + 1);

        definitionNode.set("additionalProperties", additionalPropertiesNode);
      }
    }
  }

  public static void generateFlagDefinition(
      @NotNull IFlagDefinition definition,
      @NotNull ObjectNode definitionNode,
      @NotNull GenerationState state) {
    definitionNode.put("$ref",
        state.getDatatypeManager().getJsonDefinitionRefForDatatype(definition.getJavaTypeAdapter()));
  }

}
