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

package gov.nist.secauto.metaschema.core.metapath.cst.path;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpressionVisitor;
import gov.nist.secauto.metaschema.core.metapath.item.ItemUtils;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.ShortClassName") // intentional
public enum Axis implements IExpression {
  SELF(Stream::of),
  PARENT(focus -> Stream.ofNullable(focus.getParentNodeItem())),
  ANCESTOR(INodeItem::ancestor),
  ANCESTOR_OR_SELF(INodeItem::ancestorOrSelf),
  CHILDREN(INodeItem::modelItems),
  DESCENDANT(INodeItem::descendant),
  DESCENDANT_OR_SELF(INodeItem::descendantOrSelf);

  @NonNull
  private final Function<INodeItem, Stream<? extends INodeItem>> action;

  Axis(@NonNull Function<INodeItem, Stream<? extends INodeItem>> action) {
    this.action = action;
  }

  /**
   * Execute the axis operation on the provided {@code focus}.
   *
   * @param focus
   *          the node to operate on
   * @return the result of the axis operation
   */
  @NonNull
  public Stream<? extends INodeItem> execute(@NonNull INodeItem focus) {
    return ObjectUtils.notNull(action.apply(focus));
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return CollectionUtil.emptyList();
  }

  @Override
  public Class<INodeItem> getBaseResultType() {
    return INodeItem.class;
  }

  @Override
  public Class<INodeItem> getStaticResultType() {
    return getBaseResultType();
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitAxis(this, context);
  }

  @Override
  public ISequence<? extends INodeItem> accept(
      DynamicContext dynamicContext,
      ISequence<?> outerFocus) {
    ISequence<? extends INodeItem> retval;
    if (outerFocus.isEmpty()) {
      retval = ISequence.empty();
    } else {
      retval = ISequence.of(outerFocus.stream()
          .map(ItemUtils::checkItemIsNodeItemForStep)
          .flatMap(item -> {
            assert item != null;
            return execute(item);
          }).distinct());
    }
    return retval;
  }
}
