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

import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IDecimalItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IIntegerItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.INumericItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IStringItem;

public interface ExpressionEvaluationVisitor<CONTEXT> {

  ISequence<? extends INumericItem> visitAddition(Addition expr, CONTEXT context);

  ISequence<? extends IBooleanItem> visitAnd(And expr, CONTEXT context);

  ISequence<? extends INodeItem> visitStep(Step expr, CONTEXT context);

  ISequence<? extends IBooleanItem> visitComparison(Comparison expr, CONTEXT context);

  ISequence<? extends INodeItem> visitContextItem(ContextItem expr, CONTEXT context);

  ISequence<? extends IDecimalItem> visitDecimalLiteral(DecimalLiteral expr, CONTEXT context);

  ISequence<? extends INumericItem> visitDivision(Division expr, CONTEXT context);

  ISequence<? extends IFlagNodeItem> visitFlag(Flag expr, CONTEXT context);

  ISequence<?> visitFunctionCall(FunctionCall expr, CONTEXT context);

  ISequence<? extends IIntegerItem> visitIntegerDivision(IntegerDivision expr, CONTEXT context);

  ISequence<? extends IIntegerItem> visitIntegerLiteral(IntegerLiteral expr, CONTEXT context);

  ISequence<?> visitMetapath(Metapath expr, CONTEXT context);

  ISequence<? extends INumericItem> visitMod(Mod expr, CONTEXT context);

  ISequence<? extends IModelNodeItem> visitModelInstance(ModelInstance modelInstance, CONTEXT context);

  ISequence<? extends INumericItem> visitMultiplication(Multiplication expr, CONTEXT context);

  ISequence<? extends INumericItem> visitNegate(Negate expr, CONTEXT context);

  ISequence<? extends IBooleanItem> visitOr(OrNode expr, CONTEXT context);

  ISequence<?> visitParenthesizedExpression(ParenthesizedExpression expr, CONTEXT context);

  ISequence<? extends INodeItem> visitRelativeDoubleSlashPath(RelativeDoubleSlashPath relativeDoubleSlashPath, CONTEXT context);

  ISequence<? extends INodeItem> visitRelativeSlashPath(RelativeSlashPath relativeSlashPath, CONTEXT context);

  ISequence<? extends INodeItem> visitRootDoubleSlashPath(RootDoubleSlashPath rootDoubleSlashPath, CONTEXT context);

  ISequence<? extends INodeItem> visitRootSlashOnlyPath(RootSlashOnlyPath rootSlashOnlyPath, CONTEXT context);

  ISequence<? extends INodeItem> visitRootSlashPath(RootSlashPath rootSlashPath, CONTEXT context);

  ISequence<? extends IStringItem> visitStringConcat(StringConcat expr, CONTEXT context);

  ISequence<? extends IStringItem> visitStringLiteral(StringLiteral expr, CONTEXT context);

  ISequence<? extends INumericItem> visitSubtraction(Subtraction expr, CONTEXT context);

  ISequence<?> visitUnion(Union expr, CONTEXT context);
}
