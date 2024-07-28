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

package gov.nist.secauto.metaschema.core.metapath.function;

import gov.nist.secauto.metaschema.core.metapath.item.IItem;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ISequenceType {
  @NonNull
  ISequenceType EMPTY = new ISequenceType() {
    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public Class<? extends IItem> getType() {
      return null;
    }

    @Override
    public Occurrence getOccurrence() {
      return Occurrence.ZERO;
    }

    @Override
    public String toSignature() {
      return "()";
    }
  };

  /**
   * Create new sequence type using the provide type and occurrence.
   *
   * @param type
   *          the sequence item type
   * @param occurrence
   *          the expected occurrence of the sequence
   * @return the new sequence type
   */
  @SuppressWarnings("PMD.ShortMethodName")
  @NonNull
  static ISequenceType of(@NonNull Class<? extends IItem> type, @NonNull Occurrence occurrence) {
    return Occurrence.ZERO.equals(occurrence)
        ? EMPTY
        : new SequenceTypeImpl(type, occurrence);
  }

  /**
   * Determine if the sequence is empty (if it holds any data) or not.
   *
   * @return {@code true} if the sequence is empty or {@code false} otherwise
   */
  boolean isEmpty();

  /**
   * Get the type of the sequence.
   *
   * @return the type of the sequence or {@code null} if the sequence is empty
   */
  Class<? extends IItem> getType();

  /**
   * Get the occurrence of the sequence.
   *
   * @return the occurrence of the sequence or {@code Occurrence#ZERO} if the
   *         sequence is empty
   */
  Occurrence getOccurrence();

  /**
   * Get the signature of the function as a string.
   *
   * @return the signature
   */
  @NonNull
  String toSignature();
}
