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

package gov.nist.secauto.metaschema.model.common.validation;

import gov.nist.secauto.metaschema.model.common.IResourceLoader;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import javax.xml.transform.Source;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A common interface for Metaschema related content validators.
 */
public interface IContentValidator extends IResourceLoader {
  /**
   * Validate the resource at provided {@code path}.
   *
   * @param path
   *          the resource to validate
   * @return the result of the validation
   * @throws IOException
   *           if an error occurred while performing validation
   */
  @NonNull
  default IValidationResult validate(@NonNull Path path) throws IOException {
    InputSource source = toInputSource(ObjectUtils.notNull(path.toUri()));
    return validate(source);
  }

  /**
   * Validate the resource at provided {@code path}.
   *
   * @param url
   *          the resource to validate
   * @return the result of the validation
   * @throws IOException
   *           if an error occurred while performing validation
   * @throws URISyntaxException
   *           if there is a problem with the provided {@code url}
   */
  @NonNull
  default IValidationResult validate(@NonNull URL url) throws IOException, URISyntaxException {
    InputSource source = toInputSource(ObjectUtils.notNull(url.toURI()));
    return validate(source);
  }

  /**
   * Validate the resource associated with the provided input stream {@code is}.
   *
   * @param source
   *          information about how to access the resource
   * @return the result of the validation
   * @throws IOException
   *           if an error occurred while performing validation
   */
  @NonNull
  IValidationResult validate(@NonNull InputSource source) throws IOException;

  /**
   * Validate the target using the provided XML schemas.
   *
   * @param target
   *          the target to validate
   * @param schemaSources
   *          the XML schema sources to validate with
   * @return the validation result
   * @throws IOException
   *           if an error occurred while performing validation
   * @throws SAXException
   *           if an error occurred while parsing the XML target or schema
   */
  @NonNull
  static IValidationResult validateWithXmlSchema(@NonNull Path target, @NonNull List<Source> schemaSources)
      throws IOException, SAXException {
    return new XmlSchemaContentValidator(schemaSources).validate(target);
  }

  /**
   * Validate the target using the provided JSON schema.
   *
   * @param target
   *          the target to validate
   * @param schema
   *          the JSON schema to validate with
   * @return the validation result
   * @throws IOException
   *           if an error occurred while performing validation
   * @see JsonSchemaContentValidator#toJsonObject(InputStream)
   */
  @NonNull
  static IValidationResult validateWithJsonSchema(@NonNull Path target, @NonNull JSONObject schema)
      throws IOException {
    return new JsonSchemaContentValidator(schema).validate(target);
  }
}
