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

package gov.nist.secauto.metaschema.binding.metapath.xdm.type;

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ExpressionEvaluationVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.format.IFieldPathSegment;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FieldNodeItemImpl
    extends AbstractModelNodeItem<IFieldPathSegment>
    implements IFieldNodeItem {

  public FieldNodeItemImpl(Object value, IFieldPathSegment segment, IAssemblyNodeItem parent) {
    super(value, segment, parent);
  }

  @Override
  public Stream<? extends INodeItem> getChildInstances(ExpressionEvaluationVisitor<INodeContext> visitor,
      IExpression<?> expr, boolean recurse) {
    // check the current node
    @SuppressWarnings("unchecked")
    Stream<? extends INodeItem> retval = (Stream<? extends INodeItem>) expr.accept(visitor, this).asStream();

    {
      List<? extends INodeItem> list = retval.collect(Collectors.toList());
      for (INodeItem item : list) {
        System.out.println(String.format("field(current) item: %s = %s", item.getMetapath(), item.getValue()));
      }
      retval = list.stream();
    }
    //
    // // get matching flag instances
    // Collection<? extends IFlagInstance> flags =
    // getPathSegment().getDefinition().getFlagInstances().values();
    // Stream<IFlagNodeItem> flagStream = flags.stream().map(flagInstance -> {
    // return (FlagProperty) flagInstance;
    // }).flatMap(flagInstance -> {
    // return flagInstance.getNodeItemFromParentInstance(this);
    // }).flatMap(flag -> {
    // IMetapathResult result = expr.accept(visitor, flag);
    // return result.asSequence().asStream().map(item -> (IFlagNodeItem) item);
    // });
    //
    // return Stream.concat(flagStream, retval);
    return retval;
  }

}
