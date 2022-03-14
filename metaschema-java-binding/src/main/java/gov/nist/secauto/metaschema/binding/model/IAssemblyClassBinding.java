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

package gov.nist.secauto.metaschema.binding.model;

import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonProblemHandler;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

public interface IAssemblyClassBinding extends IClassBinding, IBoundAssemblyDefinition {

  /**
   * Parses JSON into a bound object. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   * <p>
   * This method expects the parser's current token to be:
   * <ul>
   * <li>{@code null} indicating that the parser has not yet parsed a JSON node;</li>
   * <li>a {@link JsonToken#START_OBJECT} which represents the object wrapper containing the root
   * field,</li>
   * <li>a {@link JsonToken#FIELD_NAME} representing the root field to parse, or</li>
   * <li>a peer field to the root field that will be handled by the
   * {@link IJsonProblemHandler#handleUnknownRootProperty(IAssemblyClassBinding, String, IJsonParsingContext)}
   * method.</li>
   * </ul>
   * <p>
   * After parsing the current token will be:
   * <ul>
   * <li>the next token after the {@link JsonToken#END_OBJECT} corresponding to the initial
   * {@link JsonToken#START_OBJECT} parsed by this method;</li>
   * <li>the next token after the {@link JsonToken#END_OBJECT} for the root field's value; or</li>
   * <li>the next token after all fields and associated values have been parsed looking for the root
   * field. This next token will be the {@link JsonToken#END_OBJECT} for the object containing the
   * fields. In this case the method will throw an {@link IOException} indicating the root was not
   * found.</li>
   * </ul>
   * 
   * @param context
   *          the JSON parser
   * @return the bound object instance representing the JSON object
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  @NotNull
  Object readRoot(@NotNull IJsonParsingContext context) throws IOException;

  /**
   * Parses JSON into a bound object.
   * <p>
   * This method expects the parser's current token to be:
   * <ul>
   * <li>{@code null} indicating that the parser has not yet parsed a JSON node, or</li>
   * <li>a {@link JsonToken#START_OBJECT} which represents the object containing the data of this
   * assembly.</li>
   * </ul>
   * <p>
   * After parsing the current token will be the {@link JsonToken#END_OBJECT} corresponding to the
   * initial {@link JsonToken#START_OBJECT} parsed by this method.
   * 
   * @param context
   *          the JSON parser
   * @param instance
   *          the bound object to read data into
   * @return the bound object instance representing the JSON object
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  @NotNull
  Object readObject(@NotNull IJsonParsingContext context) throws IOException;

  /**
   * Parses XML into a bound object. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   * 
   * @param context
   *          the XML parser
   * @return the bound object instance representing the JSON object
   * @throws XMLStreamException
   *           if an error occurred while parsing into XML
   * @throws IOException
   *           if an error occurred while reading the input
   */
  // TODO: merge the XMLStreamException into IOException
  @NotNull
  Object readRoot(@NotNull IXmlParsingContext context) throws XMLStreamException, IOException;

  /**
   * Writes data in a bound object to JSON. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   * 
   * @param instance
   *          the bound object
   * @param context
   *          the JSON serializer
   * @throws IOException
   *           if an error occurred while reading the JSON
   */
  void writeRoot(@NotNull Object instance, @NotNull IJsonWritingContext context) throws IOException;

  /**
   * Writes data in a bound object to XML. This assembly must be a root assembly for which a call to
   * {@link IAssemblyClassBinding#isRoot()} will return {@code true}.
   * 
   * @param instance
   *          the bound object
   * @param context
   *          the XML serializer
   * @throws XMLStreamException
   *           if an error occurred while parsing into XML
   * @throws IOException
   *           if an error occurred while writing the output
   */
  // TODO: merge the XMLStreamException into IOException
  void writeRoot(@NotNull Object instance, @NotNull IXmlWritingContext context) throws XMLStreamException, IOException;
}
