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

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

public class JsonDefinitionGenerator {
  private JsonDefinitionGenerator() {
    // disable construction
  }

  public static void generateDescription(IDefinition definition, JsonGenerator jsonGenerator) throws IOException {
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
      jsonGenerator.writeStringField("description", retval.toString());
    }
  }

  public static void generateTitle(IDefinition definition, JsonGenerator jsonGenerator) throws IOException {
    String formalName = definition.getFormalName();
    if (formalName != null) {
      jsonGenerator.writeStringField("title", formalName);
    }
  }

  public static void generateDefinition(@NotNull INamedDefinition definition,
      @NotNull JsonDatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator)
      throws IOException {
    String name = datatypeManager.getJsonDefinitionNameForDefinition(definition).toString();
    jsonGenerator.writeFieldName(name);

    jsonGenerator.writeStartObject();

    jsonGenerator.writeStringField("$id", datatypeManager.getJsonDefinitionRefForDefinition(definition).toString());

    generateTitle(definition, jsonGenerator);
    generateDescription(definition, jsonGenerator);

    switch (definition.getModelType()) {
    case ASSEMBLY:
      generateAssemblyDefinition((IAssemblyDefinition) definition, datatypeManager, jsonGenerator);
      break;
    case FIELD:
      generateFieldDefinition((IFieldDefinition) definition, datatypeManager, jsonGenerator);
      break;
    case FLAG:
      generateFlagDefinition((IFlagDefinition) definition, datatypeManager, jsonGenerator);
      break;
    default:
      break;
    }
    jsonGenerator.writeEndObject();
  }

  public static void generateAssemblyDefinition(
      @NotNull IAssemblyDefinition definition,
      @NotNull JsonDatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStringField("type", "object");

    // determine the flag instances to generate
    IFlagInstance jsonKeyFlag = definition.getJsonKeyFlagInstance();
    Collection<@NotNull ? extends IFlagInstance> flags
        = FlagInstanceFilter.filterFlags(definition.getFlagInstances(), jsonKeyFlag);

    JsonPropertyGenerator.InstanceProperties properties = new JsonPropertyGenerator.InstanceProperties();

    // generate flag properties
    for (IFlagInstance flag : flags) {
      JsonPropertyGenerator.generateFlagProperty(flag, datatypeManager, properties);
    }
    // generate model properties
    Collection<? extends INamedModelInstance> instances = definition.getNamedModelInstances();
    for (INamedModelInstance instance : instances) {
      JsonPropertyGenerator.generateInstanceProperty(instance, datatypeManager, properties);
    }

    Collection<? extends IChoiceInstance> choices = definition.getChoiceInstances();
    if (choices.isEmpty()) {
      properties.generate(jsonGenerator);

      jsonGenerator.writeBooleanField("additionalProperties", false);
    } else {
      JsonPropertyGenerator.generateChoices(choices, datatypeManager, properties, jsonGenerator);
    }
  }

  public static void generateFieldDefinition(
      @NotNull IFieldDefinition definition,
      @NotNull JsonDatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator) throws IOException {
    Collection<@NotNull ? extends IFlagInstance> flags = definition.getFlagInstances();
    IFlagInstance jsonKeyFlag = definition.getJsonKeyFlagInstance();
    if (flags.isEmpty() || (jsonKeyFlag != null && flags.size() == 1)) {
      // field is a simple value if there are no flags or if the only flag is a JSON key
      jsonGenerator.writeStringField("$ref",
          datatypeManager.getJsonDefinitionRefForDatatype(definition.getDatatype()).toString());
    } else {
      jsonGenerator.writeStringField("type", "object");

      // determine the flag instances to generate
      IFlagInstance jsonValueKeyFlag = definition.getJsonValueKeyFlagInstance();
      flags = FlagInstanceFilter.filterFlags(flags, jsonKeyFlag, jsonValueKeyFlag);

      JsonPropertyGenerator.InstanceProperties properties = new JsonPropertyGenerator.InstanceProperties();

      // generate flag properties
      for (IFlagInstance flag : flags) {
        JsonPropertyGenerator.generateFlagProperty(flag, datatypeManager, properties);
      }

      // generate value property
      if (jsonValueKeyFlag == null) {
        if (definition.isCollapsible()) {
          JsonPropertyGenerator.generateCollapsibleFieldValueInstance(definition, datatypeManager, properties);
        } else {
          JsonPropertyGenerator.generateSimpleFieldValueInstance(definition, datatypeManager, properties);
        }
      }

      properties.generate(jsonGenerator);

      if (jsonValueKeyFlag == null) {
        jsonGenerator.writeBooleanField("additionalProperties", false);
      } else {
        jsonGenerator.writeFieldName("additionalProperties");

        if (definition.isCollapsible()) {
          jsonGenerator.writeTree(JsonPropertyGenerator.generateCollapsibleFieldValueType(definition, datatypeManager));
        } else {
          jsonGenerator.writeStartObject();
          // the type of the additional properties must be the datatype of the field value
          jsonGenerator.writeStringField("$ref",
              datatypeManager.getJsonDefinitionRefForDatatype(definition.getDatatype()).toString());
          jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeNumberField("minProperties", properties.getRequired().size() + 1);
        jsonGenerator.writeNumberField("maxProperties", properties.getProperties().size() + 1);
      }
    }
  }

  public static void generateFlagDefinition(
      @NotNull IFlagDefinition definition,
      @NotNull JsonDatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeStringField("$ref",
        datatypeManager.getJsonDefinitionRefForDatatype(definition.getDatatype()).toString());
  }

}
