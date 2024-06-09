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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface INamed {

  /**
   * Retrieve the name of the model element.
   *
   * @return the name
   */
  @NonNull
  String getName();

  /**
   * Retrieve the name to use for the model element, instead of the name.
   *
   * @return the use name or {@code null} if no use name is defined
   */
  // from INamedModelElement
  @Nullable
  default String getUseName() {
    // no use-name by default
    return null;
  }

  /**
   * Get the name to use based on the provided names. This method will return the
   * use name provided by {@link #getUseName()} if the call is not {@code null},
   * and fall back to the name provided by {@link #getName()} otherwise. This is
   * the model name to use for the for an instance where the instance is
   * referenced.
   *
   * @return the use name if available, or the name if not
   *
   * @see #getUseName()
   * @see #getName()
   */
  // from INamedModelElement
  @NonNull
  default String getEffectiveName() {
    @Nullable String useName = getUseName();
    return useName == null ? getName() : useName;
  }

  /**
   * Retrieve the XML namespace for this instance.
   * <p>
   * Multiple calls to this method are expected to produce the same, deterministic
   * return value.
   *
   * @return the XML namespace or {@code null} if no namespace is defined
   */
  @Nullable
  default String getXmlNamespace() {
    return getXmlQName().getNamespaceURI();
  }

  /**
   * Get the unique XML qualified name for this model element.
   * <p>
   * The qualified name is considered to be unique relative to all sibling
   * elements. For a flag, this name will be unique among all flag instances on
   * the same field or assembly definition. For a field or assembly, this name
   * will be unique among all sibling field or assembly instances on the same
   * assembly definition.
   * <p>
   * Multiple calls to this method are expected to produce the same, deterministic
   * return value.
   * <p>
   * If {@link #getXmlNamespace()} is {@code null}, the the resulting QName will
   * have the namespace {@link XMLConstants#NULL_NS_URI}.
   * <p>
   * This implementation may be overridden by implementation that cache the QName
   * or provide for a more efficient method for QName creation.
   *
   * @return the XML qualified name, or {@code null} if there isn't one
   */
  // REFACTOR: rename to getQName
  @NonNull
  QName getXmlQName();

  /**
   * Retrieve the index value to use for binary naming.
   *
   * @return the name index or {@code null} if no name index is defined
   */
  // from INamedModelElement
  @Nullable
  default Integer getIndex() {
    // no index by default
    return null;
  }

  /**
   * Retrieve the index value to use for binary naming, instead of the name.
   *
   * @return the use name index or {@code null} if no use name index is defined
   */
  // from INamedModelElement
  @Nullable
  default Integer getUseIndex() {
    // no use-name index by default
    return null;
  }

  /**
   * Get the index value to use for binary naming based on the provided index
   * values.
   * <p>
   * This method will return the use index value provided by
   * {@link #getUseIndex()} if the call result is not {@code null}, and fall back
   * to the index value provided by {@link #getIndex()} otherwise.
   *
   * @return the index value if available, or {@code null} otherwise
   */
  // from INamedModelElement
  @Nullable
  default Integer getEffectiveIndex() {
    @Nullable Integer useIndex = getUseIndex();
    return useIndex == null ? getIndex() : useIndex;
  }
}
