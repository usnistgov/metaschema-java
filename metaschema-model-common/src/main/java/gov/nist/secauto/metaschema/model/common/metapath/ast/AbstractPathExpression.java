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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public abstract class AbstractPathExpression<RESULT_TYPE extends IItem>
    extends AbstractExpression
    implements IPathExpression<RESULT_TYPE> {
  @Override
  public abstract Class<RESULT_TYPE> getBaseResultType();

  @Override
  public Class<? extends RESULT_TYPE> getStaticResultType() {
    return getBaseResultType();
  }

  @SuppressWarnings("null")
  @NotNull
  protected ISequence<?> evaluateInNodeContext(@NotNull IExpression expr, @NotNull DynamicContext dynamicContext,
      @NotNull ISequence<? extends INodeItem> contextItems) {
    ISequence<?> retval;
    if (contextItems.isEmpty()) {
      retval = ISequence.empty();
    } else {
      // evaluate the right path in the context of the left's children
      Stream<? extends IItem> result = contextItems.asStream()
          .flatMap(node -> expr.accept(dynamicContext, node).asStream());
      retval = ISequence.of(result);
    }
    return retval;
  }

  @NotNull
  protected Stream<? extends INodeItem> searchExpression(@NotNull IExpression expr,
      @NotNull DynamicContext dynamicContext, @NotNull INodeContext context) {

    // check the current node
    @SuppressWarnings("unchecked")
    Stream<? extends INodeItem> nodeMatches
        = (Stream<? extends INodeItem>) expr.accept(dynamicContext, context).asStream();

    // create a stream of flags and model elements to check
    Stream<? extends IFlagNodeItem> flags = context.flags();
    Stream<? extends INodeItem> modelItems = context.modelItems();

    @SuppressWarnings("null")
    Stream<? extends INodeItem> childMatches = Stream.concat(flags, modelItems)
        .flatMap(instance -> {
          return searchExpression(expr, dynamicContext, instance);
        });

    @SuppressWarnings("null")
    @NotNull
    Stream<? extends INodeItem> result = Stream.concat(nodeMatches, childMatches);
    return result;
  }

  @NotNull
  protected Stream<? extends INodeItem> search(@NotNull IExpression expr, @NotNull DynamicContext dynamicContext,
      @NotNull INodeContext context) {
    Stream<? extends INodeItem> retval;
    // if (expr instanceof Flag) {
    // // check instances as a flag
    // retval = searchFlags((Flag) expr, context);
    // } else if (expr instanceof ModelInstance) {
    // // check instances as a ModelInstance
    // retval = searchModelInstances((ModelInstance) expr, context);
    // } else {
    // recurse tree
    // searchExpression(expr, context);
    retval = searchExpression(expr, dynamicContext, context);
    ;
    // }
    return retval;
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
  // @NotNull
  // protected Stream<? extends IModelNodeItem> searchModelInstances(@NotNull ModelInstance
  // modelInstance,
  // @NotNull INodeContext context) {
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
  // @NotNull
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
  // @NotNull
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
