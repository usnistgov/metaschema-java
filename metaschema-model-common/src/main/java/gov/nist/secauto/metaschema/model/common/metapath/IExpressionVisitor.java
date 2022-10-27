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

  RESULT visitAddition(@NonNull Addition expr, CONTEXT context);

  RESULT visitAnd(@NonNull And expr, CONTEXT context);

  RESULT visitExcept(@NonNull Except except, CONTEXT context);

  RESULT visitStep(@NonNull Step expr, CONTEXT context);

  RESULT visitValueComparison(@NonNull ValueComparison expr, CONTEXT context);

  RESULT visitGeneralComparison(@NonNull GeneralComparison generalComparison, CONTEXT context);

  RESULT visitContextItem(@NonNull ContextItem expr, CONTEXT context);

  RESULT visitDecimalLiteral(@NonNull DecimalLiteral expr, CONTEXT context);

  RESULT visitDivision(@NonNull Division expr, CONTEXT context);

  RESULT visitFlag(@NonNull Flag expr, CONTEXT context);

  RESULT visitFunctionCall(@NonNull FunctionCall expr, CONTEXT context);

  RESULT visitIntegerDivision(@NonNull IntegerDivision expr, CONTEXT context);

  RESULT visitIntegerLiteral(@NonNull IntegerLiteral expr, CONTEXT context);

  RESULT visitIntersect(@NonNull Intersect intersect, CONTEXT context);

  RESULT visitMetapath(@NonNull Metapath expr, CONTEXT context);

  RESULT visitModulo(@NonNull Modulo expr, CONTEXT context);

  RESULT visitModelInstance(@NonNull ModelInstance modelInstance, CONTEXT context);

  RESULT visitMultiplication(@NonNull Multiplication expr, CONTEXT context);

  RESULT visitName(@NonNull Name expr, CONTEXT context);

  RESULT visitNegate(@NonNull Negate expr, CONTEXT context);

  RESULT visitOr(@NonNull Or expr, CONTEXT context);

  RESULT visitParentItem(@NonNull ParentItem parentContextItem, CONTEXT context);

  RESULT visitPredicate(@NonNull Predicate predicate, CONTEXT context);

  RESULT visitRelativeDoubleSlashPath(@NonNull RelativeDoubleSlashPath relativeDoubleSlashPath,
      CONTEXT context);

  RESULT visitRelativeSlashPath(@NonNull RelativeSlashPath relativeSlashPath, CONTEXT context);

  RESULT visitRootDoubleSlashPath(@NonNull RootDoubleSlashPath rootDoubleSlashPath, CONTEXT context);

  RESULT visitRootSlashOnlyPath(@NonNull RootSlashOnlyPath rootSlashOnlyPath, CONTEXT context);

  RESULT visitRootSlashPath(@NonNull RootSlashPath rootSlashPath, CONTEXT context);

  RESULT visitStringConcat(@NonNull StringConcat expr, CONTEXT context);

  RESULT visitStringLiteral(@NonNull StringLiteral expr, CONTEXT context);

  RESULT visitSubtraction(@NonNull Subtraction expr, CONTEXT context);

  RESULT visitUnion(@NonNull Union expr, CONTEXT context);

  RESULT visitWildcard(@NonNull Wildcard expr, CONTEXT context);

  RESULT visitLet(@NonNull Let expr, CONTEXT context);

  RESULT visitVariableReference(@NonNull VariableReference expr, CONTEXT context);
}
