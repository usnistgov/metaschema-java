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

package gov.nist.secauto.metaschema.databind.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.format.DataFormatDetector;
import com.fasterxml.jackson.core.format.DataFormatMatcher;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.json.JsonFactoryFactory;
import gov.nist.secauto.metaschema.databind.io.yaml.impl.YamlFactoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides a means to analyze content to determine what {@link Format} the data
 * is represented as.
 */
public class FormatDetector {

  private final DataFormatDetector detector;

  /**
   * Construct a new format detector using the default configuration.
   */
  public FormatDetector() {
    this(new DefaultConfiguration<>());
  }

  /**
   * Construct a new format detector using the provided {@code configuration}.
   *
   * @param configuration
   *          the deserialization configuration to use for detection
   */
  public FormatDetector(
      @NonNull IConfiguration<DeserializationFeature<?>> configuration) {
    this(configuration, newDetectorFactory(configuration));
  }

  /**
   * Construct a new format detector using the provided {@code configuration}.
   *
   * @param configuration
   *          the deserialization configuration to use for detection
   * @param detectors
   *          the JSON parser instances to use for format detection
   */
  protected FormatDetector(
      @NonNull IConfiguration<DeserializationFeature<?>> configuration,
      @NonNull JsonFactory... detectors) {
    int lookaheadBytes = configuration.get(DeserializationFeature.FORMAT_DETECTION_LOOKAHEAD_LIMIT);
    this.detector = new DataFormatDetector(detectors)
        .withMinimalMatch(MatchStrength.INCONCLUSIVE)
        .withOptimalMatch(MatchStrength.SOLID_MATCH)
        .withMaxInputLookahead(lookaheadBytes - 1);

  }

  @NonNull
  private static JsonFactory[] newDetectorFactory(@NonNull IConfiguration<DeserializationFeature<?>> config) {
    JsonFactory[] detectorFactory = new JsonFactory[3];
    detectorFactory[0] = YamlFactoryFactory.newParserFactoryInstance(config);
    detectorFactory[1] = JsonFactoryFactory.instance();
    detectorFactory[2] = new XmlFactory();
    return detectorFactory;
  }

  /**
   * Analyzes the provided {@code resource} to determine it's format.
   *
   * @param resource
   *          the resource to analyze
   * @return the analysis result
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  public Result detect(@NonNull URL resource) throws IOException {
    try (InputStream is = ObjectUtils.notNull(resource.openStream())) {
      return detect(is);
    }
  }

  /**
   * Analyzes the data from the provided {@code inputStream} to determine it's
   * format.
   *
   * @param inputStream
   *          the resource stream to analyze
   * @return the analysis result
   * @throws IOException
   *           if an error occurred while reading the resource
   */
  @NonNull
  public Result detect(@NonNull InputStream inputStream) throws IOException {
    DataFormatMatcher matcher = detector.findFormat(inputStream);
    switch (matcher.getMatchStrength()) {
    case FULL_MATCH:
    case SOLID_MATCH:
    case WEAK_MATCH:
    case INCONCLUSIVE:
      return new Result(matcher);
    case NO_MATCH:
    default:
      throw new IOException("Unable to identify format");
    }
  }

  public static class Result {
    @NonNull
    private final DataFormatMatcher matcher;

    private Result(@NonNull DataFormatMatcher matcher) {
      this.matcher = matcher;
    }

    /**
     * Get the detected format.
     *
     * @return the format
     */
    @NonNull
    public Format getFormat() {
      Format retval;
      String formatName = matcher.getMatchedFormatName();
      if (YAMLFactory.FORMAT_NAME_YAML.equals(formatName)) {
        retval = Format.YAML;
      } else if (JsonFactory.FORMAT_NAME_JSON.equals(formatName)) {
        retval = Format.JSON;
      } else if (XmlFactory.FORMAT_NAME_XML.equals(formatName)) {
        retval = Format.XML;
      } else {
        throw new UnsupportedOperationException(String.format("The detected format '%s' is not supported", formatName));
      }
      return retval;
    }

    /**
     * Get an {@link InputStream} that can be used to read the analyzed data from
     * the start.
     *
     * @return the stream
     */
    @SuppressWarnings("resource")
    @NonNull
    public InputStream getDataStream() {
      return ObjectUtils.notNull(matcher.getDataStream());
    }

    // @SuppressWarnings("resource")
    // @NonNull
    // public JsonParser getParser() throws IOException {
    // return ObjectUtils.notNull(matcher.createParserWithMatch());
    // }

    /**
     * Get the strength of the match.
     *
     * @return the strength
     */
    @NonNull
    public MatchStrength getMatchStrength() {
      return ObjectUtils.notNull(matcher.getMatchStrength());
    }
  }
}
