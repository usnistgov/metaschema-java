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

package gov.nist.secauto.metaschema.databind.io.json;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.core.model.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldScalar;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedField;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedNamed;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import java.io.IOException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaJsonWriter implements IJsonWritingContext {
  @NonNull
  private final JsonGenerator writer;
  @NonNull
  private final ItemWriter itemWriter = new ItemWriter();

  /**
   * Construct a new Module-aware JSON writer.
   *
   * @param generator
   *          the JSON generator to write with
   * @see DefaultJsonProblemHandler
   */
  public MetaschemaJsonWriter(@NonNull JsonGenerator generator) {
    this.writer = generator;
  }

  @Override
  public JsonGenerator getWriter() {
    return writer;
  }

  @NonNull
  protected ItemWriter getItemWriter() {
    return itemWriter;
  }

  @Override
  public void writeItemFlag(Object parentItem, IBoundInstanceFlag instance) throws IOException {
    writeInstance(instance, parentItem);
  }

  @Override
  public void writeItemField(Object parentItem, IBoundInstanceModelFieldScalar instance) throws IOException {
    writeModelInstance(instance, parentItem);
  }

  @Override
  public void writeItemField(Object parentItem, IBoundInstanceModelFieldComplex instance) throws IOException {
    writeModelInstance(instance, parentItem);
  }

  @Override
  public void writeItemField(Object parentItem, IBoundInstanceModelGroupedField instance) throws IOException {
    throw new UnsupportedOperationException("not needed");
  }

  @Override
  public void writeItemField(Object item, IBoundDefinitionModelFieldComplex definition) throws IOException {
    definition.writeItem(item, getItemWriter());
  }

  @Override
  public void writeItemFieldValue(Object parentItem, IBoundFieldValue fieldValue) throws IOException {
    Object item = fieldValue.getValue(parentItem);

    // handle json value key
    IBoundInstanceFlag jsonValueKey = fieldValue.getParentFieldDefinition().getJsonValueKeyFlagInstance();
    if (item == null) {
      if (jsonValueKey != null) {
        item = fieldValue.getDefaultValue();
      }
    } else if (item.equals(fieldValue.getResolvedDefaultValue())) {
      // same as default
      item = null;
    }

    if (item != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"

      String valueKeyName;
      if (jsonValueKey != null) {
        Object keyValue = jsonValueKey.getValue(parentItem);
        if (keyValue == null) {
          throw new IOException(String.format("Null value for json-value-key for definition '%s'",
              jsonValueKey.getContainingDefinition().toCoordinates()));
        }
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getJavaTypeAdapter().asString(keyValue);
      } else {
        valueKeyName = fieldValue.getParentFieldDefinition().getEffectiveJsonValueKeyName();
      }
      writer.writeFieldName(valueKeyName);
      // LOGGER.info("FIELD: {}", valueKeyName);

      getItemWriter().writeItemFieldValue(item, fieldValue);
    }
  }

  @Override
  public void writeItemAssembly(Object parentItem, IBoundInstanceModelAssembly instance) throws IOException {
    writeModelInstance(instance, parentItem);
  }

  @Override
  public void writeItemAssembly(Object parentItem, IBoundInstanceModelGroupedAssembly instance) throws IOException {
    throw new UnsupportedOperationException("not needed");
  }

  @Override
  public void writeItemAssembly(Object item, IBoundDefinitionModelAssembly definition) throws IOException {
    definition.writeItem(item, getItemWriter());
  }

  @Override
  public void writeChoiceGroupItem(Object parentItem, IBoundInstanceModelChoiceGroup instance) throws IOException {
    writeModelInstance(instance, parentItem);
  }

  private void writeInstance(
      @NonNull IBoundProperty instance,
      @NonNull Object parentItem) throws IOException {
    Object value = instance.getValue(parentItem);
    if (!(value == null || value.equals(instance.getResolvedDefaultValue()))) {
      writer.writeFieldName(instance.getJsonName());
      instance.writeItem(value, getItemWriter());
    }
  }

  private void writeModelInstance(
      @NonNull IBoundInstanceModel instance,
      @NonNull Object parentItem) throws IOException {
    Object value = instance.getValue(parentItem);
    if (value != null) {
      // this if is not strictly needed, since isEmpty will return false on a null
      // value
      // checking null here potentially avoids the expensive operation of instatiating
      IModelInstanceCollectionInfo collectionInfo = instance.getCollectionInfo();
      if (!collectionInfo.isEmpty(value)) {
        writer.writeFieldName(instance.getJsonName());
        collectionInfo.writeItems(new ModelInstanceWriteHandler(instance), value);
      }
    }
  }

  private final class ItemWriter implements IItemWriteHandler {
    @Override
    public void writeItemFlag(Object item, IBoundInstanceFlag instance) throws IOException {
      writeScalarItem(item, instance);
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelFieldScalar instance) throws IOException {
      writeScalarItem(item, instance);
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelFieldComplex instance) throws IOException {
      writeModelObject(
          instance,
          item,
          this::writeObjectProperties);
    }

    @Override
    public void writeItemField(Object item, IBoundInstanceModelGroupedField instance) throws IOException {
      writeGroupedModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelGroupedField>) this::writeDiscriminatorProperty)
              .andThen(this::writeObjectProperties));
    }

    @Override
    public void writeItemField(Object item, IBoundDefinitionModelFieldComplex definition) throws IOException {
      writeDefinitionObject(
          definition,
          item,
          (ObjectWriter<IBoundDefinitionModelFieldComplex>) this::writeObjectProperties);
    }

    @Override
    public void writeItemFieldValue(Object item, IBoundFieldValue fieldValue) throws IOException {
      fieldValue.getJavaTypeAdapter().writeJsonValue(item, writer);
    }

    @Override
    public void writeItemAssembly(Object item, IBoundInstanceModelAssembly instance) throws IOException {
      writeModelObject(instance, item, this::writeObjectProperties);
    }

    @Override
    public void writeItemAssembly(Object item, IBoundInstanceModelGroupedAssembly instance) throws IOException {
      writeGroupedModelObject(
          instance,
          item,
          ((ObjectWriter<IBoundInstanceModelGroupedAssembly>) this::writeDiscriminatorProperty)
              .andThen(this::writeObjectProperties));
    }

    @Override
    public void writeItemAssembly(Object item, IBoundDefinitionModelAssembly definition) throws IOException {
      writeDefinitionObject(definition, item, this::writeObjectProperties);
    }

    @Override
    public void writeChoiceGroupItem(Object item, IBoundInstanceModelChoiceGroup instance) throws IOException {
      IBoundInstanceModelGroupedNamed actualInstance = instance.getItemInstance(item);
      assert actualInstance != null;
      actualInstance.writeItem(item, this);
    }

    private void writeScalarItem(@NonNull Object item, @NonNull IFeatureScalarItemValueHandler handler)
        throws IOException {
      handler.getJavaTypeAdapter().writeJsonValue(item, writer);
    }

    private <T extends IBoundInstanceModelGroupedNamed> void writeDiscriminatorProperty(
        @NonNull Object parentItem,
        @NonNull T instance) throws IOException {

      IBoundInstanceModelChoiceGroup choiceGroup = instance.getParentContainer();

      // write JSON object discriminator
      String discriminatorProperty = choiceGroup.getJsonDiscriminatorProperty();
      String discriminatorValue = instance.getEffectiveDisciminatorValue();

      writer.writeStringField(discriminatorProperty, discriminatorValue);
    }

    private <T extends IFeatureComplexItemValueHandler> void writeObjectProperties(
        @NonNull Object parentItem,
        @NonNull T handler) throws IOException {
      for (IBoundProperty instance : handler.getJsonProperties().values()) {
        assert instance != null;
        instance.writeItem(parentItem, MetaschemaJsonWriter.this);
      }
    }

    private <T extends IFeatureComplexItemValueHandler> void writeDefinitionObject(
        @NonNull T handler,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {
      writer.writeStartObject();

      propertyWriter.accept(parentItem, handler);
      writer.writeEndObject();
    }

    private <T extends IFeatureComplexItemValueHandler & IBoundInstanceModel> void writeModelObject(
        @NonNull T handler,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {
      writer.writeStartObject();

      IBoundInstanceFlag jsonKey = handler.getItemJsonKey(parentItem);
      if (jsonKey != null) {
        Object keyValue = jsonKey.getValue(parentItem);
        if (keyValue == null) {
          throw new IOException(String.format("Null value for json-key for definition '%s'",
              jsonKey.getContainingDefinition().toCoordinates()));
        }

        // the field will be the JSON key value
        String key = jsonKey.getJavaTypeAdapter().asString(keyValue);
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      propertyWriter.accept(parentItem, handler);

      if (jsonKey != null) {
        // next the value will be a start object
        writer.writeEndObject();
      }
      writer.writeEndObject();
    }

    private <T extends IFeatureComplexItemValueHandler & IBoundInstanceModelGroupedNamed> void writeGroupedModelObject(
        @NonNull T handler,
        @NonNull Object parentItem,
        @NonNull ObjectWriter<T> propertyWriter) throws IOException {
      writer.writeStartObject();

      IBoundInstanceModelChoiceGroup choiceGroup = handler.getParentContainer();
      IBoundInstanceFlag jsonKey = choiceGroup.getItemJsonKey(parentItem);
      if (jsonKey != null) {
        Object keyValue = jsonKey.getValue(parentItem);
        if (keyValue == null) {
          throw new IOException(String.format("Null value for json-key for definition '%s'",
              jsonKey.getContainingDefinition().toCoordinates()));
        }

        // the field will be the JSON key value
        String key = jsonKey.getJavaTypeAdapter().asString(keyValue);
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      propertyWriter.accept(parentItem, handler);

      if (jsonKey != null) {
        // next the value will be a start object
        writer.writeEndObject();
      }
      writer.writeEndObject();
    }
  }

  private class ModelInstanceWriteHandler
      extends AbstractModelInstanceWriteHandler {
    public ModelInstanceWriteHandler(
        @NonNull IBoundInstanceModel instance) {
      super(instance);
    }

    @Override
    public void writeList(List<?> items) throws IOException {
      IBoundInstanceModel instance = getCollectionInfo().getInstance();

      boolean writeArray = false;
      if (JsonGroupAsBehavior.LIST.equals(instance.getJsonGroupAsBehavior())
          || JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(instance.getJsonGroupAsBehavior())
              && items.size() > 1) {
        // write array, then items
        writeArray = true;
        writer.writeStartArray();
      } // only other option is a singleton value, write item

      super.writeList(items);

      if (writeArray) {
        // write the end array
        writer.writeEndArray();
      }
    }

    @Override
    public void writeItem(Object item) throws IOException {
      IBoundInstanceModel instance = getInstance();
      instance.writeItem(item, getItemWriter());
    }
  }
}
