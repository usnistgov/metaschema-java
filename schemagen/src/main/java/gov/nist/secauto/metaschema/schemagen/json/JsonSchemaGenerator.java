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
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationException;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.json.IDefineableJsonSchema.IKey;
import gov.nist.secauto.metaschema.schemagen.json.impl.JsonDatatypeManager;
import gov.nist.secauto.metaschema.schemagen.json.impl.JsonGenerationState;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

public class JsonSchemaGenerator
    extends AbstractSchemaGenerator<JsonGenerator, JsonDatatypeManager, JsonGenerationState> {
  @NonNull
  private final JsonFactory jsonFactory;

  public JsonSchemaGenerator() {
    this(new JsonFactory());
  }

  public JsonSchemaGenerator(@NonNull JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  @NonNull
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  @SuppressWarnings("resource")
  @Override
  protected JsonGenerator newWriter(Writer out) {
    try {
      return ObjectUtils.notNull(getJsonFactory().createGenerator(out)
          .setCodec(new ObjectMapper())
          .useDefaultPrettyPrinter()
          .disable(Feature.AUTO_CLOSE_TARGET));
    } catch (IOException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  @Override
  protected JsonGenerationState newGenerationState(
      IModule module,
      JsonGenerator schemaWriter,
      IConfiguration<SchemaGenerationFeature<?>> configuration) {
    return new JsonGenerationState(module, schemaWriter, configuration);
  }

  @Override
  protected void generateSchema(JsonGenerationState state) {
    IModule module = state.getModule();
    try {
      state.writeStartObject();

      state.writeField("$schema", "http://json-schema.org/draft-07/schema#");
      state.writeField("$id",
          String.format("%s/%s-%s-schema.json",
              module.getXmlNamespace(),
              module.getShortName(),
              module.getVersion()));
      state.writeField("$comment", module.getName().toMarkdown());
      state.writeField("type", "object");

      ObjectNode definitionsObject = state.generateDefinitions();
      if (!definitionsObject.isEmpty()) {
        state.writeField("definitions", definitionsObject);
      }

      List<IAssemblyDefinition> rootAssemblyDefinitions = state.getMetaschemaIndex().getDefinitions().stream()
          .map(entry -> entry.getDefinition())
          .filter(
              definition -> definition instanceof IAssemblyDefinition && ((IAssemblyDefinition) definition).isRoot())
          .map(definition -> (IAssemblyDefinition) definition)
          .collect(Collectors.toUnmodifiableList());

      if (rootAssemblyDefinitions.isEmpty()) {
        throw new SchemaGenerationException("No root definitions found");
      }

      // generate the properties first to ensure all definitions are identified
      List<RootPropertyEntry> rootEntries = rootAssemblyDefinitions.stream()
          .map(root -> {
            assert root != null;
            return new RootPropertyEntry(root, state);
          })
          .collect(Collectors.toUnmodifiableList());

      @SuppressWarnings("resource") JsonGenerator writer = state.getWriter(); // NOPMD not owned

      if (rootEntries.size() == 1) {
        rootEntries.iterator().next().write(writer);
      } else {
        writer.writeFieldName("oneOf");
        writer.writeStartArray();

        for (RootPropertyEntry root : rootEntries) {
          assert root != null;
          writer.writeStartObject();
          root.write(writer);
          writer.writeEndObject();
        }

        writer.writeEndArray();
      }

      state.writeEndObject();
    } catch (IOException ex) {
      throw new SchemaGenerationException(ex);
    }
  }

  @NonNull
  private static Map<String, ObjectNode> generateRootProperties(
      @NonNull IAssemblyDefinition definition,
      @NonNull JsonGenerationState state) {
    Map<String, ObjectNode> properties = new LinkedHashMap<>(); // NOPMD no concurrent access

    properties.put("$schema", JsonNodeFactory.instance.objectNode()
        .put("type", "string")
        .put("format", "uri-reference"));

    ObjectNode rootObj = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());
    IDefinitionJsonSchema<IAssemblyDefinition> schema = state.getSchema(IKey.of(definition));
    schema.generateSchemaOrRef(rootObj, state);

    properties.put(definition.getRootJsonName(), rootObj);
    return properties;
  }

  private static class RootPropertyEntry {
    @NonNull
    private final IAssemblyDefinition definition;
    @NonNull
    private final Map<String, ObjectNode> properties;

    public RootPropertyEntry(
        @NonNull IAssemblyDefinition definition,
        @NonNull JsonGenerationState state) {
      this.definition = definition;
      this.properties = generateRootProperties(definition, state);
    }

    @NonNull
    protected IAssemblyDefinition getDefinition() {
      return definition;
    }

    @NonNull
    protected Map<String, ObjectNode> getProperties() {
      return properties;
    }

    public void write(JsonGenerator writer) throws IOException {
      writer.writeFieldName("properties");
      writer.writeStartObject();

      for (Map.Entry<String, ObjectNode> entry : getProperties().entrySet()) {
        writer.writeFieldName(entry.getKey());
        writer.writeTree(entry.getValue());
      }

      writer.writeEndObject();

      writer.writeFieldName("required");
      writer.writeStartArray();
      writer.writeString(getDefinition().getRootJsonName());
      writer.writeEndArray();

      writer.writeBooleanField("additionalProperties", false);
    }
  }
}
