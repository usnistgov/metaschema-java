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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.ICycledAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.INodeItem;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

abstract class AbstractPathExpression<RESULT_TYPE extends IItem>
    extends AbstractExpression
    implements IPathExpression<RESULT_TYPE> {
  @Override
  public abstract Class<RESULT_TYPE> getBaseResultType();

  @Override
  public Class<? extends RESULT_TYPE> getStaticResultType() {
    return getBaseResultType();
  }

  @NonNull
  protected INodeItem checkContext(INodeContext context) {
    INodeItem contextItem = context.getNodeItem();
    if (contextItem == null) {
      throw new TypeMetapathException(TypeMetapathException.NOT_A_NODE_ITEM_FOR_STEP, "The context node item is null");
    }
    return contextItem;
  }

  /**
   * A callback used to evaluate the provided {@code expression} in the context of a set of context
   * items.
   *
   * @param expression
   *          the expression to evaluate
   * @param dynamicContext
   *          the evaluation context
   * @param contextItems
   *          the set of items to evaluate the expression against
   * @return the resulting set of items produced by evaluating each context item against the
   *         expression
   */
  @SuppressWarnings("null")
  @NonNull
  protected ISequence<?> evaluateInNodeContext(@NonNull IExpression expression, @NonNull DynamicContext dynamicContext,
      @NonNull ISequence<? extends INodeItem> contextItems) {
    ISequence<?> retval;
    if (contextItems.isEmpty()) {
      retval = ISequence.empty();
    } else {
      // evaluate the right path in the context of the left's children
      Stream<? extends IItem> result = contextItems.asStream()
          .flatMap(node -> expression.accept(dynamicContext, node).asStream());
      retval = ISequence.of(result);
    }
    return retval;
  }

  /**
   * Evaluate the {@code nodeContext} and its ancestors against the provided {@code expression},
   * keeping any matching nodes.
   *
   * @param expression
   *          the expression to evaluate
   * @param dynamicContext
   *          the evaluation context
   * @param nodeContext
   *          the current context node
   * @return the matching nodes
   */
  @NonNull
  protected Stream<? extends INodeItem> searchExpression(@NonNull IExpression expression,
      @NonNull DynamicContext dynamicContext, @NonNull INodeContext nodeContext) {

    // check the current node
    @SuppressWarnings("unchecked") Stream<? extends INodeItem> nodeMatches
        = (Stream<? extends INodeItem>) expression.accept(dynamicContext, nodeContext).asStream();

    Stream<? extends INodeItem> childMatches;
    if (nodeContext instanceof ICycledAssemblyNodeItem) {
      // hack to prevent stack overflow
      childMatches = Stream.empty();
    } else {
      // create a stream of flags and model elements to check
      Stream<? extends IFlagNodeItem> flags = nodeContext.flags();
      Stream<? extends INodeItem> modelItems = nodeContext.modelItems();
      childMatches = Stream.concat(flags, modelItems)
          .flatMap(instance -> {
            assert instance != null;
            return searchExpression(expression, dynamicContext, instance);
          });
    }

    @SuppressWarnings("null")
    @NonNull Stream<? extends INodeItem> result = Stream.concat(nodeMatches, childMatches);
    return result;
  }

  /**
   * Evaluate the {@code nodeContext} and its ancestors against the provided {@code expression},
   * keeping any matching nodes.
   *
   * @param expression
   *          the expression to evaluate
   * @param dynamicContext
   *          the evaluation context
   * @param nodeContext
   *          the current context node
   * @return the matching nodes
   */
  @NonNull
  protected Stream<? extends INodeItem> search(@NonNull IExpression expression, @NonNull DynamicContext dynamicContext,
      @NonNull INodeContext nodeContext) {
    // Stream<? extends INodeItem> retval;
    // if (expr instanceof Flag) {
    // // check instances as a flag
    // retval = searchFlags((Flag) expr, context);
    // } else if (expr instanceof ModelInstance) {
    // // check instances as a ModelInstance
    // retval = searchModelInstances((ModelInstance) expr, context);
    // } else {
    // recurse tree
    // searchExpression(expr, context);
    // retval = searchExpression(expr, dynamicContext, context);
    // }
    // return retval;
    return searchExpression(expression, dynamicContext, nodeContext);

  }

  // /**
  // * Recursively searches the node graph for {@link IRequiredValueModelNodeItem} instances that
  // match
  // * the provided {@link ModelInstance} expression. The resulting nodes are returned in document
  // * order.
  // *
  // * @param modelInstance
  // * the search expression
  // * @param context
  // * the current node context
  // * @return a stream of matching model node items
  // */
  // @NonNull
  // protected Stream<? extends IModelNodeItem> searchModelInstances(@NonNull ModelInstance
  // modelInstance,
  // @NonNull INodeContext context) {
  //
  // // check if the current node context matches the expression
  // Stream<? extends IModelNodeItem> nodeMatches = matchModelInstance(modelInstance, context);
  //
  // // next iterate over the child model instances, if the context item is an assembly
  // @SuppressWarnings("null")
  // Stream<? extends IModelNodeItem> childMatches
  // = context.modelItems().flatMap(modelItem -> {
  // // apply the search criteria to these node items
  // return searchModelInstances(modelInstance, modelItem);
  // });
  //
  // // combine the results
  // @SuppressWarnings("null")
  // @NonNull
  // Stream<? extends IModelNodeItem> retval = Stream.concat(nodeMatches, childMatches);
  // return retval;
  // }

  // /**
  // * Recursively searches the node graph for {@link IRequiredValueFlagNodeItem} instances that match
  // the provided
  // * {@link Flag} expression. The resulting nodes are returned in document order.
  // *
  // * @param expr
  // * the search expression
  // * @param context
  // * the current node context
  // * @return a stream of matching flag node items
  // */
  // @NonNull
  // private Stream<? extends IRequiredValueFlagNodeItem> searchFlags(Flag expr, INodeContext context)
  // {
  //
  // // check if any flags on the the current node context matches the expression
  // Stream<? extends IRequiredValueFlagNodeItem> retval = context.getMatchingChildFlags(expr);
  //
  // // next iterate over the child model instances, if the context item is an assembly
  // INodeItem contextItem = context.getContextNodeItem();
  //
  // if (contextItem instanceof IRequiredValueAssemblyNodeItem) {
  // IRequiredValueAssemblyNodeItem assemblyContextItem = (IRequiredValueAssemblyNodeItem)
  // contextItem;
  //
  // Stream<? extends IRequiredValueFlagNodeItem> childFlagInstances =
  // assemblyContextItem.modelItems().flatMap(modelItem -> {
  // // apply the search criteria to these node items
  // return searchFlags(expr, modelItem);
  // });
  // retval = Stream.concat(retval, childFlagInstances);
  // }
  // return retval;
  // return Stream.empty();
  // }
}
