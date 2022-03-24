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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;

import org.jetbrains.annotations.NotNull;

/**
 * A common interface for all constraint definitions.
 */
public interface IConstraint {
  /**
   * The degree to which a constraint violation is significant.
   * <p>
   * These values are ordered from least significant to most significant.
   */
  enum Level {
    /**
     * A violation of the constraint represents a point of interest.
     */
    INFORMATIONAL,
    /**
     * A violation of the constraint represents a potential issue with the content.
     */
    WARNING,
    /**
     * A violation of the constraint represents a fault in the content. This may include issues around
     * compatibility, integrity, consistency, etc.
     */
    ERROR,
    /**
     * A violation of the constraint represents a serious fault in the content that will prevent typical
     * use of the content.
     */
    CRITICAL;
  }

  /**
   * The default level to use if no level is provided.
   */
  @NotNull
  static final Level DEFAULT_LEVEL = Level.ERROR;
  /**
   * The default target Metapath expression to use if no target is provided.
   */
  @NotNull
  static final MetapathExpression DEFAULT_TARGET = MetapathExpression.CONTEXT_NODE;
  /**
   * The default target Metapath expression to use if no target is provided.
   */
  @NotNull
  static final String DEFAULT_TARGET_METAPATH = ".";

  /**
   * Retrieve the unique identifier for the constraint.
   * 
   * @return the identifier or {@code null} if no identifier is defined
   */
  String getId();

  /**
   * The significance of a violation of this constraint.
   * 
   * @return the level
   */
  @NotNull
  Level getLevel();

  /**
   * Retrieve the Metapath expression to use to query the targets of the constraint.
   * 
   * @return a Metapath expression
   */
  @NotNull
  MetapathExpression getTarget();

  /**
   * Retrieve the remarks associated with the constraint.
   * 
   * @return the remarks or {@code null} if no remarks are defined
   */
  MarkupMultiline getRemarks();
}
