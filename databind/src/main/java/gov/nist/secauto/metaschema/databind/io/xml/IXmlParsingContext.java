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

import gov.nist.secauto.metaschema.core.model.IFlagContainer;
import gov.nist.secauto.metaschema.databind.io.IParsingContext;
import gov.nist.secauto.metaschema.databind.strategy.IClassBindingStrategy;
import gov.nist.secauto.metaschema.databind.strategy.impl.IModelInstanceBindingStrategy;

import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.XMLStreamReader2;

import java.io.IOException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IXmlParsingContext extends IParsingContext<XMLEventReader2, IXmlProblemHandler> {

  /**
   * Read the XML data associated with the {@code targetInstance} and apply it to
   * the provided {@code parentObject}.
   *
   * @param <T>
   *          the resulting object type
   * @param instanceStrategy
   *          the instance strategy to parse data for
   * @param parentObject
   *          the Java object that data parsed by this method will be stored in
   * @param start
   *          the XML element start and attribute data previously parsed
   * @return the Java object read, or {@code null} if no data was read
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   */
  @Nullable
  <T> T readModelInstanceValue(
      @NonNull IModelInstanceBindingStrategy<?> instanceStrategy,
      @NonNull Object parentObject,
      @NonNull StartElement start) throws XMLStreamException, IOException;

  /**
   * Reads a XML element storing the associated data in a Java class instance,
   * returning the resulting instance.
   * <p>
   * When called the next {@link XMLEvent} of the {@link XMLStreamReader2} is
   * expected to be a {@link XMLStreamConstants#START_ELEMENT} that is the XML
   * element associated with the Java class.
   * <p>
   * After returning the next {@link XMLEvent} of the {@link XMLStreamReader2} is
   * expected to be a the next event after the
   * {@link XMLStreamConstants#END_ELEMENT} for the XML
   * {@link XMLStreamConstants#START_ELEMENT} element associated with the Java
   * class.
   *
   * @param <T>
   *          the resulting object type
   * @param bindingStrategy
   *          the Module definition info that describes the syntax of the data to
   *          read
   * @param parentObject
   *          the Java object parent of the target object, which can be
   *          {@code null} if there is no parent
   * @param start
   *          the XML element start and attribute data previously parsed
   * @return the Java object containing the data parsed by this method
   * @throws IOException
   *           if an error occurred while parsing the input
   * @throws XMLStreamException
   *           if an error occurred while parsing XML events
   *
   */
  @NonNull
  <T> T readDefinitionValue(
      @NonNull IClassBindingStrategy<? extends IFlagContainer> bindingStrategy,
      @Nullable Object parentObject,
      @NonNull StartElement start) throws IOException, XMLStreamException;
}
