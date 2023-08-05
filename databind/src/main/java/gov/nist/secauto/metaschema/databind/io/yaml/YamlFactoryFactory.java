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

package gov.nist.secauto.metaschema.databind.io.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.SerializationFeature;
import gov.nist.secauto.metaschema.databind.io.json.JsonFactoryFactory;

import org.yaml.snakeyaml.LoaderOptions;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class YamlFactoryFactory {
  private YamlFactoryFactory() {
    // disable construction
  }

  /**
   * Create a new {@link YAMLFactory} configured to parse YAML.
   *
   * @param config
   *          the deserialization configuration
   *
   * @return the factory
   */
  @NonNull
  public static YAMLFactory newParserFactoryInstance(
      @NonNull IMutableConfiguration<DeserializationFeature<?>> config) {
    YAMLFactoryBuilder builder = YAMLFactory.builder();
    LoaderOptions loaderOptions = builder.loaderOptions();
    if (loaderOptions == null) {
      loaderOptions = new LoaderOptions();
    }

    int codePointLimit = config.get(DeserializationFeature.YAML_CODEPOINT_LIMIT);
    loaderOptions.setCodePointLimit(codePointLimit);
    builder.loaderOptions(loaderOptions);

    YAMLFactory retval = ObjectUtils.notNull(builder.build());
    JsonFactoryFactory.configureJsonFactory(retval);
    return retval;
  }

  /**
   * Create a new {@link YAMLFactory} configured to generate YAML.
   *
   * @param config
   *          the serialization configuration
   *
   * @return the factory
   */
  @NonNull
  public static YAMLFactory newGeneratorFactoryInstance(
      @NonNull IMutableConfiguration<SerializationFeature<?>> config) {
    YAMLFactoryBuilder builder = YAMLFactory.builder();
    YAMLFactory retval = ObjectUtils.notNull(builder
        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        .enable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        .disable(YAMLGenerator.Feature.SPLIT_LINES)
        .build());
    JsonFactoryFactory.configureJsonFactory(retval);
    return retval;
  }
}
