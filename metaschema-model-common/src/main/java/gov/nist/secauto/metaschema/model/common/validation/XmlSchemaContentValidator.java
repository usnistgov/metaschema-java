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

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class XmlSchemaContentValidator
    extends AbstractContentValidator {
  private final Schema schema;

  @SuppressWarnings("null")
  @NotNull
  private static Schema toSchema(@NotNull List<? extends Source> schemaSources) throws SAXException {
    SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    // schemafactory.setResourceResolver(new ClasspathResourceResolver());
    Schema retval;
    if (schemaSources.isEmpty()) {
      retval = schemafactory.newSchema();
    } else {
      retval = schemafactory.newSchema(schemaSources.toArray(new Source[0]));
    }
    return retval;
  }

  public XmlSchemaContentValidator(@NotNull List<? extends Source> schemaSources) throws SAXException {
    this(toSchema(ObjectUtils.requireNonNull(schemaSources, "schemaSources")));
  }

  protected XmlSchemaContentValidator(@NotNull Schema schema) {
    this.schema = ObjectUtils.requireNonNull(schema, "schema");
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  public IValidationResult validate(@NotNull InputStream is, @NotNull URI documentUri) throws IOException {
    return validate(new StreamSource(is, documentUri.toString()), documentUri);
  }

  @NotNull
  public IValidationResult validate(Source xmlSource, @NotNull URI documentUri) throws IOException {
    Validator validator = schema.newValidator();
    XmlValidationErrorHandler errorHandler = new XmlValidationErrorHandler(documentUri);
    validator.setErrorHandler(errorHandler);
    try {
      validator.validate(xmlSource);
    } catch (SAXException ex) {
      throw new IOException(String.format("Unexpected failure during validation of '%s'", documentUri), ex);
    }
    return errorHandler;
  }

  public static class XmlValidationFinding implements IValidationFinding {
    @NotNull
    private final URI documentUri;
    @NotNull
    private final SAXParseException exception;
    @NotNull
    private final Level severity;

    public XmlValidationFinding(@NotNull Level severity, @NotNull SAXParseException exception,
        @NotNull URI documentUri) {
      this.documentUri = ObjectUtils.requireNonNull(documentUri, "documentUri");
      this.exception = ObjectUtils.requireNonNull(exception, "exception");
      this.severity = ObjectUtils.requireNonNull(severity, "severity");
    }

    @Override
    public Level getSeverity() {
      return severity;
    }

    @SuppressWarnings("null")
    @Override
    public URI getDocumentUri() {
      String systemId = getCause().getSystemId();
      return systemId == null ? documentUri : URI.create(systemId);
    }

    @SuppressWarnings("null")
    @Override
    public String getMessage() {
      return getCause().getLocalizedMessage();
    }

    @NotNull
    @Override
    public SAXParseException getCause() {
      return exception;
    }
  }

  private static class XmlValidationErrorHandler implements ErrorHandler, IValidationResult {
    @NotNull
    private final URI documentUri;
    @NotNull
    private final List<XmlValidationFinding> findings = new LinkedList<>();
    @NotNull
    private Level highestSeverity = Level.INFORMATIONAL;

    public XmlValidationErrorHandler(@NotNull URI documentUri) {
      this.documentUri = ObjectUtils.requireNonNull(documentUri, "documentUri");
    }

    @NotNull
    public URI getDocumentUri() {
      return documentUri;
    }

    private void adjustHighestSeverity(@NotNull Level severity) {
      if (highestSeverity.ordinal() < severity.ordinal()) {
        highestSeverity = severity;
      }
    }

    @SuppressWarnings("null")
    @Override
    public void warning(SAXParseException ex) throws SAXException {
      findings.add(new XmlValidationFinding(Level.WARNING, ex, getDocumentUri()));
      adjustHighestSeverity(Level.WARNING);
    }

    @SuppressWarnings("null")
    @Override
    public void error(SAXParseException ex) throws SAXException {
      findings.add(new XmlValidationFinding(Level.ERROR, ex, getDocumentUri()));
      adjustHighestSeverity(Level.ERROR);
    }

    @SuppressWarnings("null")
    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
      findings.add(new XmlValidationFinding(Level.CRITICAL, ex, getDocumentUri()));
      adjustHighestSeverity(Level.CRITICAL);
    }

    @SuppressWarnings("null")
    @Override
    @NotNull
    public List<@NotNull XmlValidationFinding> getFindings() {
      return Collections.unmodifiableList(findings);
    }

    @Override
    public Level getHighestSeverity() {
      return highestSeverity;
    }
  }
}
