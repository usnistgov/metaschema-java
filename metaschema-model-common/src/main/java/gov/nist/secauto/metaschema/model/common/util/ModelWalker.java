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

package gov.nist.secauto.metaschema.model.common.util;

import gov.nist.secauto.metaschema.model.common.definition.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.definition.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.instance.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.instance.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.instance.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.instance.IModelInstance;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Walks a Metaschema model. The "visit" methods can be implemented by child classes to perform
 * processing on a visited node.
 * 
 * @param <DATA>
 *          state information that is carried through the walk
 */
public abstract class ModelWalker<DATA> {
  /**
   * Generate default state information.
   * 
   * @return the state information
   */
  protected abstract DATA getDefaultData();

  /**
   * Will visit the provided metaschema flag definition.
   * 
   * @param flag
   *          the metaschema flag definition to walk
   */
  public void walk(@NotNull IFlagDefinition flag) {
    walk(flag, getDefaultData());
  }

  /**
   * Will visit the provided metaschema flag definition.
   * 
   * @param flag
   *          the metaschema flag definition to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IFlagDefinition flag, DATA data) {
    visit(flag, data);
  }

  /**
   * Will visit the provided metaschema field definition, and then walk the associated flag instances.
   * 
   * @param field
   *          the metaschema field definition to walk
   */
  public void walk(@NotNull IFieldDefinition field) {
    walk(field, getDefaultData());
  }

  /**
   * Will visit the provided metaschema field definition, and then walk the associated flag instances.
   * 
   * @param field
   *          the metaschema field definition to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IFieldDefinition field, DATA data) {
    if (visit(field, data)) {
      walkFlagInstances(field.getFlagInstances(), data);
    }
  }

  /**
   * Will visit the provided metaschema assembly definition, and then walk the associated flag and
   * model instances.
   * 
   * @param assembly
   *          the metaschema assembly definition to walk
   */
  public void walk(@NotNull IAssemblyDefinition assembly) {
    walk(assembly, getDefaultData());
  }

  /**
   * Will visit the provided metaschema assembly definition, and then walk the associated flag and
   * model instances.
   * 
   * @param assembly
   *          the metaschema assembly definition to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IAssemblyDefinition assembly, DATA data) {
    if (visit(assembly, data)) {
      walkFlagInstances(assembly.getFlagInstances(), data);
      walkModelInstances(assembly.getModelInstances(), data);
    }
  }
  /**
   * Will walk the provided model definition.
   * 
   * @param definition
   *          the definition to walk
   */
  public void walkDefinition(@NotNull IDefinition definition) {
    walkDefinition(definition, getDefaultData());
  }

  /**
   * Will walk the provided model definition.
   * 
   * @param definition
   *          the definition to walk
   * @param data
   *          additional state information to operate on
   */
  public void walkDefinition(@NotNull IDefinition definition, DATA data) {
    if (definition instanceof IAssemblyDefinition) {
      walk((IAssemblyDefinition) definition, data);
    } else if (definition instanceof IFieldDefinition) {
      walk((IFieldDefinition) definition, data);
    } else if (definition instanceof IFlagDefinition) {
      walk((IFlagDefinition) definition, data);
    }
  }
  /**
   * Will visit the provided metaschema flag instance, and then walk the associated flag definition.
   * 
   * @param instance
   *          the metaschema flag instance to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IFlagInstance instance, DATA data) {
    if (visit(instance, data)) {
      walk(instance.getDefinition(), data);
    }
  }

  /**
   * Will visit the provided metaschema field instance, and then walk the associated field definition.
   * 
   * @param instance
   *          the metaschema field instance to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IFieldInstance instance, DATA data) {
    if (visit(instance, data)) {
      walk(instance.getDefinition(), data);
    }
  }

  /**
   * Will visit the provided metaschema assembly instance, and then walk the associated assembly
   * definition.
   * 
   * @param instance
   *          the metaschema assembly instance to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IAssemblyInstance instance, DATA data) {
    if (visit(instance, data)) {
      walk(instance.getDefinition(), data);
    }
  }

  /**
   * Will visit the provided metaschema choice instance, and then walk the choice's child model
   * instances.
   * 
   * @param instance
   *          the metaschema choice instance to walk
   * @param data
   *          additional state information to operate on
   */
  public void walk(@NotNull IChoiceInstance instance, DATA data) {
    if (visit(instance, data)) {
      walkModelInstances(instance.getModelInstances(), data);
    }
  }

  /**
   * Will walk each of the provided flag instances.
   * 
   * @param instances
   *          a collection of flag instances to visit
   * @param data
   *          additional state information to operate on
   */
  protected void walkFlagInstances(@NotNull Collection<@NotNull ? extends IFlagInstance> instances, DATA data) {
    for (IFlagInstance instance : instances) {
      walk(instance, data);
    }
  }

  /**
   * Will walk each of the provided model instances.
   * 
   * @param instances
   *          a collection of model instances to visit
   * @param data
   *          additional state information to operate on
   */
  protected void walkModelInstances(@NotNull Collection<@NotNull ? extends IModelInstance> instances, DATA data) {
    for (IModelInstance instance : instances) {
      walkModelInstance(instance, data);
    }
  }

  /**
   * Will walk the provided model instance.
   * 
   * @param instance
   *          the instance to walk
   * @param data
   *          additional state information to operate on
   */
  protected void walkModelInstance(@NotNull IModelInstance instance, DATA data) {
    if (instance instanceof IAssemblyInstance) {
      walk((IAssemblyInstance) instance, data);
    } else if (instance instanceof IFieldInstance) {
      walk((IFieldInstance) instance, data);
    } else if (instance instanceof IChoiceInstance) {
      walk((IChoiceInstance) instance, data);
    }
  }

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   * @param data
   *          additional state information to operate on
   */
  protected abstract void visit(@NotNull IFlagDefinition def, DATA data);

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IFieldDefinition def, DATA data) {
    return true;
  }

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IAssemblyDefinition def, DATA data) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IFlagInstance instance, DATA data) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IFieldInstance instance, DATA data) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IAssemblyInstance instance, DATA data) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @param data
   *          additional state information to operate on
   * @return {@code true} if the child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(@NotNull IChoiceInstance instance, DATA data) {
    return true;
  }
}
