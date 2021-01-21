/**
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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.annotations.GroupAs;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

public class ListPropertyInfo extends AbstractCollectionPropertyInfo implements CollectionPropertyInfo {

  public ListPropertyInfo(ParameterizedType type, PropertyAccessor propertyAccessor, GroupAs groupAs) {
    super(type, propertyAccessor, groupAs);
  }

  @Override
  public Class<?> getItemType() {
    ParameterizedType actualType = getType();
    // this is a List so there is only a single generic type
    return (Class<?>) actualType.getActualTypeArguments()[0];
  }

  @Override
  public boolean isList() {
    return true;
  }

  @Override
  public boolean isMap() {
    return false;
  }

  @Override
  public ListPropertyCollector newPropertyCollector() {
    return new ListPropertyCollector(this);
  }

  private static class ListPropertyCollector extends AbstractPropertyCollector<ListPropertyInfo> {
    @SuppressWarnings("rawtypes")
    private List collection;

    @SuppressWarnings("rawtypes")
    protected ListPropertyCollector(ListPropertyInfo propertyInfo) {
      super(propertyInfo);
      this.collection = new LinkedList();
    }

    @Override
    public void applyCollection(Object obj) throws BindingException {
      getPropertyInfo().getPropertyAccessor().setValue(obj, collection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(Object item) {
      collection.add(item);
    }

    @Override
    protected Object getCollection() {
      return collection;
    }

  }

}
