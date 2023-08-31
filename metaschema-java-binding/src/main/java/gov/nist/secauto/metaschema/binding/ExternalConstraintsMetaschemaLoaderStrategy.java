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

package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintSet;

import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

class ExternalConstraintsMetaschemaLoaderStrategy
    extends AbstractMetaschemaLoaderStrategy {
  @NonNull
  private final Set<IConstraintSet> externalConstraintSets;
  private final Set<IMetaschema> resolvedMetaschemas = new HashSet<>();

  protected ExternalConstraintsMetaschemaLoaderStrategy(
      @NonNull IBindingContext bindingContext,
      @NonNull Set<IConstraintSet> externalConstraintSets) {
    super(bindingContext);
    this.externalConstraintSets = externalConstraintSets;
  }

  @NonNull
  protected Set<IConstraintSet> getExternalConstraintSets() {
    return externalConstraintSets;
  }

  @Override
  public IClassBinding getClassBinding(@NonNull Class<?> clazz) {
    IClassBinding retval = super.getClassBinding(clazz);
    if (retval != null) {
      // force loading of metaschema information to apply constraints
      IMetaschema metaschema = retval.getContainingMetaschema();
      synchronized (resolvedMetaschemas) {
        if (!resolvedMetaschemas.contains(metaschema)) {
          // add first, to avoid loops
          resolvedMetaschemas.add(metaschema);
          try {
            IConstraintSet.applyConstraintSetToMetaschema(getExternalConstraintSets(), metaschema);
          } catch (MetaschemaException ex) {
            throw new IllegalStateException(ex);
          }
        }
      }
    }
    return retval;
  }
}