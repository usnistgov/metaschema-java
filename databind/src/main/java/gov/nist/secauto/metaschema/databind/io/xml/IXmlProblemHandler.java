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

package gov.nist.secauto.metaschema.databind.io.xml;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.io.IProblemHandler;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;

import java.io.IOException;
import java.util.Collection;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IXmlProblemHandler extends IProblemHandler {
  /**
   * Callback used to handle an attribute that is unknown to the model being
   * parsed.
   *
   * @param parentDefinition
   *          the bound class currently describing the data being parsed
   * @param targetObject
   *          the Java object for the {@code parentDefinition}
   * @param attribute
   *          the unknown attribute
   * @param parsingContext
   *          the XML parsing context used for parsing
   * @return {@code true} if the attribute was handled by this method, or
   *         {@code false} otherwise
   * @throws IOException
   *           if an error occurred while handling the unrecognized data
   */
  default boolean handleUnknownAttribute(
      @NonNull IBoundDefinitionModelComplex parentDefinition,
      @NonNull IBoundObject targetObject,
      @NonNull Attribute attribute,
      @NonNull IXmlParsingContext parsingContext) throws IOException {
    return false;
  }

  /**
   * Callback used to handle an element that is unknown to the model being parsed.
   *
   * @param parentDefinition
   *          the bound assembly class on which the missing instances are found
   * @param targetObject
   *          the Java object for the {@code parentDefinition}
   * @param start
   *          the parsed XML start element
   * @param parsingContext
   *          the XML parsing context used for parsing
   * @return {@code true} if the element was handled by this method, or
   *         {@code false} otherwise
   * @throws IOException
   *           if an error occurred while handling the unrecognized data
   */
  default boolean handleUnknownElement(
      @NonNull IBoundDefinitionModelAssembly parentDefinition,
      @NonNull IBoundObject targetObject,
      @NonNull StartElement start,
      @NonNull IXmlParsingContext parsingContext) throws IOException {
    return false;
  }

  /**
   * A callback used to handle bound flag instances for which no data was found
   * when the content was parsed.
   * <p>
   * This can be used to supply default or prescribed values based on application
   * logic.
   *
   * @param parentDefinition
   *          the bound assembly class on which the missing instances are found
   * @param targetObject
   *          the Java object for the {@code parentDefinition}
   * @param unhandledInstances
   *          the set of instances that had no data to parse
   * @throws IOException
   *           if an error occurred while handling the missing instances
   */
  default void handleMissingFlagInstances(
      @NonNull IBoundDefinitionModelComplex parentDefinition,
      @NonNull IBoundObject targetObject,
      @NonNull Collection<IBoundInstanceFlag> unhandledInstances)
      throws IOException {
    handleMissingInstances(parentDefinition, targetObject, unhandledInstances);
  }

  /**
   * A callback used to handle bound model instances for which no data was found
   * when the content was parsed.
   * <p>
   * This can be used to supply default or prescribed values based on application
   * logic.
   *
   * @param parentDefinition
   *          the bound assembly class on which the missing instances are found
   * @param targetObject
   *          the Java object for the {@code parentDefinition}
   * @param unhandledInstances
   *          the set of instances that had no data to parse
   * @throws IOException
   *           if an error occurred while handling the missing instances
   */
  default void handleMissingModelInstances(
      @NonNull IBoundDefinitionModelAssembly parentDefinition,
      @NonNull IBoundObject targetObject,
      @NonNull Collection<? extends IBoundInstanceModel<?>> unhandledInstances)
      throws IOException {
    handleMissingInstances(parentDefinition, targetObject, unhandledInstances);
  }
}
