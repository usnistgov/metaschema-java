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

package gov.nist.secauto.metaschema.modules.sarif;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.model.constraint.ConstraintValidationFinding;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.core.model.validation.IValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator.XmlValidationFinding;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.IVersionInfo;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.core.util.UriUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.SerializationFeature;

import org.schemastore.json.sarif.x210.Artifact;
import org.schemastore.json.sarif.x210.ArtifactLocation;
import org.schemastore.json.sarif.x210.Location;
import org.schemastore.json.sarif.x210.LogicalLocation;
import org.schemastore.json.sarif.x210.Message;
import org.schemastore.json.sarif.x210.MultiformatMessageString;
import org.schemastore.json.sarif.x210.PhysicalLocation;
import org.schemastore.json.sarif.x210.Region;
import org.schemastore.json.sarif.x210.ReportingDescriptor;
import org.schemastore.json.sarif.x210.Result;
import org.schemastore.json.sarif.x210.Run;
import org.schemastore.json.sarif.x210.Sarif;
import org.schemastore.json.sarif.x210.Tool;
import org.schemastore.json.sarif.x210.ToolComponent;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class SarifValidationHandler {
  private enum Kind {
    NOT_APPLICABLE("notApplicable"),
    PASS("pass"),
    FAIL("fail"),
    REVIEW("review"),
    OPEN("open"),
    INFORMATIONAL("informational");

    @NonNull
    private final String label;

    Kind(@NonNull String label) {
      this.label = label;
    }

    @NonNull
    public String getLabel() {
      return label;
    }
  }

  private enum SeverityLevel {
    NONE("none"),
    NOTE("note"),
    WARNING("warning"),
    ERROR("error");

    @NonNull
    private final String label;

    SeverityLevel(@NonNull String label) {
      this.label = label;
    }

    @NonNull
    public String getLabel() {
      return label;
    }
  }

  @NonNull
  private final URI source;
  @Nullable
  private final IVersionInfo toolVersion;
  private final AtomicInteger artifactIndex = new AtomicInteger(-1);
  private final AtomicInteger ruleIndex = new AtomicInteger(-1);
  @NonNull
  private final Map<URI, ArtifactRecord> artifacts = new LinkedHashMap<>();
  @NonNull
  private final Map<IConstraint, RuleRecord> rules = new LinkedHashMap<>();
  @NonNull
  private final List<IResult> results = new LinkedList<>();

  public SarifValidationHandler(
      @NonNull URI source,
      @Nullable IVersionInfo toolVersion) {
    if (!source.isAbsolute()) {
      throw new IllegalArgumentException(String.format("The source URI '%s' is not absolute.", source.toASCIIString()));
    }

    this.source = source;
    this.toolVersion = toolVersion;
  }

  public URI getSource() {
    return source;
  }

  public IVersionInfo getToolVersion() {
    return toolVersion;
  }

  public void addFindings(@NonNull List<? extends IValidationFinding> findings) {
    for (IValidationFinding finding : findings) {
      assert finding != null;
      addFinding(finding);
    }
  }

  public void addFinding(@NonNull IValidationFinding finding) {
    if (finding instanceof JsonValidationFinding) {
      addJsonValidationFinding((JsonValidationFinding) finding);
    } else if (finding instanceof XmlValidationFinding) {
      addXmlValidationFinding((XmlValidationFinding) finding);
    } else if (finding instanceof ConstraintValidationFinding) {
      addConstraintValidationFinding((ConstraintValidationFinding) finding);
    } else {
      throw new IllegalStateException();
    }
  }

  public URI relativize(@NonNull URI output, @NonNull URI artifact) throws IOException {
    try {
      return UriUtils.relativize(output, artifact, true);
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }
  }

  private RuleRecord getRuleRecord(@NonNull IConstraint constraint) {
    RuleRecord retval = rules.get(constraint);
    if (retval == null) {
      retval = new RuleRecord(constraint);
      rules.put(constraint, retval);
    }
    return retval;
  }

  private ArtifactRecord getArtifactRecord(@NonNull URI artifactUri) {
    ArtifactRecord retval = artifacts.get(artifactUri);
    if (retval == null) {
      retval = new ArtifactRecord(artifactUri);
      artifacts.put(artifactUri, retval);
    }
    return retval;
  }

  private void addJsonValidationFinding(@NonNull JsonValidationFinding finding) {
    results.add(new SchemaResult(finding));
  }

  private void addXmlValidationFinding(@NonNull XmlValidationFinding finding) {
    results.add(new SchemaResult(finding));
  }

  private void addConstraintValidationFinding(@NonNull ConstraintValidationFinding finding) {
    results.add(new ConstraintResult(finding));
  }

  public void write(@NonNull Path outputFile) throws IOException {

    URI output = outputFile.toUri();

    Sarif sarif = new Sarif();
    sarif.setVersion("2.1.0");

    Run run = new Run();

    sarif.addRun(run);

    Artifact artifact = new Artifact();

    artifact.setLocation(getArtifactRecord(source).generateArtifactLocation(output));

    run.addArtifact(artifact);

    for (IResult result : results) {
      result.generateResults(output).forEach(run::addResult);
    }

    if (!rules.isEmpty() || toolVersion != null) {
      Tool tool = new Tool();
      ToolComponent driver = new ToolComponent();

      IVersionInfo toolVersion = getToolVersion();
      if (toolVersion != null) {
        driver.setName(toolVersion.getName());
        driver.setVersion(toolVersion.getVersion());
      }

      for (RuleRecord rule : rules.values()) {
        driver.addRule(rule.generate());
      }

      tool.setDriver(driver);
      run.setTool(tool);
    }

    IBindingContext.instance().newSerializer(Format.JSON, Sarif.class)
        .disableFeature(SerializationFeature.SERIALIZE_ROOT)
        .serialize(
            sarif,
            outputFile,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING);
  }

  private interface IResult {
    @NonNull
    IValidationFinding getFinding();

    @NonNull
    List<Result> generateResults(@NonNull URI output) throws IOException;
  }

  private abstract class AbstractResult<T extends IValidationFinding> implements IResult {
    @NonNull
    private final T finding;

    protected AbstractResult(@NonNull T finding) {
      this.finding = finding;
    }

    @Override
    public T getFinding() {
      return finding;
    }

    @NonNull
    protected Kind kind(@NonNull IValidationFinding finding) {
      IValidationFinding.Kind kind = finding.getKind();

      Kind retval;
      switch (kind) {
      case FAIL:
        retval = Kind.FAIL;
        break;
      case INFORMATIONAL:
        retval = Kind.INFORMATIONAL;
        break;
      case NOT_APPLICABLE:
        retval = Kind.NOT_APPLICABLE;
        break;
      case PASS:
        retval = Kind.PASS;
        break;
      default:
        throw new IllegalArgumentException(String.format("Invalid finding kind '%s'.", kind));
      }
      return retval;
    }

    @NonNull
    protected SeverityLevel level(@NonNull Level severity) {
      SeverityLevel retval;
      switch (severity) {
      case CRITICAL:
      case ERROR:
        retval = SeverityLevel.ERROR;
        break;
      case INFORMATIONAL:
      case DEBUG:
        retval = SeverityLevel.NOTE;
        break;
      case WARNING:
        retval = SeverityLevel.WARNING;
        break;
      case NONE:
        retval = SeverityLevel.NONE;
        break;
      default:
        throw new IllegalArgumentException(String.format("Invalid severity '%s'.", severity));
      }
      return retval;
    }

    protected void message(@NonNull IValidationFinding finding, @NonNull Result result) {
      String message = finding.getMessage();
      if (message == null) {
        message = "";
      }

      Message msg = new Message();
      msg.setText(message);
      result.setMessage(msg);
    }

    protected void location(@NonNull IValidationFinding finding, @NonNull Result result, @NonNull URI base)
        throws IOException {
      IResourceLocation location = finding.getLocation();
      if (location != null) {
        // region
        Region region = new Region();

        if (location.getLine() > -1) {
          region.setStartLine(BigInteger.valueOf(location.getLine()));
          region.setEndLine(BigInteger.valueOf(location.getLine()));
        }
        if (location.getColumn() > -1) {
          region.setStartColumn(BigInteger.valueOf(location.getColumn()));
          region.setEndColumn(BigInteger.valueOf(location.getColumn() + 1));
        }
        if (location.getByteOffset() > -1) {
          region.setByteOffset(BigInteger.valueOf(location.getByteOffset()));
          region.setByteLength(BigInteger.ZERO);
        }
        if (location.getCharOffset() > -1) {
          region.setCharOffset(BigInteger.valueOf(location.getCharOffset()));
          region.setCharLength(BigInteger.ZERO);
        }

        PhysicalLocation physical = new PhysicalLocation();

        URI documentUri = finding.getDocumentUri();
        if (documentUri != null) {
          physical.setArtifactLocation(getArtifactRecord(documentUri).generateArtifactLocation(base));
        }
        physical.setRegion(region);

        LogicalLocation logical = new LogicalLocation();

        logical.setDecoratedName(finding.getPath());

        Location loc = new Location();
        loc.setPhysicalLocation(physical);
        loc.setLogicalLocation(logical);
        result.addLocation(loc);
      }
    }
  }

  private class SchemaResult
      extends AbstractResult<IValidationFinding> {

    protected SchemaResult(@NonNull IValidationFinding finding) {
      super(finding);
    }

    @Override
    public List<Result> generateResults(@NonNull URI output) throws IOException {
      IValidationFinding finding = getFinding();

      Result result = new Result();

      result.setKind(kind(finding).getLabel());
      result.setLevel(level(finding.getSeverity()).getLabel());
      message(finding, result);
      location(finding, result, output);

      return CollectionUtil.singletonList(result);
    }
  }

  private class ConstraintResult
      extends AbstractResult<ConstraintValidationFinding> {

    protected ConstraintResult(@NonNull ConstraintValidationFinding finding) {
      super(finding);
    }

    @Override
    public List<Result> generateResults(@NonNull URI output) throws IOException {
      ConstraintValidationFinding finding = getFinding();

      List<Result> retval = new LinkedList<>();

      Kind kind = kind(finding);
      SeverityLevel level = level(finding.getSeverity());

      for (IConstraint constraint : finding.getConstraints()) {
        assert constraint != null;
        RuleRecord rule = getRuleRecord(constraint);

        Result result = new Result();

        String id = constraint.getId();
        if (id != null) {
          result.setRuleId(id);
        }
        result.setRuleIndex(BigInteger.valueOf(rule.getIndex()));
        result.setGuid(rule.getGuid());
        result.setKind(kind.getLabel());
        result.setLevel(level.getLabel());
        message(finding, result);
        location(finding, result, output);

        retval.add(result);
      }
      return retval;
    }
  }

  private class RuleRecord {
    private final int index;
    @NonNull
    private final UUID guid;
    @NonNull
    private final IConstraint constraint;

    public RuleRecord(@NonNull IConstraint constraint) {
      this.guid = ObjectUtils.notNull(UUID.randomUUID());
      this.constraint = constraint;
      this.index = ruleIndex.addAndGet(1);
    }

    public int getIndex() {
      return index;
    }

    @NonNull
    public UUID getGuid() {
      return guid;
    }

    public IConstraint getConstraint() {
      return constraint;
    }

    @NonNull
    private ReportingDescriptor generate() {
      ReportingDescriptor retval = new ReportingDescriptor();
      IConstraint constraint = getConstraint();
      // String name = constraint.getId();
      // if (name != null) {
      // retval.setName(name);
      // }

      String id = constraint.getId();
      if (id != null) {
        retval.setId(id);
      }
      retval.setGuid(getGuid());
      String formalName = constraint.getFormalName();
      if (formalName != null) {
        MultiformatMessageString text = new MultiformatMessageString();
        text.setText(formalName);
        retval.setShortDescription(text);
      }
      MarkupLine description = constraint.getDescription();
      if (description != null) {
        MultiformatMessageString text = new MultiformatMessageString();
        text.setMarkdown(description.toMarkdown());
        retval.setFullDescription(text);
      }
      return retval;
    }

  }

  private class ArtifactRecord {
    private final URI uri;
    private final int index;

    public ArtifactRecord(@NonNull URI uri) {
      this.uri = uri;
      this.index = artifactIndex.addAndGet(1);
    }

    @NonNull
    public URI getUri() {
      return uri;
    }

    public int getIndex() {
      return index;
    }

    public ArtifactLocation generateArtifactLocation(@NonNull URI baseUri) throws IOException {
      ArtifactLocation location = new ArtifactLocation();
      location.setUri(relativize(baseUri, getUri()));
      location.setIndex(BigInteger.valueOf(getIndex()));
      return location;
    }
  }
}
