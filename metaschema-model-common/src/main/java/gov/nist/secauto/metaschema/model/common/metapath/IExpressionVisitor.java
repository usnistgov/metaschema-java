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

import org.jetbrains.annotations.NotNull;

/**
 * Used to support processing a Metapath expression based on the visitor pattern. Each type of
 * expression node in the Metapath abstract syntax tree (AST) is represented.
 *
 * @param <RESULT>
 *          the result of processing any node
 * @param <CONTEXT>
 *          additional state to pass between nodes visited
 */
public interface IExpressionVisitor<RESULT, CONTEXT> {

  RESULT visitAddition(@NotNull Addition expr, CONTEXT context);

  RESULT visitAnd(@NotNull And expr, CONTEXT context);

  RESULT visitExcept(@NotNull Except except, CONTEXT context);

  RESULT visitStep(@NotNull Step expr, CONTEXT context);

  RESULT visitValueComparison(@NotNull ValueComparison expr, CONTEXT context);

  RESULT visitGeneralComparison(@NotNull GeneralComparison generalComparison, CONTEXT context);

  RESULT visitContextItem(@NotNull ContextItem expr, CONTEXT context);

  RESULT visitDecimalLiteral(@NotNull DecimalLiteral expr, CONTEXT context);

  RESULT visitDivision(@NotNull Division expr, CONTEXT context);

  RESULT visitFlag(@NotNull Flag expr, CONTEXT context);

  RESULT visitFunctionCall(@NotNull FunctionCall expr, CONTEXT context);

  RESULT visitIntegerDivision(@NotNull IntegerDivision expr, CONTEXT context);

  RESULT visitIntegerLiteral(@NotNull IntegerLiteral expr, CONTEXT context);

  RESULT visitIntersect(@NotNull Intersect intersect, CONTEXT context);

  RESULT visitMetapath(@NotNull Metapath expr, CONTEXT context);

  RESULT visitModulo(@NotNull Modulo expr, CONTEXT context);

  RESULT visitModelInstance(@NotNull ModelInstance modelInstance, CONTEXT context);

  RESULT visitMultiplication(@NotNull Multiplication expr, CONTEXT context);

  RESULT visitName(@NotNull Name expr, CONTEXT context);

  RESULT visitNegate(@NotNull Negate expr, CONTEXT context);

  RESULT visitOr(@NotNull Or expr, CONTEXT context);
  
  RESULT visitParentItem(@NotNull ParentItem parentContextItem, CONTEXT context);

  RESULT visitPredicate(@NotNull Predicate predicate, CONTEXT context);

  RESULT visitRelativeDoubleSlashPath(@NotNull RelativeDoubleSlashPath relativeDoubleSlashPath,
      CONTEXT context);

  RESULT visitRelativeSlashPath(@NotNull RelativeSlashPath relativeSlashPath, CONTEXT context);

  RESULT visitRootDoubleSlashPath(@NotNull RootDoubleSlashPath rootDoubleSlashPath, CONTEXT context);

  RESULT visitRootSlashOnlyPath(@NotNull RootSlashOnlyPath rootSlashOnlyPath, CONTEXT context);

  RESULT visitRootSlashPath(@NotNull RootSlashPath rootSlashPath, CONTEXT context);

  RESULT visitStringConcat(@NotNull StringConcat expr, CONTEXT context);

  RESULT visitStringLiteral(@NotNull StringLiteral expr, CONTEXT context);

  RESULT visitSubtraction(@NotNull Subtraction expr, CONTEXT context);

  RESULT visitUnion(@NotNull Union expr, CONTEXT context);

  RESULT visitWildcard(@NotNull Wildcard expr, CONTEXT context);
}
