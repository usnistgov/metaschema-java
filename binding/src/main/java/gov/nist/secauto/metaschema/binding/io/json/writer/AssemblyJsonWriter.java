/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.io.json.writer;

import com.fasterxml.jackson.core.JsonGenerator;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.old.AssemblyItemBinding;
import gov.nist.secauto.metaschema.binding.io.json.old.BoundClassItemBinding;
import gov.nist.secauto.metaschema.binding.io.json.old.DataTypeItemBinding;
import gov.nist.secauto.metaschema.binding.io.json.old.FieldItemBinding;
import gov.nist.secauto.metaschema.binding.io.json.old.ItemBinding;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldValuePropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.ModelItemPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.model.property.PropertyInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AssemblyJsonWriter<CLASS> extends AbstractJsonWriter<CLASS, AssemblyClassBinding<CLASS>> {

  public AssemblyJsonWriter(AssemblyClassBinding<CLASS> classBinding) {
    super(classBinding);
  }

  @Override
  public void writeJson(Object obj, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException {
    JsonGenerator generator = writingContext.getEventWriter();
    try {
      generator.writeStartObject();

      List<FlagPropertyBinding> flags = FlagUtil.filterFlags(obj, getClassBinding().getFlagPropertyBindings(), filter);

      if (!flags.isEmpty()) {
        // Output flags
        FlagUtil.writeFlags(obj, flags, writingContext);
      }

      writeBody(obj, writingContext);

      generator.writeEndObject();
    } catch (IOException ex) {
      throw new BindingException(ex);
    }
  }

  protected void writeBody(Object obj, JsonWritingContext writingContext) throws BindingException, IOException {
    for (ModelItemPropertyBinding propertyBinding : getClassBinding().getModelItemPropertyBindings()) {
      PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();
      Object value = propertyInfo.getValue(obj);

      if (value != null) {
        handlePropertyBinding(value, propertyBinding, writingContext);
      }
    }
  }

  protected void handlePropertyBinding(Object value, ModelItemPropertyBinding propertyBinding,
      JsonWritingContext writingContext) throws BindingException, IOException {
    PropertyInfo propertyInfo = propertyBinding.getPropertyInfo();
    BindingContext bindingContext = writingContext.getBindingContext();

    // first determine what type of item we need to write
    Class<?> itemClass = propertyBinding.getPropertyInfo().getItemType();
    ClassBinding<?> itemClassBinding = bindingContext.getClassBinding(itemClass);
    ItemBinding<?> itemBinding;
    if (itemClassBinding != null) {
      if (itemClassBinding instanceof FieldClassBinding) {
        itemBinding
            = new FieldItemBinding((FieldClassBinding<?>) itemClassBinding, (FieldPropertyBinding) propertyBinding);
      } else if (itemClassBinding instanceof AssemblyClassBinding) {
        itemBinding = new AssemblyItemBinding((AssemblyClassBinding<?>) itemClassBinding,
            (AssemblyPropertyBinding) propertyBinding);
      } else {
        throw new UnsupportedOperationException(String.format("Unsupported class binding '%s' for class '%s'",
            itemClassBinding.getClass().getName(), itemClassBinding.getClazz().getName()));
      }
    } else {
      itemBinding = new DataTypeItemBinding(propertyBinding);
    }

    if (propertyInfo instanceof CollectionPropertyInfo) {
      CollectionPropertyInfo collectionPropertyInfo = (CollectionPropertyInfo) propertyInfo;
      if (collectionPropertyInfo.isList()) { // In this case we need to consider if collapse is used
        boolean isAllowSingleton
            = JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(collectionPropertyInfo.getJsonGroupAsBehavior());
        @SuppressWarnings("unchecked")
        List<? extends Object> values = (List<? extends Object>) value;
        if (!values.isEmpty()) {
          if (itemClassBinding != null) {
            if (itemClassBinding instanceof FieldClassBinding
                && ((FieldClassBinding<?>) itemClassBinding).isCollapsible()) {

              if (itemClassBinding.getFlagPropertyBindings().isEmpty() || values.size() == 1) {
                // no need to collapse a single entry
                handleCollection(values, isAllowSingleton, itemBinding, writingContext);
              } else {
                @SuppressWarnings("unchecked")
                BoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding> fieldItemBinding
                    = (BoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding>) itemBinding;
                handleCollapsibleFieldCollection(values, isAllowSingleton, fieldItemBinding, writingContext);
              }
            } else {
              handleCollection(values, isAllowSingleton, itemBinding, writingContext);
            }
          } else {
            handleCollection(values, isAllowSingleton, itemBinding, writingContext);
          }
        }
      } else {
        // must be a map, which implies use of JsonGroupAsBehavior.KEYED with a bound
        // class that has a JsonKey
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) value;
        Collection<Object> entries = values.values();
        if (!values.isEmpty()) {
          @SuppressWarnings("unchecked")
          BoundClassItemBinding<ClassBinding<?>, ModelItemPropertyBinding> boundClassItemBinding
              = (BoundClassItemBinding<ClassBinding<?>, ModelItemPropertyBinding>) itemBinding;
          handleMapPropertyBinding(entries, boundClassItemBinding, writingContext);
        }
      }
    } else {
      handleCollection(Collections.singletonList(value), true, itemBinding, writingContext);
    }
  }

  private void handleCollection(List<? extends Object> items, boolean isAllowSingleton, ItemBinding<?> itemBinding,
      JsonWritingContext writingContext) throws BindingException, IOException {

    // figure out the field name
    ModelItemPropertyBinding propertyBinding = itemBinding.getPropertyBinding();
    String fieldName = propertyBinding.getJsonFieldName(writingContext.getBindingContext());

    JsonGenerator generator = writingContext.getEventWriter();
    generator.writeFieldName(fieldName);

    if (isAllowSingleton && items.size() == 1) {
      itemBinding.writeValue(items.get(0), null, writingContext);
    } else {
      generator.writeStartArray();

      for (Object item : items) {
        itemBinding.writeValue(item, null, writingContext);
      }

      generator.writeEndArray();
    }
  }

  private void handleMapPropertyBinding(Collection<Object> items,
      BoundClassItemBinding<ClassBinding<?>, ModelItemPropertyBinding> itemBinding, JsonWritingContext writingContext)
      throws IOException, BindingException {

    FlagPropertyBinding jsonKey = itemBinding.getClassBinding().getJsonKeyFlagPropertyBinding();
    PropertyInfo jsonKeyPropertyInfo = jsonKey.getPropertyInfo();

    // figure out the field name
    ModelItemPropertyBinding propertyBinding = itemBinding.getPropertyBinding();

    String fieldName = propertyBinding.getJsonFieldName(writingContext.getBindingContext());

    JsonGenerator generator = writingContext.getEventWriter();
    generator.writeFieldName(fieldName);

    generator.writeStartObject();

    for (Object item : items) {
      String key = jsonKeyPropertyInfo.getValue(item).toString();

      generator.writeFieldName(key);
      itemBinding.writeValue(item, (PropertyBinding binding) -> binding.equals(jsonKey), writingContext);
    }

    generator.writeEndObject();
  }

  private void handleCollapsibleFieldCollection(List<? extends Object> values, boolean isAllowSingleton,
      BoundClassItemBinding<FieldClassBinding<?>, FieldPropertyBinding> itemBinding, JsonWritingContext writingContext)
      throws BindingException, IOException {

    FieldClassBinding<?> classBinding = itemBinding.getClassBinding();
    FieldValuePropertyBinding fieldValue = classBinding.getFieldValuePropertyBinding();
    PropertyInfo fieldValuePropertyInfo = fieldValue.getPropertyInfo();
    CollapseKeyBuilder builder = new CollapseKeyBuilder(classBinding);

    Map<CollapseKeyBuilder.Key, List<Object>> keyToValuesMap = new LinkedHashMap<>();
    for (Object item : values) {
      CollapseKeyBuilder.Key key = builder.build(item);

      Object value = fieldValuePropertyInfo.getValue(item);

      List<Object> itemValues = keyToValuesMap.get(key);
      if (itemValues == null) {
        itemValues = new LinkedList<>();
        keyToValuesMap.put(key, itemValues);
      }
      itemValues.add(value);
    }

    // figure out the field name
    // String fieldName = collectionPropertyInfo.getGroupXmlQName().getLocalPart();
    String fieldName = itemBinding.getPropertyBinding().getJsonFieldName(writingContext.getBindingContext());

    JsonGenerator generator = writingContext.getEventWriter();
    generator.writeFieldName(fieldName);

    if (isAllowSingleton && keyToValuesMap.size() == 1) {
      Map.Entry<CollapseKeyBuilder.Key, List<Object>> entry = keyToValuesMap.entrySet().iterator().next();
      entry.getKey().write(entry.getValue(), writingContext);
    } else {
      generator.writeStartArray();

      for (Map.Entry<CollapseKeyBuilder.Key, List<Object>> entry : keyToValuesMap.entrySet()) {
        entry.getKey().write(entry.getValue(), writingContext);
      }

      generator.writeEndArray();
    }
  }
}
