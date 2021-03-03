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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValueProperty;
import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollapseKeyBuilder {
  private final FieldClassBinding classBinding;
  private final ArrayList<FlagProperty> flagProperties;
  private final Map<Key, List<Object>> keyToValuesMap;

  public CollapseKeyBuilder(FieldClassBinding classBinding) {
    this.classBinding = classBinding;
    this.flagProperties = new ArrayList<>(classBinding.getFlagProperties().values());
    this.keyToValuesMap = new LinkedHashMap<>();
  }

  protected FieldClassBinding getClassBinding() {
    return classBinding;
  }

  protected ArrayList<FlagProperty> getFlagProperties() {
    return flagProperties;
  }

  public void addAll(Collection<? extends Object> instances) throws IOException {
    for (Object instance : instances) {
      add(instance);
    }
  }

  public void add(Object instance) throws IOException {
    int size = getFlagProperties().size();
    Object[] flagValues = new Object[size];
    int index = 0;
    for (FlagProperty flag : getFlagProperties()) {
      flagValues[index++] = flag.getValue(instance);
    }

    Key key = new Key(flagValues);
    List<Object> values = this.keyToValuesMap.get(key);
    if (values == null) {
      values = new LinkedList<>();
      this.keyToValuesMap.put(key, values);
    }

    Object value = getClassBinding().getFieldValue().getValue(instance);
    values.add(value);
  }

  public void write(boolean writeObjectWrapper, JsonWritingContext context) throws IOException {
    FieldClassBinding classBinding = getClassBinding();
    FlagProperty jsonKey = classBinding.getJsonKey();
    FlagProperty jsonValueKey = classBinding.getJsonValueKeyFlag();
    FieldValueProperty fieldValue = classBinding.getFieldValue();
    ArrayList<FlagProperty> flagProperties = getFlagProperties();

    // first build an index of the flag properties
    List<Integer> flagIndex;
    Integer jsonKeyIndex = null;
    Integer jsonValueKeyIndex = null;
    if (flagProperties.isEmpty()) {
      flagIndex = Collections.emptyList();
    } else {
      flagIndex = new ArrayList<>(flagProperties.size());
      int index = 0;
      for (FlagProperty flag : flagProperties) {
        if (jsonKey != null && flag.isJsonKey()) {
          jsonKeyIndex = index++;
        } else if (flag.isJsonValueKey()) {
          jsonValueKeyIndex = index++;
        } else {
          // regular properties
          flagIndex.add(index++);
        }
      }
    }

    JsonGenerator writer = context.getWriter();
    // for each key, we need to write the properties
    for (Map.Entry<Key, List<Object>> entry : keyToValuesMap.entrySet()) {
      Key key = entry.getKey();

      Object[] flagValues = key.getFlagValues();

      if (writeObjectWrapper) {
        writer.writeStartObject();
      }

      // first write the JSON key if it is configured
      if (jsonKey != null && jsonKeyIndex != null) {
        // the field
        writer.writeFieldName(jsonKey.getValueAsString(flagValues[jsonKeyIndex]));

        writer.writeStartObject();
      }

      // next, write the flags
      for (int index : flagIndex) {
        FlagProperty flag = flagProperties.get(index);
        Object flagValue = flagValues[index];

        if (flagValue != null) {
          writer.writeFieldName(flag.getJsonPropertyName());
          flag.writeValue(flagValue, context);
        }
      }

      // finally write the field value
      List<Object> fieldValues = entry.getValue();
      if (!fieldValues.isEmpty()) {
        String valueKeyName;
        if (jsonValueKey != null && jsonValueKeyIndex != null) {
          valueKeyName = jsonValueKey.getValueAsString(flagValues[jsonValueKeyIndex]);
        } else {
          valueKeyName = fieldValue.getJsonPropertyName();
        }
        writer.writeFieldName(valueKeyName);
        if (fieldValues.size() > 1) {
          writer.writeStartArray();
        }
        for (Object value : fieldValues) {
          fieldValue.writeValue(value, context);
        }
        if (fieldValues.size() > 1) {
          writer.writeEndArray();
        }
      }

      if (jsonKey != null) {
        // close the JSON key's object
        writer.writeEndObject();
      }

      if (writeObjectWrapper) {
        writer.writeEndObject();
      }
    }
  }

  public class Key {
    private final Object[] flagValues;
    private Integer hashCode;

    public Key(Object[] flagValues) {
      this.flagValues = flagValues;
    }

    protected Object[] getFlagValues() {
      return flagValues;
    }

    @Override
    public synchronized int hashCode() {
      if (hashCode == null) {
        final int prime = 31;
        hashCode = 1;
        hashCode = prime * hashCode + getEnclosingInstance().hashCode();
        hashCode = prime * hashCode + Arrays.hashCode(flagValues);
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Key)) {
        return false;
      }
      Key other = (Key) obj;
      if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
        return false;
      }
      return Objects.equals(flagValues, other.flagValues);
    }

    private CollapseKeyBuilder getEnclosingInstance() {
      return CollapseKeyBuilder.this;
    }
  }
}
