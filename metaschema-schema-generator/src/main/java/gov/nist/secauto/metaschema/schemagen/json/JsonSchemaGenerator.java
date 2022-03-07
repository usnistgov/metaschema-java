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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.secauto.metaschema.model.UsedDefinitionModelWalker;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.INamedDefinition;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class JsonSchemaGenerator implements ISchemaGenerator {
  private final JsonFactory jsonFactory;

  public JsonSchemaGenerator() {
    this(new JsonFactory());
  }

  public JsonSchemaGenerator(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  @Override
  public void generateFromMetaschema(@NotNull IMetaschema metaschema, @NotNull Writer out) throws IOException {
    JsonGenerator jsonGenerator = getJsonFactory().createGenerator(out);
    jsonGenerator.setCodec(new ObjectMapper());
    jsonGenerator.useDefaultPrettyPrinter();

    generateSchemaMetadata(metaschema, jsonGenerator);

    jsonGenerator.flush();
  }

  protected void generateSchemaMetadata(@NotNull IMetaschema metaschema, @NotNull JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeStartObject();

    jsonGenerator.writeStringField("$schema", "http://json-schema.org/draft-07/schema#");
    jsonGenerator.writeStringField("$id",
        String.format("%s/%s-%s-schema.json",
            metaschema.getXmlNamespace(),
            metaschema.getShortName(),
            metaschema.getVersion()));
    jsonGenerator.writeStringField("$comment", metaschema.getName().toMarkdown());
    jsonGenerator.writeStringField("type", "object");

    Collection<@NotNull ? extends INamedDefinition> definitions
        = UsedDefinitionModelWalker.collectUsedDefinitionsFromMetaschema(metaschema);

    DatatypeManager datatypeManager = new DatatypeManager();

    generateDefinitions(definitions, datatypeManager, jsonGenerator);

    Set<IAssemblyDefinition> rootAssemblies = new LinkedHashSet<>();

    for (IDefinition definition : definitions) {
      if (definition instanceof IAssemblyDefinition) {
        IAssemblyDefinition assemblyDefinition = (IAssemblyDefinition) definition;
        if (assemblyDefinition.isRoot()) {
          rootAssemblies.add(assemblyDefinition);
        }
      }
    }

    if (!rootAssemblies.isEmpty()) {
      generateRootProperties(rootAssemblies, datatypeManager, jsonGenerator);
    }
    jsonGenerator.writeEndObject();
  }

  protected void generateDefinitions(
      @NotNull Collection<@NotNull ? extends INamedDefinition> definitions,
      @NotNull DatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator) throws IOException {
    if (!definitions.isEmpty()) {
      jsonGenerator.writeFieldName("definitions");
      jsonGenerator.writeStartObject();

      for (INamedDefinition definition : definitions) {
        JsonDefinitionGenerator.generateDefinition(definition, datatypeManager, jsonGenerator);
      }

      // write datatypes
      datatypeManager.generateDatatypes(jsonGenerator);

      jsonGenerator.writeEndObject();
    }
  }

  protected void generateRootProperties(
      @NotNull Set<IAssemblyDefinition> rootAssemblies,
      @NotNull DatatypeManager datatypeManager,
      @NotNull JsonGenerator jsonGenerator) throws IOException {
    // generate root properties
    jsonGenerator.writeFieldName("properties");
    jsonGenerator.writeStartObject();

    jsonGenerator.writeFieldName("$schema");
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("type", "string");
    jsonGenerator.writeStringField("format", "uri-reference");
    jsonGenerator.writeEndObject();

    for (IAssemblyDefinition root : rootAssemblies) {
      jsonGenerator.writeFieldName(root.getRootJsonName());
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("$ref", datatypeManager.getJsonDefinitionRefForDefinition(root).toString());
      jsonGenerator.writeEndObject();
    }
    jsonGenerator.writeEndObject();

    // generate root requires
    if (rootAssemblies.size() == 1) {
      generateRootRequired(rootAssemblies.iterator().next(), jsonGenerator);
    } else {
      jsonGenerator.writeFieldName("oneOf");
      jsonGenerator.writeStartArray();

      for (IAssemblyDefinition root : rootAssemblies) {
        jsonGenerator.writeStartObject();
        generateRootRequired(root, jsonGenerator);
        jsonGenerator.writeEndObject();
      }

      jsonGenerator.writeEndArray();
    }

    jsonGenerator.writeBooleanField("additionalProperties", false);
  }

  protected void generateRootRequired(@NotNull IAssemblyDefinition root, @NotNull JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeFieldName("required");
    jsonGenerator.writeStartArray();
    jsonGenerator.writeString(root.getRootJsonName());
    jsonGenerator.writeEndArray();

  }
}
