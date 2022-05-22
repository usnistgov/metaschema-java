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
import gov.nist.secauto.metaschema.model.common.metapath.antlr.metapath10Lexer;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * An immutable expression that combines the evaluation of a sub-expression, with the evaluation of
 * a series of predicate expressions that filter the result of the evaluation.
 */
public class Step implements IExpression {
  public enum Axis {
    SELF(metapath10Lexer.KW_SELF),
    PARENT(metapath10Lexer.KW_PARENT),
    ANCESTOR(metapath10Lexer.KW_ANCESTOR),
    ANCESTOR_OR_SELF(metapath10Lexer.KW_ANCESTOR_OR_SELF),
    CHILDREN(metapath10Lexer.KW_CHILD),
    DESCENDANT(metapath10Lexer.KW_DESCENDANT),
    DESCENDANT_OR_SELF(metapath10Lexer.KW_DESCENDANT_OR_SELF);

    private final int keywordIndex;

    private Axis(int keywordIndex) {
      this.keywordIndex = keywordIndex;
    }

    public int getKeywordIndex() {
      return keywordIndex;
    }
  }

  @NotNull
  private final Axis axis;
  @NotNull
  private final IExpression step;
  @NotNull
  private final Class<@NotNull ? extends IItem> staticResultType;

  /**
   * Construct a new step expression.
   * 
   * @param axis
   *          the axis to evaluate against
   * @param step
   *          the sub-expression to evaluate before filtering with the predicates
   */
  @SuppressWarnings("null")
  public Step(@NotNull Axis axis, @NotNull IExpression step) {
    this.axis = axis;
    this.step = step;
    this.staticResultType = ExpressionUtils.analyzeStaticResultType(IItem.class, List.of(step));
  }

  @NotNull
  public Axis getAxis() {
    return axis;
  }

  /**
   * Get the step's sub-expression.
   * 
   * @return the sub-expression
   */
  @NotNull
  public IExpression getStep() {
    return step;
  }


  @Override
  public Class<@NotNull ? extends IItem> getStaticResultType() {
    return staticResultType;
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends IExpression> getChildren() {
    return Collections.singletonList(getStep());
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitStep(this, context);
  }

  @SuppressWarnings("null")
  @Override
  public ISequence<?> accept(DynamicContext dynamicContext, INodeContext context) {
    Stream<@NotNull ? extends INodeItem> items;
    switch (getAxis()) {
    case SELF:
      items = Stream.of(context.getContextNodeItem());
      break;
    case ANCESTOR:
      items = context.getContextNodeItem().ancestor();
      break;
    case ANCESTOR_OR_SELF:
      items = context.getContextNodeItem().ancestorOrSelf();
      break;
    case CHILDREN:
      items = context.getContextNodeItem().children();
      break;
    case DESCENDANT:
      items = context.getContextNodeItem().descendant();
      break;
    case DESCENDANT_OR_SELF:
      items = context.getContextNodeItem().descendantOrSelf();
      break;
    case PARENT:
      items = Stream.ofNullable(context.getContextNodeItem().getParentNodeItem());
      break;
    default:
      throw new UnsupportedOperationException(getAxis().name());
    }

    IExpression step = getStep();

    return ISequence.of(items.flatMap(item -> {
      ISequence<?> result = step.accept(dynamicContext, item);
      return result.asStream();
    }));
  }
  

  @SuppressWarnings("null")
  @Override
  public String toASTString() {
    return String.format("%s[axis=%s]", getClass().getName(), getAxis().name());
  }
}
