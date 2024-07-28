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

package gov.nist.secauto.metaschema.core.model.validation;

import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

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

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Supports validating an XML resource using an XML schema.
 */
public class XmlSchemaContentValidator
    extends AbstractContentValidator {
  private final Schema schema;

  @SuppressWarnings("null")
  @NonNull
  private static Schema toSchema(@NonNull List<? extends Source> schemaSources) throws SAXException {
    SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    // schemafactory.setResourceResolver(new ClasspathResourceResolver());
    Schema retval;
    if (schemaSources.isEmpty()) {
      retval = schemafactory.newSchema();
    } else {
      retval = schemafactory.newSchema(schemaSources.toArray(new Source[0]));
    }

    // TODO verify source input streams are closed
    return retval;
  }

  /**
   * Construct a new XML schema validator using the provided XML schema sources.
   *
   * @param schemaSources
   *          the XML schemas to use for validation
   * @throws SAXException
   *           if an error occurred while parsing the provided XML schemas
   */
  public XmlSchemaContentValidator(@NonNull List<? extends Source> schemaSources) throws SAXException {
    this(toSchema(ObjectUtils.requireNonNull(schemaSources, "schemaSources")));
  }

  /**
   * Construct a new XML schema validator using the provided pre-parsed XML
   * schema(s).
   *
   * @param schema
   *          the pre-parsed XML schema(s) to use for validation
   */
  protected XmlSchemaContentValidator(@NonNull Schema schema) {
    this.schema = ObjectUtils.requireNonNull(schema, "schema");
  }

  private Schema getSchema() {
    return schema;
  }

  @Override
  public IValidationResult validate(InputStream is, URI documentUri) throws IOException {
    Source xmlSource = new StreamSource(is, documentUri.toASCIIString());

    Validator validator = getSchema().newValidator();
    XmlValidationErrorHandler errorHandler = new XmlValidationErrorHandler(documentUri);
    validator.setErrorHandler(errorHandler);
    try {
      validator.validate(xmlSource);
    } catch (SAXException ex) {
      throw new IOException(String.format("Unexpected failure during validation of '%s'", documentUri), ex);
    }
    return errorHandler;
  }

  /**
   * Records an identified individual validation result found during XML schema
   * validation.
   */
  public static class XmlValidationFinding implements IValidationFinding, IResourceLocation {
    @NonNull
    private final URI documentUri;
    @NonNull
    private final SAXParseException exception;
    @NonNull
    private final Level severity;

    /**
     * Construct a new XML schema validation finding, which represents an issue
     * identified during XML schema validation.
     *
     * @param severity
     *          the finding significance
     * @param exception
     *          the XML schema validation exception generated during schema
     *          validation representing the issue
     * @param resourceUri
     *          the resource the issue was found in
     */
    public XmlValidationFinding(
        @NonNull Level severity,
        @NonNull SAXParseException exception,
        @NonNull URI resourceUri) {
      this.severity = ObjectUtils.requireNonNull(severity, "severity");
      this.exception = ObjectUtils.requireNonNull(exception, "exception");
      this.documentUri = ObjectUtils.requireNonNull(resourceUri, "documentUri");
    }

    @Override
    public String getIdentifier() {
      // always null
      return null;
    }

    @Override
    public Kind getKind() {
      return Level.WARNING.equals(getSeverity()) ? Kind.PASS : Kind.FAIL;
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

    @Override
    public int getLine() {
      return getCause().getLineNumber();
    }

    @Override
    public int getColumn() {
      return getCause().getColumnNumber();
    }

    @Override
    public long getCharOffset() {
      // not known
      return -1;
    }

    @Override
    public long getByteOffset() {
      // not known
      return -1;
    }

    @Override
    public IResourceLocation getLocation() {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public String getPathKind() {
      // not known
      return null;
    }

    @Override
    public String getPath() {
      // not known
      return null;
    }

    @SuppressWarnings("null")
    @Override
    public String getMessage() {
      return getCause().getLocalizedMessage();
    }

    @NonNull
    @Override
    public SAXParseException getCause() {
      return exception;
    }
  }

  private static class XmlValidationErrorHandler implements ErrorHandler, IValidationResult {
    @NonNull
    private final URI documentUri;
    @NonNull
    private final List<XmlValidationFinding> findings = new LinkedList<>();
    @NonNull
    private Level highestSeverity = Level.INFORMATIONAL;

    public XmlValidationErrorHandler(@NonNull URI documentUri) {
      this.documentUri = ObjectUtils.requireNonNull(documentUri, "documentUri");
    }

    @NonNull
    public URI getDocumentUri() {
      return documentUri;
    }

    private void adjustHighestSeverity(@NonNull Level severity) {
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
    @NonNull
    public List<XmlValidationFinding> getFindings() {
      return Collections.unmodifiableList(findings);
    }

    @Override
    public Level getHighestSeverity() {
      return highestSeverity;
    }
  }
}
