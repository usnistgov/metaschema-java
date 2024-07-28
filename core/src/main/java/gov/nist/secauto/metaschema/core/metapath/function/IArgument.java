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
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents a single function argument signature.
 */
public interface IArgument {
  /**
   * Get the argument's name.
   *
   * @return the argument's name
   */
  @NonNull
  String getName();

  /**
   * Get information about the type of sequence supported by the argument.
   *
   * @return the sequence information
   */
  @NonNull
  ISequenceType getSequenceType();

  /**
   * Get the signature of the argument.
   *
   * @return the argument's signature
   */
  @NonNull
  String toSignature();

  /**
   * Get a new argument builder.
   *
   * @return the new argument builder
   */
  @NonNull
  static Builder builder() {
    return new Builder();
  }

  /**
   * Used to create an argument's signature using a builder pattern.
   */
  final class Builder {
    private String name;
    @NonNull
    private Class<? extends IItem> type = IItem.class;
    private Occurrence occurrence;

    private Builder() {
      // construct a new non-initialized builder
    }

    private Builder(@NonNull String name) {
      this.name = name;
    }

    /**
     * Define the name of the function argument.
     *
     * @param name
     *          the argument's name
     * @return this builder
     */
    @NonNull
    public Builder name(@NonNull String name) {
      if (Objects.requireNonNull(name, "name").isBlank()) {
        throw new IllegalArgumentException("the name must be non-blank");
      }
      this.name = name.trim();
      return this;
    }

    /**
     * Define the type of the function argument.
     * <p>
     * By default an argument has the type {@link IItem}.
     *
     * @param type
     *          the argument's type
     * @return this builder
     */
    @NonNull
    public Builder type(@NonNull Class<? extends IItem> type) {
      this.type = Objects.requireNonNull(type, "type");
      return this;
    }

    /**
     * Identifies the argument's cardinality as a single, optional item (zero or
     * one).
     *
     * @return this builder
     */
    @NonNull
    public Builder zeroOrOne() {
      return occurrence(Occurrence.ZERO_OR_ONE);
    }

    /**
     * Identifies the argument's cardinality as a single, required item (one).
     *
     * @return this builder
     */
    @NonNull
    public Builder one() {
      return occurrence(Occurrence.ONE);
    }

    /**
     * Identifies the argument's cardinality as an optional series of items (zero or
     * more).
     *
     * @return this builder
     */
    @NonNull
    public Builder zeroOrMore() {
      return occurrence(Occurrence.ZERO_OR_MORE);
    }

    /**
     * Identifies the argument's cardinality as a required series of items (one or
     * more).
     *
     * @return this builder
     */
    @NonNull
    public Builder oneOrMore() {
      return occurrence(Occurrence.ONE_OR_MORE);
    }

    @NonNull
    private Builder occurrence(@NonNull Occurrence occurrence) {
      Objects.requireNonNull(occurrence, "occurrence");
      this.occurrence = occurrence;
      return this;
    }

    /**
     * Builds the argument's signature.
     *
     * @return the argument's signature
     */
    @NonNull
    public IArgument build() {
      return new ArgumentImpl(
          ObjectUtils.requireNonNull(name, "the argument name must not be null"),
          ISequenceType.of(type, ObjectUtils.requireNonNull(occurrence, "occurrence")));
    }
  }
}
