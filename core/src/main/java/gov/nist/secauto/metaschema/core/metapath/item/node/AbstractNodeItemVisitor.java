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

package gov.nist.secauto.metaschema.core.metapath.item.node;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used by implementations of this class to visit a sequence of node items in a
 * directed graph, using depth-first ordering.
 *
 * @param <CONTEXT>
 *          the type of data to pass to each visited node
 * @param <RESULT>
 *          the type of result produced by visitation
 */
public abstract class AbstractNodeItemVisitor<CONTEXT, RESULT> implements INodeItemVisitor<CONTEXT, RESULT> {
  /**
   * Visit the provided {@code item}.
   *
   * @param item
   *          the item to visit
   * @param context
   *          provides contextual information for use by the visitor
   * @return the result produced by visiting the item
   */
  public final RESULT visit(@NonNull INodeItemVisitable item, CONTEXT context) {
    return item.accept(this, context);
  }

  /**
   * Visit any child flags associated with the provided {@code item}.
   *
   * @param item
   *          the item to visit
   * @param context
   *          provides contextual information for use by the visitor
   * @return the result produced by visiting the item's flags
   */
  protected RESULT visitFlags(@NonNull INodeItem item, CONTEXT context) {
    RESULT result = defaultResult();
    for (IFlagNodeItem flag : item.getFlags()) {
      assert flag != null;
      if (!shouldVisitNextChild(item, flag, result, context)) {
        break;
      }

      RESULT childResult = flag.accept(this, context);
      result = aggregateResult(result, childResult, context);
    }
    return result;
  }

  /**
   * Visit any child model items associated with the provided {@code item}.
   *
   * @param item
   *          the item to visit
   * @param context
   *          provides contextual information for use by the visitor
   * @return the result produced by visiting the item's child model items
   */
  protected RESULT visitModelChildren(@NonNull INodeItem item, CONTEXT context) {
    RESULT result = defaultResult();

    for (List<? extends IModelNodeItem<?, ?>> childItems : item.getModelItems()) {
      for (IModelNodeItem<?, ?> childItem : childItems) {
        assert childItem != null;
        if (!shouldVisitNextChild(item, childItem, result, context)) {
          break;
        }

        RESULT childResult = childItem.accept(this, context);
        result = aggregateResult(result, childResult, context);
      }
    }
    return result;
  }

  /**
   * Determine if the child should be visited next, or skipped.
   *
   * @param parent
   *          the parent of the child to visit next
   * @param child
   *          the next child to visit
   * @param result
   *          the current visitation result
   * @param context
   *          provides contextual information for use by the visitor
   * @return {@code true} if the child should be visited, or {@code false} if the
   *         child should be skipped
   */
  protected boolean shouldVisitNextChild(
      @NonNull INodeItem parent,
      @NonNull INodeItem child,
      RESULT result,
      CONTEXT context) {
    // this is the default behavior, which can be overridden
    return true;
  }

  /**
   * Determine if the child should be visited next, or skipped.
   *
   * @param parent
   *          the parent of the child to visit next
   * @param child
   *          the next child to visit
   * @param result
   *          the current visitation result
   * @param context
   *          provides contextual information for use by the visitor
   * @return {@code true} if the child should be visited, or {@code false} if the
   *         child should be skipped
   */
  protected boolean shouldVisitNextChild(
      @NonNull INodeItem parent,
      @NonNull IModelNodeItem<?, ?> child,
      RESULT result,
      CONTEXT context) {
    // this is the default behavior, which can be overridden
    return true;
  }

  /**
   * The initial, default visitation result, which will be used as the basis for
   * aggregating results produced when visiting.
   *
   * @return the default result
   * @see #aggregateResult(Object, Object, Object)
   */
  protected abstract RESULT defaultResult();

  /**
   * Combine two results into a single, aggregate result.
   *
   * @param first
   *          the original result
   * @param second
   *          the new result to combine with the original result
   * @param context
   *          provides contextual information for use by the visitor
   * @return the combined result
   */
  protected RESULT aggregateResult(RESULT first, RESULT second, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return second;
  }

  @Override
  public RESULT visitDocument(IDocumentNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return visitModelChildren(item, context);
    // return visitAssembly(item.getRootAssemblyNodeItem(), context);
  }

  @Override
  public RESULT visitFlag(IFlagNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return defaultResult();
  }

  @Override
  public RESULT visitField(IFieldNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return visitFlags(item, context);
  }

  @Override
  public RESULT visitAssembly(IAssemblyNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context), context);
  }

  @Override
  public RESULT visitAssembly(IAssemblyInstanceGroupedNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context), context);
  }

  @Override
  public RESULT visitMetaschema(IModuleNodeItem item, CONTEXT context) {
    // this is the default behavior, which can be overridden
    return aggregateResult(visitFlags(item, context), visitModelChildren(item, context), context);
  }
}
