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
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundChoiceGroupInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundJavaProperty;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.info.AbstractModelInstanceWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureComplexItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IFeatureScalarItemValueHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaJsonWriter implements IJsonWritingContext, IItemWriteHandler {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonWriter.class);

  @NonNull
  private final JsonGenerator writer;

  /**
   * Construct a new Module-aware JSON writer.
   *
   * @param generator
   *          the JSON generator to write with
   * @see DefaultJsonProblemHandler
   */
  public MetaschemaJsonWriter(
      @NonNull JsonGenerator generator) {
    this.writer = generator;
  }

  @Override
  public JsonGenerator getWriter() {
    return writer;
  }

  /**
   * Writes data in a bound object to JSON. This assembly must be a root assembly
   * for which a call to {@link IAssemblyClassBinding#isRoot()} will return
   * {@code true}.
   *
   * @param targetDefinition
   *          the definition describing the root element data to write
   * @param targetObject
   *          the bound object
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  public void write(
      @NonNull IAssemblyClassBinding targetDefinition,
      @NonNull Object targetObject) throws IOException {
    if (!targetDefinition.isRoot()) {
      throw new UnsupportedOperationException(
          String.format("The assembly '%s' is not a root assembly.", targetDefinition.getBoundClass().getName()));
    }
    // first write the initial START_OBJECT
    writer.writeStartObject();

    writer.writeFieldName(targetDefinition.getRootJsonName());

    targetDefinition.writeItem(targetObject, this);

    // end of root object
    writer.writeEndObject();
  }

  @Override
  public void writeDefinitionValue(
      IClassBinding targetDefinition,
      Object targetObject,
      Map<String, ? extends IBoundJavaProperty> instances) throws IOException {
    for (IBoundJavaProperty instance : instances.values()) {
      assert instance != null;
      writeInstance(instance, targetObject);
    }

    if (targetDefinition instanceof IFieldClassBinding) {
      IFieldClassBinding fieldDefinition = (IFieldClassBinding) targetDefinition;
      IBoundFieldValueInstance fieldValueInstance = fieldDefinition.getFieldValueInstance();
      Object fieldValue = fieldValueInstance.getValue(targetObject);
      if (fieldValue != null) {
        String valueKeyName;
        IBoundFlagInstance jsonValueKey = fieldDefinition.getJsonValueKeyFlagInstance();
        if (jsonValueKey != null) {
          valueKeyName = jsonValueKey.getValueAsString(jsonValueKey.getValue(targetObject));
        } else {
          valueKeyName = fieldValueInstance.getJsonValueKeyName();
        }
        writer.writeFieldName(valueKeyName);
        fieldValueInstance.getJavaTypeAdapter().writeJsonValue(fieldValue, writer);
      }
    }
  }

  /**
   * Write the instance data contained in the {@code parentObject} based on the
   * structure described by the {@code targetInstance}.
   *
   * @param targetInstance
   *          the instance to write data for
   * @param parentObject
   *          the Java object containing the instance data to write
   * @throws IOException
   *           if an error occurred while writing the data
   */
  protected void writeInstance(
      @NonNull IBoundJavaProperty targetInstance,
      @NonNull Object parentObject)
      throws IOException {
    if (targetInstance instanceof IBoundFlagInstance) {
      writeFlagInstanceValue((IBoundFlagInstance) targetInstance, parentObject);
    } else if (targetInstance instanceof IBoundNamedModelInstance) {
      writeModelInstanceValues((IBoundNamedModelInstance) targetInstance, parentObject);
    } else if (targetInstance instanceof IBoundFieldValueInstance) {
      writeFieldValueInstanceValue((IBoundFieldValueInstance) targetInstance, parentObject);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetInstance.getClass().getName()));
    }
  }

  /**
   * Write the instance data contained in the {@code parentObject} based on the
   * structure described by the {@code targetInstance}.
   *
   * @param targetInstance
   *          the instance to write data for
   * @param parentObject
   *          the Java object containing the instance data to write
   * @throws IOException
   *           if an error occurred while writing the data
   */
  protected void writeFlagInstanceValue(
      @NonNull IBoundFlagInstance targetInstance,
      @NonNull Object parentObject) throws IOException {
    Object value = targetInstance.getValue(parentObject);
    if (value != null) {
      // write the field name
      writer.writeFieldName(targetInstance.getJsonName());

      // write the value
      targetInstance.getDefinition().getJavaTypeAdapter().writeJsonValue(value, writer);
    }
  }

  /**
   * Write the instance data contained in the {@code parentObject} based on the
   * structure described by the {@code targetInstance}.
   *
   * @param targetInstance
   *          the instance to write data for
   * @param parentObject
   *          the Java object containing the instance data to write
   * @throws IOException
   *           if an error occurred while writing the data
   */
  protected void writeModelInstanceValues(
      @NonNull IBoundNamedModelInstance targetInstance,
      @NonNull Object parentObject) throws IOException {
    Object value = targetInstance.getValue(parentObject);

    if (value != null && !targetInstance.getItemValues(value).isEmpty()) {
      // write the field name
      writer.writeFieldName(targetInstance.getJsonName());

      IModelInstanceCollectionInfo collectionInfo = targetInstance.getCollectionInfo();

      ModelInstanceWriteHandler handler = new ModelInstanceWriteHandler(collectionInfo);

      // dispatch to the property info implementation to address cardinality
      collectionInfo.writeItems(handler, value);
    }
  }

  /**
   * Write the instance data contained in the {@code parentObject} based on the
   * structure described by the {@code targetInstance}.
   *
   * @param targetInstance
   *          the instance to write data for
   * @param parentObject
   *          the Java object containing the instance data to write
   * @throws IOException
   *           if an error occurred while writing the data
   */
  protected void writeFieldValueInstanceValue(
      @NonNull IBoundFieldValueInstance targetInstance,
      @NonNull Object parentObject) throws IOException {
    Object value = targetInstance.getValue(parentObject);
    if (value != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      IBoundFlagInstance jsonValueKey = targetInstance.getContainingDefinition().getJsonValueKeyFlagInstance();

      String valueKeyName;
      if (jsonValueKey != null) {
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getValue(parentObject).toString();
      } else {
        valueKeyName = targetInstance.getJsonValueKeyName();
      }
      writer.writeFieldName(valueKeyName);
      LOGGER.info("FIELD: {}", valueKeyName);
      targetInstance.getJavaTypeAdapter().writeJsonValue(value, writer);
    }
  }

  @SuppressWarnings("resource") // not owned
  @Override
  public void writeScalarItem(Object item, IFeatureScalarItemValueHandler handler) throws IOException {
    handler.getJavaTypeAdapter().writeJsonValue(ObjectUtils.requireNonNull(item), getWriter());
  }

  @SuppressWarnings("resource") // not owned
  @Override
  public void writeComplexItem(Object item, IFeatureComplexItemValueHandler handler) throws IOException {
    JsonGenerator writer = getWriter();

    writer.writeStartObject();

    IClassBinding definition = handler.getClassBinding();
    IBoundFlagInstance jsonKey = handler.getJsonKey();
    if (jsonKey != null) {
      // the field will be the JSON key
      String key = jsonKey.toStringFromItem(item);
      if (key == null) {
        throw new IOException(new NullPointerException("Null key value"));
      }
      writer.writeFieldName(key);

      // next the value will be a start object
      writer.writeStartObject();
    }

    writeDefinitionValue(
        definition,
        item,
        MetaschemaJsonUtil.getJsonInstanceMap(definition, jsonKey));

    if (jsonKey != null) {
      writer.writeEndObject();
    }

    writer.writeEndObject();
  }

  @Override
  public void writeChoiceGroupItem(Object item, IBoundChoiceGroupInstance instance) {
    throw new UnsupportedOperationException("implement");
  }

  private class ModelInstanceWriteHandler
      extends AbstractModelInstanceWriteHandler {
    public ModelInstanceWriteHandler(
        @NonNull IModelInstanceCollectionInfo collectionInfo) {
      super(collectionInfo);
    }

    @Override
    public void writeList(List<?> items) throws IOException {
      @SuppressWarnings("resource") // not owned
      JsonGenerator writer = getWriter();
      IBoundModelInstance instance = getCollectionInfo().getInstance();

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
      IBoundModelInstance instance = getCollectionInfo().getInstance();
      instance.writeItem(item, MetaschemaJsonWriter.this);
    }
  }
}
