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
import gov.nist.secauto.metaschema.core.metapath.cst.AbstractNamedInstanceExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpressionVisitor;
import gov.nist.secauto.metaschema.core.metapath.item.ItemUtils;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public class Flag // NOPMD - intentional name
    extends AbstractNamedInstanceExpression<IFlagNodeItem> {

  /**
   * Construct a new expression that finds any child {@link IFlagNodeItem} that
   * matches the provided {@code test}.
   *
   * @param test
   *          the test to use to match
   */
  public Flag(@NonNull IExpression test) {
    super(test);
  }

  @Override
  public Class<IFlagNodeItem> getBaseResultType() {
    return IFlagNodeItem.class;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFlag(this, context);
  }

  @Override
  public ISequence<? extends IFlagNodeItem> accept(
      DynamicContext dynamicContext,
      ISequence<?> focus) {
    return ISequence.of(ObjectUtils.notNull(focus.stream()
        .map(ItemUtils::checkItemIsNodeItemForStep)
        .flatMap(item -> {
          assert item != null;
          return match(item);
        })));
  }

  /**
   * Get a stream of matching child node items for the provided {@code context}.
   *
   * @param focusedItem
   *          the node item to match child items of
   * @return the stream of matching node items
   */
  @SuppressWarnings("null")
  @NonNull
  protected Stream<? extends IFlagNodeItem> match(@NonNull INodeItem focusedItem) {
    Stream<? extends IFlagNodeItem> retval;
    if (getTest() instanceof NameTest) {
      QName name = ((NameTest) getTest()).getName();

      IFlagNodeItem item = focusedItem.getFlagByName(name);
      retval = item == null ? Stream.empty() : Stream.of(item);
    } else {
      // wildcard
      retval = focusedItem.flags();
    }
    return retval;
  }
}
