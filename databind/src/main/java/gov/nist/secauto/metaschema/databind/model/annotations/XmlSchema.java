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

package gov.nist.secauto.metaschema.databind.model.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation provides package-level Module information.
 */
@Retention(RUNTIME)
@Target(PACKAGE)
public @interface XmlSchema {
  /**
   * The default value of a schema location, which indicates that no schema will
   * be associated.
   * <p>
   * The value "##none" was chosen because ## is not a valid sequence in
   * xs:anyURI.
   */
  String NO_LOCATION = ModelUtil.NO_STRING_VALUE;

  /**
   * Defines the XML namespace URI and prefix to use for this model. If a prefix
   * is not provided, the XML prefix will be auto-generated.
   *
   * @return an array of namespace definitions
   */
  XmlNs[] xmlns() default {};

  /**
   * Name of the XML namespace.
   * <p>
   * If the value is "##none", then there is no prefix defined.
   *
   * @return a namespace string in the form of a URI
   */
  String namespace() default ModelUtil.NO_STRING_VALUE;

  /**
   * The location of the associated XML schema.
   *
   * @return a location string in the form of a URI
   */
  String xmlSchemaLocation() default NO_LOCATION;

  /**
   * The location of the associated JSON schema.
   *
   * @return a location string in the form of a URI
   */
  String jsonSchemaLocation() default NO_LOCATION;

  /**
   * Get the default XML element form.
   *
   * @return the XML element form
   */
  XmlNsForm xmlElementFormDefault() default XmlNsForm.UNSET;

  /**
   * Get the default XML attribute form.
   *
   * @return the XML attribute form
   */
  XmlNsForm xmlAttributeFormDefault() default XmlNsForm.UNSET;
}
