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

import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IExpressionEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface INodeContext {

  @NotNull
  INodeItem getNodeItem();

  /**
   * Searches the node graph for {@link INodeItem} instances that match the provided
   * {@link IExpression}. The resulting nodes are returned in document order.
   * 
   * @param expr
   *          the search expression
   * @param recurse
   *          if the search should recurse over the child model instances
   * @return a stream of matching flag node items
   */
  @NotNull
  Stream<? extends INodeItem> getMatchingChildInstances(@NotNull IExpressionEvaluationVisitor visitor,
      @NotNull IExpression<?> expr,
      boolean recurse);

  /**
   * Searches the child flags for {@link IFlagNodeItem} instances that match the provided {@link Flag}
   * expression. The resulting nodes are returned in document order.
   * 
   * @param flag
   *          the search expression
   * @return a stream of matching flag node items
   */
  @NotNull
  Stream<? extends IFlagNodeItem> getMatchingChildFlags(@NotNull Flag flag);

  /**
   * Searches the child model nodes for {@link IModelNodeItem} instances that match the provided
   * {@link ModelInstance} expression. The resulting nodes are returned in document order.
   * 
   * @param modelInstance
   *          the search expression
   * @return a stream of matching model node items
   */
  @NotNull
  Stream<? extends IModelNodeItem> getMatchingChildModelInstances(@NotNull ModelInstance modelInstance);

  // default IMetapathResult evaluateMetapath(MetapathExpression metapath) {
  // MetaschemaPathEvaluationVisitor visitor = new MetaschemaPathEvaluationVisitor();
  // // logger.info(String.format("Evaluating path '%s' as AST '%s'", metapath.getPath(),
  // // metapath.toString()));
  // return visitor.visit(metapath.getASTNode(), this);
  //
  // }
}