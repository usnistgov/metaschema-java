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

import gov.nist.secauto.metaschema.model.common.datatype.adapter.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.INumericItem;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.IStringItem;
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
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

public interface IExpressionEvaluationVisitor {
  @NotNull
  ISequence<?> visit(@NotNull IExpression<?> expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IAnyAtomicItem> visitAddition(@NotNull Addition expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IBooleanItem> visitAnd(@NotNull And expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitStep(@NotNull Step expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IBooleanItem> visitComparison(@NotNull Comparison expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitContextItem(@NotNull ContextItem expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IDecimalItem> visitDecimalLiteral(@NotNull DecimalLiteral expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IAnyAtomicItem> visitDivision(@NotNull Division expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IFlagNodeItem> visitFlag(@NotNull Flag expr, @NotNull INodeContext context);

  @NotNull
  ISequence<?> visitFunctionCall(@NotNull FunctionCall expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IIntegerItem> visitIntegerDivision(@NotNull IntegerDivision expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IIntegerItem> visitIntegerLiteral(@NotNull IntegerLiteral expr, @NotNull INodeContext context);

  @NotNull
  ISequence<?> visitMetapath(@NotNull Metapath expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INumericItem> visitMod(@NotNull Mod expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IModelNodeItem> visitModelInstance(@NotNull ModelInstance modelInstance,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IAnyAtomicItem> visitMultiplication(@NotNull Multiplication expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INumericItem> visitNegate(@NotNull Negate expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IBooleanItem> visitOr(@NotNull Or expr, @NotNull INodeContext context);

  @NotNull
  ISequence<?> visitParenthesizedExpression(@NotNull ParenthesizedExpression expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(@NotNull RelativeDoubleSlashPath relativeDoubleSlashPath,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitRelativeSlashPath(@NotNull RelativeSlashPath relativeSlashPath,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitRootDoubleSlashPath(@NotNull RootDoubleSlashPath rootDoubleSlashPath,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitRootSlashOnlyPath(@NotNull RootSlashOnlyPath rootSlashOnlyPath,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends INodeItem> visitRootSlashPath(@NotNull RootSlashPath rootSlashPath,
      @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IStringItem> visitStringConcat(@NotNull StringConcat expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IStringItem> visitStringLiteral(@NotNull StringLiteral expr, @NotNull INodeContext context);

  @NotNull
  ISequence<? extends IAnyAtomicItem> visitSubtraction(@NotNull Subtraction expr, @NotNull INodeContext context);

  @NotNull
  ISequence<?> visitUnion(@NotNull Union expr, @NotNull INodeContext context);
}
