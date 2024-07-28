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
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides the means to aggregate multiple validation result sets into a single
 * result set.
 */
public final class AggregateValidationResult implements IValidationResult {
  @NonNull
  private final List<IValidationFinding> findings;
  @NonNull
  private final Level highestSeverity;

  private AggregateValidationResult(@NonNull List<IValidationFinding> findings, @NonNull Level highestSeverity) {
    this.findings = CollectionUtil.unmodifiableList(findings);
    this.highestSeverity = highestSeverity;
  }

  /**
   * Aggregate multiple provided results into a single result set.
   *
   * @param results
   *          the results to aggregate
   * @return the combined results
   */
  public static IValidationResult aggregate(@NonNull IValidationResult... results) {
    Stream<? extends IValidationFinding> stream = Stream.empty();
    for (IValidationResult result : results) {
      stream = Stream.concat(stream, result.getFindings().stream());
    }
    assert stream != null;
    return aggregate(stream);
  }

  private static IValidationResult aggregate(@NonNull Stream<? extends IValidationFinding> findingStream) {
    AtomicReference<Level> highestSeverity = new AtomicReference<>(Level.INFORMATIONAL);

    List<IValidationFinding> findings = new LinkedList<>();
    findingStream.sequential().forEachOrdered(finding -> {
      findings.add(finding);
      Level severity = finding.getSeverity();
      if (highestSeverity.get().ordinal() < severity.ordinal()) {
        highestSeverity.set(severity);
      }
    });

    return new AggregateValidationResult(findings, ObjectUtils.notNull(highestSeverity.get()));
  }

  @Override
  public Level getHighestSeverity() {
    return highestSeverity;
  }

  @Override
  public List<? extends IValidationFinding> getFindings() {
    return findings;
  }
}
