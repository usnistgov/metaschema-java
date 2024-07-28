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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.model.constraint.impl.ExternalSource;
import gov.nist.secauto.metaschema.core.model.constraint.impl.InternalModelSource;
import gov.nist.secauto.metaschema.core.model.constraint.impl.UnknownInternalModelSource;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A descriptor that identifies where a given constraint was defined.
 */
public interface ISource {
  enum SourceType {
    /**
     * A constraint embedded in a model.
     */
    MODEL,
    /**
     * A constraint defined externally from a model.
     */
    EXTERNAL;
  }

  /**
   * Get the descriptor for a
   * {@link gov.nist.secauto.metaschema.core.model.constraint.ISource.SourceType#MODEL}
   * source with no associated resource.
   *
   * @return the source descriptor
   */
  @NonNull
  static ISource modelSource() {
    return UnknownInternalModelSource.instance();
  }

  /**
   * Get the descriptor for a
   * {@link gov.nist.secauto.metaschema.core.model.constraint.ISource.SourceType#MODEL}
   * source with as associated resource.
   *
   * @param location
   *          the resource the constraint was defined in
   *
   * @return the source descriptor
   */
  @NonNull
  static ISource modelSource(@Nullable URI location) {
    return location == null ? modelSource() : InternalModelSource.instance(location);
  }

  /**
   * Get the descriptor for a
   * {@link gov.nist.secauto.metaschema.core.model.constraint.ISource.SourceType#EXTERNAL}
   * source with as associated resource.
   *
   * @param location
   *          the resource the constraint was defined in
   *
   * @return the source descriptor
   */
  @NonNull
  static ISource externalSource(@NonNull URI location) {
    return ExternalSource.instance(location);
  }

  /**
   * Get the type of source.
   *
   * @return the type
   */
  @NonNull
  ISource.SourceType getSourceType();

  /**
   * Get the resource where the constraint was defined, if known.
   *
   * @return the resource or {@code null} if the resource is not known
   */
  @Nullable
  URI getSource();
}
