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
import gov.nist.secauto.metaschema.core.model.util.XmlEventUtil;
import gov.nist.secauto.metaschema.databind.io.AbstractProblemHandler;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

/**
 * Handles problems identified in the parsed XML.
 * <p>
 * The default problem handler will report unknown attributes, and provide empty
 * collections for multi-valued model items and default values for flags and
 * single valued fields.
 */
public class DefaultXmlProblemHandler
    extends AbstractProblemHandler
    implements IXmlProblemHandler {
  private static final Logger LOGGER = LogManager.getLogger(DefaultXmlProblemHandler.class);

  private static final QName XSI_SCHEMA_LOCATION
      = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
  private static final Set<QName> IGNORED_QNAMES;

  static {
    IGNORED_QNAMES = new HashSet<>();
    IGNORED_QNAMES.add(XSI_SCHEMA_LOCATION);
  }

  @Override
  public boolean handleUnknownAttribute(
      IBoundDefinitionModelComplex parentDefinition,
      IBoundObject targetObject,
      Attribute attribute,
      IXmlParsingContext parsingContext) {
    QName qname = attribute.getName();
    // check if warning is needed
    if (LOGGER.isWarnEnabled() && !IGNORED_QNAMES.contains(qname)) {
      LOGGER.atWarn().log("Skipping unrecognized attribute '{}'{}.",
          qname,
          XmlEventUtil.generateLocationMessage(attribute.getLocation()));
    }
    // always ignore
    return true;
  }
}
