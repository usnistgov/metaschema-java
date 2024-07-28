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

import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Provides information about an individual finding that is the result of a
 * completed content validation.
 */
public interface IValidationFinding {
  /**
   * The finding type.
   */
  enum Kind {
    /**
     * The finding does not apply to the intended purpose of the validation.
     */
    NOT_APPLICABLE,
    /**
     * The finding represents a successful result.
     */
    PASS,
    /**
     * The finding represents an unsuccessful result.
     */
    FAIL,
    /**
     * The finding is providing information that does not indicate success or
     * failure.
     */
    INFORMATIONAL;
  }

  /**
   * Get the unique identifier for the finding.
   *
   * @return the identifier
   */
  @Nullable
  String getIdentifier();

  /**
   * Get the finding's severity.
   *
   * @return the severity
   */
  @NonNull
  IConstraint.Level getSeverity();

  /**
   * Get the finding type.
   *
   * @return the finding type
   */
  @NonNull
  Kind getKind();

  /**
   * Get the document's URI.
   *
   * @return the document's URI or {@code null} if it is not known
   */
  @Nullable
  URI getDocumentUri();

  /**
   * Get the location in the associated resource associated with the finding.
   *
   * @return the location or {@code null} if no location is known
   */
  @Nullable
  IResourceLocation getLocation();

  /**
   * Get the path expression type provided by the {@link #getPath()} method.
   *
   * @return the path type identifier or {@code null} if unknown
   */
  @Nullable
  String getPathKind();

  /**
   * A format specific path to the finding in the source document.
   *
   * @return the path or {@code null} if unknown
   */
  @Nullable
  String getPath();

  /**
   * Get the finding message.
   *
   * @return the message or {@code null} if there is no message
   */
  @Nullable
  String getMessage();

  /**
   * Get the exception associated with the finding.
   *
   * @return the {@link Throwable} or {@code null} if no thowable is associated
   *         with the finding
   */
  Throwable getCause();
}
