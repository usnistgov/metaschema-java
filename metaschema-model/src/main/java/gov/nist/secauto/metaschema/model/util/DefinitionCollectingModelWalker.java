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

package gov.nist.secauto.metaschema.model.util;

import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.definitions.InfoElementDefinition;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Supports walking a portion of a metaschema model collecting a set of definitions that match the
 * provided filter. For a definition to be collected, the filter must return {@code true}.
 */
public abstract class DefinitionCollectingModelWalker
    extends ModelWalker {

  private final Function<InfoElementDefinition, Boolean> filter;
  private final Set<InfoElementDefinition> definitions = new LinkedHashSet<>();

  /**
   * Construct a new walker using the provided filter.
   * 
   * @param filter
   *          the filter to match definitions against
   */
  protected DefinitionCollectingModelWalker(Function<InfoElementDefinition, Boolean> filter) {
    Objects.requireNonNull(filter, "filter");
    this.filter = filter;
  }

  /**
   * Retrieves the filter used for matching.
   * 
   * @return the filter
   */
  protected Function<InfoElementDefinition, Boolean> getFilter() {
    return filter;
  }

  /**
   * Return the collection of definitions matching the configured filter.
   * 
   * @return the collection of definitions
   */
  public Collection<InfoElementDefinition> getDefinitions() {
    return definitions;
  }

  @Override
  protected void visit(FlagDefinition def) {
    if (getFilter().apply(def)) {
      definitions.add(def);
    }
  }

  @Override
  protected boolean visit(FieldDefinition def) {
    if (definitions.contains(def)) {
      // no need to visit, since this has already been seen
      return false;
    } else {
      if (getFilter().apply(def)) {
        definitions.add(def);
      }
      return true;
    }
  }

  @Override
  protected boolean visit(AssemblyDefinition def) {
    if (definitions.contains(def)) {
      // no need to visit, since this has already been seen
      return false;
    } else {
      if (getFilter().apply(def)) {
        definitions.add(def);
      }
      return true;
    }
  }
}
