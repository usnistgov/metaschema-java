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

import gov.nist.secauto.metaschema.core.metapath.IExpression;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IArgument {
  @NonNull
  String getName();

  @NonNull
  ISequenceType getSequenceType();

  boolean isSupported(IExpression expression);

  @NonNull
  String toSignature();

  @NonNull
  static Builder newBuilder() {
    return new Builder();
  }

  class Builder {
    private String name;
    private Class<? extends IItem> type = IItem.class;
    private Occurrence occurrence = Occurrence.ONE;

    public Builder() {
      // construct a new non-initialized builder
    }

    public Builder(@NonNull String name) {
      this.name = name;
    }

    @NonNull
    public Builder name(@NonNull String name) {
      Objects.requireNonNull(name, "name");
      if (name.isBlank()) {
        throw new IllegalArgumentException("the name must be non-blank");
      }
      this.name = name.trim();
      return this;
    }

    @NonNull
    public Builder type(@NonNull Class<? extends IItem> type) {
      Objects.requireNonNull(type, "type");
      this.type = type;
      return this;
    }

    @NonNull
    public Builder zeroOrOne() {
      return occurrence(Occurrence.ZERO_OR_ONE);
    }

    @NonNull
    public Builder one() {
      return occurrence(Occurrence.ONE);
    }

    @NonNull
    public Builder zeroOrMore() {
      return occurrence(Occurrence.ZERO_OR_MORE);
    }

    @NonNull
    public Builder oneOrMore() {
      return occurrence(Occurrence.ONE_OR_MORE);
    }

    @NonNull
    public Builder occurrence(@NonNull Occurrence occurrence) {
      Objects.requireNonNull(occurrence, "occurrence");
      this.occurrence = occurrence;
      return this;
    }

    @NonNull
    public IArgument build() {
      return new ArgumentImpl(
          ObjectUtils.requireNonNull(name, "the name must not be null"),
          new SequenceTypeImpl(type, occurrence));
    }

  }

}
