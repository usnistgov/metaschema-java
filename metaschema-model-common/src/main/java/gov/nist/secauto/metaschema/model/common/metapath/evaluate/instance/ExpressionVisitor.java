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

import gov.nist.secauto.metaschema.model.common.metapath.ast.Addition;
import gov.nist.secauto.metaschema.model.common.metapath.ast.And;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Comparison;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ContextItem;
import gov.nist.secauto.metaschema.model.common.metapath.ast.DecimalLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Division;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.FunctionCall;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerDivision;
import gov.nist.secauto.metaschema.model.common.metapath.ast.IntegerLiteral;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Metapath;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Mod;
import gov.nist.secauto.metaschema.model.common.metapath.ast.ModelInstance;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Multiplication;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
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
import gov.nist.secauto.metaschema.model.common.metapath.ast.Wildcard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ExpressionVisitor<RESULT, CONTEXT> {

  @Nullable
  RESULT visitAddition(@NotNull Addition expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitAnd(@NotNull And expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitStep(@NotNull Step expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitComparison(@NotNull Comparison expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitContextItem(@NotNull ContextItem expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitDecimalLiteral(@NotNull DecimalLiteral expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitDivision(@NotNull Division expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitFlag(@NotNull Flag expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitFunctionCall(@NotNull FunctionCall expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitIntegerDivision(@NotNull IntegerDivision expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitIntegerLiteral(@NotNull IntegerLiteral expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitMetapath(@NotNull Metapath expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitMod(@NotNull Mod expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitModelInstance(@NotNull ModelInstance modelInstance, @NotNull CONTEXT context);

  @Nullable
  RESULT visitMultiplication(@NotNull Multiplication expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitName(@NotNull Name expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitNegate(@NotNull Negate expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitOr(@NotNull Or expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitParenthesizedExpression(@NotNull ParenthesizedExpression expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitRelativeDoubleSlashPath(@NotNull RelativeDoubleSlashPath relativeDoubleSlashPath,
      @NotNull CONTEXT context);

  @Nullable
  RESULT visitRelativeSlashPath(@NotNull RelativeSlashPath relativeSlashPath, @NotNull CONTEXT context);

  @Nullable
  RESULT visitRootDoubleSlashPath(@NotNull RootDoubleSlashPath rootDoubleSlashPath, @NotNull CONTEXT context);

  @Nullable
  RESULT visitRootSlashOnlyPath(@NotNull RootSlashOnlyPath rootSlashOnlyPath, @NotNull CONTEXT context);

  @Nullable
  RESULT visitRootSlashPath(@NotNull RootSlashPath rootSlashPath, @NotNull CONTEXT context);

  @Nullable
  RESULT visitStringConcat(@NotNull StringConcat expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitStringLiteral(@NotNull StringLiteral expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitSubtraction(@NotNull Subtraction expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitUnion(@NotNull Union expr, @NotNull CONTEXT context);

  @Nullable
  RESULT visitWildcard(@NotNull Wildcard expr, @NotNull CONTEXT context);
}
