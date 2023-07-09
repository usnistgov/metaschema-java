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

import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Lexer;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An immutable expression that combines the evaluation of a sub-expression, with the evaluation of
 * a series of predicate expressions that filter the result of the evaluation.
 */
class Step implements IExpression { // NOPMD - intentional
  public enum Axis {
    SELF(metapath10Lexer.KW_SELF),
    PARENT(metapath10Lexer.KW_PARENT),
    ANCESTOR(metapath10Lexer.KW_ANCESTOR),
    ANCESTOR_OR_SELF(metapath10Lexer.KW_ANCESTOR_OR_SELF),
    CHILDREN(metapath10Lexer.KW_CHILD),
    DESCENDANT(metapath10Lexer.KW_DESCENDANT),
    DESCENDANT_OR_SELF(metapath10Lexer.KW_DESCENDANT_OR_SELF);

    private final int keywordIndex;

    Axis(int keywordIndex) {
      this.keywordIndex = keywordIndex;
    }

    public int getKeywordIndex() {
      return keywordIndex;
    }
  }

  @NonNull
  private final Axis axis;
  @NonNull
  private final IExpression stepExpression;
  @NonNull
  private final Class<? extends IItem> staticResultType;

  /**
   * Construct a new stepExpression expression.
   *
   * @param axis
   *          the axis to evaluate against
   * @param step
   *          the sub-expression to evaluate before filtering with the predicates
   */
  @SuppressWarnings("null")
  protected Step(@NonNull Axis axis, @NonNull IExpression step) {
    this.axis = axis;
    this.stepExpression = step;
    this.staticResultType = ExpressionUtils.analyzeStaticResultType(IItem.class, List.of(step));
  }

  @NonNull
  public Axis getAxis() {
    return axis;
  }

  /**
   * Get the stepExpression's sub-expression.
   *
   * @return the sub-expression
   */
  @NonNull
  public IExpression getStep() {
    return stepExpression;
  }

  @Override
  public Class<? extends IItem> getStaticResultType() {
    return staticResultType;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return CollectionUtil.singletonList(getStep());
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitStep(this, context);
  }

  @Override
  public ISequence<?> accept(DynamicContext dynamicContext, INodeContext context) {
    Stream<? extends INodeItem> items;
    switch (getAxis()) {
    case SELF:
      items = Stream.of(context.getNodeItem());
      break;
    case ANCESTOR:
      items = context.getNodeItem().ancestor();
      break;
    case ANCESTOR_OR_SELF:
      items = context.getNodeItem().ancestorOrSelf();
      break;
    case CHILDREN:
      items = context.getNodeItem().modelItems();
      break;
    case DESCENDANT:
      items = context.getNodeItem().descendant();
      break;
    case DESCENDANT_OR_SELF:
      items = context.getNodeItem().descendantOrSelf();
      break;
    case PARENT:
      items = Stream.ofNullable(context.getNodeItem().getParentNodeItem());
      break;
    default:
      throw new UnsupportedOperationException(getAxis().name());
    }

    IExpression step = getStep();

    return ISequence.of(ObjectUtils.notNull(
        items.flatMap(item -> {
          assert item != null;
          ISequence<?> result = step.accept(dynamicContext, item);
          return result.asStream();
        })));
  }

  @SuppressWarnings("null")
  @Override
  public String toASTString() {
    return String.format("%s[axis=%s]", getClass().getName(), getAxis().name());
  }
}
