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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance;

import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.AbstractExpressionVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ParenthesizedExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Step;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Union;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class MetaschemaInstanceEvaluationVisitor
    extends AbstractExpressionVisitor<IInstanceSet, IMetaschemaContext> {
  private boolean allowedRoot = false;
  private boolean filterUsingPredicates = false;

  protected boolean isallowedRoot() {
    return allowedRoot;
  }

  protected void setAllowedRoot(boolean allowedRoot) {
    this.allowedRoot = allowedRoot;
  }

  public IInstanceSet visit(IExpression<?> expr, IMetaschemaContext context) {
    return expr.accept(this, context);
  }

  protected boolean isFilterUsingPredicates() {
    return filterUsingPredicates;
  }

  protected void setFilterUsingPredicates(boolean filterUsingPredicates) {
    this.filterUsingPredicates = filterUsingPredicates;
  }

  @Override
  public IInstanceSet visitRootDoubleSlashPath(RootDoubleSlashPath expr, IMetaschemaContext context) {
    if (isallowedRoot()) {
      return context.search(this, expr.getNode(), context);
    } else {
      throw new UnsupportedOperationException("root searching is not supported");
    }
  }

  @Override
  public IInstanceSet visitRootSlashOnlyPath(RootSlashOnlyPath expr, IMetaschemaContext context) {
    if (isallowedRoot()) {
      return IInstanceSet.EMPTY_INSTANCE_SET;
    } else {
      throw new UnsupportedOperationException("root searching is not supported");
    }
  }

  @Override
  public IInstanceSet visitRootSlashPath(RootSlashPath expr, IMetaschemaContext context) {
    if (isallowedRoot()) {
      return expr.getNode().accept(this, context);
    } else {
      throw new UnsupportedOperationException("root searching is not supported");
    }
  }

  @Override
  public IInstanceSet visitRelativeSlashPath(RelativeSlashPath expr, IMetaschemaContext context) {
    IExpression<?> left = expr.getLeft();
    IInstanceSet leftResult = left.accept(this, context);

    IExpression<?> right = expr.getRight();
    return right.accept(this, context.newInstanceMetaschemaContext(leftResult));
  }

  @Override
  public IInstanceSet visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, IMetaschemaContext context) {
    IInstanceSet leftResult = expr.getLeft().accept(this, context);

    return context.search(this, expr.getRight(), context.newInstanceMetaschemaContext(leftResult));
  }

  @Override
  public IInstanceSet visitContextItem(ContextItem expr, IMetaschemaContext context) {
    return context.getInstanceSet();
  }

  @Override
  public IInstanceSet visitFlag(Flag expr, IMetaschemaContext context) {
    return context.getChildFlag(expr.getInstanceMatcher());
  }

  @Override
  public IInstanceSet visitModelInstance(ModelInstance expr, IMetaschemaContext context) {
    return context.getChildModelInstance(expr.getInstanceMatcher());
  }

  private IInstanceSet filter(IInstanceSet result, List<IExpression<?>> predicates) {
    IInstanceSet retval = result;
    if (!predicates.isEmpty()) {
      // TODO: implement
      throw new UnsupportedOperationException();
    }
    return retval;
  }

  @Override
  public IInstanceSet visitStep(Step expr, IMetaschemaContext context) {
    IInstanceSet retval = expr.getStep().accept(this, context);

    if (isFilterUsingPredicates()) {
      retval = filter(retval, expr.getPredicates());
    }
    return retval;
  }

  @Override
  public IInstanceSet visitParenthesizedExpression(ParenthesizedExpression expr, IMetaschemaContext context) {
    return expr.getChild().accept(this, context);
  }

  protected IInstanceSet buildUnion(List<? extends IExpression<?>> children, IMetaschemaContext context) {
    IInstanceSet retval;
    if (children.isEmpty()) {
      retval = IInstanceSet.EMPTY_INSTANCE_SET;
    } else if (children.size() == 1) {
      retval = children.iterator().next().accept(this, context);
    } else {
      LinkedHashSet<IInstance> result = new LinkedHashSet<>();
      for (IExpression<?> expression : children) {
        IInstanceSet instanceSet = expression.accept(this, context);
        result.addAll(instanceSet.getInstances());
      }

      if (result.isEmpty()) {
        retval = IInstanceSet.EMPTY_INSTANCE_SET;
      } else {
        retval = new DefaultInstanceSet(new ArrayList<>(result));
      }
    }
    return retval;
  }

  @Override
  public IInstanceSet visitMetapath(Metapath expr, IMetaschemaContext context) {
    return buildUnion(expr.getChildren(), context);
  }

  @Override
  public IInstanceSet visitUnion(Union expr, IMetaschemaContext context) {
    return buildUnion(expr.getChildren(), context);
  }

}
