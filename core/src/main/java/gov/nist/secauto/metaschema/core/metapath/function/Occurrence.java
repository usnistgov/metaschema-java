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

import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Identifies the occurrence of a sequence used a function argument or return
 * value.
 */
public enum Occurrence {
  /**
   * An empty sequence.
   */
  ZERO("", true, Occurrence::handleZero),
  /**
   * The occurrence indicator {@code "?"}.
   */
  ZERO_OR_ONE("?", true, Occurrence::handleZeroOrOne),
  /**
   * No occurrence indicator.
   */
  ONE("", false, Occurrence::handleOne),
  /**
   * The occurrence indicator {@code "*"}.
   */
  ZERO_OR_MORE("*", true, Occurrence::handleZeroOrMore),
  /**
   * The occurrence indicator {@code "+"}.
   */
  ONE_OR_MORE("+", false, Occurrence::handleOneOrMore);

  @NonNull
  private final String indicator;
  private final boolean optional;
  @NonNull
  private final ISequenceHandler sequenceHandler;

  Occurrence(@NonNull String indicator, boolean optional, @NonNull ISequenceHandler sequenceHandler) {
    Objects.requireNonNull(indicator, "indicator");
    this.indicator = indicator;
    this.optional = optional;
    this.sequenceHandler = sequenceHandler;
  }

  /**
   * Get the occurrence indicator to use in the signature string for the argument.
   *
   * @return the occurrence indicator
   */
  @NonNull
  public String getIndicator() {
    return indicator;
  }

  /**
   * Determine if providing a value is optional based on the occurrence.
   *
   * @return {@code true} if providing a value is optional or {@code false} if
   *         required
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * Get the handler used to check that a sequence meets the occurrence
   * requirement.
   *
   * @return the handler
   */
  @NonNull
  public ISequenceHandler getSequenceHandler() {
    return sequenceHandler;
  }

  @NonNull
  private static <T extends IItem> ISequence<T> handleZero(@NonNull ISequence<T> sequence) {
    int size = sequence.size();
    if (size != 0) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("an empty sequence expected, but size is '%d'", size));
    }
    return ISequence.empty();
  }

  @NonNull
  private static <T extends IItem> ISequence<T> handleOne(@NonNull ISequence<T> sequence) {
    int size = sequence.size();
    if (size != 1) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("a sequence of one expected, but size is '%d'", size));
    }

    T item = sequence.getFirstItem(true);
    return item == null ? ISequence.empty() : ISequence.of(item);
  }

  @NonNull
  private static <T extends IItem> ISequence<T> handleZeroOrOne(@NonNull ISequence<T> sequence) {
    int size = sequence.size();
    if (size > 1) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("a sequence of zero or one expected, but size is '%d'", size));
    }

    T item = sequence.getFirstItem(false);
    return item == null ? ISequence.empty() : ISequence.of(item);
  }

  @NonNull
  private static <T extends IItem> ISequence<T> handleZeroOrMore(@NonNull ISequence<T> sequence) {
    return sequence;
  }

  @NonNull
  private static <T extends IItem> ISequence<T> handleOneOrMore(@NonNull ISequence<T> sequence) {
    int size = sequence.size();
    if (size < 1) {
      throw new InvalidTypeMetapathException(
          null,
          String.format("a sequence of one or more expected, but size is '%d'", size));
    }
    return sequence;
  }

  @FunctionalInterface
  public interface ISequenceHandler {
    /**
     * Check the provided sequence matches the occurrence.
     * <p>
     * This method may return a new sequence that more efficiently addresses the
     * occurrence.
     *
     * @param <T>
     *          the sequence item Java type
     * @param sequence
     *          the sequence to check occurrence for
     * @return the sequence
     * @throws InvalidTypeMetapathException
     *           if the sequence doesn't match the required occurrence
     */
    @NonNull
    <T extends IItem> ISequence<T> handle(@NonNull ISequence<T> sequence);
  }
}
