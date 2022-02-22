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

import gov.nist.secauto.metaschema.binding.model.IBoundFieldDefinition;
import gov.nist.secauto.metaschema.binding.model.IFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.IBoundFieldInstance;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidTypeFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractBoundXdmFieldNodeItem<INSTANCE extends IBoundFieldInstance>
    extends AbstractBoundXdmModelNodeItem<INSTANCE>
    implements IBoundXdmFieldNodeItem {

  /**
   * Used to cache this object as an atomic item.
   */
  private IAnyAtomicItem atomicItem;

  public AbstractBoundXdmFieldNodeItem(
      @NotNull INSTANCE instance,
      @NotNull Object value,
      int position) {
    super(instance, value, position);
  }

  protected synchronized void initAtomicItem() {
    if (atomicItem == null) {
      IDefinition definition = getInstance().getDefinition();
      if (definition instanceof IValuedDefinition) {
        IJavaTypeAdapter<?> type = ((IValuedDefinition) definition).getDatatype();
        atomicItem = type.newItem(getValue());
      } else {
        throw new InvalidTypeFunctionMetapathException(InvalidTypeFunctionMetapathException.NODE_HAS_NO_TYPED_VALUE,
            String.format("the node type '%s' does not have a typed value", this.getItemName()));
      }
    }
  }

  @Override
  public IAnyAtomicItem toAtomicItem() {
    initAtomicItem();
    return atomicItem;
  }

  @Override
  public Map<@NotNull String, ? extends List<@NotNull ? extends IBoundXdmModelNodeItem>> getModelItems() {
    return Collections.emptyMap();
  }

  @Override
  public IFieldClassBinding getClassBinding() {
    return getDefinition().getClassBinding();
  }

  @Override
  public IBoundFieldDefinition getDefinition() {
    return getInstance().getDefinition();
  }
}
