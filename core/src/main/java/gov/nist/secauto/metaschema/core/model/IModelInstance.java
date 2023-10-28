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

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * This marker interface is used to identify a field or assembly instance that
 * is a member of an assembly's model.
 */
public interface IModelInstance extends IInstance {
  @Override
  IModelContainer getParentContainer();

  /**
   * Retrieve the Metaschema assembly definition on which this instance is
   * declared.
   *
   * @return the parent Metaschema assembly definition
   */
  @Override
  IAssemblyDefinition getContainingDefinition();

  /**
   * Get the name used for the associated element wrapping a collection of
   * elements in XML. This value is required when {@link #getXmlGroupAsBehavior()}
   * = {@link XmlGroupAsBehavior#GROUPED}. This name will be the element name
   * wrapping a collection of elements.
   *
   * @return the groupAs QName or {@code null} if no name is configured, such as
   *         when {@link #getMaxOccurs()} = 1.
   */
  @Nullable
  default QName getXmlGroupAsQName() {
    QName retval = null;
    if (XmlGroupAsBehavior.GROUPED.equals(getXmlGroupAsBehavior())) {
      String namespace = getGroupAsXmlNamespace();
      if (namespace != null) {
        retval = new QName(namespace, getGroupAsName());
      } else {
        retval = new QName(getGroupAsName());
      }
    }
    return retval;
  }

  /**
   * Get the minimum cardinality for this associated instance. This value must be
   * less than or equal to the maximum cardinality returned by
   * {@link #getMaxOccurs()}.
   *
   * @return {@code 0} or a positive integer value
   */
  int getMinOccurs();

  /**
   * Get the maximum cardinality for this associated instance. This value must be
   * greater than or equal to the minimum cardinality returned by
   * {@link #getMinOccurs()}, or {@code -1} if unbounded.
   *
   * @return a positive integer value or {@code -1} if unbounded
   */
  int getMaxOccurs();

  /**
   * Get the name provided for grouping. An instance in Metaschema must have a
   * group name if the instance has a cardinality greater than {@code 1}.
   *
   * @return the group-as name or {@code null} if no name is configured, such as
   *         when {@link #getMaxOccurs()} = 1
   */
  @Nullable
  default String getGroupAsName() {
    // no group-as by default
    return null;
  }

  /**
   * Retrieve the XML namespace for this group.
   *
   * @return the XML namespace or {@code null} if no namespace is used
   */
  // REFACTOR: analyze implementations to move up?
  @Nullable
  default String getGroupAsXmlNamespace() {
    // no group-as by default
    return null;
  }

  /**
   * Gets the configured JSON group-as strategy. A JSON group-as strategy is only
   * required when {@link #getMaxOccurs()} &gt; 1.
   *
   * @return the JSON group-as strategy, or {@code JsonGroupAsBehavior#NONE} if
   *         {@link #getMaxOccurs()} = 1
   */
  @NonNull
  default JsonGroupAsBehavior getJsonGroupAsBehavior() {
    return JsonGroupAsBehavior.NONE;
  }

  /**
   * Gets the configured XML group-as strategy. A XML group-as strategy is only
   * required when {@link #getMaxOccurs()} &gt; 1.
   *
   * @return the JSON group-as strategy, or {@code XmlGroupAsBehavior#UNGROUPED}
   *         if {@link #getMaxOccurs()} = 1
   */
  @NonNull
  default XmlGroupAsBehavior getXmlGroupAsBehavior() {
    return XmlGroupAsBehavior.UNGROUPED;
  }
}
