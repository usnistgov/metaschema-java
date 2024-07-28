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
import gov.nist.secauto.metaschema.databind.io.IParsingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModel;

import org.codehaus.stax2.XMLEventReader2;

import java.io.IOException;

import javax.xml.stream.XMLStreamConstants;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IXmlParsingContext extends IParsingContext<XMLEventReader2, IXmlProblemHandler> {

  /**
   * Parses XML into a bound object based on the provided {@code definition}.
   * <p>
   * Parses the {@link XMLStreamConstants#START_DOCUMENT}, any processing
   * instructions, and the element.
   *
   * @param <CLASS>
   *          the returned object type
   * @param definition
   *          the definition describing the element data to read
   * @return the parsed object
   * @throws IOException
   *           if an error occurred while parsing the input
   */
  <CLASS> CLASS read(@NonNull IBoundDefinitionModelComplex definition) throws IOException;

  /**
   * Read the data associated with the {@code instance} and apply it to the
   * provided {@code parentObject}.
   *
   * @param <T>
   *          the item Java type
   * @param instance
   *          the instance to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @param parseGrouping
   *          if {@code true} parse the instance's grouping element or
   *          {@code false} otherwise
   * @return {@code true} if the instance was parsed, or {@code false} if the data
   *         did not contain information for this instance
   * @throws IOException
   *           if an error occurred while parsing the input
   *
   */
  <T> boolean readItems(
      @NonNull IBoundInstanceModel<T> instance,
      @NonNull IBoundObject parentObject,
      boolean parseGrouping) throws IOException;
}
