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

package gov.nist.secauto.metaschema.binding.metapath.item;

import gov.nist.secauto.metaschema.binding.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

class XdmFlagNodeItemImpl
    extends AbstractBoundXdmValuedNodeItem
    implements IFlagNodeItem {

  @NotNull
  private final IBoundFlagInstance instance;
  @NotNull
  private final IModelNodeItem parent;

  /**
   * Used to cache this object as an atomic item.
   */
  private IAnyAtomicItem atomicItem;

  public XdmFlagNodeItemImpl(
      @NotNull IBoundFlagInstance instance,
      @NotNull Object value,
      @NotNull IModelNodeItem parent) {
    super(value);
    this.instance = instance;
    this.parent = parent;
  }

  @Override
  @NotNull
  public IBoundFlagInstance getInstance() {
    return instance;
  }

  @Override
  public IFlagDefinition getDefinition() {
    return getInstance().getDefinition();
  }

  @Override
  public IAnyAtomicItem toAtomicItem() {
    synchronized (this) {
      if (atomicItem == null) {
        atomicItem = getInstance().getDefinition().getJavaTypeAdapter().newItem(getValue());
      }
    }
    return ObjectUtils.notNull(atomicItem);
  }

  @Override
  @NotNull
  public IModelNodeItem getParentNodeItem() {
    return parent;
  }

  @Override
  public IModelNodeItem getParentContentNodeItem() {
    return getParentNodeItem();
  }

  @SuppressWarnings("null")
  @Override
  public Stream<? extends INodeItem> getPathStream() {
    return Stream.concat(getParentNodeItem().getPathStream(), Stream.of(this));
  }
}
