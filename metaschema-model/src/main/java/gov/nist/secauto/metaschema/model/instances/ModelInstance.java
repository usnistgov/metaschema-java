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

package gov.nist.secauto.metaschema.model.instances;

import gov.nist.secauto.metaschema.model.ModelType;
import gov.nist.secauto.metaschema.model.definitions.AssemblyDefinition;

public interface ModelInstance extends InfoElementInstance<AssemblyDefinition> {
  /**
   * Provides the Metaschema model type.
   * 
   * @return the model type
   */
  ModelType getModelType();

  /**
   * Retrieve the Metaschema assembly definition on which the info element was declared.
   * 
   * @return the Metaschema assembly definition on which the info element was declared
   */
  AssemblyDefinition getContainingDefinition();

  /**
   * Get the minimum cardinality for this associated instance. This value must be less than or equal
   * to the maximum cardinality returned by {@link #getMaxOccurs()}.
   * 
   * @return {@code 0} or a positive integer value
   */
  int getMinOccurs();

  /**
   * Get the maximum cardinality for this associated instance. This value must be greater than or
   * equal to the minimum cardinality returned by {@link #getMinOccurs()}.
   * 
   * @return a positive integer value
   */
  int getMaxOccurs();

  /**
   * Get the name used for grouping. In JSON, this name will be the property name. In XML when
   * {@link #getXmlGroupAsBehavior()} = {@link XmlGroupAsBehavior#GROUPED}, this name will be the
   * element name wrapping a collection of elements.
   * 
   * @return the group-as name or {@code null} if no name is configured, such as when
   *         {@link #getMaxOccurs()} = 1.
   */
  String getGroupAsName();

  /**
   * Gets the configured JSON group-as strategy. A JSON group-as strategy is only required when
   * {@link #getMaxOccurs()} &gt; 1.
   * 
   * @return the JSON group-as strategy, or {@code null} if {@link #getMaxOccurs()} = 1
   */
  JsonGroupAsBehavior getJsonGroupAsBehavior();

  /**
   * Gets the configured XML group-as strategy. A XML group-as strategy is only required when
   * {@link #getMaxOccurs()} &gt; 1.
   * 
   * @return the JSON group-as strategy, or {@code null} if {@link #getMaxOccurs()} = 1
   */
  XmlGroupAsBehavior getXmlGroupAsBehavior();
}
