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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.model.common.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.schemagen.AbstractGenerationState;
import gov.nist.secauto.metaschema.schemagen.SchemaGenerationFeature;
import gov.nist.secauto.metaschema.schemagen.json.datatype.JsonDatatypeManager;
import gov.nist.secauto.metaschema.schemagen.json.schema.AssemblyDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.DataTypeJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.DataTypeRestrictionDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.FieldDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.FlagDefinitionJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.IDefineableJsonSchema;
import gov.nist.secauto.metaschema.schemagen.json.schema.IJsonSchema;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;

public class JsonGenerationState
    extends AbstractGenerationState<JsonGenerator, JsonDatatypeManager> {

  private final Set<IDefineableJsonSchema> definitionSchemas = new LinkedHashSet<>();
  private final Map<IDefinition, IJsonSchema> definitionToSchemaMap = new ConcurrentHashMap<>();
  private final Map<IValuedDefinition, IJsonSchema> definitionValueToDataTypeSchemaMap = new ConcurrentHashMap<>();
  private final Map<IDataTypeAdapter<?>, IJsonSchema> dataTypeToSchemaMap = new ConcurrentHashMap<>();

  public JsonGenerationState(
      @NonNull IMetaschema metaschema,
      @NonNull JsonGenerator writer,
      @NonNull IConfiguration<SchemaGenerationFeature> configuration) {
    super(metaschema, writer, configuration, new JsonDatatypeManager());
  }

  @NonNull
  public IJsonSchema getDataTypeSchemaForDefinition(@NonNull IValuedDefinition definition) {
    IJsonSchema retval = definitionValueToDataTypeSchemaMap.get(definition);
    if (retval == null) {
      AllowedValueCollection allowedValuesCollection = getContextIndependentEnumeratedValues(definition);
      List<IAllowedValue> allowedValues = allowedValuesCollection.getValues();

      if (allowedValues.isEmpty()) {
        // by default, just use the built-in type
        retval = getSchema(definition.getJavaTypeAdapter());
      } else {
        IDefineableJsonSchema restriction
            = new DataTypeRestrictionDefinitionJsonSchema(definition, allowedValuesCollection);
        registerDefinitionSchema(restriction);
        retval = restriction;
      }
      definitionValueToDataTypeSchemaMap.put(definition, retval);
      retval.resolveSubSchemas(this);
    }
    return retval;
  }

  @NonNull
  public IJsonSchema getSchema(@NonNull IDefinition definition) {
    IJsonSchema retval = definitionToSchemaMap.get(definition);
    if (retval == null) {
      IDefineableJsonSchema definitionSchema;
      switch (definition.getModelType()) {
      case ASSEMBLY:
        definitionSchema = new AssemblyDefinitionJsonSchema((IAssemblyDefinition) definition);
        break;
      case FIELD:
        definitionSchema = new FieldDefinitionJsonSchema((IFieldDefinition) definition);
        break;
      case FLAG:
        definitionSchema = new FlagDefinitionJsonSchema((IFlagDefinition) definition);
        break;
      default:
        throw new UnsupportedOperationException(definition.getModelType().toString());
      }
      definitionToSchemaMap.put(definition, definitionSchema);
      registerDefinitionSchema(definitionSchema);
      definitionSchema.resolveSubSchemas(this);
      retval = definitionSchema;
    }
    return retval;
  }

  @NonNull
  public IJsonSchema getSchema(@NonNull IDataTypeAdapter<?> datatype) {
    IJsonSchema retval = dataTypeToSchemaMap.get(datatype);
    if (retval == null) {
      retval = new DataTypeJsonSchema(
          getDatatypeManager().getTypeNameForDatatype(datatype),
          datatype);
      dataTypeToSchemaMap.put(datatype, retval);
    }
    return retval;
  }

  protected void registerDefinitionSchema(@NonNull IDefineableJsonSchema schema) {
    if (schema.isDefinition(this)) {
      synchronized (definitionSchemas) {
        if (!definitionSchemas.contains(schema)) {
          definitionSchemas.add(schema);
        }
      }
    }
  }

  public ObjectNode generateDefinitions() {
    ObjectNode definitionsObject = ObjectUtils.notNull(JsonNodeFactory.instance.objectNode());

    for (IDefineableJsonSchema schema : definitionSchemas) {
      schema.generateDefinition(this, definitionsObject);
    }

    getDatatypeManager().generateDatatypes(definitionsObject);

    return definitionsObject;
  }

  @SuppressWarnings("resource")
  public void writeStartObject() throws IOException {
    getWriter().writeStartObject();
  }

  @SuppressWarnings("resource")
  public void writeEndObject() throws IOException {
    getWriter().writeEndObject();
  }

  @SuppressWarnings("resource")
  public void writeField(String fieldName, String value) throws IOException {
    getWriter().writeStringField(fieldName, value);

  }

  @SuppressWarnings("resource")
  public void writeField(String fieldName, ObjectNode obj) throws IOException {
    JsonGenerator writer = getWriter(); // NOPMD not closable here

    writer.writeFieldName(fieldName);
    writer.writeTree(obj);
  }
}
