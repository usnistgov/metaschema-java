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
import gov.nist.secauto.metaschema.model.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.instances.ModelInstance;

import java.util.Collection;
import java.util.List;

/**
 * Walks a metaschema model. The "visit" methods can be implemented by child classes to perform
 * processing on a visited node.
 */
public abstract class ModelWalker {

  /**
   * Will visit the provided metaschema field definition, and then walk the associated flag instances.
   * 
   * @param field
   *          the metaschema field definition to walk
   */
  public void walk(FieldDefinition field) {
    if (visit(field)) {
      walkFlagInstances(field.getFlagInstances().values());
    }
  }

  /**
   * Will visit the provided metaschema assembly definition, and then walk the associated flag and
   * model instances.
   * 
   * @param assembly
   *          the metaschema assembly definition to walk
   */
  public void walk(AssemblyDefinition assembly) {
    if (visit(assembly)) {
      walkFlagInstances(assembly.getFlagInstances().values());
      walkModelInstances(assembly.getModelInstances());
    }
  }

  /**
   * Will visit the provided metaschema flag instance, and then walk the associated flag definition.
   * 
   * @param instance
   *          the metaschema flag instance to walk
   */
  public void walk(FlagInstance<?> instance) {
    if (visit(instance)) {
      visit(instance.getDefinition());
    }
  }

  /**
   * Will visit the provided metaschema field instance, and then walk the associated field definition.
   * 
   * @param instance
   *          the metaschema field instance to walk
   */
  public void walk(FieldInstance<?> instance) {
    if (visit(instance)) {
      walk(instance.getDefinition());
    }
  }

  /**
   * Will visit the provided metaschema assembly instance, and then walk the associated assembly
   * definition.
   * 
   * @param instance
   *          the metaschema assembly instance to walk
   */
  public void walk(AssemblyInstance<?> instance) {
    if (visit(instance)) {
      walk(instance.getDefinition());
    }
  }

  /**
   * Will visit the provided metaschema choice instance, and then walk the choice's child model
   * instances.
   * 
   * @param instance
   *          the metaschema choice instance to walk
   */
  public void walk(ChoiceInstance instance) {
    if (visit(instance)) {
      walkModelInstances(instance.getModelInstances());
    }
  }

  /**
   * Will walk each of the provided flag instances.
   * 
   * @param instances
   *          a collection of flag instances to visit
   */
  protected void walkFlagInstances(Collection<? extends FlagInstance<?>> instances) {
    for (FlagInstance<?> instance : instances) {
      walk(instance);
    }
  }

  /**
   * Will walk each of the provided model instances.
   * 
   * @param instances
   *          a collection of model instances to visit
   */
  protected void walkModelInstances(List<ModelInstance> instances) {
    for (ModelInstance instance : instances) {
      walkModelInstance(instance);
    }
  }

  /**
   * Will walk the provided model instance.
   * 
   * @param instance
   *          the instance to walk
   */
  protected void walkModelInstance(ModelInstance instance) {
    if (instance instanceof AssemblyInstance) {
      walk((AssemblyInstance<?>) instance);
    } else if (instance instanceof FieldInstance) {
      walk((FieldInstance<?>) instance);
    } else if (instance instanceof ChoiceInstance) {
      walk((ChoiceInstance) instance);
    }
  }

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   */
  protected void visit(FlagDefinition def) {
  }

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   * @return {@code true} if child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(FieldDefinition def) {
    return true;
  }

  /**
   * Called when the provided definition is walked. This can be overridden by child classes to enable
   * processing of the visited definition.
   * 
   * @param def
   *          the definition that is visited
   * @return {@code true} if child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(AssemblyDefinition def) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(FlagInstance<?> instance) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(FieldInstance<?> instance) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @return {@code true} if the associated definition is to be walked, or {@code false} otherwise
   */
  protected boolean visit(AssemblyInstance<?> instance) {
    return true;
  }

  /**
   * Called when the provided instance is walked. This can be overridden by child classes to enable
   * processing of the visited instance.
   * 
   * @param instance
   *          the instance that is visited
   * @return {@code true} if the child instances are to be walked, or {@code false} otherwise
   */
  protected boolean visit(ChoiceInstance instance) {
    return true;
  }
}
