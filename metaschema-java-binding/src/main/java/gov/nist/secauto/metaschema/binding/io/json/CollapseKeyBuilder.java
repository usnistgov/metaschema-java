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

import gov.nist.secauto.metaschema.binding.model.IBoundFieldValueInstance;
import gov.nist.secauto.metaschema.binding.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.binding.model.IFieldClassBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CollapseKeyBuilder {
  private final IFieldClassBinding classBinding;
  private final List<IBoundFlagInstance> flagProperties;
  private final Map<CollapseKey, List<Object>> keyToValuesMap;

  public CollapseKeyBuilder(IFieldClassBinding classBinding) {
    this.classBinding = classBinding;
    this.flagProperties = new ArrayList<>(classBinding.getFlagInstances());
    this.keyToValuesMap = new LinkedHashMap<>();
  }

  protected IFieldClassBinding getClassBinding() {
    return classBinding;
  }

  protected List<IBoundFlagInstance> getFlagProperties() {
    return flagProperties;
  }

  public void addAll(Collection<? extends Object> instances) {
    for (Object instance : instances) {
      add(instance);
    }
  }

  // TODO: check and handle nullness of values
  public void add(@NonNull Object instance) {
    int size = getFlagProperties().size();
    Object[] flagValues = new Object[size];
    int index = 0;
    for (IBoundFlagInstance flag : getFlagProperties()) {
      flagValues[index++] = flag.getValue(instance);
    }

    CollapseKey key = new CollapseKey(flagValues);
    List<Object> values = this.keyToValuesMap.get(key);
    if (values == null) {
      values = new LinkedList<>();
      this.keyToValuesMap.put(key, values);
    }

    Object value = getClassBinding().getFieldValueInstance().getValue(instance);
    values.add(value);
  }

  public void write(boolean writeObjectWrapper, @NonNull IJsonWritingContext context) throws IOException {
    IFieldClassBinding classBinding = getClassBinding();
    IBoundFlagInstance jsonKey = classBinding.getJsonKeyFlagInstance();
    IBoundFlagInstance jsonValueKey = classBinding.getJsonValueKeyFlagInstance();
    IBoundFieldValueInstance fieldValue = classBinding.getFieldValueInstance();
    List<IBoundFlagInstance> flagProperties = getFlagProperties();

    // first build an index of the flag properties
    List<Integer> flagIndex;
    Integer jsonKeyIndex = null;
    Integer jsonValueKeyIndex = null;
    if (flagProperties.isEmpty()) {
      flagIndex = Collections.emptyList();
    } else {
      flagIndex = new ArrayList<>(flagProperties.size());
      int index = 0;
      for (IBoundFlagInstance flag : flagProperties) {
        if (jsonKey != null && jsonKey.equals(flag)) {
          jsonKeyIndex = index++;
        } else if (jsonValueKey != null && jsonValueKey.equals(flag)) {
          jsonValueKeyIndex = index++;
        } else {
          // regular properties
          flagIndex.add(index++);
        }
      }
    }

    JsonGenerator writer = context.getWriter(); // NOPMD - intentional
    // for each key, we need to write the properties
    for (Map.Entry<CollapseKey, List<Object>> entry : keyToValuesMap.entrySet()) {
      CollapseKey key = entry.getKey();

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
        IBoundFlagInstance flag = flagProperties.get(index);
        Object flagValue = flagValues[index];

        if (flagValue != null) {
          writer.writeFieldName(flag.getJsonName());
          flag.writeValue(flagValue, context);
        }
      }

      // finally write the field value
      List<Object> fieldValues = entry.getValue();
      if (!fieldValues.isEmpty()) {
        String valueKeyName;
        if (jsonValueKey == null || jsonValueKeyIndex == null) {
          valueKeyName = fieldValue.getJsonValueKeyName();
        } else {
          valueKeyName = jsonValueKey.getValueAsString(flagValues[jsonValueKeyIndex]);
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

  public class CollapseKey {
    private final Object[] flagValues;
    private Integer cachedHashCode;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "access is restricted")
    public CollapseKey(Object... values) {
      this.flagValues = Arrays.copyOf(values, values.length);
    }

    protected Object[] getFlagValues() {
      return Arrays.copyOf(flagValues, flagValues.length);
    }

    @Override
    public int hashCode() {
      synchronized (this) {
        if (cachedHashCode == null) {
          final int prime = 31;
          cachedHashCode = 1;
          cachedHashCode = prime * cachedHashCode + getEnclosingInstance().hashCode();
          cachedHashCode = prime * cachedHashCode + Arrays.hashCode(flagValues);
        }
        return cachedHashCode;
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true; // NOPMD - readability
      }
      if (!(obj instanceof CollapseKey)) {
        return false; // NOPMD - readability
      }
      CollapseKey other = (CollapseKey) obj;
      if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
        return false; // NOPMD - readability
      }
      return Arrays.equals(flagValues, other.flagValues);
    }

    private CollapseKeyBuilder getEnclosingInstance() {
      return CollapseKeyBuilder.this;
    }
  }
}
