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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassIntrospector;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapPropertyInfo extends AbstractCollectionPropertyInfo implements CollectionPropertyInfo {

  public MapPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
    super(type, propertyAccessor, groupAs);
  }

  public Class<?> getKeyType() {
    ParameterizedType actualType = getType();
    // this is a Map so the first generic type is the key
    return (Class<?>) actualType.getActualTypeArguments()[0];
  }

  @Override
  public Class<?> getItemType() {
    return getValueType();
  }

  public Class<?> getValueType() {
    ParameterizedType actualType = getType();
    // this is a Map so the second generic type is the value
    return (Class<?>) actualType.getActualTypeArguments()[1];
  }

  @Override
  public boolean isList() {
    return false;
  }

  @Override
  public boolean isMap() {
    return true;
  }

  @Override
  public MapPropertyCollector newPropertyCollector() {
    return new MapPropertyCollector(this);
  }

  public PropertyAccessor getJsonKey() {
    Class<?> itemClass = (Class<?>) getItemType();
    return ClassIntrospector.getJsonKey(itemClass);
  }

  private static class MapPropertyCollector extends AbstractPropertyCollector<MapPropertyInfo> {
    private final Map<String, Object> map = new LinkedHashMap<>();

    protected MapPropertyCollector(MapPropertyInfo propertyInfo) {
      super(propertyInfo);
    }

    @Override
    public void add(Object item) throws BindingException {
      PropertyAccessor keyAccessor = getPropertyInfo().getJsonKey();
      if (keyAccessor == null) {
        throw new BindingException("No JSON key found");
      }

      String key = keyAccessor.getValue(item).toString();
      map.put(key, item);
    }

    @Override
    protected Object getCollection() {
      return map;
    }

  }
}
