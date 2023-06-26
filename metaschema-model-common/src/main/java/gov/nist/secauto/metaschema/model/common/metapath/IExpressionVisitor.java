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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Used to support processing a Metapath expression based on the visitor pattern. Each type of
 * expression node in the Metapath abstract syntax tree (AST) is represented.
 *
 * @param <RESULT>
 *          the result of processing any node
 * @param <CONTEXT>
 *          additional state to pass between nodes visited
 */
interface IExpressionVisitor<RESULT, CONTEXT> {

  @Nullable
  RESULT visitAddition(@NonNull Addition expr, @NonNull CONTEXT context);

  RESULT visitAnd(@NonNull And expr, @NonNull CONTEXT context);

  RESULT visitExcept(@NonNull Except except, @NonNull CONTEXT context);

  RESULT visitStep(@NonNull Step expr, @NonNull CONTEXT context);

  RESULT visitValueComparison(@NonNull ValueComparison expr, @NonNull CONTEXT context);

  RESULT visitGeneralComparison(@NonNull GeneralComparison generalComparison, @NonNull CONTEXT context);

  RESULT visitContextItem(@NonNull ContextItem expr, @NonNull CONTEXT context);

  RESULT visitDecimalLiteral(@NonNull DecimalLiteral expr, @NonNull CONTEXT context);

  RESULT visitDivision(@NonNull Division expr, @NonNull CONTEXT context);

  RESULT visitFlag(@NonNull Flag expr, @NonNull CONTEXT context);

  RESULT visitFunctionCall(@NonNull FunctionCall expr, @NonNull CONTEXT context);

  RESULT visitIntegerDivision(@NonNull IntegerDivision expr, @NonNull CONTEXT context);

  RESULT visitIntegerLiteral(@NonNull IntegerLiteral expr, @NonNull CONTEXT context);

  RESULT visitIntersect(@NonNull Intersect intersect, @NonNull CONTEXT context);

  RESULT visitMetapath(@NonNull Metapath expr, @NonNull CONTEXT context);

  RESULT visitModulo(@NonNull Modulo expr, @NonNull CONTEXT context);

  RESULT visitModelInstance(@NonNull ModelInstance modelInstance, @NonNull CONTEXT context);

  RESULT visitMultiplication(@NonNull Multiplication expr, @NonNull CONTEXT context);

  RESULT visitName(@NonNull Name expr, @NonNull CONTEXT context);

  RESULT visitNegate(@NonNull Negate expr, @NonNull CONTEXT context);

  RESULT visitOr(@NonNull Or expr, @NonNull CONTEXT context);

  RESULT visitParentItem(@NonNull ParentItem parentContextItem, @NonNull CONTEXT context);

  RESULT visitPredicate(@NonNull Predicate predicate, @NonNull CONTEXT context);

  RESULT visitRelativeDoubleSlashPath(@NonNull RelativeDoubleSlashPath relativeDoubleSlashPath,
      @NonNull CONTEXT context);

  RESULT visitRelativeSlashPath(@NonNull RelativeSlashPath relativeSlashPath, @NonNull CONTEXT context);

  RESULT visitRootDoubleSlashPath(@NonNull RootDoubleSlashPath rootDoubleSlashPath, @NonNull CONTEXT context);

  RESULT visitRootSlashOnlyPath(@NonNull RootSlashOnlyPath rootSlashOnlyPath, @NonNull CONTEXT context);

  RESULT visitRootSlashPath(@NonNull RootSlashPath rootSlashPath, @NonNull CONTEXT context);

  RESULT visitStringConcat(@NonNull StringConcat expr, @NonNull CONTEXT context);

  RESULT visitStringLiteral(@NonNull StringLiteral expr, @NonNull CONTEXT context);

  RESULT visitSubtraction(@NonNull Subtraction expr, @NonNull CONTEXT context);

  RESULT visitUnion(@NonNull Union expr, @NonNull CONTEXT context);

  RESULT visitWildcard(@NonNull Wildcard expr, @NonNull CONTEXT context);

  RESULT visitLet(@NonNull Let expr, @NonNull CONTEXT context);

  RESULT visitVariableReference(@NonNull VariableReference expr, @NonNull CONTEXT context);
}
