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

package gov.nist.secauto.metaschema.core.model.validation;

import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;

import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides data that is the result of a completed content validation.
 */
public interface IValidationResult {
  @NonNull
  IValidationResult PASSING_RESULT = new IValidationResult() {

    @Override
    public boolean isPassing() {
      return true;
    }

    @Override
    public Level getHighestSeverity() {
      return Level.INFORMATIONAL;
    }

    @SuppressWarnings("null")
    @Override
    public List<? extends IValidationFinding> getFindings() {
      return Collections.emptyList();
    }
  };

  /**
   * Determines if the result of validation was valid or not.
   *
   * @return {@code true} if the result was determined to be valid or
   *         {@code false} otherwise
   */
  default boolean isPassing() {
    return getHighestSeverity().ordinal() < Level.ERROR.ordinal();
  }

  /**
   * Get the highest finding severity level for the validation. The level
   * {@link Level#INFORMATIONAL} will be returned if no validation findings were
   * identified.
   *
   * @return the highest finding severity level
   */
  @NonNull
  Level getHighestSeverity();

  /**
   * Get the list of validation findings, which may be empty.
   *
   * @return the list
   */
  @NonNull
  List<? extends IValidationFinding> getFindings();
}
