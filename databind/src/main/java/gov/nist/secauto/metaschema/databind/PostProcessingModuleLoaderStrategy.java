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

package gov.nist.secauto.metaschema.databind;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IModuleLoader;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

class PostProcessingModuleLoaderStrategy
    extends SimpleModuleLoaderStrategy {
  @NonNull
  private final List<IModuleLoader.IModulePostProcessor> modulePostProcessors;
  private final Set<IModule> resolvedModules = new HashSet<>();

  protected PostProcessingModuleLoaderStrategy(
      @NonNull IBindingContext bindingContext,
      @NonNull List<IModuleLoader.IModulePostProcessor> modulePostProcessors) {
    super(bindingContext);
    this.modulePostProcessors = CollectionUtil.unmodifiableList(new ArrayList<>(modulePostProcessors));
  }

  @NonNull
  protected List<IModuleLoader.IModulePostProcessor> getModulePostProcessors() {
    return modulePostProcessors;
  }

  @Override
  public IBoundDefinitionModelComplex getBoundDefinitionForClass(@NonNull Class<? extends IBoundObject> clazz) {
    IBoundDefinitionModelComplex retval = super.getBoundDefinitionForClass(clazz);
    if (retval != null) {
      // force loading of metaschema information to apply constraints
      IModule module = retval.getContainingModule();
      synchronized (resolvedModules) {
        if (!resolvedModules.contains(module)) {
          // add first, to avoid loops
          resolvedModules.add(module);
          handleModule(module);
        }
      }
    }
    return retval;
  }

  private void handleModule(@NonNull IModule module) {
    for (IModuleLoader.IModulePostProcessor postProcessor : getModulePostProcessors()) {
      postProcessor.processModule(module);
    }
  }
}
