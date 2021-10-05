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

package gov.nist.secauto.metaschema.model.common.metapath.evaluate;

import gov.nist.secauto.metaschema.model.common.metapath.INodeContext;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Negate;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Or;
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
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

public interface IExpressionEvaluationVisitor {
  ISequence<?> visit(IExpression<?> expr, INodeContext context);
  
  ISequence<? extends IAnyAtomicItem> visitAddition(Addition expr, INodeContext context);

  ISequence<? extends IBooleanItem> visitAnd(And expr, INodeContext context);

  ISequence<? extends INodeItem> visitStep(Step expr, INodeContext context);

  ISequence<? extends IBooleanItem> visitComparison(Comparison expr, INodeContext context);

  ISequence<? extends INodeItem> visitContextItem(ContextItem expr, INodeContext context);

  ISequence<? extends IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, INodeContext context);

  ISequence<? extends IAnyAtomicItem> visitDivision(Division expr, INodeContext context);

  ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, INodeContext context);

  ISequence<?> visitFunctionCall(FunctionCall expr, INodeContext context);

  ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, INodeContext context);

  ISequence<? extends IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, INodeContext context);

  ISequence<?> visitMetapath(Metapath expr, INodeContext context);

  ISequence<? extends INumericItem> visitMod(Mod expr, INodeContext context);

  ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance modelInstance, INodeContext context);

  ISequence<? extends IAnyAtomicItem> visitMultiplication(Multiplication expr, INodeContext context);

  ISequence<? extends INumericItem> visitNegate(Negate expr, INodeContext context);

  ISequence<? extends IBooleanItem> visitOr(Or expr, INodeContext context);

  ISequence<?> visitParenthesizedExpression(ParenthesizedExpression expr, INodeContext context);

  ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath relativeDoubleSlashPath,
      INodeContext context);

  ISequence<? extends INodeItem> visitRelativeSlashPath(RelativeSlashPath relativeSlashPath, INodeContext context);

  ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath rootDoubleSlashPath, INodeContext context);

  ISequence<? extends INodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath rootSlashOnlyPath, INodeContext context);

  ISequence<? extends INodeItem> visitRootSlashPath(RootSlashPath rootSlashPath, INodeContext context);

  ISequence<? extends IStringItem> visitStringConcat(StringConcat expr, INodeContext context);

  ISequence<? extends IStringItem> visitStringLiteral(StringLiteral expr, INodeContext context);

  ISequence<? extends IAnyAtomicItem> visitSubtraction(Subtraction expr, INodeContext context);

  ISequence<?> visitUnion(Union expr, INodeContext context);
}
