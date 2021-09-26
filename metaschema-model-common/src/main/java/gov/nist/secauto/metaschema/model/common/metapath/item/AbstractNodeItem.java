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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.datatypes.DataTypes;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IValuedDefinition;
import gov.nist.secauto.metaschema.model.common.metapath.format.IPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.type.TypeFactory;

import java.util.Objects;

public abstract class AbstractNodeItem<SEGMENT extends IPathSegment, PARENT extends IModelNodeItem>
    extends AbstractPathItem<SEGMENT> implements INodeItem {
  /**
   * The current node.
   */
  private final Object value;

  private final PARENT parent;

  // /**
  // * Used to cache this object as a string.
  // */
  // private IStringItem stringItem;

  /**
   * Used to cache this object as an atomic item.
   */
  private IAnyAtomicItem atomicItem;

  public AbstractNodeItem(Object value, SEGMENT segment, PARENT parent) {
    super(segment);
    Objects.requireNonNull(value, "value");
    this.value = value;
    this.parent = parent;
  }

  @Override
  public INodeItem getNodeItem() {
    return this;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public PARENT getParent() {
    return parent;
  }

  protected synchronized void initAtomicItem() {
    if (atomicItem == null) {
      IDefinition definition = getPathSegment().getDefinition();
      if (definition instanceof IValuedDefinition) {
        DataTypes type = ((IValuedDefinition) definition).getDatatype();
        atomicItem = TypeFactory.instance().getTypeForDataType(type).newItem(getValue());
      } else {
        throw new MetapathDynamicException("FOTY0012",
            String.format("the node type '%s' does not have a typed value", this.getClass().getName()));
      }
    }
  }

  @Override
  public IAnyAtomicItem toAtomicItem() {
    initAtomicItem();
    return atomicItem;
  }
  //
  // protected synchronized void initStringItem() {
  // if (stringItem == null && value != null) {
  // IDefinition definition = getDefinition();
  // if (definition instanceof IValuedDefinition) {
  // String string = ((IValuedDefinition)
  // definition).getDatatype().getJavaTypeAdapter().asString(value);
  // stringItem = IStringItem.valueOf(string);
  // } else {
  // throw new UnsupportedOperationException();
  // }
  // stringItem = IStringItem.valueOf(asString());
  // }
  // }
  //
  // @Override
  // public IStringItem toStringItem() {
  // initStringItem();
  // return stringItem;
  // }

  @Override
  public String toString() {
    return super.toString() + " " + getValue();
  }
}
