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

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedInstance;
import gov.nist.secauto.metaschema.databind.model.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.IRootAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.info.IModelPropertyInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaJsonWriter implements IJsonWritingContext {
  private static final Logger LOGGER = LogManager.getLogger(MetaschemaJsonWriter.class);

  @NonNull
  private final JsonGenerator writer;

  public MetaschemaJsonWriter(
      @NonNull JsonGenerator generator) {
    this.writer = generator;
  }

  @Override
  public JsonGenerator getWriter() {
    return writer;
  }

  /**
   * Writes data in a bound object to JSON. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   *
   * @param targetDefinition
   *          the definition describing the root element data to write
   * @param targetObject
   *          the bound object
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  public void write(
      @NonNull IRootAssemblyClassBinding targetDefinition,
      @NonNull Object targetObject) throws IOException {

    // first read the initial START_OBJECT
    writer.writeStartObject();

    writer.writeFieldName(targetDefinition.getRootJsonName());

    writeDefinitionValues(targetDefinition, CollectionUtil.singleton(targetObject), true);

    // end of root object
    writer.writeEndObject();
  }

  @Override
  public void writeDefinitionValues(
      IClassBinding targetDefinition,
      Collection<? extends Object> targetObjects,
      boolean writeObjectWrapper)
      throws IOException {
    if (targetDefinition instanceof IAssemblyClassBinding) {
      writeDefinitionValues((IAssemblyClassBinding) targetDefinition, targetObjects, writeObjectWrapper);
    } else if (targetDefinition instanceof IFieldClassBinding) {
      writeDefinitionValues((IFieldClassBinding) targetDefinition, targetObjects, writeObjectWrapper);
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported class binding type: %s", targetDefinition.getClass().getName()));
    }
  }

  protected void writeDefinitionValues(
      @NonNull IAssemblyClassBinding targetDefinition,
      Collection<? extends Object> targetObjects,
      boolean writeObjectWrapper)
      throws IOException {
    for (Object item : targetObjects) {
      assert item != null;
      writeDefinitionValue(targetDefinition, item, writeObjectWrapper);
    }
  }

  protected void writeDefinitionValues(
      @NonNull IFieldClassBinding targetDefinition,
      @NonNull Collection<? extends Object> items,
      boolean writeObjectWrapper)
      throws IOException {
    if (items.isEmpty()) {
      return;
    }

    Predicate<IBoundFlagInstance> flagFilter = null;

    IBoundFlagInstance jsonKey = targetDefinition.getJsonKeyFlagInstance();
    if (jsonKey != null) {
      flagFilter = (flag) -> {
        return !jsonKey.equals(flag);
      };
    }

    IBoundFlagInstance jsonValueKey = targetDefinition.getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      if (flagFilter == null) {
        flagFilter = (flag) -> {
          return !jsonValueKey.equals(flag);
        };
      } else {
        flagFilter = flagFilter.and((flag) -> {
          return !jsonValueKey.equals(flag);
        });
      }
    }

    Map<String, ? extends IBoundNamedInstance> properties = targetDefinition.getNamedInstances(flagFilter);

    for (Object item : items) {
      assert item != null;
      if (writeObjectWrapper) {
        writer.writeStartObject();
      }

      if (jsonKey != null) {
        // if there is a json key, the first field will be the key
        Object flagValue = jsonKey.getValue(item);
        String key = jsonKey.getValueAsString(flagValue);
        if (key == null) {
          throw new IOException(new NullPointerException("Null key value")); // NOPMD - intentional
        }
        writer.writeFieldName(key);

        // next the value will be a start object
        writer.writeStartObject();
      }

      for (IBoundNamedInstance property : properties.values()) {
        assert property != null;
        writeInstance(property, item);
      }

      IBoundFieldValueInstance fieldValueInstance = targetDefinition.getFieldValueInstance();
      Object fieldValue = fieldValueInstance.getValue(item);
      if (fieldValue != null) {
        String valueKeyName;
        if (jsonValueKey != null) {
          valueKeyName = jsonValueKey.getValueAsString(jsonValueKey.getValue(item));
        } else {
          valueKeyName = fieldValueInstance.getJsonValueKeyName();
        }
        writer.writeFieldName(valueKeyName);
        fieldValueInstance.getJavaTypeAdapter().writeJsonValue(fieldValue, writer);
      }

      if (jsonKey != null) {
        writer.writeEndObject();
      }

      if (writeObjectWrapper) {
        writer.writeEndObject();
      }
    }
  }

  /**
   * Serializes the provided instance in JSON.
   *
   * @param targetDefinition
   *          the definition describing the data to write
   * @param targetObject
   *          the instance to serialize
   * @param writeObjectWrapper
   *          {@code true} if the start and end object should be written, or {@code false} otherwise
   * @throws IOException
   *           if an error occurs while writing to the output context
   * @throws NullPointerException
   *           if there is a JSON key configured and the key property's value is {@code null}
   */
  protected void writeDefinitionValue(
      @NonNull IAssemblyClassBinding targetDefinition,
      @NonNull Object targetObject,
      boolean writeObjectWrapper)
      throws IOException {
    if (writeObjectWrapper) {
      writer.writeStartObject();
    }

    IBoundFlagInstance jsonKey = targetDefinition.getJsonKeyFlagInstance();
    Map<String, ? extends IBoundNamedInstance> properties;
    if (jsonKey == null) {
      properties = targetDefinition.getNamedInstances(null);
    } else {
      properties = targetDefinition.getNamedInstances((flag) -> {
        return !jsonKey.equals(flag);
      });

      // if there is a json key, the first field will be the key
      Object flagValue = jsonKey.getValue(targetObject);
      String key = jsonKey.getValueAsString(flagValue);
      if (key == null) {
        throw new IOException(new NullPointerException("Null key value"));
      }
      writer.writeFieldName(key);

      // next the value will be a start object
      writer.writeStartObject();
    }

    for (IBoundNamedInstance property : properties.values()) {
      assert property != null;
      writeInstance(property, targetObject);
    }

    if (jsonKey != null) {
      // write the END_OBJECT for the JSON key value
      writer.writeEndObject();
    }

    if (writeObjectWrapper) {
      writer.writeEndObject();
    }
  }

  protected void writeInstance(
      @NonNull IBoundNamedInstance targetInstance,
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

  protected void writeModelInstanceValues(
      @NonNull IBoundNamedModelInstance targetInstance,
      @NonNull Object parentInstance) throws IOException {
    IModelPropertyInfo propertyInfo = targetInstance.getPropertyInfo();
    if (propertyInfo.isValueSet(parentInstance)) {
      // write the field name
      writer.writeFieldName(targetInstance.getJsonName());

      // dispatch to the property info implementation to address cardinality
      propertyInfo.writeValues(parentInstance, this);
    }
  }

  protected void writeFieldValueInstanceValue(
      @NonNull IBoundFieldValueInstance targetInstance,
      @NonNull Object instance) throws IOException {
    Object value = targetInstance.getValue(instance);
    if (value != null) {
      // There are two modes:
      // 1) use of a JSON value key, or
      // 2) a simple value named "value"
      IBoundFlagInstance jsonValueKey = targetInstance.getParentClassBinding().getJsonValueKeyFlagInstance();

      String valueKeyName;
      if (jsonValueKey != null) {
        // this is the JSON value key case
        valueKeyName = jsonValueKey.getValue(instance).toString();
      } else {
        valueKeyName = targetInstance.getJsonValueKeyName();
      }
      writer.writeFieldName(valueKeyName);
      LOGGER.info("FIELD: {}", valueKeyName);
      targetInstance.getJavaTypeAdapter().writeJsonValue(value, writer);
    }
  }
}
