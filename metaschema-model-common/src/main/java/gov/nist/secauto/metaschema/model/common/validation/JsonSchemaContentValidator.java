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

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class JsonSchemaContentValidator implements IContentValidator {
  @NonNull
  private final Schema schema;

  public JsonSchemaContentValidator(@NonNull InputStream schemaInputStream) {
    this(new JSONObject(new JSONTokener(Objects.requireNonNull(schemaInputStream, "schemaInputStream"))));
  }

  @SuppressWarnings("null")
  public JsonSchemaContentValidator(@NonNull JSONObject jsonSchema) {
    this(SchemaLoader.load(Objects.requireNonNull(jsonSchema, "jsonSchema")));
  }

  protected JsonSchemaContentValidator(@NonNull Schema schema) {
    this.schema = ObjectUtils.requireNonNull(schema, "schema");
  }

  @NonNull
  public Schema getSchema() {
    return schema;
  }

  @Override
  public IValidationResult validate(@NonNull InputSource source) throws IOException {
    URI uri = ObjectUtils.notNull(URI.create(source.getSystemId()));

    JSONObject json;
    if (source.getCharacterStream() != null) {
      // attempt to use a provided character stream
      json = new JSONObject(new JSONTokener(source.getCharacterStream()));
    } else if (source.getByteStream() != null) {
      // attempt to use a provided byte stream stream
      json = new JSONObject(new JSONTokener(source.getByteStream()));
    } else {
      // fall back to a URL-based connection
      URL url = uri.toURL();
      try (InputStream is = url.openStream()) {
        json = new JSONObject(new JSONTokener(is));
      }
    }
    return validate(json, uri);
  }

  @SuppressWarnings("null")
  @NonNull
  public IValidationResult validate(@NonNull JSONObject json, @NonNull URI documentUri) {
    IValidationResult retval;
    try {
      schema.validate(json);
      retval = IValidationResult.PASSING_RESULT;
    } catch (ValidationException ex) {
      retval = new JsonValidationResult(
          handleValidationException(ex, documentUri)
              .collect(Collectors.toList()));
    }

    return retval;
  }

  @SuppressWarnings("null")
  @NonNull
  protected Stream<JsonValidationFinding> handleValidationException(@NonNull ValidationException ex,
      @NonNull URI documentUri) {
    JsonValidationFinding finding = new JsonValidationFinding(ex, documentUri);
    Stream<JsonValidationFinding> childFindings = ex.getCausingExceptions().stream()
        .flatMap(exception -> {
          return handleValidationException(exception, documentUri);
        });
    return Stream.concat(Stream.of(finding), childFindings);
  }

  public static class JsonValidationFinding implements IValidationFinding {
    @NonNull
    private final ValidationException exception;
    @NonNull
    private final URI documentUri;

    public JsonValidationFinding(@NonNull ValidationException exception, @NonNull URI documentUri) {
      this.exception = ObjectUtils.requireNonNull(exception, "exception");
      this.documentUri = ObjectUtils.requireNonNull(documentUri, "documentUri");
    }

    @Override
    public IConstraint.Level getSeverity() {
      return IConstraint.Level.ERROR;
    }

    @Override
    public URI getDocumentUri() {
      return documentUri;
    }

    @SuppressWarnings("null")
    @Override
    public String getMessage() {
      return getCause().getLocalizedMessage();
    }

    @NonNull
    @Override
    public ValidationException getCause() {
      return exception;
    }
  }

  private static class JsonValidationResult implements IValidationResult {
    @NonNull
    private final List<JsonValidationFinding> findings;

    @SuppressWarnings("null")
    public JsonValidationResult(@NonNull List<JsonValidationFinding> findings) {
      this.findings = Collections.unmodifiableList(Objects.requireNonNull(findings, "findings"));
    }

    @Override
    public IConstraint.Level getHighestSeverity() {
      return findings.isEmpty() ? IConstraint.Level.INFORMATIONAL : IConstraint.Level.ERROR;
    }

    @Override
    public List<? extends IValidationFinding> getFindings() {
      return findings;
    }

  }

}
