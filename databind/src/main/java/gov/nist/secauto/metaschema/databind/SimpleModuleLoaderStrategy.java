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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundModule;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.databind.model.impl.DefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.impl.DefinitionField;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class SimpleModuleLoaderStrategy implements IBindingContext.IModuleLoaderStrategy {
  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Map<Class<?>, IBoundModule> modulesByClass = new ConcurrentHashMap<>();
  @NonNull
  private final Map<Class<?>, IBoundDefinitionModelComplex> definitionsByClass = new ConcurrentHashMap<>();

  protected SimpleModuleLoaderStrategy(@NonNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
  }

  @NonNull
  private IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public IBoundModule loadModule(@NonNull Class<? extends IBoundModule> clazz) {
    IBoundModule retval;
    synchronized (this) {
      retval = modulesByClass.get(clazz);
      if (retval == null) {
        retval = AbstractBoundModule.createInstance(clazz, getBindingContext());
        modulesByClass.put(clazz, retval);
      }
    }
    return ObjectUtils.notNull(retval);
  }

  @Override
  public IBoundDefinitionModelComplex getBoundDefinitionForClass(@NonNull Class<?> clazz) {
    IBoundDefinitionModelComplex retval;
    synchronized (this) {
      retval = definitionsByClass.get(clazz);
      if (retval == null) {
        retval = newBoundDefinition(clazz);
        if (retval != null) {
          definitionsByClass.put(clazz, retval);
        }
      }
    }
    return retval;
  }

  @Nullable
  private IBoundDefinitionModelComplex newBoundDefinition(@NonNull Class<?> clazz) {
    IBoundDefinitionModelComplex retval = null;
    if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      retval = new DefinitionAssembly(clazz, getBindingContext());
    } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
      retval = new DefinitionField(clazz, getBindingContext());
    }
    return retval;
  }
}
