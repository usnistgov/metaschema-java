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

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.AbstractBoundMetaschema;
import gov.nist.secauto.metaschema.databind.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.MetaschemaField;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class AbstractModuleLoaderStrategy implements IModuleLoaderStrategy {
  @NonNull
  private final IBindingContext bindingContext;
  @NonNull
  private final Map<Class<?>, IModule> modulesByClass = new HashMap<>(); // NOPMD - intentional
  @NonNull
  private final Map<Class<?>, IClassBinding> classBindingsByClass = new HashMap<>(); // NOPMD - intentional

  protected AbstractModuleLoaderStrategy(@NonNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
  }

  @NonNull
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public IModule getModuleByClass(@NonNull Class<? extends IModule> clazz) {
    IModule retval;
    synchronized (this) {
      retval = modulesByClass.get(clazz);
      if (retval == null) {
        retval = AbstractBoundMetaschema.createInstance(clazz, getBindingContext());
        modulesByClass.put(clazz, retval);
      }
    }
    return ObjectUtils.notNull(retval);
  }

  @Override
  public IClassBinding getClassBinding(@NonNull Class<?> clazz) {
    IClassBinding retval;
    synchronized (this) {
      retval = classBindingsByClass.get(clazz);
      if (retval == null) {
        retval = newClassBinding(clazz);
        if (retval != null) {
          classBindingsByClass.put(clazz, retval);
        }
      }
    }
    return retval;
  }

  @Nullable
  protected IClassBinding newClassBinding(@NonNull Class<?> clazz) {
    IClassBinding retval = null;
    if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      retval = DefaultAssemblyClassBinding.createInstance(clazz, getBindingContext());
    } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
      retval = DefaultFieldClassBinding.createInstance(clazz, getBindingContext());
    }
    return retval;
  }

  @Override
  public Map<Class<?>, IClassBinding> getClassBindingsByClass() {
    synchronized (this) {
      return CollectionUtil.unmodifiableMap(classBindingsByClass);
    }
  }
}
