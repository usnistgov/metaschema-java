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

package gov.nist.secauto.metaschema.model.common;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A marker interface for Metaschema constructs that can be members of a Metaschema definition's
 * model that are named.
 */
public interface INamedModelElement extends IModelElement {
  /**
   * The formal display name.
   * 
   * @return the formal name
   */
  String getFormalName();

  /**
   * The resolved formal display name, which allows an instance to override a definition's name.
   * 
   * @return the formal name
   */
  String getEffectiveFormalName();

  /**
   * Get the text that describes the basic use of the element.
   * 
   * @return a line of markup text
   */
  MarkupLine getDescription();

  /**
   * Get the text that describes the basic use of the element, which allows an instance to override a
   * definition's description.
   * 
   * @return a line of markup text
   */
  MarkupLine getEffectiveDescription();

  /**
   * Get the mapping of property name to values for the model element.
   * 
   * @return the mapping
   */
  @NonNull
  Map<QName, Set<String>> getProperties();

  /**
   * Determine if a property is defined.
   * 
   * @param qname
   *          the qualified name of the property
   * @return {@code true} if the property is defined or {@code false} otherwise
   */
  default boolean hasProperty(@NonNull QName qname) {
    return getProperties().containsKey(qname);
  }

  /**
   * Get the values associated with a given property.
   * 
   * @param qname
   *          the qualified name of the property
   * @return the values or an empty set
   */
  @NonNull
  default Set<String> getPropertyValues(@NonNull QName qname) {
    Set<String> retval = getProperties().get(qname);
    if (retval == null) {
      retval = CollectionUtil.emptySet();
    }
    return retval;
  }

  /**
   * Determine if a given property, with a given {@code qname}, has the identified {@code value}.
   * 
   * @param qname
   *          the qualified name of the property
   * @param value
   *          the expected property value
   * @return {@code true} if the property value is defined or {@code false} otherwise
   */
  default boolean hasPropertyValue(@NonNull QName qname, @NonNull String value) {
    Set<String> values = getProperties().get(qname);
    return values != null && values.contains(value);
  }

  // @NonNull
  // default QName getXmlQName() {
  // String namespace = getXmlNamespace();
  //
  // @NonNull
  // QName retval;
  // if (namespace != null) {
  // retval = new QName(namespace, getEffectiveName());
  // } else {
  // retval = new QName(getEffectiveName());
  // }
  // return retval;
  // }

  /**
   * Get the name used for the associated property in JSON/YAML.
   * 
   * @return the JSON property name
   */
  @NonNull
  default String getJsonName() {
    return getEffectiveName();
  }

  /**
   * Get the name to use based on the provided names. This method will return the use name provided by
   * {@link #getUseName()} if the call is not {@code null}, and fall back to the name provided by
   * {@link #getName()} otherwise. This is the model name to use for the for an instance where the
   * instance is referenced.
   * 
   * @return the use name if available, or the name if not
   * 
   * @see #getUseName()
   * @see #getName()
   */
  @NonNull
  default String getEffectiveName() {
    @Nullable
    String useName = getUseName();
    return useName == null ? getName() : useName;
  }

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
  @Nullable
  String getUseName();
}
