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

import gov.nist.secauto.metaschema.binding.model.AbstractBoundMetaschema;
import gov.nist.secauto.metaschema.binding.model.DefaultAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.DefaultFieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaAssembly;
import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaField;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

class AbstractMetaschemaLoaderStrategy implements IMetaschemaLoaderStrategy {
  @NotNull
  private final IBindingContext bindingContext;
  @NotNull
  private final Map<@NotNull Class<?>, IMetaschema> metaschemasByClass = new HashMap<>(); // NOPMD - intentional
  @NotNull
  private final Map<@NotNull Class<?>, IClassBinding> classBindingsByClass = new HashMap<>(); // NOPMD - intentional


  protected AbstractMetaschemaLoaderStrategy(@NotNull IBindingContext bindingContext) {
    this.bindingContext = bindingContext;
  }

  @NotNull
  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public IMetaschema getMetaschemaInstanceByClass(@NotNull Class<? extends AbstractBoundMetaschema> clazz) {
    IMetaschema retval;
    synchronized (this) {
      retval = metaschemasByClass.get(clazz);
      if (retval == null) {
        retval = newMetaschema(clazz);
        metaschemasByClass.put(clazz, retval);
      }
    }
    return ObjectUtils.notNull(retval);
  }

  @NotNull
  protected IMetaschema newMetaschema(@NotNull Class<? extends AbstractBoundMetaschema> clazz) {
    return AbstractBoundMetaschema.createInstance(clazz, getBindingContext());
  }
  
  @Override
  public IClassBinding getClassBinding(@NotNull Class<?> clazz) {
    IClassBinding retval;
    synchronized (this) {
      retval = classBindingsByClass.get(clazz);
      if (retval == null) {
        retval = newClassBinding(clazz);
        classBindingsByClass.put(clazz, retval);
      }
    }
    return ObjectUtils.notNull(retval);
  }

  @NotNull
  protected IClassBinding newClassBinding(@NotNull Class<?> clazz) {
    IClassBinding retval;
    if (clazz.isAnnotationPresent(MetaschemaAssembly.class)) {
      retval = DefaultAssemblyClassBinding.createInstance(clazz, getBindingContext());
    } else if (clazz.isAnnotationPresent(MetaschemaField.class)) {
      retval = DefaultFieldClassBinding.createInstance(clazz, getBindingContext());
    } else {
      throw new IllegalArgumentException(String.format(
          "Class '%s' does not represent a Metaschema definition"
              + " since it is missing a '%s' or '%s' annotation.",
          clazz.getName(), MetaschemaAssembly.class.getName(), MetaschemaField.class.getName()));
    }
    return retval;
  }
  
  @Override
  public Map<@NotNull Class<?>, IClassBinding> getClassBindingsByClass() {
    synchronized (this) {
      return CollectionUtil.unmodifiableMap(classBindingsByClass);
    }
  }
}
