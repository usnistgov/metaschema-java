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

package gov.nist.secauto.metaschema.core.model;

import java.util.Collection;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Indicates that the Metaschema type that has a complex model that can contain
 * field and assembly instances.
 */
public interface IContainerModel extends IContainer {

  @Override
  default boolean hasChildren() {
    return !getModelInstances().isEmpty();
  }

  /**
   * Retrieve the Metaschema module definition containing this container.
   *
   * @return the containing Metaschema module definition
   */
  @NonNull
  IAssemblyDefinition getOwningDefinition();

  /**
   * Get all model instances within the container.
   *
   * @return an ordered collection of model instances
   */
  @NonNull
  Collection<? extends IModelInstance> getModelInstances();

  /**
   * Get all named model instances within the container.
   *
   * @return an ordered mapping of use name to model instance
   */
  @NonNull
  Collection<? extends INamedModelInstance> getNamedModelInstances();

  /**
   * Get the model instance contained within the model with the associated use
   * name.
   *
   * @param name
   *          the effective name of the model instance
   * @return the matching model instance, or {@code null} if no match was found
   * @see INamedModelInstance#getEffectiveName()
   */
  @Nullable
  INamedModelInstance getNamedModelInstanceByName(QName name);

  /**
   * Get all field instances within the container.
   *
   * @return a mapping of use name to field instance
   */
  @NonNull
  Collection<? extends IFieldInstance> getFieldInstances();

  /**
   * Get the field instance contained within the model with the associated use
   * name.
   *
   * @param name
   *          the use name of the field instance
   * @return the matching field instance, or {@code null} if no match was found
   * @see IFieldInstance#getUseName()
   */
  @Nullable
  IFieldInstance getFieldInstanceByName(QName name);

  /**
   * Get all assembly instances within the container.
   *
   * @return a mapping of use name to assembly instance
   */
  @NonNull
  Collection<? extends IAssemblyInstance> getAssemblyInstances();

  /**
   * Get the assembly instance contained within the model with the associated use
   * name.
   *
   * @param name
   *          the effective name of the assembly instance
   * @return the matching assembly instance, or {@code null} if no match was found
   * @see INamedModelInstance#getEffectiveName()
   */
  @Nullable
  IAssemblyInstance getAssemblyInstanceByName(QName name);
}
