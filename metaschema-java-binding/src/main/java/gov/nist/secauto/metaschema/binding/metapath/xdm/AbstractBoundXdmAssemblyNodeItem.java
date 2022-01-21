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

package gov.nist.secauto.metaschema.binding.metapath.xdm;

import gov.nist.secauto.metaschema.binding.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyProperty;
import gov.nist.secauto.metaschema.binding.model.property.FieldProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractBoundXdmAssemblyNodeItem<INSTANCE extends AssemblyProperty>
    extends AbstractBoundXdmModelNodeItem<INSTANCE> implements IBoundXdmAssemblyNodeItem {

  private Map<String, List<IBoundXdmModelNodeItem>> modelItems;

  public AbstractBoundXdmAssemblyNodeItem(INSTANCE instance, Object value, int position) {
    super(instance, value, position);
  }

  @Override
  public AssemblyDefinition getDefinition() {
    return getInstance().getDefinition();
  }

  @Override
  public Map<String, List<IBoundXdmModelNodeItem>> getModelItems() {
    initModelItems();
    return modelItems;
  }

  protected synchronized void initModelItems() {
    if (this.modelItems == null) {
      Map<String, List<IBoundXdmModelNodeItem>> modelItems = new LinkedHashMap<>();
      Object parentValue = getValue();
      for (NamedModelProperty instance : getDefinition().getNamedModelInstances()) {

        Object instanceValue = instance.getValue(parentValue);
        Stream<? extends Object> itemValues = instance.getItemValues(instanceValue).stream();
        AtomicInteger index = new AtomicInteger();
        List<IBoundXdmModelNodeItem> items = itemValues.map(itemValue -> {
          IBoundXdmModelNodeItem item;
          if (instance instanceof AssemblyProperty) {
            item = IXdmFactory.INSTANCE.newAssemblyNodeItem((AssemblyProperty) instance, itemValue,
                index.incrementAndGet(), this);
          } else if (instance instanceof FieldProperty) {
            item = IXdmFactory.INSTANCE.newFieldNodeItem((FieldProperty) instance, itemValue, index.incrementAndGet(),
                this);
          } else {
            throw new UnsupportedOperationException("unsupported instance type: " + instance.getClass().getName());
          }
          return item;
        }).collect(Collectors.toList());
        modelItems.put(instance.getEffectiveName(), items);
      }
      this.modelItems = modelItems;
    }
  }
}
