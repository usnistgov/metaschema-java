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

package gov.nist.secauto.metaschema.binding.validation;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonSchemaContentValidator
    extends AbstractContentValidator {
  private final Schema schema;

  public JsonSchemaContentValidator(@NotNull InputStream schemaInputStream) {
    this(new JSONObject(new JSONTokener(Objects.requireNonNull(schemaInputStream, "schemaInputStream"))));
  }

  public JsonSchemaContentValidator(@NotNull JSONObject jsonSchema) {
    this(SchemaLoader.load(Objects.requireNonNull(jsonSchema, "jsonSchema")));
  }

  protected JsonSchemaContentValidator(@NotNull Schema schema) {
    this.schema = Objects.requireNonNull(schema, "schema");
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  protected IValidationResult validateInternal(@NotNull InputStream is, @NotNull URI uri) {
    JSONObject json = new JSONObject(new JSONTokener(is));
    return validate(json, uri);
  }

  @NotNull
  public IValidationResult validate(JSONObject json, URI documentUri) {
    IValidationResult retval;
    try {
      schema.validate(json);
      retval = IValidationResult.PASSING_RESULT;
    } catch (ValidationException ex) {
      Stream<JsonValidationFinding> findings = handleValidationException(ex, documentUri);
      retval = new JsonValidationResult(findings.collect(Collectors.toList()));
    }

    return retval;
  }

  protected Stream<JsonValidationFinding> handleValidationException(@NotNull ValidationException ex,
      @NotNull URI documentUri) {
    return Stream.concat(
        Stream.of(new JsonValidationFinding(ex, documentUri)),
        ex.getCausingExceptions().stream()
            .flatMap(exception -> handleValidationException(exception, documentUri)));
  }

  public static class JsonValidationFinding implements IValidationFinding {
    @NotNull
    private final ValidationException exception;
    @NotNull
    private final URI documentUri;

    public JsonValidationFinding(@NotNull ValidationException exception, @NotNull URI documentUri) {
      this.exception = Objects.requireNonNull(exception, "exception");
      this.documentUri = Objects.requireNonNull(documentUri, "documentUri");
    }

    @Override
    public IConstraint.Level getSeverity() {
      return IConstraint.Level.ERROR;
    }

    @Override
    public URI getDocumentUri() {
      return documentUri;
    }

    @Override
    public String getMessage() {
      return getCause().getLocalizedMessage();
    }

    @NotNull
    @Override
    public ValidationException getCause() {
      return exception;
    }

    @Override
    public <RESULT, CONTEXT> RESULT visit(@NotNull IValidationFindingVisitor<RESULT, CONTEXT> visitor,
        CONTEXT context) {
      return visitor.visit(this, context);
    }
  }

  private static class JsonValidationResult implements IValidationResult {
    @NotNull
    private final List<JsonValidationFinding> findings;

    public JsonValidationResult(@NotNull List<JsonValidationFinding> findings) {
      this.findings = Collections.unmodifiableList(Objects.requireNonNull(findings, "findings"));
    }

    @Override
    public boolean isPassing() {
      return !findings.isEmpty();
    }

    @Override
    public IConstraint.Level getHighestSeverity() {
      return isPassing() ? IConstraint.Level.INFORMATIONAL : IConstraint.Level.ERROR;
    }

    @Override
    public List<? extends IValidationFinding> getFindings() {
      return findings;
    }

  }

}
