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

package gov.nist.secauto.metaschema.core.model.constraint.impl;

import gov.nist.secauto.metaschema.core.model.constraint.ISource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Implements a
 * {@link gov.nist.secauto.metaschema.core.model.constraint.ISource.SourceType#EXTERNAL}
 * source with an associated resource.
 */
public final class ExternalSource implements ISource {
  @NonNull
  private static final Map<URI, ExternalSource> sources = new HashMap<>(); // NOPMD - intentional

  @NonNull
  private final URI modelUri;

  /**
   * Get a new instance of an external source associated with a resource
   * {@code location}.
   *
   * @param location
   *          the resource location containing a constraint
   * @return the source
   */
  @NonNull
  public static ISource instance(@NonNull URI location) {
    ISource retval;
    synchronized (sources) {
      retval = sources.get(location);
      if (retval == null) {
        retval = new ExternalModelSource(location);
      }
    }
    return retval;
  }

  private ExternalSource(@NonNull URI modelSource) {
    this.modelUri = modelSource;
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.EXTERNAL;
  }

  @NonNull
  @Override
  public URI getSource() {
    return modelUri;
  }
}
