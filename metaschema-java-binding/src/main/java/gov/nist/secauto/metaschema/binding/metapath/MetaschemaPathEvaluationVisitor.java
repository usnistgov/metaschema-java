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

package gov.nist.secauto.metaschema.binding.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.ast.AbstractExpressionVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Addition;
import gov.nist.secauto.metaschema.model.common.metapath.ast.And;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Comparison;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.DecimalLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Division;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.FunctionCall;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerDivision;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Mod;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Multiplication;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Negate;
import gov.nist.secauto.metaschema.model.common.metapath.ast.OrNode;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ParenthesizedExpression;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RelativeSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootDoubleSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashOnlyPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.RootSlashPath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Step;
import gov.nist.secauto.metaschema.model.common.metapath.ast.StringConcat;
import gov.nist.secauto.metaschema.model.common.metapath.ast.StringLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Subtraction;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Union;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Wildcard;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.IContext;

public class MetaschemaPathEvaluationVisitor
    extends AbstractExpressionVisitor<IPathResult, IContext> {

  @Override
  public IBooleanResult visitAnd(And expr, IContext context) {
    // TODO Auto-generated method stub
    return BooleanResult.TRUE;
  }

  @Override
  public IBooleanResult visitOr(OrNode expr, IContext context) {
    // TODO Auto-generated method stub
    return BooleanResult.TRUE;
  }

  @Override
  public IBooleanResult visitNegate(Negate expr, IContext context) {
    // TODO Auto-generated method stub
    return BooleanResult.TRUE;
  }

  @Override
  public IBooleanResult visitComparison(Comparison expr, IContext context) {
    // TODO Auto-generated method stub
    return BooleanResult.TRUE;
  }

  @Override
  public IItemSet visitStep(Step expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitModelInstance(ModelInstance expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitRelativeDoubleSlashPath(RelativeDoubleSlashPath expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitRelativeSlashPath(RelativeSlashPath expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitRootDoubleSlashPath(RootDoubleSlashPath expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitRootSlashOnlyPath(RootSlashOnlyPath expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitRootSlashPath(RootSlashPath expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitContextItem(ContextItem expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public IItemSet visitFlag(Flag expr, IContext context) {
    // TODO Auto-generated method stub
    return IItemSet.EMPTY;
  }

  @Override
  public INumberResult visitAddition(Addition expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public INumberResult visitSubtraction(Subtraction expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public INumberResult visitMultiplication(Multiplication expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public INumberResult visitDivision(Division expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IIntegerResult visitIntegerDivision(IntegerDivision expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IIntegerResult visitMod(Mod expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IIntegerResult visitIntegerLiteral(IntegerLiteral expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IDecimalResult visitDecimalLiteral(DecimalLiteral expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStringResult visitName(Name expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStringResult visitStringConcat(StringConcat expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStringResult visitStringLiteral(StringLiteral expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IPathResult visitFunctionCall(FunctionCall expr, IContext context) {
    // TODO Auto-generated method stub
    return super.visitFunctionCall(expr, context);
  }

  @Override
  public IPathResult visitMetapath(Metapath expr, IContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IPathResult visitParenthesizedExpression(ParenthesizedExpression expr, IContext context) {
    // TODO Auto-generated method stub
    return super.visitParenthesizedExpression(expr, context);
  }

  @Override
  public IPathResult visitUnion(Union expr, IContext context) {
    // TODO Auto-generated method stub
    return super.visitUnion(expr, context);
  }

  @Override
  public IPathResult visitWildcard(Wildcard expr, IContext context) {
    // TODO Auto-generated method stub
    return super.visitWildcard(expr, context);
  }

}
