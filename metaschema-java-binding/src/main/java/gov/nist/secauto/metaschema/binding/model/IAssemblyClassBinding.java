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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IAssemblyClassBinding extends IClassBinding, IBoundAssemblyDefinition {


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
}
